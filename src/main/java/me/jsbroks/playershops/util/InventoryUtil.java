package me.jsbroks.playershops.util;

import me.jsbroks.playershops.PlayerShops;
import me.jsbroks.playershops.core.config.Lang;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class InventoryUtil {
    private static PlayerShops plugin;

    public InventoryUtil(final PlayerShops plugin) {
        InventoryUtil.plugin = plugin;
    }

    /**
     * Converts inventory to base 64
     *
     * @param inventory Inventory to convert to base 64
     * @return String
     */
    public static String toBase64(Inventory inventory) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeInt(inventory.getSize());
            for (int i = 0; i < inventory.getSize(); i++) {
                dataOutput.writeObject(inventory.getItem(i));
            }
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }

    /**
     * Decode base 64 to inventory
     *
     * @param title Title of the inventory
     * @param data  Data string to convert to inventory
     * @return Inventory
     */
    public static Inventory fromBase64(String title, String data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            Inventory inventory = Bukkit.getServer().createInventory(null, dataInput.readInt(), TextUtil.colorize(title));
            for (int i = 0; i < inventory.getSize(); i++) {
                inventory.setItem(i, (ItemStack) dataInput.readObject());
            }
            dataInput.close();
            return inventory;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getInventoryTitle(OfflinePlayer player) {
        return TextUtil.colorize(plugin.getLang().getString("Settings.ShopPrefix")) + " " + player.getName();
    }
}
