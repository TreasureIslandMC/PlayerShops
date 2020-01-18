package me.jsbroks.playershops.listener;

import me.jsbroks.playershops.PlayerShops;
import me.jsbroks.playershops.core.config.Lang;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class ChatEvents implements Listener {
    private PlayerShops plugin;

    public ChatEvents(final PlayerShops plugin) {
        this.plugin = plugin;
    }

    //onTabComplete...
    @EventHandler
    public void onCommandProcess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        //Shop Command
        if (plugin.getLang().contains("CustomCommands.Shop")) {
            String cShop = plugin.getLang().getString("CustomCommands.Shop").toUpperCase();
            if (message.toUpperCase().startsWith(cShop)) {
                player.chat("/ps shop" + message.substring(cShop.length()));
                event.setCancelled(true);
                return;
            }
        }

        //Search Command
        if (plugin.getLang().contains("CustomCommands.Search")) {
            String cSearch = plugin.getLang().getString("CustomCommands.Search").toUpperCase();
            if (message.toUpperCase().startsWith(cSearch)) {
                player.chat("/ps search" + message.substring(cSearch.length()));
                event.setCancelled(true);
                return;
            }
        }

        //Sell Command
        if (plugin.getLang().contains("CustomCommands.Sell")) {
            String cSearch = plugin.getLang().getString("CustomCommands.Sell").toUpperCase();
            if (message.toUpperCase().startsWith(cSearch)) {
                player.chat("/ps sell" + message.substring(cSearch.length()));
                event.setCancelled(true);
                return;
            }

        }
    }
}
