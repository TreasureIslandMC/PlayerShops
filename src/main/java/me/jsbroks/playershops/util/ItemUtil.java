package me.jsbroks.playershops.util;

import me.jsbroks.playershops.PlayerShops;
import me.jsbroks.playershops.core.config.Lang;
import me.jsbroks.playershops.core.ItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.*;

public class ItemUtil {
    private static PlayerShops plugin;

    public ItemUtil(final PlayerShops plugin) {
        ItemUtil.plugin = plugin;
    }

    /**
     * Creates a transaction bill
     *
     * @param player Player who purchases the item
     * @param price  Price the player purchased the item
     * @param item   Item the player purchased
     * @return ItemStack
     */
    public static ItemStack createBill(Player player, double price, ItemStack item) {

        String title = plugin.getLang().getString("Bill.Title");
        List<String> lores = plugin.getLang().getStringList("Bill.Lore");
        Material material = Material.getMaterial(plugin.getLang().getString("Bill.Material"));

        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat(plugin.getLang().getString("Bill.DateFormat"));

        List<String> newLore = new ArrayList<>();

        boolean containsPrice = false;
        for (String lore: lores) {
            if (!containsPrice && lore.contains("%price%")) {
                containsPrice = true;
            }
            newLore.add(lore
                    .replaceAll("%player%", player.getName())
                    .replaceAll("%item%", item.getType().name())
                    .replaceAll("%amount%", String.valueOf(item.getAmount()))
                    .replaceAll("%price%", NumberUtil.stringFormatDecimalPlaces(price))
                    .replaceAll("%date", format.format(date))
            );
        }

        if (containsPrice == false) {
            System.out.println("[PlayerShops] Creating a bill with no price lore, this will cause errors in the future");
        }

        return new ItemStackBuilder(material).withName(title).withLore(newLore).hideAttributes().build();
    }

    public static boolean isBill(ItemStack item) {
        if (item.getType() == Material.valueOf(plugin.getLang().getString("Bill.Material"))) {
            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                ItemMeta itemMeta = item.getItemMeta();
                String name = itemMeta.getDisplayName();

                return name.equalsIgnoreCase(TextUtil.colorize(plugin.getLang().getString("Bill.Title")));
            }
        }
        return false;
    }

    public static double getBillMoney(ItemStack bill) {
        if (bill.hasItemMeta()) {
            ItemMeta itemMeta = bill.getItemMeta();
            List<String> lores = itemMeta.getLore();
            List<String> configLores = plugin.getLang().getStringList("Bill.Lore");
            for (int i = 0; i < configLores.size(); i++) {
                String configLore = configLores.get(i);
                if (configLore.contains("%price%")) {
                    configLore = TextUtil.colorize(configLore);
                    String[] itemLore = lores.get(i).split(" ");
                    String[] formatLore = configLore.split(" ");
                    for (int x = 0; x < formatLore.length; x ++) {
                        String format = formatLore[x];
                        if (format.contains("%price%")) {
                            String priceLore = TextUtil.removeColorization(itemLore[x]);
                            return Double.parseDouble(priceLore.replaceAll("[^\\d.]", ""));
                        }
                    }
                    break;
                }
            }
        }
        return 0.0;
    }

    public static double getItemPrice(ItemStack item) {

        String priceFormat[] = TextUtil.colorize(plugin.getLang().getString("Settings.PriceLore")).split("%price%", 2);

        if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
            ItemMeta itemMeta = item.getItemMeta();
            List<String> lore = itemMeta.getLore();

            for (String l : lore) {
                if (!priceFormat[0].equalsIgnoreCase("")) {
                    if (l.startsWith(priceFormat[0])) {
                        String price = l.substring(priceFormat[0].length(), l.length() - priceFormat[1].length()).replaceAll("[^\\d.]", "");
                        return Double.parseDouble(price);
                    }
                } else if (!priceFormat[1].equalsIgnoreCase("")) {
                    if (l.endsWith(priceFormat[1])) {
                        String price = l.substring(priceFormat[0].length(), l.length() - priceFormat[1].length()).replaceAll("[^\\d.]", "");
                        return Double.parseDouble(price);
                    }
                }
            }
        }

        return 0;
    }

    public static double getItemPriceEach(ItemStack item) {

        String[] priceFormat = TextUtil.colorize(plugin.getLang().getString("Settings.PriceEachLore")).split("%price%", 2);

        if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
            ItemMeta itemMeta = item.getItemMeta();
            List<String> lore = itemMeta.getLore();

            for (String l : lore) {
                if (!priceFormat[0].equalsIgnoreCase("")) {
                    if (l.startsWith(priceFormat[0])) {
                        String price = l.substring(priceFormat[0].length(), l.length() - priceFormat[1].length()).replaceAll("[^\\d.]", "");
                        return NumberUtil.formatDecimalPlaces(Double.parseDouble(price));
                    }
                } else if (!priceFormat[1].equalsIgnoreCase("")) {
                    if (l.endsWith(priceFormat[1])) {
                        String price = l.substring(priceFormat[0].length(), l.length() - priceFormat[1].length()).replaceAll("[^\\d.]", "");
                        return NumberUtil.formatDecimalPlaces(Double.parseDouble(price));
                    }
                }
            }
        }

        return 0;
    }

    public static ItemStack removePriceLore(ItemStack item) {
        if (item.hasItemMeta()) {
            ItemMeta itemMeta = item.getItemMeta();
            if (itemMeta.hasLore()) {
                List<String> lore = itemMeta.getLore();
                Set<String> removeLore = new HashSet<>();

                String priceEach[] = TextUtil.colorize(plugin.getLang().getString("Settings.PriceEachLore")).split("%price%", 2);
                String price[] = TextUtil.colorize(plugin.getLang().getString("Settings.PriceLore")).split("%price%", 2);

                for (String l : lore) {
                    if (l.startsWith(priceEach[0])) {
                        removeLore.add(l);
                    } else if (l.startsWith(price[0])) {
                        removeLore.add(l);
                    } else if (!price[1].equalsIgnoreCase("")) {
                        if (l.endsWith(price[0])) {
                            removeLore.add(l);
                        }
                    }else if (!priceEach[1].equalsIgnoreCase("")) {
                        if (l.endsWith(priceEach[0])) {
                            removeLore.add(l);
                        }
                    }
                }

                lore.removeAll(removeLore);
                itemMeta.setLore(lore);
            }
            itemMeta.removeItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(itemMeta);
        }

        return item;
    }

    public static ItemStack addPriceLore(ItemStack item, double price) {
        ItemMeta itemMeta = item.getItemMeta();

        List<String> lore = new ArrayList<>();

        if (itemMeta.hasLore()) {
            lore.addAll(itemMeta.getLore());
        }

        lore.add(TextUtil.colorize(plugin.getLang().getString("Settings.PriceLore").replaceAll("%price%", NumberUtil.stringFormatDecimalPlaces(price))));
        lore.add(TextUtil.colorize(plugin.getLang().getString("Settings.PriceEachLore").replaceAll("%price%", NumberUtil.stringFormatDecimalPlaces(price/item.getAmount()))));
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemMeta.setLore(lore);


        item.setItemMeta(itemMeta);
        return item;
    }

    public static boolean isSign(Block block){
        return block.getState() instanceof Sign;
    }

}
