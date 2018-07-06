package me.jsbroks.playershops.listener;

import me.jsbroks.playershops.core.Config;
import me.jsbroks.playershops.core.async.AsyncEvents;
import me.jsbroks.playershops.util.InventoryUtil;
import me.jsbroks.playershops.util.TextUtil;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

import static me.jsbroks.playershops.PlayerShops.*;

/**
 * Events Include
 *
 * PlayerJoinEvent, PlayerQuitEvent, PlayerLoginEvent, AsyncPlayerPreLoginEvent
 */
public class ConnectionEvents implements Listener {

    @EventHandler
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {

        if (event.getLoginResult() == AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            UUID uuid = event.getUniqueId();

            if (offlineInventories.containsKey(uuid)) {
                Inventory inv = offlineInventories.get(uuid);
                onlineInventories.put(uuid, inv);
                offlineInventories.remove(uuid);
            } else {

                if (!databaseHandler.containsPlayer(event.getUniqueId())) {
                    System.out.println("[PlayerShops] Player not found, creating player...");
                    databaseHandler.createPlayer(uuid, event.getName());
                }

                System.out.println("[PlayerShops] Loading " + event.getName() + "'s shop...");
                String data = databaseHandler.getInventory(uuid);

                if (!data.equalsIgnoreCase("Not Set") && !onlineInventories.containsKey(uuid)) {
                    onlineInventories.put(uuid, InventoryUtil.fromBase64(TextUtil.colorize(Config.config.getString("Settings.ShopPrefix")) + " " + event.getName(), data));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        if (update && player.hasPermission("playershops.update")) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    TextUtil.sendMessage(player, Config.lang.getString("Update"));
                }
            }.runTaskLater(plugin , 4);
        }

        AsyncEvents.playerJoin(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (onlineInventories.containsKey(uuid)) {
            Inventory inv = onlineInventories.get(uuid);
            if (inv.getViewers().size() > 0) {
                offlineInventories.put(uuid, inv);
                for (HumanEntity humanEntity : inv.getViewers()) {
                    humanEntity.closeInventory();
                    humanEntity.openInventory(offlineInventories.get(uuid));
                }
                onlineInventories.remove(uuid);
            }

            AsyncEvents.playerQuit(player, inv);
        }
    }

}
