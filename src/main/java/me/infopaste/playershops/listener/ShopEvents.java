package me.infopaste.playershops.listener;

import me.infopaste.playershops.api.events.ShopBuyEvent;
import me.infopaste.playershops.api.events.ShopClickEvent;
import me.infopaste.playershops.api.events.ShopCloseEvent;
import me.infopaste.playershops.core.Config;
import me.infopaste.playershops.core.Economy;
import me.infopaste.playershops.core.hooks.HookManager;
import me.infopaste.playershops.core.async.AsyncEvents;
import me.infopaste.playershops.util.ItemUtil;

import me.infopaste.playershops.util.NumberUtil;
import me.infopaste.playershops.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

import static me.infopaste.playershops.Main.needToBeSaved;
import static me.infopaste.playershops.Main.tLogger;
import static me.infopaste.playershops.core.Economy.canAfford;

public class ShopEvents implements Listener {

    @EventHandler
    public void onShopClick(ShopClickEvent event) {

        Player player = event.getWhoClicked();
        OfflinePlayer owner = event.getShopOwner();

        Inventory inv = event.getInventory();
        ItemStack item = event.getItem();

        boolean isBill = ItemUtil.isBill(item);

        if (event.isEditMode() || item.getType() == Material.AIR) {
            return;
        }

        if (owner.isOnline() && owner.getName().equalsIgnoreCase(player.getName())) {

            if (isBill) {

                double money = ItemUtil.getBillMoney(item);
                double taxAmount = 0;

                if (money >= Config.config.getDouble("Transaction.Buy.DontApply")) {

                    Economy.TaxType taxType = Economy.TaxType.valueOf(Config.config.getString("Transaction.Buy.Tax"));

                    if (taxType != Economy.TaxType.NONE) {

                        taxAmount = taxType.calculateTax(money, Config.config.getDouble("Transaction.Buy.Amount"));

                        List<String> items = Config.config.getStringList("Transaction.Buy.Items");

                        //Apply special item taxes
                        taxAmount = Economy.specialItems(items, taxType, money, item, taxAmount);

                        //Apply discounts if they apply
                        taxAmount = Economy.taxDiscount(player, taxAmount);

                        if (!canAfford(player, taxAmount)) {
                            TextUtil.sendMessage(player, Config.lang.getString("Transaction.Tax.CantAfford")
                                    .replaceAll("%amount%", NumberUtil.stringFormatDecimalPlaces(taxAmount))
                                    .replaceAll("%taxtype%", taxType.toString()));
                            return;
                        }

                        TextUtil.sendMessage(player, Config.lang.getString("Transaction.Tax.Applied")
                                .replaceAll("%amount%", NumberUtil.stringFormatDecimalPlaces(taxAmount))
                                .replaceAll("%taxtype%", taxType.toString()));

                        tLogger.tax(player, taxType, taxAmount);

                        HookManager.withdrawMoney(player, taxAmount);
                    }

                }

                HookManager.depositMoney(player, money);

                if (Config.config.getBoolean("Bill.PlayerKeepsBill")) {

                    if (player.getInventory().firstEmpty() == -1) {
                        TextUtil.sendMessage(player, Config.lang.getString("NoInventorySpace"));
                        return;
                    }

                    inv.removeItem(item);
                    ItemMeta itemMeta = item.getItemMeta();
                    itemMeta.setDisplayName(itemMeta.getDisplayName() + " " + TextUtil.colorize(Config.config.getString("Bill.Completed")));
                    item.setItemMeta(itemMeta);

                    player.getInventory().addItem(item);
                } else {
                    inv.removeItem(item);
                }

                TextUtil.sendMessage(player, Config.lang.getString("Transaction.Bill").replaceAll("%money%", NumberUtil.stringFormatDecimalPlaces(money-taxAmount)));
                tLogger.bill(player);
                needToBeSaved.add(inv);
                return;
            }

            if (player.getInventory().firstEmpty() == -1) {
                TextUtil.sendMessage(player, Config.lang.getString("NoInventorySpace"));
                return;
            }

            inv.setItem(event.getSlot(), new ItemStack(Material.AIR));
            player.getInventory().addItem(ItemUtil.removePriceLore(item));
            needToBeSaved.add(inv);
            return;

        } else {
            //Another player is access the shop of the owner

            if (isBill) {
                return;
            }

            double itemPrice = ItemUtil.getItemPrice(item);

            if (Economy.canAfford(player, itemPrice)) {

                // Player is buying an item and can afford it
                item = ItemUtil.removePriceLore(item);

                if (player.getInventory().firstEmpty() == -1) {
                    TextUtil.sendMessage(player, Config.lang.getString("NoInventorySpace"));
                    return;
                }

                HookManager.withdrawMoney(player, itemPrice);
                inv.setItem(event.getSlot(), ItemUtil.createBill(player, itemPrice, item));
                player.getInventory().addItem(item);

                needToBeSaved.add(inv);

                ShopBuyEvent shopBuyEvent = new ShopBuyEvent(player, owner, itemPrice, item);
                Bukkit.getServer().getPluginManager().callEvent(shopBuyEvent);

                return;
            } else {
                TextUtil.sendMessage(player, Config.lang.getString("Transaction.NotEnoughMoney"));
                return;
            }
        }
    }

    @EventHandler
    public void onShopClose(ShopCloseEvent event) {
        Player player = event.getPlayer();
        Inventory inv = event.getInventory();
        OfflinePlayer shopOwner = event.getShopOwner();

        AsyncEvents.closeShopInventory(player, shopOwner, inv);
    }

    @EventHandler
    public void onShopBuy(ShopBuyEvent event) {
        Player buyer = event.getBuyer();
        OfflinePlayer seller = event.getSeller();

        ItemStack item = event.getItem();
        int amount = item.getAmount();
        String name = item.getType().name();
        String price = event.getPriceFormated();

        tLogger.transaction(buyer, seller, item, event.getPrice());

        if (seller.isOnline()) {
            Player sellerOnline = seller.getPlayer();
            TextUtil.sendMessage(sellerOnline, Config.lang.getString("Transaction.Notify")
                    .replaceAll("%player%", buyer.getName())
                    .replaceAll("%amount%", String.valueOf(amount))
                    .replaceAll("%item%", name)
                    .replaceAll("%price%", price));
        }

        TextUtil.sendMessage(buyer, Config.lang.getString("Transaction.Message")
                .replaceAll("%player%", seller.getName())
                .replaceAll("%amount%", String.valueOf(amount))
                .replaceAll("%item%", name)
                .replaceAll("%price%", price));
    }
}
