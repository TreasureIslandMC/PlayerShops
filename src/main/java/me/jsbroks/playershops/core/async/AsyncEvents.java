package me.jsbroks.playershops.core.async;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import static me.jsbroks.playershops.PlayerShops.*;

public class AsyncEvents {

    public static void playerQuit(final Player player, final Inventory inv) {
        new BukkitRunnable() {
            @Override
            public void run() {

                if (inv.getViewers().size() > 0) {

                    offlineInventories.put(player.getUniqueId(), inv);

                    for (HumanEntity humanEntity : inv.getViewers()) {
                        humanEntity.closeInventory();
                        humanEntity.openInventory(offlineInventories.get(player.getUniqueId()));
                    }
                    onlineInventories.remove(player.getUniqueId());
                }

                onlineInventories.remove(player.getUniqueId());
                databaseHandler.setInventory(player.getUniqueId(), inv);
            }
        }.runTaskAsynchronously(plugin);
    }

    public static void closeShopInventory(final Player player, final OfflinePlayer shopOwner, final Inventory inventory) {
        new BukkitRunnable() {
            @Override
            public void run() {

                playersInEditMode.remove(player);

                if (needToBeSaved.contains(inventory)) {
                    databaseHandler.setInventory(shopOwner.getUniqueId(), inventory);
                    needToBeSaved.remove(inventory);
                }

            }
        }.runTaskAsynchronously(plugin);
    }
}
