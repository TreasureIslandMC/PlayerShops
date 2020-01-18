package me.jsbroks.playershops.util;

import me.jsbroks.playershops.PlayerShops;
import me.jsbroks.playershops.core.config.Lang;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class TextUtil {
    private static PlayerShops plugin;

    public TextUtil(final PlayerShops plugin) {
        TextUtil.plugin = plugin;
    }

    public static String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static void sendMessage(CommandSender sender, String message) {
        if (!message.equalsIgnoreCase("") || !message.equalsIgnoreCase(null)) {
            sender.sendMessage(TextUtil.colorize(message.replaceAll("%prefix%", plugin.getLang().getString("Prefix"))));
        }
    }

    @Deprecated
    public static String removeColorization(String string) {
        return string.replaceAll("§1|§2|§3|§4|§5|§6|§7|§8|§9|§a|§b|§c|§d", "");
    }
}

