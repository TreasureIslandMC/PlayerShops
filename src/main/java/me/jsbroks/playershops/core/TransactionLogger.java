package me.jsbroks.playershops.core;

import me.jsbroks.playershops.util.NumberUtil;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import static me.jsbroks.playershops.Main.plugin;

public class TransactionLogger {

    private boolean enabled;
    private boolean enabledSell;
    private boolean enabledTransaction;
    private boolean enabledAdminMode;
    private boolean enabledTax;
    private boolean enabledBill;

    private File file;

    public TransactionLogger(String file) {
        enabled = Config.config.getBoolean("Settings.Log.Enabled");
        if (enabled) {

            enabledSell = Config.config.getBoolean("Settings.Log.AddItemToShop");
            enabledTransaction = Config.config.getBoolean("Settings.Log.Transaction");
            enabledBill = Config.config.getBoolean("Settings.Log.BillCollection");
            enabledTax = Config.config.getBoolean("Settings.Log.Taxes");

            this.file = new File(plugin.getDataFolder() + "/logs/", file);
            createFile();
        } else {
            enabledBill = false;
            enabledAdminMode = false;
            enabledTax = false;
            enabledSell = false;
            enabledTransaction = false;
        }
    }

    public void tax(Player player, Economy.TaxType taxType, double price) {
        if (enabledTax) {
            String name = player.getName();
            String tax = taxType.name();
            String cost = NumberUtil.stringFormatDecimalPlaces(price);

            String log = "%t% tax fee applied to %n% of %p%"
                    .replaceAll("%p%", cost)
                    .replaceAll("%t%", tax)
                    .replaceAll("%n%", name);

            writeString(log);
        }
    }

    public void transaction(Player buyer, OfflinePlayer seller, ItemStack item, double price) {
        if (enabledTransaction) {
            String buyerName = buyer.getName();
            String sellerName = seller.getName();
            String cost = NumberUtil.stringFormatDecimalPlaces(price);
            int amount = item.getAmount();

            String log = "%b% bought %a% %m% from %s% for %p%"
                    .replaceAll("%p%", cost)
                    .replaceAll("%b%", buyerName)
                    .replaceAll("%a%", String.valueOf(amount))
                    .replaceAll("%s%", sellerName);

            writeString(log);
        }
    }

    public void sell(Player player, ItemStack item, double price) {
        if (enabledSell) {
            String sellerName = player.getName();
            String cost = NumberUtil.stringFormatDecimalPlaces(price);
            String itemName = item.getType().name();
            String amount = String.valueOf(item.getAmount());

            String log = "%s% added %a% %m% for %p%"
                    .replaceAll("%p%", cost)
                    .replaceAll("%m%", itemName)
                    .replaceAll("%a%", amount)
                    .replaceAll("%s%", sellerName);

            writeString(log);
        }

    }

    public void bill(Player player) {
        if (enabledBill) {
            String playerName = player.getName();

            String log = "%p% collected a bill"
                    .replaceAll("%p%", playerName);

            writeString(log);
        }
    }

    public void createFile() {
        if (!file.exists()) {
            try {

                plugin.getLogger().info("Creating new transaction log file");
                File dataFolder = new File(plugin.getDataFolder() + "/logs/");
                if(!dataFolder.exists()) {
                    dataFolder.mkdir();
                }

                file.createNewFile();

                writeString(" ***** Transaction log file created ***** ");

            } catch (IOException e) {
                plugin.getLogger().info("Could not create transaction file");
                e.printStackTrace();
            }
        }
    }

    public void writeString(String string) {
        if (enabled) {
            try {
                FileWriter fw = new FileWriter(file, true);
                PrintWriter pw = new PrintWriter(fw);

                Date date = new Date();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                String prefix = "[" + df.format(date) + "] ";
                pw.println(prefix + string);

                pw.flush();
                pw.close();

            } catch (IOException e) {
                plugin.getLogger().info("Could not write to transaction file");
            }
        }
    }
}
