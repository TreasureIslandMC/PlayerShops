package me.infopaste.playershops.core;

import me.infopaste.playershops.core.hooks.HookManager;
import me.infopaste.playershops.util.NumberUtil;
import me.infopaste.playershops.util.TextUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class Economy {

    public static boolean canAfford(OfflinePlayer player, double cost) {
        double balance = HookManager.getBalance(player);

        if (balance >= cost) {
            return true;
        }

        return false;
    }

    public static double specialItems(List<String> items, TaxType taxType, double money, ItemStack item, double defaultTax) {
        for(String itemType: items) {
            String[] formatString = itemType.split("|");

            if (itemType.equalsIgnoreCase(item.getType().name())) {
                double taxValue = Double.parseDouble(formatString[1]);
                return taxType.calculateTax(money, taxValue);
            }
        }

        return defaultTax;
    }

    public static double taxDiscount(Player player, double defaultTax) {

        String configSection = "Transaction.TaxDiscounts";

        for (String section : Config.config.getConfigurationSection(configSection).getKeys(false)) {

            configSection = configSection + "." + section;

            if (player.hasPermission(Config.config.getString(configSection + ".Permission"))) {
                TaxType taxType = TaxType.valueOf(Config.config.getString(configSection + ".Tax"));
                double value = Config.config.getDouble(configSection + ".Amount");
                double amount = taxType.discountAmount(defaultTax, value);
                value = taxType.discountTax(defaultTax, value);
                if (Config.config.contains(configSection + ".Message")) {
                    TextUtil.sendMessage(player, Config.config.getString(configSection + ".Message")
                            .replaceAll("%discount%", NumberUtil.stringFormatDecimalPlaces(amount))
                            .replaceAll("%tax%", NumberUtil.stringFormatDecimalPlaces(value)));
                }
                return value;
            }
        }
        return defaultTax;
    }

    public enum TaxType {

        PERCENT,
        FLAT,
        NONE;

        public double calculateTax(double cost, double tax) {
            switch (this) {
                case PERCENT:
                    return NumberUtil.formatDecimalPlaces(cost * tax);
                case FLAT:
                    return tax;
                case NONE:
                    return 0.0;
            }

            return 0.0;
        }

        public double discountTax(double tax, double discount) {
            switch (this) {
                case PERCENT:
                    return NumberUtil.formatDecimalPlaces(tax - (tax*discount));
                case FLAT:
                    return tax - discount;
                case NONE:
                    return 0.0;
            }

            return 0.0;
        }

        public double discountAmount(double tax, double discount) {
            switch (this) {
                case PERCENT:
                    return NumberUtil.formatDecimalPlaces(tax*discount);
                case FLAT:
                    return discount;
                case NONE:
                    return 0.0;
            }

            return 0.0;
        }
    }
}
