package me.jsbroks.playershops.api.events;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;


public class ShopClickEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private Player player;
    private int slot;
    private ItemStack item;
    private Inventory inventory;
    private OfflinePlayer shopOwner;
    private boolean isEditMode;
    private ClickType clickType;

    private boolean cancelled;

    /**
     * Event which is fired when a player clicks inside a shop inventory
     *
     * @param player    Player who clicked
     * @param shopOwner Owner of the shop
     * @param inventory Inventory in which player clicked
     * @param slot      Slot the player clicked
     * @param item      Item the player clicked
     */
    public ShopClickEvent(Player player, OfflinePlayer shopOwner, ClickType clickType, Inventory inventory, int slot, ItemStack item, boolean editMode) {
        this.player = player;
        this.item = item;
        this.inventory = inventory;
        this.slot = slot;
        this.shopOwner = shopOwner;
        this.isEditMode = editMode;
        this.clickType = clickType;
    }

    public Player getWhoClicked() {
        return player;
    }

    public OfflinePlayer getShopOwner() {
        return shopOwner;
    }

    public ItemStack getItem() {
        return item;
    }

    public int getSlot() {
        return slot;
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public ClickType getClick() {
        return clickType;
    }
}
