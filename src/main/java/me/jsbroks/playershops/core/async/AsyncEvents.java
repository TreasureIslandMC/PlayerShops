package me.jsbroks.playershops.core.async;

import me.jsbroks.playershops.core.Config;
import me.jsbroks.playershops.util.InventoryUtil;
import me.jsbroks.playershops.util.ItemUtil;
import me.jsbroks.playershops.util.PermissionUtil;
import me.jsbroks.playershops.util.TextUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

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

    public static void playerJoin(final Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {

                UUID uuid = player.getUniqueId();

                Inventory inv;
                if (onlineInventories.containsKey(uuid)) {
                    inv = onlineInventories.get(uuid);
                } else if (offlineInventories.containsKey(uuid)) {
                    inv = offlineInventories.get(uuid);

                } else {
                    String data = databaseHandler.getInventory(uuid);
                    if (data.equalsIgnoreCase("Not Set")) {
                        return;
                    } else {
                        inv = InventoryUtil.fromBase64(InventoryUtil.getInventoryTitle(player), data);
                    }
                }

                onlineInventories.put(uuid, inv);

                if (inv.getViewers().size() > 0) {

                    offlineInventories.remove(uuid);

                    for (HumanEntity humanEntity : inv.getViewers()) {
                        humanEntity.closeInventory();
                        humanEntity.openInventory(onlineInventories.get(uuid));
                    }
                }

                if (Config.config.getBoolean("Bill.JoinNotification")) {
                    int billCounter = 0;

                    for (ItemStack item : inv.getContents()) {
                        if (item != null) {
                            if (ItemUtil.isBill(item)) {
                                billCounter++;
                            }
                        }
                    }


                    final int bCount = billCounter;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (bCount > 0) {
                                TextUtil.sendMessage(player, Config.lang.getString("Transaction.CollectBills").replaceAll("%amount%", String.valueOf(bCount)));
                            }
                        }
                    }.runTaskLater(plugin, 5);
                }

                // Check Shop Size

                PermissionUtil.checkInventory(player,inv);

            }
        }.runTaskAsynchronously(plugin);
    }

    public static void closeShopInventory(final Player player, final OfflinePlayer shopOwner, final Inventory inventory) {
        new BukkitRunnable() {
            @Override
            public void run() {

                if (playersInEditMode.contains(player)) {
                    playersInEditMode.remove(player);
                }

                if (needToBeSaved.contains(inventory)) {
                    databaseHandler.setInventory(shopOwner.getUniqueId(), inventory);
                    needToBeSaved.remove(inventory);
                }

            }
        }.runTaskAsynchronously(plugin);
    }
}
