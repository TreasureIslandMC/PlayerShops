package me.jsbroks.playershops.util;

import me.jsbroks.playershops.core.Config;

import java.text.DecimalFormat;

public class NumberUtil {

    public static double formatDecimalPlaces(double value) {
        return Double.parseDouble(stringFormatDecimalPlaces(value).replaceAll(",",""));
    }

    public static String stringFormatDecimalPlaces(double value) {

        String pattern = Config.config.getString("Settings.DecimalFormat");
        DecimalFormat df = new DecimalFormat(pattern);

        return df.format(value);
    }
}
