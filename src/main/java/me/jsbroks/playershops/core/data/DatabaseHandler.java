package me.jsbroks.playershops.core.data;

import com.google.common.collect.Multimap;
import me.jsbroks.playershops.util.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.sql.ResultSet;
import java.util.UUID;

import static me.jsbroks.playershops.PlayerShops.*;


public enum DatabaseHandler {

    MYSQL,
    SQL,
    FILE,
    NONE,
    MONGODB,
    REDIS;

    public static MySQLManager mySQLManager;
    public static FileManager fileManager;
    public static SQLManager sqlManager;

    public void close() {
        switch (this) {
            case MYSQL:
                mySQLManager.closeMySQL();
                return;
            case FILE:
                fileManager = null;
                return;
            case SQL:
                return;
        }
    }

    public void setUp() {
        switch (this) {
            case MYSQL:
                plugin.getLogger().info("Setting up MySQL Database");

                mySQLManager = new MySQLManager();
                databaseHandler = MYSQL;

                mySQLManager.setupMySQL();
                return;

            case SQL:
                databaseHandler = SQL;
                sqlManager = new SQLManager();

                plugin.getLogger().info("Setting up SQL Database");
                plugin.getLogger().info("Error... Feature coming soon");
                MYSQL.setUp();
                return;

            case FILE:
                plugin.getLogger().info("Setting up File Database");
                databaseHandler = FILE;
                fileManager = new FileManager();
                return;

            case NONE:
                plugin.getLogger().info("Could not read Database type... disabling plugin.");
                Bukkit.getPluginManager().disablePlugin(plugin);
                return;
        }
    }

    public Multimap<Double, OfflinePlayer> search(Material material) {
        switch (this) {
            case MYSQL:
                return mySQLManager.search(material);
            case SQL:
                return mySQLManager.search(material);
            case FILE:
                return fileManager.search(material);
        }
        return null;
    }

    public String getInventory(UUID uuid) {
        switch (this) {
            case MYSQL:
                return mySQLManager.getInventory(uuid);

            case FILE:
                return fileManager.getInventory(uuid);
        }
        return "Not Set";
    }

    public void setInventory(UUID uuid, Inventory inventory) {
        switch (this) {
            case MYSQL:
                mySQLManager.setInventory(uuid, inventory);
                return;

            case FILE:
                fileManager.setInventory(uuid, inventory);
                return;
        }
    }

    public boolean containsPlayer(UUID uuid) {
        switch (this) {
            case MYSQL:
                return mySQLManager.playerExists(uuid);

            case FILE:
                return fileManager.playerExists(uuid);
        }

        return false;
    }

    public void createPlayer(UUID uuid, String name) {
        switch (this) {
            case MYSQL:
                mySQLManager.createPlayer(uuid, name);
                return;

            case SQL:
                return;

            case FILE:
                fileManager.createPlayer(uuid);
                return;
        }
    }

    public ResultSet getResults() {
        switch (this) {
            case MYSQL:
                return mySQLManager.getResults();
            default:
                return null;
        }
    }

    public void createPlayer(String uuid, String name, String lastseen, String inventory) {
        switch (this) {
            case MYSQL:
                mySQLManager.createPlayer(uuid, name, lastseen, inventory);
                return;

            case SQL:
                return;

            case FILE:
                fileManager.createPlayer(uuid, name, lastseen, inventory);
                return;
        }
    }

    public int cleanDatabase(int days) {
        switch (this) {
            case MYSQL:
                return mySQLManager.cleanDatabase(days);

            case FILE:
                return fileManager.clean(days);
        }
        return 0;
    }

    public void loadOnlinePlayers() {
        switch (this) {
            case MYSQL:
                for (Player player : Bukkit.getOnlinePlayers()) {
                    UUID uuid = player.getUniqueId();
                    if (mySQLManager.playerExists(uuid)) {
                        String data = mySQLManager.getInventory(uuid);
                        if (!data.equalsIgnoreCase("Not Set")) {
                            onlineInventories.put(uuid, InventoryUtil.fromBase64(InventoryUtil.getInventoryTitle(player), data));
                        }
                    }
                }
                return;

            case FILE:
                for (Player player : Bukkit.getOnlinePlayers()) {
                    UUID uuid = player.getUniqueId();
                    if (fileManager.playerExists(uuid)) {
                        String data = fileManager.getInventory(uuid);
                        if (!data.equalsIgnoreCase("Not Set")) {
                            onlineInventories.put(uuid, InventoryUtil.fromBase64(InventoryUtil.getInventoryTitle(player), data));
                        }
                    }
                }
                return;
        }
    }

    public void deletePlayer(OfflinePlayer offlinePlayer) {
        switch (this) {
            case MYSQL:
                mySQLManager.deletePlayer(offlinePlayer.getUniqueId());
                return;

            case FILE:
                fileManager.deletePlayer(offlinePlayer);
                return;
        }
    }

    public boolean playerExist(OfflinePlayer offlinePlayer) {
        switch (this) {
            case MYSQL:
                return mySQLManager.playerExists(offlinePlayer.getUniqueId());

            case FILE:
                return fileManager.playerExists(offlinePlayer.getUniqueId());

        }
        return false;
    }
}
