package me.jsbroks.playershops.util;

import me.jsbroks.playershops.PlayerShops;
import me.jsbroks.playershops.core.config.Lang;

import java.text.DecimalFormat;

public class NumberUtil {
    private static PlayerShops plugin;

    public NumberUtil(final PlayerShops plugin) {
        NumberUtil.plugin = plugin;
    }

    public static double formatDecimalPlaces(double value) {
        return Double.parseDouble(stringFormatDecimalPlaces(value).replaceAll(",",""));
    }

    public static String stringFormatDecimalPlaces(double value) {
        String pattern = plugin.getLang().getString("Settings.DecimalFormat");
        DecimalFormat df = new DecimalFormat(pattern);

        return df.format(value);
    }
}
