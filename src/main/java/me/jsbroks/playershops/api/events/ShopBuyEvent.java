package me.jsbroks.playershops.api.events;

import me.jsbroks.playershops.util.NumberUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class ShopBuyEvent extends Event implements Cancellable{

    private static final HandlerList handlers = new HandlerList();

    private Player buyer;
    private OfflinePlayer seller;
    private double price;
    private ItemStack item;

    private boolean cancelled;

    public ShopBuyEvent(Player buyer, OfflinePlayer seller, double price, ItemStack item) {
        this.buyer = buyer;
        this.seller = seller;
        this.item = item;
        this.price = price;
    }

    public Player getBuyer() {
        return buyer;
    }

    public OfflinePlayer getSeller() {
        return seller;
    }

    public double getPrice() {
        return price;
    }

    public String getPriceFormated() {
        return NumberUtil.stringFormatDecimalPlaces(price);
    }

    public ItemStack getItem() {
        return item;
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
