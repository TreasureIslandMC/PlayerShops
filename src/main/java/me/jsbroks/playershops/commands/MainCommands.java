package me.jsbroks.playershops.commands;

import me.jsbroks.playershops.core.config.Lang;
import me.jsbroks.playershops.core.TransactionLogger;
import me.jsbroks.playershops.core.async.AsyncCommands;
import me.jsbroks.playershops.core.data.DatabaseHandler;
import me.jsbroks.playershops.util.NumberUtil;
import me.jsbroks.playershops.util.TextUtil;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import static me.jsbroks.playershops.PlayerShops.*;

public class MainCommands implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("playershop")) {

            boolean isPlayer = sender instanceof Player;
            String permission = plugin.getLang().getString("PermissionDenied");

            if (args.length == 0) {
                helpMenu(sender);
                return true;
            }

            /*

            PLAYER ONLY COMMANDS

             */

            if(args[0].equalsIgnoreCase("sell") &&  isPlayer) {

                Player player = (Player) sender;

                if (player.hasPermission("playershops.sell")) {

                    if (blockCreative(player)) return true;

                    // ps sell <price>
                    if (args.length != 2) {
                        TextUtil.sendMessage(player, plugin.getLang().getString("Commands.Sell.Format"));
                        return true;
                    }

                    //Item is hand is null
                    ItemStack item = player.getItemInHand();
                    if (item.getType() == null || item.getType() == Material.AIR) {
                        TextUtil.sendMessage(player, plugin.getLang().getString("Commands.Sell.NoItemInHand"));
                        return true;
                    }

                    if (!NumberUtils.isNumber(args[1])) {
                        TextUtil.sendMessage(player, plugin.getLang().getString("Commands.Sell.InvalidNumber"));
                        return true;
                    }

                    double price = NumberUtil.formatDecimalPlaces(Double.valueOf(args[1]));

                    if (price > plugin.getLang().getDouble("Settings.MaxPrice")) {
                        TextUtil.sendMessage(player, plugin.getLang().getString("Commands.Sell.PriceToHigh"));
                        return true;
                    } else if (price < plugin.getLang().getDouble("Settings.MinPrice")) {
                        TextUtil.sendMessage(player, plugin.getLang().getString("Commands.Sell.PriceToLow"));
                        return true;
                    }

                    String configSection = "ItemPermissions";

                    if (!player.hasPermission("playershops.bypass.item.permissions")) {
                        for (String section : plugin.getLang().getConfigurationSection(configSection).getKeys(false)) {
                            configSection = configSection + "." + section;
                            if (plugin.getLang().contains(configSection + ".Permission")
                                    && player.hasPermission(plugin.getLang().getString(configSection + ".Permission"))) {

                                boolean sellItems = true;
                                if (plugin.getLang().contains(configSection + ".SellItems")) {
                                    sellItems = plugin.getLang().getBoolean(configSection + ".SellItems");
                                }

                                if (plugin.getLang().contains(configSection + ".Items")) {
                                    List<String> items = plugin.getLang().getStringList(configSection + ".Items");
                                    if(items.contains(item.getType().name())) {
                                        if(sellItems) {
                                            break;
                                        } else {
                                            player.sendMessage("cant sell item");
                                        }
                                    } else {
                                        if (sellItems) {
                                            player.sendMessage("Cant sell that item");
                                            return true;
                                        } else {
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    AsyncCommands.sellCommand(item, player, price);

                    return true;
                } else {
                    TextUtil.sendMessage(player, permission);
                }
                return true;
            }


            if (args[0].equalsIgnoreCase("shop") && isPlayer) {

                Player player = (Player) sender;

                if (blockCreative(player)) return true;

                if (player.hasPermission("playershops.shop")) {

                    if (args.length != 2) {
                        if (onlineInventories.containsKey(player.getUniqueId())) {
                            player.openInventory(onlineInventories.get(player.getUniqueId()));
                            return true;
                        } else {
                            TextUtil.sendMessage(player, plugin.getLang().getString("Commands.Shop.Format"));
                            return true;
                        }
                    }

                    OfflinePlayer seller = Bukkit.getOfflinePlayer(args[1]);

                    if (seller.isOnline()) {
                        if (onlineInventories.containsKey(seller.getUniqueId())) {
                            player.openInventory(onlineInventories.get(seller.getUniqueId()));
                            return true;
                        }
                    }

                    AsyncCommands.shopCommand(player, seller);
                    return true;


                } else {
                    TextUtil.sendMessage(player, permission);
                    return true;
                }
            }


            if(args[0].equalsIgnoreCase("search") &&  isPlayer) {

                Player player = (Player) sender;

                if (blockCreative(player)) return true;

                if (player.hasPermission("playershops.search")) {

                    String search;
                    if (args.length == 1) {
                        Material hand = player.getItemInHand().getType();
                        if (hand == Material.AIR) {
                            TextUtil.sendMessage(player, plugin.getLang().getString("Commands.Search.NoItemInHand"));
                            return true;
                        } else {
                            search = hand.name();
                        }
                    } else if (args.length == 2) {
                        search = args[1];
                    } else {
                        TextUtil.sendMessage(player, plugin.getLang().getString("Commands.Search.Format"));
                        return true;

                    }

                    AsyncCommands.searchDatabase(player, search);

                } else {
                    TextUtil.sendMessage(player, permission);
                }

                return true;
            }


            if(args[0].equalsIgnoreCase("edit") &&  isPlayer) {

                Player player = (Player) sender;

                if (player.hasPermission("playershops.edit")) {

                    if (args.length != 2) {
                        TextUtil.sendMessage(player, plugin.getLang().getString("Commands.Edit.Format"));
                        return true;
                    }

                    OfflinePlayer seller = Bukkit.getOfflinePlayer(args[1]);
                    playersInEditMode.add(player);
                    AsyncCommands.shopCommand(player, seller);

                } else {
                    TextUtil.sendMessage(player, permission);
                }
                return true;
            }



            /*

            CONSOLE AND PLAYER COMMANDS

             */

            if (args[0].equalsIgnoreCase("delete")) {
                if (sender.hasPermission("playershops.delete")) {
                    if (args.length != 2) {
                        TextUtil.sendMessage(sender, plugin.getLang().getString("Commands.Delete.Format"));
                        return true;
                    }

                    AsyncCommands.deleteCommand(sender, Bukkit.getOfflinePlayer(args[1]));

                } else {
                    TextUtil.sendMessage(sender, permission);
                }
                return true;
            }



            if (args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("playershops.reload")) {

                    if (args.length != 1) {
                        TextUtil.sendMessage(sender, plugin.getLang().getString("Commands.Reload.Format"));
                        return true;
                    }

                    plugin.getLang().reload();

                    databaseHandler.close();
                    DatabaseHandler.valueOf(plugin.getLang().getString("Database.Type")).setUp();

                    Date date = new Date();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    tLogger = new TransactionLogger(df.format(date) + ".txt");

                    try {
                        Class.forName("org.spigotmc.SpigotConfig");
                    } catch (ClassNotFoundException ignore) {
                        plugin.getLogger().info("Using spigot will unlock all features");
                        spigot = false;
                    }

                    onlineInventories.clear();
                    offlineInventories.clear();

                    if (Bukkit.getOnlinePlayers().size() > 0) {
                        plugin.getLogger().log(Level.WARNING, "Reloading the server is highly not recommend");
                        plugin.getLogger().info("Loading shops of all players online... (Not Async)");
                        databaseHandler.loadOnlinePlayers();
                        plugin.getLogger().info("Done!");
                    }

                    TextUtil.sendMessage(sender, plugin.getLang().getString("Commands.Reload.Success"));

                } else {
                    TextUtil.sendMessage(sender, permission);
                }
                return true;
            }

            //TODO: Set command
            if (args[0].equalsIgnoreCase("setconfig")) {
                if (sender.hasPermission("playershops.setconfig")) {
                    if (args.length > 2) {
                        if (plugin.getLang().contains(args[1])) {

                            StringBuilder str = new StringBuilder();
                            for (int i = 2; i < args.length; i++) {
                                str.append(args[i] + " ");
                            }

                            args[2] = str.toString().substring(0, str.length()-1);

                            if (args[2].equalsIgnoreCase("true")) {
                                plugin.getLang().set(args[1], true);
                            } else if (args[2].equalsIgnoreCase("false")) {
                                plugin.getLang().set(args[1], false);
                            } else if (NumberUtils.isNumber(args[2])) {
                                plugin.getLang().set(args[1], Double.valueOf(args[2]));
                            } else {
                                plugin.getLang().set(args[1], args[2]);
                            }

                            plugin.getLang().save();
                            TextUtil.sendMessage(sender, plugin.getLang().getString("Commands.SetConfig.Success")
                                    .replaceAll("%location%", args[1])
                                    .replaceAll("%value%", args[2]));

                        } else {
                            TextUtil.sendMessage(sender, plugin.getLang().getString("Commands.SetConfig.NotFound")
                                    .replaceAll("%location%", args[1]));
                        }

                    } else {
                        TextUtil.sendMessage(sender, plugin.getLang().getString("Commands.SetConfig.Format"));
                    }
                } else {
                    TextUtil.sendMessage(sender, permission);
                }
                return true;
            }

            //TODO: Converting command
            if (args[0].equalsIgnoreCase("convert")) {
                if (sender.hasPermission("playershops.convert")) {
                    if (args.length != 2) {
                        TextUtil.sendMessage(sender, plugin.getLang().getString("Commands.Convert.Format"));
                        return true;
                    }

                    Path path = Paths.get(plugin.getDataFolder() + "/data/");

                    if (databaseHandler != DatabaseHandler.MYSQL) {
                        TextUtil.sendMessage(sender, plugin.getLang().getString("Commands.Convert.MySQLSelected"));
                        return true;
                    }

                    if (args[1].equalsIgnoreCase("mysql")) {

                        if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
                            TextUtil.sendMessage(sender, plugin.getLang().getString("Commands.Convert.NoDataFolder"));
                            return true;
                        }

                        AsyncCommands.convertToMySQL(sender);
                    } else if (args[1].equalsIgnoreCase("file")) {
                        AsyncCommands.convertToFile(sender);
                    }

                } else {
                    TextUtil.sendMessage(sender, permission);
                }
                return true;
            }


            if (args[0].equalsIgnoreCase("clean")) {
                if (sender.hasPermission("playershops.clean")) {

                    if (args.length < 1 || args.length > 2) {
                        TextUtil.sendMessage(sender, plugin.getLang().getString("Commands.Clean.Format"));
                        return true;
                    }

                    int days = plugin.getLang().getInt("Settings.CleanDatabase.OlderThan");

                    if (args.length == 2) {
                        if (NumberUtils.isNumber(args[1])) {
                            days = Integer.valueOf(args[1]);
                        }
                    }

                    TextUtil.sendMessage(sender, plugin.getLang().getString("Commands.Clean.Started")
                            .replaceAll("%days%", String.valueOf(days)));

                    AsyncCommands.cleanDatabase(sender, days);

                } else {
                    TextUtil.sendMessage(sender, permission);
                }
                return true;
            }

            //If its not any of the above commands, send help menu
            helpMenu(sender);
        }
        return true;
    }

    private void helpMenu(CommandSender sender) {

        List<String> helpMenu = new ArrayList<>();

        // Commands players can use

        if (sender instanceof Player) {

            if (sender.hasPermission("playershops.sell")) {
                helpMenu.add(plugin.getLang().getString("Commands.Sell.Format"));
            }
            if (sender.hasPermission("playershops.shop")) {
                helpMenu.add(plugin.getLang().getString("Commands.Shop.Format"));
            }
            if (sender.hasPermission("playershops.search")) {
                helpMenu.add(plugin.getLang().getString("Commands.Search.Format"));
            }
            if (sender.hasPermission("playershops.edit")) {
                helpMenu.add(plugin.getLang().getString("Commands.Edit.Format"));
            }
        }

        //Commands both console and players can use

        if (sender.hasPermission("playershops.clean")) {
            helpMenu.add(plugin.getLang().getString("Commands.Clean.Format"));
        }
        if (sender.hasPermission("playershops.delete")) {
            helpMenu.add(plugin.getLang().getString("Commands.Delete.Format"));
        }
        if (sender.hasPermission("playershops.reload")) {
            helpMenu.add(plugin.getLang().getString("Commands.Reload.Format"));
        }
        if (sender.hasPermission("playershops.setconfig")) {
            helpMenu.add(plugin.getLang().getString("Commands.SetConfig.Format"));
        }
        if (sender.hasPermission("playershops.convert")) {
            helpMenu.add(plugin.getLang().getString("Commands.Convert.Format"));
        }

        Collections.sort(helpMenu);

        for(String string: plugin.getLang().getStringList("Commands.HelpMenu.Top")) {
            if (!string.equalsIgnoreCase("") || string != null) {
                TextUtil.sendMessage(sender, string);
            }
        }

        for (String string: helpMenu) {
            if (!string.equalsIgnoreCase("") || string != null) {
                TextUtil.sendMessage(sender, string);
            }
        }

        for(String string: plugin.getLang().getStringList("Commands.HelpMenu.Bottom")) {
            if (!string.equalsIgnoreCase("") || string != null) {
                TextUtil.sendMessage(sender, string);
            }
        }
    }

    private boolean blockCreative(Player player) {
        if (plugin.getLang().getBoolean("Settings.BlockCreative")) {
            if (player.getGameMode() == GameMode.CREATIVE) {
                TextUtil.sendMessage(player, plugin.getLang().getString("BlockCreative"));
                return true;
            }
        }
        return false;
    }
}
