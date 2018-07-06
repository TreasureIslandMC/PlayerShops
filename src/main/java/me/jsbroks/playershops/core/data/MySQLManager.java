package me.jsbroks.playershops.core.data;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.huskehhh.mysql.mysql.MySQL;
import me.jsbroks.playershops.Main;
import me.jsbroks.playershops.core.Config;
import me.jsbroks.playershops.util.InventoryUtil;
import me.jsbroks.playershops.util.ItemUtil;
import me.jsbroks.playershops.util.MapUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;

class MySQLManager {

    MySQL mySQL;
    Connection connection;

    String host;
    String port;
    String database;
    String username;
    String password;

    String prefix = "[PlayerShops-MySQL] ";

    public void setupMySQL() {
        try {
            // Database Connection Information
            this.host = Config.config.getString("Database.Host");
            this.port = Config.config.getString("Database.Port");
            this.database = Config.config.getString("Database.Database");
            this.username = Config.config.getString("Database.Username");
            this.password = Config.config.getString("Database.Password");

            mySQL = new MySQL(host, port, database, username, password);

            mySQL.openConnection();

            connection = mySQL.getConnection();
            System.out.println(prefix + "MySQL successfully connected!");

            String sql = "CREATE TABLE IF NOT EXISTS PlayerShops(UUID varchar(36) UNIQUE, Username varchar(255), Inventory TEXT, LastSeen varchar(20));";
            PreparedStatement statement = connection.prepareStatement(sql);
            // Create Table PlayerShops if one doesn't exist
            // UUID | Username | Inventory | LastSeen
            statement.executeUpdate(sql);

        } catch (Exception e) {
            System.out.println(this.prefix + "Could not connection to MySQL! " + e.getCause().getMessage());
            System.out.println("= = = = = = = = [MySQL PlayerShops Error] = = = = = = = =");
            System.out.println(e.getMessage());
            System.out.println("= = = = = = = = = = = = = = = = = = = = = = = = = = = = =");
            System.out.println(this.prefix + "Have you entered in all the correct information?");
            System.out.println(this.prefix + "Reverting to FILE data storage");
            Main.error = true;
            DatabaseHandler.FILE.setUp();

        }
    }

    public void closeMySQL() {
        try {
            this.mySQL.closeConnection();
        } catch (Exception ignore) {
        }
    }

    public void openConnection() {
        try {
            if (!mySQL.checkConnection()) {
                mySQL.openConnection();
                connection = mySQL.getConnection();
            }
        } catch (Exception ignore) {
            System.out.println(prefix + "Could not reestablish connection with MySQL");
        }
    }

    // ------------------------------------------------------------ \\

