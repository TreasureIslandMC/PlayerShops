package me.jsbroks.playershops.core.async;

import com.google.common.collect.Multimap;
import me.jsbroks.playershops.core.Config;
import me.jsbroks.playershops.core.Economy;
import me.jsbroks.playershops.core.data.DatabaseHandler;
import me.jsbroks.playershops.core.hooks.HookManager;
import me.infopaste.playershops.util.*;
import me.jsbroks.playershops.util.*;
import net.md_5.bungee.api.chat.*;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.ResultSet;
import java.util.*;
import java.util.List;

import static me.jsbroks.playershops.Main.*;
import static me.jsbroks.playershops.core.Economy.canAfford;

public class AsyncCommands {

    public static void sellCommand(final ItemStack item, final Player player, final double price) {
        new BukkitRunnable() {
            @Override
            public void run() {

                if (price >= Config.config.getDouble("Transaction.Sell.DontApply")) {

                    Economy.TaxType taxType = Economy.TaxType.valueOf(Config.config.getString("Transaction.Sell.Tax"));

                    if (taxType != Economy.TaxType.NONE) {

                        double taxAmount = taxType.calculateTax(price, Config.config.getDouble("Transaction.Sell.Amount"));

                        List<String> items = Config.config.getStringList("Transaction.Sell.Items");

                        //Apply item discounts
                        taxAmount = Economy.specialItems(items, taxType, price, item, taxAmount);

                        //Apply discounts if they apply
                        taxAmount = Economy.taxDiscount(player, taxAmount);

                        if (!canAfford(player, taxAmount)) {
                            TextUtil.sendMessage(player, Config.lang.getString("Transaction.Tax.CantAfford")
                                    .replaceAll("%amount%", NumberUtil.stringFormatDecimalPlaces(taxAmount))
                                    .replaceAll("%taxtype%", taxType.toString()));
                            return;
                        }

                        TextUtil.sendMessage(player, Config.lang.getString("Transaction.Tax.Applied")
                                .replaceAll("%amount%", NumberUtil.stringFormatDecimalPlaces(taxAmount))
                                .replaceAll("%taxtype%", taxType.toString()));

                        HookManager.withdrawMoney(player, taxAmount);
                        tLogger.tax(player, taxType, taxAmount);
                    }
                }

                //String data = mySQLManager.getInventory(player.getUniqueId());
                UUID uuid = player.getUniqueId();

                Inventory inv;

                if (onlineInventories.containsKey(player.getUniqueId())) {
                    inv = onlineInventories.get(player.getUniqueId());
                } else {
                    String data = databaseHandler.getInventory(uuid);
                    if (data.equalsIgnoreCase("Not Set")) {
                        inv = Bukkit.createInventory(null, PermissionUtil.getInventorySize(player), InventoryUtil.getInventoryTitle(player));
                    } else {
                        inv = InventoryUtil.fromBase64(InventoryUtil.getInventoryTitle(player), data);
                    }
                }

                //Check if player has space in inventory
                int freeSlot = inv.firstEmpty();

                if (freeSlot < 0) {
                    TextUtil.sendMessage(player, Config.lang.getString("Commands.Sell.ShopFull"));
                    return;
                }

                player.getInventory().removeItem(item);

                //Add item to inventory
                inv.setItem(freeSlot, ItemUtil.addPriceLore(item, price));
                tLogger.sell(player, item, price);
                //Save it to database and onlineInventories
                databaseHandler.setInventory(uuid, inv);
                onlineInventories.put(uuid, inv);

            }
        }.runTaskAsynchronously(plugin);
    }

    public static void shopCommand(final Player buyer, final OfflinePlayer seller) {

        new BukkitRunnable() {
            @Override
            public void run() {

                if (onlineInventories.containsKey(seller.getUniqueId())) {
                    Inventory inventory = onlineInventories.get(seller.getUniqueId());
                    buyer.openInventory(inventory);

                } else if (offlineInventories.containsKey(seller.getUniqueId())) {
                    Inventory inventory = offlineInventories.get(seller.getUniqueId());
                    buyer.openInventory(inventory);

                } else if (databaseHandler.containsPlayer(seller.getUniqueId())) {
                    String data = databaseHandler.getInventory(seller.getUniqueId());

                    if (data.equalsIgnoreCase("Not Set")) {
                        TextUtil.sendMessage(buyer, Config.lang.getString("Commands.Shop.NotSet").replaceAll("%player%", seller.getName()));
                    } else {
                        Inventory inv = InventoryUtil.fromBase64(InventoryUtil.getInventoryTitle(seller), data);
                        buyer.openInventory(inv);
                        offlineInventories.put(seller.getUniqueId(), inv);
                    }

                } else {
                    TextUtil.sendMessage(buyer, Config.lang.getString("Commands.Shop.NotFound").replaceAll("%player%", seller.getName()));
                }

            }
        }.runTaskAsynchronously(plugin);
    }

