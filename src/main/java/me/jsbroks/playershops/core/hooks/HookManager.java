package me.jsbroks.playershops.core.hooks;

import me.jsbroks.playershops.PlayerShops;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class HookManager {

    private static VaultHook vault;

    public static void loadDependencies() {
        if (doesPluginExist("Vault", "[PlayerShop] Hooked: Vault")) {
            vault = new VaultHook();
        } else {
            PlayerShops.plugin.getLogger().info("Could not find Vault, disabling...");
            Bukkit.getPluginManager().disablePlugin(PlayerShops.plugin);
        }

        if (doesPluginExist("ProtectionStones")) {
            PlayerShops.plugin.getLogger().info("ProtectionStones is known you over ride playershops command '/ps'");
            PlayerShops.plugin.getLogger().info("You can use other commands such as '/pshops', '/playershops' to access it");
        }
    }

    private static boolean doesPluginExist(String plugin, String message) {
        boolean hooked = doesPluginExist(plugin);
        if (hooked) {
            System.out.println(message);
        }
        return hooked;
    }

    private static boolean doesPluginExist(String plugin) {
        return Bukkit.getPluginManager().getPlugin(plugin) != null;
    }

    private static boolean isVaultLoaded() {
        return vault != null;
    }

    public static double getBalance(OfflinePlayer player) {
        if (isVaultLoaded()) {
            return vault.getBalance(player);
        }
        return -1.0;
    }

    public static void depositMoney(OfflinePlayer player, double amount) {
        if (isVaultLoaded()) {
            vault.depositPlayer(player, amount);
        }
    }

    public static void withdrawMoney(OfflinePlayer player, double amount) {
        if (isVaultLoaded()) {
            vault.withdrawPlayer(player, amount);
        }
    }

}
