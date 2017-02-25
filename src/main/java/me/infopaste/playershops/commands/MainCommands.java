package me.infopaste.playershops.commands;

import me.infopaste.playershops.core.Config;
import me.infopaste.playershops.core.TransactionLogger;
import me.infopaste.playershops.core.async.AsyncCommands;
import me.infopaste.playershops.core.data.DatabaseHandler;
import me.infopaste.playershops.util.NumberUtil;
import me.infopaste.playershops.util.TextUtil;
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

import static me.infopaste.playershops.Main.*;

public class MainCommands implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("playershop")) {

            boolean isPlayer = sender instanceof Player;
            String permission = Config.lang.getString("PermissionDenied");

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
                        TextUtil.sendMessage(player, Config.lang.getString("Commands.Sell.Format"));
                        return true;
                    }

                    //Item is hand is null
                    ItemStack item = player.getItemInHand();
                    if (item.getType() == null || item.getType() == Material.AIR) {
                        TextUtil.sendMessage(player, Config.lang.getString("Commands.Sell.NoItemInHand"));
                        return true;
                    }

                    if (!NumberUtils.isNumber(args[1])) {
                        TextUtil.sendMessage(player, Config.lang.getString("Commands.Sell.InvalidNumber"));
                        return true;
                    }

                    double price = NumberUtil.formatDecimalPlaces(Double.valueOf(args[1]));

                    if (price > Config.config.getDouble("Settings.MaxPrice")) {
                        TextUtil.sendMessage(player, Config.lang.getString("Commands.Sell.PriceToHigh"));
                        return true;
                    } else if (price < Config.config.getDouble("Settings.MinPrice")) {
                        TextUtil.sendMessage(player, Config.lang.getString("Commands.Sell.PriceToLow"));
                        return true;
                    }

                    String configSection = "ItemPermissions";

                    for (String section : Config.config.getConfigurationSection(configSection).getKeys(false)) {
                        configSection = configSection + "." + section;
                        if (Config.config.contains(configSection + ".Permission") && player.hasPermission(Config.config.getString(configSection + ".Permission"))) {

                            boolean sellItems = true;
                            if (Config.config.contains(configSection + ".SellItems")) {
                                sellItems = Config.config.getBoolean(configSection + ".SellItems");
                            }

                            if (Config.config.contains(configSection + ".Items")) {
                                List<String> items = Config.config.getStringList(configSection + ".Items");
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
                            TextUtil.sendMessage(player, Config.lang.getString("Commands.Shop.Format"));
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
                            TextUtil.sendMessage(player, Config.lang.getString("Commands.Search.NoItemInHand"));
                            return true;
                        } else {
                            search = hand.name();
                        }
                    } else if (args.length == 2) {
                        search = args[1];
                    } else {
                        TextUtil.sendMessage(player, Config.lang.getString("Commands.Search.Format"));
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
                        TextUtil.sendMessage(player, Config.lang.getString("Commands.Edit.Format"));
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
                        TextUtil.sendMessage(sender, Config.lang.getString("Commands.Delete.Format"));
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
                        TextUtil.sendMessage(sender, Config.lang.getString("Commands.Reload.Format"));
                        return true;
                    }

                    Config.reloadConfig();
                    Config.reloadLang();

                    databaseHandler.close();
                    DatabaseHandler.valueOf(Config.config.getString("Database.Type")).setUp();

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

                    TextUtil.sendMessage(sender, Config.lang.getString("Commands.Reload.Success"));

                } else {
                    TextUtil.sendMessage(sender, permission);
                }
                return true;
            }

            //TODO: Set command
            if (args[0].equalsIgnoreCase("setconfig")) {
                if (sender.hasPermission("playershops.setconfig")) {
                    if (args.length > 2) {
                        if (Config.config.contains(args[1])) {

                            StringBuilder str = new StringBuilder();
                            for (int i = 2; i < args.length; i++) {
                                str.append(args[i] + " ");
                            }

                            args[2] = str.toString().substring(0, str.length()-1);

                            if (args[2].equalsIgnoreCase("true")) {
                                Config.config.set(args[1], true);
                            } else if (args[2].equalsIgnoreCase("false")) {
                                Config.config.set(args[1], false);
                            } else if (NumberUtils.isNumber(args[2])) {
                                Config.config.set(args[1], Double.valueOf(args[2]));
                            } else {
                                Config.config.set(args[1], args[2]);
                            }

                            Config.saveConfig();
                            TextUtil.sendMessage(sender, Config.lang.getString("Commands.SetConfig.Success")
                                    .replaceAll("%location%", args[1])
                                    .replaceAll("%value%", args[2]));

                        } else {
                            TextUtil.sendMessage(sender, Config.lang.getString("Commands.SetConfig.NotFound")
                                    .replaceAll("%location%", args[1]));
                        }

                    } else {
                        TextUtil.sendMessage(sender, Config.lang.getString("Commands.SetConfig.Format"));
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
                        TextUtil.sendMessage(sender, Config.lang.getString("Commands.Convert.Format"));
                        return true;
                    }

                    Path path = Paths.get(plugin.getDataFolder() + "/data/");

                    if (databaseHandler != DatabaseHandler.MYSQL) {
                        TextUtil.sendMessage(sender, Config.lang.getString("Commands.Convert.MySQLSelected"));
                        return true;
                    }

                    if (args[1].equalsIgnoreCase("mysql")) {

                        if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
                            TextUtil.sendMessage(sender, Config.lang.getString("Commands.Convert.NoDataFolder"));
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
                        TextUtil.sendMessage(sender, Config.lang.getString("Commands.Clean.Format"));
                        return true;
                    }

                    int days = Config.config.getInt("Settings.CleanDatabase.OlderThan");

                    if (args.length == 2) {
                        if (NumberUtils.isNumber(args[1])) {
                            days = Integer.valueOf(args[1]);
                        }
                    }

                    TextUtil.sendMessage(sender, Config.lang.getString("Commands.Clean.Started")
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
                helpMenu.add(Config.lang.getString("Commands.Sell.Format"));
            }
            if (sender.hasPermission("playershops.shop")) {
                helpMenu.add(Config.lang.getString("Commands.Shop.Format"));
            }
            if (sender.hasPermission("playershops.search")) {
                helpMenu.add(Config.lang.getString("Commands.Search.Format"));
            }
            if (sender.hasPermission("playershops.edit")) {
                helpMenu.add(Config.lang.getString("Commands.Edit.Format"));
            }
        }

        //Commands both console and players can use

        if (sender.hasPermission("playershops.clean")) {
            helpMenu.add(Config.lang.getString("Commands.Clean.Format"));
        }
        if (sender.hasPermission("playershops.delete")) {
            helpMenu.add(Config.lang.getString("Commands.Delete.Format"));
        }
        if (sender.hasPermission("playershops.reload")) {
            helpMenu.add(Config.lang.getString("Commands.Reload.Format"));
        }
        if (sender.hasPermission("playershops.setconfig")) {
            helpMenu.add(Config.lang.getString("Commands.SetConfig.Format"));
        }
        if (sender.hasPermission("playershops.convert")) {
            helpMenu.add(Config.lang.getString("Commands.Convert.Format"));
        }

        Collections.sort(helpMenu);

        for(String string: Config.lang.getStringList("Commands.HelpMenu.Top")) {
            if (!string.equalsIgnoreCase("") || string != null) {
                TextUtil.sendMessage(sender, string);
            }
        }

        for (String string: helpMenu) {
            if (!string.equalsIgnoreCase("") || string != null) {
                TextUtil.sendMessage(sender, string);
            }
        }

        for(String string: Config.lang.getStringList("Commands.HelpMenu.Bottom")) {
            if (!string.equalsIgnoreCase("") || string != null) {
                TextUtil.sendMessage(sender, string);
            }
        }
    }

    private boolean blockCreative(Player player) {
        if (Config.config.getBoolean("Settings.BlockCreative")) {
            if (player.getGameMode() == GameMode.CREATIVE) {
                TextUtil.sendMessage(player, Config.lang.getString("BlockCreative"));
                return true;
            }
        }
        return false;
    }
}
