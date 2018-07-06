package me.jsbroks.playershops.util;

import me.jsbroks.playershops.core.Config;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static me.jsbroks.playershops.Main.databaseHandler;
import static me.jsbroks.playershops.Main.onlineInventories;
import static me.jsbroks.playershops.Main.plugin;

public class PermissionUtil {

    public static int getInventorySize(Player player) {
        int rows = 1;

        if (player.hasPermission("playershops.size.6")) {
            rows = 6;
        } else if (player.hasPermission("playershops.size.5")) {
            rows = 5;
        } else if (player.hasPermission("playershops.size.4")) {
            rows = 4;
        } else if (player.hasPermission("playershops.size.3")) {
            rows = 3;
        } else if (player.hasPermission("playershops.size.2")) {
            rows = 2;
        } else if (player.hasPermission("playershops.size.1")) {
            rows = 1;
        } else if (player.hasPermission("playershops.size.0")) {
            rows = 0;
        }

        return rows * 9;
    }

    /**
     * Compares size of shop to the size a player has permission to have
     *
     * @param player Player who's permission you'd like to compare the shop size too
     * @param shop   Shop that is being compared to the players permission (allowed size)
     * @return
     * e = Player has the same shop size as allowed
     * l = Player has a larger shops size then allowed
     * s = Player has a smaller shop size then allowed
     */
    public static String inventorySizeToPermission(Player player, Inventory shop) {
        int permissionSize = getInventorySize(player);
        int inventorySize = shop.getSize();

        if (inventorySize == permissionSize) {
            // Player has the same inventory size as allowed
            return "e";
        } else if (inventorySize < permissionSize) {
            // Player has permission to make inventory larger
            return "s";
        } else {
            // Player has not enough permission to have an inventory that large
            return "l";
        }
    }

    public static void checkInventory(final Player player, Inventory inv) {

        UUID uuid = player.getUniqueId();
        String shopAllowed = inventorySizeToPermission(player, inv);

        if (shopAllowed.equalsIgnoreCase("s")) {
            //Player has a smaller shop then allowed
            Inventory newInv = Bukkit.createInventory(null, getInventorySize(player), inv.getName());
            newInv.setContents(inv.getContents());
            onlineInventories.put(uuid, newInv);
            databaseHandler.setInventory(uuid, newInv);

        } else if (shopAllowed.equalsIgnoreCase("l")) {
            // Player has a larger shop then allowed
            Inventory newInv = Bukkit.createInventory(null, getInventorySize(player), inv.getName());
            ItemStack[] contents = inv.getContents();

            List<ItemStack> items = new ArrayList<>();

            for (ItemStack item: contents) {
                if (item != null) {
                    items.add(item);
                }
            }

            if (items.size() < newInv.getSize()) {
                for (ItemStack item: items) {
                    newInv.addItem(item);
                }

                onlineInventories.put(uuid, newInv);
                databaseHandler.setInventory(uuid, newInv);

            } else {
                //Players inventory is to large and has extra items in it
                int size = newInv.getSize();
                List<ItemStack> removeItems = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    newInv.addItem(items.get(i));
                    removeItems.add(items.get(i));
                }

                items.removeAll(removeItems);
                final List<ItemStack> finalItems = items;

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        TextUtil.sendMessage(player, Config.lang.getString("ToManyItemsInShop"));
                        for(ItemStack item: finalItems) {
                            player.getWorld().dropItem(player.getLocation(), ItemUtil.removePriceLore(item));
                        }
                    }
                }.runTaskLater(plugin, 5L);

                onlineInventories.put(uuid, newInv);
                databaseHandler.setInventory(uuid, newInv);
            }
        }
    }

}
