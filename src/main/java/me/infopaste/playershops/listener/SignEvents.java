package me.infopaste.playershops.listener;

import me.infopaste.playershops.core.Config;
import me.infopaste.playershops.core.async.AsyncCommands;
import me.infopaste.playershops.util.TextUtil;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignEvents implements Listener {

    @EventHandler
    public void onPlayerClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {

            Block block = event.getClickedBlock();

            switch (block.getType()) {
                case SIGN:
                case SIGN_POST:
                case WALL_SIGN:
                    Sign sign = (Sign) block.getState();
                    String[] lines = sign.getLines();

                    if (lines[0].equalsIgnoreCase(TextUtil.colorize(Config.config.getString("Signs.ColorTag")))) {
                        if (player.hasPermission("playershops.sign.use")) {
                            if (lines[1].toUpperCase().contains(TextUtil.colorize(Config.config.getString("Signs.ShopKeyword")).toUpperCase())) {

                                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(TextUtil.removeColorization(lines[2]));
                                AsyncCommands.shopCommand(player, offlinePlayer);

                            } else if (lines[1].toUpperCase().contains(TextUtil.colorize(Config.config.getString("Signs.SearchKeyword")).toUpperCase())) {
                                AsyncCommands.searchDatabase(player, TextUtil.removeColorization(lines[2]));
                            }
                        } else {
                            TextUtil.sendMessage(player, Config.lang.getString("PermissionDenied"));
                        }
                    }
                    return;
            }

        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        String[] lines = event.getLines();

        if (!player.hasPermission("playershops.sign.create")) {
            player.sendMessage(TextUtil.colorize(Config.lang.getString("PermissionDenied")));
            return;
        }

        if (lines != null) {
            if (lines[0].equalsIgnoreCase(TextUtil.colorize(Config.config.getString("Signs.Tag")))) {
                event.setLine(0, TextUtil.colorize(Config.config.getString("Signs.ColorTag")));

                if (lines[1].toUpperCase().contains(TextUtil.colorize(Config.config.getString("Signs.ShopKeyword")).toUpperCase())) {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(lines[2]);
                    event.setLine(2, TextUtil.colorize(Config.config.getString("Signs.Player").replaceAll("%player%", offlinePlayer.getName())));
                } else if (lines[1].toUpperCase().contains(TextUtil.colorize(Config.config.getString("Signs.SearchKeyword")).toUpperCase())) {

                    Material material;

                    if (NumberUtils.isNumber(lines[2])) {
                        material = Material.getMaterial(Integer.valueOf(lines[2]));
                    } else {
                        material = Material.getMaterial(lines[2].toUpperCase());
                    }

                    if (material == null || material.name().equalsIgnoreCase(null)) {
                        event.setLine(0, TextUtil.colorize(Config.config.getString("Signs.InvalidTag")));
                        event.setLine(2, TextUtil.colorize(Config.config.getString("Signs.InvalidMaterial")));
                        return;
                    }

                    event.setLine(2, TextUtil.colorize(Config.config.getString("Signs.Material").replaceAll("%material%", material.name())));

                } else {
                    event.setLine(1, TextUtil.colorize(Config.config.getString("Signs.InvalidKeyword")));
                    event.setLine(0, TextUtil.colorize(Config.config.getString("Signs.InvalidTag")));
                }
            }
        }
    }

}
