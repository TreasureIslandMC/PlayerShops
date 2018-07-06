package me.jsbroks.playershops;

import me.jsbroks.playershops.commands.MainCommands;
import me.jsbroks.playershops.core.Config;
import me.jsbroks.playershops.core.TransactionLogger;
import me.jsbroks.playershops.core.data.DatabaseHandler;
import me.jsbroks.playershops.util.MapUtil;
import me.jsbroks.playershops.util.TextUtil;
import me.jsbroks.playershops.core.hooks.HookManager;
import me.jsbroks.playershops.listener.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

public class PlayerShops extends JavaPlugin {

    public static Plugin plugin;

    public static boolean spigot = true;
    public static boolean update = false;
    public static boolean error = false;
    
    public static DatabaseHandler databaseHandler;
    public static TransactionLogger tLogger;

    public static Set<Inventory> needToBeSaved;
    public static Map<UUID, Inventory> onlineInventories;
    public static Map<UUID, Inventory> offlineInventories;

    public static Set<Player> playersInEditMode;

    @Override
    public void onEnable() {
        plugin = this;

        configFiles();
        HookManager.loadDependencies();

        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        tLogger = new TransactionLogger(df.format(date) + ".txt");

        database();
        registerEvents();
        loadCommands();
        checkForUpdates();

        onlineInventories = new HashMap<>();
        offlineInventories = MapUtil.createLRUMap(Config.config.getInt("Settings.MaxOfflineInventoriesSize"));
        needToBeSaved = new HashSet<>();
        playersInEditMode = new HashSet<>();

        setUp();
    }

    @Override
    public void onDisable() {
        //Close MySQL database

        getLogger().info("Saving all shops to database.");
        for (Map.Entry<UUID, Inventory> entry : onlineInventories.entrySet()) {
            databaseHandler.setInventory(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<UUID, Inventory> entry : offlineInventories.entrySet()) {
            databaseHandler.setInventory(entry.getKey(), entry.getValue());
        }

        getLogger().info("Closing all players inventories. (In case this is a reload)");
        for(Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory().getTitle().startsWith(Config.config.getString("Settings.ShopPrefix"))) {
                player.closeInventory();
                TextUtil.sendMessage(player, Config.lang.getString("Reload.InventoryClose"));
            }
        }

        databaseHandler.close();
        plugin = null;
    }

    /**
     * Initialize connection with MySQL Database and remove an older accounts.
     *
     * @return void
     */
    private void database() {
        // Connect to MySQL
        DatabaseHandler.valueOf(Config.config.getString("Database.Type")).setUp();

        if (Config.config.getBoolean("Settings.CleanDatabase.OnEnable")) {
            getLogger().info("Cleaning database (Removing accounts over " + Config.config.getInt("Settings.CleanDatabase.OlderThan") + " days) ...");
            getLogger().info(databaseHandler.cleanDatabase(Config.config.getInt("Settings.CleanDatabase.OlderThan")) + " have been removed");
        }
    }

    private void registerEvents() {
        registerEvents(plugin, new ConnectionEvents(), new InventoryEvents(), new ShopEvents(), new SignEvents(), new ChatEvents());
    }
    
    private void loadCommands() {
        getCommand("playershop").setExecutor(new MainCommands());
    }

    private void configFiles() {
        Config.setup(this);
    }

    private void checkForUpdates() {
        //TODO: Update update checker
//        if (Config.config.getBoolean("Settings.UpdateChecker")) {
//            new BukkitRunnable() {
//                @Override
//                public void run() {
//                    plugin.getLogger().info("Checking for update...");
//
//                    String website = PlayerShops.checkWebsiteForString();
//
//                    if (website.equalsIgnoreCase(plugin.getDescription().getVersion())) {
//                        plugin.getLogger().info("You are using the most current version");
//                    } else if (website.equalsIgnoreCase("Error")) {
//                        plugin.getLogger().info("Error checking for update, couldn't connect to spigotmc.org");
//                    } else {
//                        plugin.getLogger().info("An new update is available! (" + website + ")");
//                        update = true;
//                    }
//                }
//            }.runTaskAsynchronously(plugin);
//        } else {
//            getLogger().info("Skipping checking for updates (disabled in config.yml)");
//        }
    }

    private void setUp() {
        try {
            Class.forName("org.spigotmc.SpigotConfig");
        } catch (ClassNotFoundException ignore) {
            getLogger().info("Using spigot will unlock all features");
            spigot = false;
        }

        if (Bukkit.getOnlinePlayers().size() > 0) {
            getLogger().log(Level.WARNING, "Reloading the server is highly not recommend");
            getLogger().info("Loading shops of all players online... (Not Async)");
            databaseHandler.loadOnlinePlayers();
            getLogger().info("Done!");
        }
    }

    /**
     * Quick way to register events
     *
     * @param plugin  Instances of main plugin
     * @param listeners Name of classes that extend listeners
     */
    private static void registerEvents(org.bukkit.plugin.Plugin plugin, Listener... listeners) {
        for (Listener listener : listeners) {
            Bukkit.getServer().getPluginManager().registerEvents(listener, plugin);
        }
    }

    @Deprecated
    private static String checkWebsiteForString() {
        try {

            int resource = 0;
            HttpURLConnection con = (HttpURLConnection) new URL("http://www.spigotmc.org/api/general.php").openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.getOutputStream()
                    .write(("key=98BE0FE67F88AB82B4C197FAF1DC3B69206EFDCC4D3B80FC83A00037510B99B4&resource=" + resource)
                            .getBytes("UTF-8"));
            String version = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
            if (version.length() <= 7) {
                return version.replaceAll("[B]", "");
            }
        } catch (Exception ex) {
            return "Error";
        }
        return "Error";
    }
}
