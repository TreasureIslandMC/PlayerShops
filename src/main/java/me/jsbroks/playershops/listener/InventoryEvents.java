package me.jsbroks.playershops.listener;

import me.jsbroks.playershops.api.events.ShopCloseEvent;
import me.jsbroks.playershops.core.Config;
import me.jsbroks.playershops.api.events.ShopClickEvent;
import me.jsbroks.playershops.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import static me.jsbroks.playershops.Main.playersInEditMode;

public class InventoryEvents implements Listener {

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inv = event.getInventory();
        Player player = (Player) event.getPlayer();

        String shopTitle = TextUtil.colorize(Config.config.getString("Settings.ShopPrefix"));
        String name = inv.getName();

        playersInEditMode.remove(player);

        if (name.startsWith(shopTitle)) {
            OfflinePlayer shopOwner = Bukkit.getOfflinePlayer(name.substring(shopTitle.length() + 1));

            ShopCloseEvent shopCloseEvent = new ShopCloseEvent(player,
                    shopOwner,
                    inv);

            Bukkit.getServer().getPluginManager().callEvent(shopCloseEvent);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (event.getCurrentItem() == null) {
            return;
        }

        Inventory inv = event.getClickedInventory();
        Player player = (Player) event.getWhoClicked();

        String shopTitle = TextUtil.colorize(Config.config.getString("Settings.ShopPrefix"));
        String name = inv.getName();

        if (name.startsWith(shopTitle)) {
            if (event.getCurrentItem() == null) {
                return;
            }

            boolean editMode = playersInEditMode.contains(player);

            event.setCancelled(!editMode);

            ItemStack item = event.getCurrentItem();
            OfflinePlayer shopOwner = Bukkit.getOfflinePlayer(name.substring(shopTitle.length() + 1));

            ShopClickEvent shopClickEvent = new ShopClickEvent(player,
                    shopOwner,
                    event.getClick(),
                    inv,
                    event.getSlot(),
                    item,
                    editMode);

            Bukkit.getServer().getPluginManager().callEvent(shopClickEvent);
        }
    }
}
