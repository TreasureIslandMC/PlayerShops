package me.infopaste.playershops.listener;

import me.infopaste.playershops.core.Config;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class ChatEvents implements Listener {

    @EventHandler
    public void onCommandProcess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        //Shop Command
        if (Config.config.contains("CustomCommands.Shop")) {
            String cShop = Config.config.getString("CustomCommands.Shop").toUpperCase();
            if (message.toUpperCase().startsWith(cShop)) {
                player.chat("/ps shop" + message.substring(cShop.length()));
                event.setCancelled(true);
                return;
            }
        }

        //Search Command
        if (Config.config.contains("CustomCommands.Search")) {
            String cSearch = Config.config.getString("CustomCommands.Search").toUpperCase();
            if (message.toUpperCase().startsWith(cSearch)) {
                player.chat("/ps search" + message.substring(cSearch.length()));
                event.setCancelled(true);
                return;
            }
        }

        //Sell Command
        if (Config.config.contains("CustomCommands.Sell")) {
            String cSearch = Config.config.getString("CustomCommands.Sell").toUpperCase();
            if (message.toUpperCase().startsWith(cSearch)) {
                player.chat("/ps sell" + message.substring(cSearch.length()));
                event.setCancelled(true);
                return;
            }

        }
    }
}