    public String getInventory(UUID uuid) {
        openConnection();
        try {
            String sql = "SELECT Inventory FROM PlayerShops WHERE UUID='" + uuid.toString() + "';";

            PreparedStatement statement = this.connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery(sql);

            if (rs.next()) {
                return rs.getString("Inventory");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Not Set";
    }

    public ResultSet getResults() {
        openConnection();
        try {
            String sql = "SELECT * FROM PlayerShops;";

            PreparedStatement statement = this.connection.prepareStatement(sql);
            return statement.executeQuery(sql);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setInventory(UUID uuid, Inventory inv) {
        openConnection();
        try {
            String data = InventoryUtil.toBase64(inv);

            String sql = "UPDATE PlayerShops SET Inventory='" + data + "' WHERE UUID='" + uuid.toString() + "';";
            PreparedStatement statement = connection.prepareCall(sql);
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            System.out.println(prefix + "Could not save " + uuid.toString() + " inventory to MySQL...");
        }
    }

    public void createPlayer(UUID uuid, String name) {
        openConnection();
        try {

            Date dNow = new Date();
            SimpleDateFormat ft = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");

            String sql = "INSERT INTO PlayerShops(UUID, Username, Inventory, LastSeen) VALUES ('" + uuid.toString() + "', '" + name + "', 'Not Set' , '" + ft.format(dNow) + "') ON DUPLICATE KEY UPDATE LastSeen='" + ft.format(dNow) + "',Username='" + name + "';";
            PreparedStatement statement = connection.prepareStatement(sql);

            statement.executeUpdate(sql);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createPlayer(String uuid, String name, String lastSeen, String inventory) {
        openConnection();
        try {

            String sql = "INSERT INTO PlayerShops(UUID, Username, Inventory, LastSeen) VALUES ('" + uuid + "', '" + name + "', '" + inventory + "' , '" + lastSeen + "') ON DUPLICATE KEY UPDATE LastSeen='" + lastSeen + "',Username='" + name + "',Inventory='" + inventory + "';";
            PreparedStatement statement = connection.prepareStatement(sql);

            statement.executeUpdate(sql);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove users from database with UUID
     *
     * @param uuid Players UUID who will be removed
     */
    public void deletePlayer(UUID uuid) {
        openConnection();

        try {
            String sql = "DELETE FROM PlayerShops WHERE UUID='" + uuid.toString() + "';";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean playerExists(UUID uuid) {
        openConnection();
        try {
            String sql = "SELECT EXISTS(SELECT 1 FROM PlayerShops WHERE UUID='" + uuid.toString() + "' LIMIT 1);";
            String column = "EXISTS(SELECT 1 FROM PlayerShops WHERE UUID='" + uuid.toString() + "' LIMIT 1)";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery(sql);

            if (resultSet.next() && resultSet.getInt(column) == 1) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ignore) {
            System.out.println(prefix + "Could not check if player exists or not... default:false");
        }
        return false;
    }

    public Map<UUID, Date> getDates() {
        openConnection();

        Map<UUID, Date> list = new HashMap();
        openConnection();

        SimpleDateFormat ft = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");

        try {
            String sql = "SELECT UUID,LastSeen FROM PlayerShops;";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                list.put(UUID.fromString(rs.getString("UUID")), ft.parse(rs.getString("LastSeen")));
            }
        } catch (Exception ignore) {
            System.out.println(prefix + "Could not load Dates into a list...");
        }

        return list;
    }

    public int cleanDatabase(int days) {
        openConnection();

        Date date = new Date();
        Map<UUID, Date> dates = getDates();

        int counter = 0;
        for (Map.Entry<UUID, Date> entry : dates.entrySet()) {

            Map<TimeUnit, Long> timeUnitDiff = MapUtil.computeDiff(entry.getValue(), date);

            if (timeUnitDiff.get(TimeUnit.DAYS) >= days) {
                deletePlayer(entry.getKey());
                counter++;
            }

        }

        return counter;
    }

    private Map<OfflinePlayer, Inventory> getInventories(Material material) {
        try {
            String sql = "SELECT UUID,Inventory FROM PlayerShops;";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery(sql);

            Map<OfflinePlayer, Inventory> result = new HashMap<>();

            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("UUID"));
                String data = rs.getString("Inventory");

                if (!data.equalsIgnoreCase("Not Set")) {
                    Inventory inv = InventoryUtil.fromBase64("Search",data);

                    if(inv.contains(material)) {
                        result.put(Bukkit.getOfflinePlayer(uuid), inv);
                    }
                }
            }

            return result;

        } catch (Exception ignore) {
            System.out.println(prefix + "Could not load player inventories...");
        }

        return null;
    }

    public Multimap<Double, OfflinePlayer> search(Material material) {

        Map<OfflinePlayer, Inventory> results = getInventories(material);

        Multimap<Double, OfflinePlayer> findings = ArrayListMultimap.create();
        for (Map.Entry<OfflinePlayer, Inventory> entry : results.entrySet()) {
            Inventory inv = entry.getValue();
            for (int i = 0; i < inv.getSize(); i++) {
                ItemStack item = inv.getItem(i);

                if (item != null) {
                    if(item.getType() == material) {
                        findings.put(ItemUtil.getItemPriceEach(item), entry.getKey());
                    }
                }
            }
        }

        return findings;
    }
}
