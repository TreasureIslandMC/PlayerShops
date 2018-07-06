package me.jsbroks.playershops.core.hooks;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;

class VaultHook {

    private Economy economy;

    VaultHook() {
        ServicesManager services = Bukkit.getServicesManager();
        RegisteredServiceProvider<Economy> economyProvider = services.getRegistration(Economy.class);

        if (economyProvider != null) {
            this.economy = economyProvider.getProvider();
        }
    }

    double getBalance(OfflinePlayer player) {
        if (economy != null) {
            return economy.getBalance(player);
        }
        return -1.0D;
    }

    void withdrawPlayer(OfflinePlayer player, double amount) {
        if (economy != null) {
            economy.withdrawPlayer(player, amount);
        }
    }

    void depositPlayer(OfflinePlayer player, double amount) {
        if (economy != null) {
            economy.depositPlayer(player, amount);
        }
    }

    void hasAccount(OfflinePlayer player) {
        if (economy != null) {
            economy.hasAccount(player);
        }
    }
}
