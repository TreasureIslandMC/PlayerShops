package me.infopaste.playershops.api.events;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;

public class ShopCloseEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private Player player;
    private Inventory inventory;
    private OfflinePlayer shopOwner;

    private boolean cancelled;

    /**
     * Event which is fired when a player closes a shop inventory
     *
     * @param player    Player who clicked
     * @param shopOwner Owner of the shop
     * @param inventory Inventory in which player clicked
     */
    public ShopCloseEvent(Player player, OfflinePlayer shopOwner, Inventory inventory) {
        this.player = player;
        this.inventory = inventory;
        this.shopOwner = shopOwner;
    }

    public Player getPlayer() {
        return player;
    }

    public OfflinePlayer getShopOwner() {
        return shopOwner;
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
}
