package me.jsbroks.playershops.listener;

import com.google.common.base.Preconditions;
import me.jsbroks.playershops.PlayerShops;
import me.jsbroks.playershops.core.config.Lang;
import me.jsbroks.playershops.core.async.AsyncCommands;
import me.jsbroks.playershops.util.ItemUtil;
import me.jsbroks.playershops.util.TextUtil;
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
	private PlayerShops plugin;

	public SignEvents(final PlayerShops plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerClick(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
		    return;


        final Player player = event.getPlayer();
		Block block = Preconditions.checkNotNull(event.getClickedBlock());
		if (ItemUtil.isSign(block)) {
			Sign sign = (Sign) block.getState();
			String[] lines = sign.getLines();

			if (lines[0].equalsIgnoreCase(TextUtil.colorize(plugin.getLang().getString("Signs.ColorTag")))) {
				if (player.hasPermission("playershops.sign.use")) {
					if (lines[1].toUpperCase().contains(TextUtil.colorize(plugin.getLang().getString("Signs.ShopKeyword")).toUpperCase())) {

						OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(TextUtil.removeColorization(lines[2]));
						AsyncCommands.shopCommand(player, offlinePlayer);

					} else if (lines[1].toUpperCase().contains(TextUtil.colorize(plugin.getLang().getString("Signs.SearchKeyword")).toUpperCase())) {
						AsyncCommands.searchDatabase(player, TextUtil.removeColorization(lines[2]));
					}
				} else {
					TextUtil.sendMessage(player, plugin.getLang().getString("PermissionDenied"));
				}
			}
		}


	}

	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		Player player = event.getPlayer();
		String[] lines = event.getLines();

		if (!player.hasPermission("playershops.sign.create")) {
			player.sendMessage(TextUtil.colorize(plugin.getLang().getString("PermissionDenied")));
			return;
		}

		if (lines[0].equalsIgnoreCase(TextUtil.colorize(plugin.getLang().getString("Signs.Tag")))) {
			event.setLine(0, TextUtil.colorize(plugin.getLang().getString("Signs.ColorTag")));

			if (lines[1].toUpperCase().contains(TextUtil.colorize(plugin.getLang().getString("Signs.ShopKeyword")).toUpperCase())) {
				OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(lines[2]);
				event.setLine(2, TextUtil.colorize(plugin.getLang().getString("Signs.Player").replaceAll("%player%", offlinePlayer.getName())));
			} else if (lines[1].toUpperCase().contains(TextUtil.colorize(plugin.getLang().getString("Signs.SearchKeyword")).toUpperCase())) {
				final Material material = Material.getMaterial(lines[2].toUpperCase());

				if (material == null) {
					event.setLine(0, TextUtil.colorize(plugin.getLang().getString("Signs.InvalidTag")));
					event.setLine(2, TextUtil.colorize(plugin.getLang().getString("Signs.InvalidMaterial")));
					return;
				}
				event.setLine(2, TextUtil.colorize(plugin.getLang().getString("Signs.Material").replaceAll("%material%", material.name())));

			} else {
				event.setLine(1, TextUtil.colorize(plugin.getLang().getString("Signs.InvalidKeyword")));
				event.setLine(0, TextUtil.colorize(plugin.getLang().getString("Signs.InvalidTag")));
			}
		}
	}

}