    public static void deleteCommand(final CommandSender sender, final OfflinePlayer player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (databaseHandler.containsPlayer(player.getUniqueId())) {
                    databaseHandler.deletePlayer(player);
                    onlineInventories.remove(player.getUniqueId());
                    offlineInventories.remove(player.getUniqueId());
                    TextUtil.sendMessage(sender, Config.lang.getString("Commands.Delete.Removed").replaceAll("%player%", player.getName()));
                } else {
                    TextUtil.sendMessage(sender, Config.lang.getString("Commands.Delete.NotFound").replaceAll("%player%", player.getName()));
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    public static void cleanDatabase(final CommandSender player, final int days) {
        new BukkitRunnable() {
            @Override
            public void run() {

                long start = System.currentTimeMillis();
                int amount = databaseHandler.cleanDatabase(days);
                long time = System.currentTimeMillis() - start;

                TextUtil.sendMessage(player, Config.lang.getString("Commands.Clean.Finished")
                        .replaceAll("%time%", String.valueOf(time))
                        .replaceAll("%amount%", String.valueOf(amount)));

            }
        }.runTaskAsynchronously(plugin);
    }

    public static void searchDatabase(final Player player, final String args) {
        new BukkitRunnable() {
            @Override
            public void run() {

                long start = System.currentTimeMillis();

                Material material;
                try {
                    if (NumberUtils.isNumber(args)) {
                        material = Material.getMaterial(Integer.valueOf(args));
                    } else {
                        material = Material.valueOf(args.toUpperCase());
                    }
                } catch (IllegalArgumentException e) {
                    TextUtil.sendMessage(player, Config.lang.getString("Commands.Search.InvalidMaterial"));
                    return;
                }

                if (material == Material.AIR || material == null) {
                    TextUtil.sendMessage(player, Config.lang.getString("Commands.Search.InvalidMaterial"));
                    return;
                }

                TextUtil.sendMessage(player, Config.lang.getString("Commands.Search.Searching").replace("%material%", material.name()));

                Multimap<Double, OfflinePlayer> results = databaseHandler.search(material);

                if (results.size() == 0) {
                    long time = System.currentTimeMillis() - start;
                    TextUtil.sendMessage(player, Config.lang.getString("Commands.Search.Nothing").replaceAll("%time%", String.valueOf(time)));
                    return;
                } else {

                    long time = System.currentTimeMillis() - start;

                    TreeMap<Double, Collection<OfflinePlayer>> treeMap = new TreeMap<>(results.asMap());
                    List<String> format = new ArrayList<>();
                    List<TextComponent> test = new ArrayList<>();

                    int counter = 1;

                    for (Map.Entry<Double, Collection<OfflinePlayer>> entry : treeMap.entrySet()) {
                        double price = entry.getKey();
                        Collection<OfflinePlayer> value = treeMap.get(price);
                        for (OfflinePlayer offlinePlayer : value) {

                            if (spigot) {
                                TextComponent text = new TextComponent(TextUtil.colorize(Config.lang.getString("Commands.Search.Results")
                                        .replaceAll("%counter%", String.valueOf(counter))
                                        .replaceAll("%price%", NumberUtil.stringFormatDecimalPlaces(price))
                                        .replaceAll("%player%", offlinePlayer.getName())
                                        .replaceAll("%material%", material.name())));

                                text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(TextUtil.colorize(Config.lang.getString("Commands.Search.Hover").replaceAll("%player%", offlinePlayer.getName()))).create()));
                                text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/playershop shop %player%".replaceAll("%player%", offlinePlayer.getName())));

                                test.add(text);
                            } else {

                                format.add(Config.lang.getString("Commands.Search.Results")
                                        .replaceAll("%counter%", String.valueOf(counter))
                                        .replaceAll("%price%", NumberUtil.stringFormatDecimalPlaces(price))
                                        .replaceAll("%player%", offlinePlayer.getName())
                                        .replaceAll("%material%", material.name()));
                            }
                            counter++;

                            if (counter > Config.config.getInt("Settings.SearchSize")) break;
                        }

                        if (counter > Config.config.getInt("Settings.SearchSize")) break;
                    }

                    for (String string : format) {
                        TextUtil.sendMessage(player, string);

                    }

                    for (TextComponent string : test) {
                        player.spigot().sendMessage(string);

                    }

                    TextUtil.sendMessage(player, Config.lang.getString("Commands.Search.Bottom")
                            .replaceAll("%counter%", String.valueOf(counter-1))
                            .replaceAll("%time%", String.valueOf(time)));
                }

            }
        }.runTaskAsynchronously(plugin);
    }

    public static void convertToMySQL(final CommandSender sender) {
        new BukkitRunnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();

                File dir = new File(plugin.getDataFolder() + "/data/");
                File[] files = dir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".yml");
                    }
                });

                for (File file: files) {
                    YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
                    String name = yaml.getString("Player.Name");
                    String uuid = yaml.getString("Player.UUID");
                    String data = yaml.getString("Inventory");
                    String lastSeen = yaml.getString("Player.LastSeen");

                    databaseHandler.createPlayer(uuid, name, lastSeen, data);
                }

                long time = System.currentTimeMillis() - start;

                TextUtil.sendMessage(sender, Config.lang.getString("Commands.Convert.SuccessToMySQL")
                        .replaceAll("%time%", String.valueOf(time))
                        .replaceAll("%amount%", String.valueOf(files.length)));

            }
        }.runTaskAsynchronously(plugin);
    }

    public static void convertToFile(final CommandSender sender) {
        new BukkitRunnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();

                ResultSet rs = databaseHandler.getResults();
                int counter = 0;
                DatabaseHandler.FILE.setUp();
                try {

                    while (rs.next()) {

                        databaseHandler.createPlayer(rs.getString("UUID"),rs.getString("Username"),rs.getString("LastSeen"),rs.getString("Inventory"));
                        counter++;

                    }
                } catch (Exception ignore) {}

                long time = System.currentTimeMillis() - start;

                Config.config.set("Database.Type", "FILE");
                Config.saveConfig();

                TextUtil.sendMessage(sender, Config.lang.getString("Commands.Convert.SuccessToFile")
                        .replaceAll("%time%", String.valueOf(time))
                        .replaceAll("%amount%", String.valueOf(counter)));

            }
        }.runTaskAsynchronously(plugin);
    }
}
