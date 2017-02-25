package me.infopaste.playershops.core.data;

import me.infopaste.playershops.util.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static me.infopaste.playershops.Main.plugin;
import static sun.audio.AudioPlayer.player;

class ShopFile {

    private OfflinePlayer offlinePlayer;
    private UUID uuid;
    private File playerFile;
    private FileConfiguration playerConfig;
    private YamlConfiguration yamlPlayerConfig;

    public ShopFile(UUID uuid) {
        this.uuid = uuid;
        this.offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        playerFile = new File(plugin.getDataFolder() + "/data/", uuid.toString() + ".yml");
        playerConfig = YamlConfiguration.loadConfiguration(playerFile);
        yamlPlayerConfig = YamlConfiguration.loadConfiguration(playerFile);
        createPlayer();
    }

    public ShopFile(String uuid, String name, String lastseen, String inventory) {
        this.uuid = UUID.fromString(uuid);
        this.offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        playerFile = new File(plugin.getDataFolder() + "/data/", uuid + ".yml");
        playerConfig = YamlConfiguration.loadConfiguration(playerFile);
        yamlPlayerConfig = YamlConfiguration.loadConfiguration(playerFile);
        createPlayer(uuid, name, lastseen, inventory);
    }

    public void createPlayer() {
        if (!(playerFile.exists())) {

            Date dNow = new Date();
            SimpleDateFormat ft = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");

            yamlPlayerConfig.set("Player.Name", offlinePlayer.getName());
            yamlPlayerConfig.set("Player.UUID", uuid.toString());
            yamlPlayerConfig.set("Player.LastSeen", ft.format(dNow));
            yamlPlayerConfig.set("Inventory", "Not Set");
            saveYamlFile();
        }
    }

    public void createPlayer(String uuid, String name, String lastseen, String inventory) {
        if (!(playerFile.exists())) {

            yamlPlayerConfig.set("Player.Name", name);
            yamlPlayerConfig.set("Player.UUID", uuid);
            yamlPlayerConfig.set("Player.LastSeen", lastseen);
            yamlPlayerConfig.set("Inventory", inventory);
            saveYamlFile();
        }
    }

    public void saveYamlFile() {
        try {
            yamlPlayerConfig.save(playerFile);
        } catch (Exception ignore) {
        }
    }

    public String getInventory() {
        return  yamlPlayerConfig.getString("Inventory");
    }

    public void setInventory(Inventory inv) {
        String data = InventoryUtil.toBase64(inv);
        yamlPlayerConfig.set("Inventory", data);
        saveYamlFile();
    }

    public boolean exists() {
        return playerFile.exists();
    }

    public void delete() {
        playerFile.delete();
    }

    public void clean() {
        String date = yamlPlayerConfig.getString("Player.LastSeen");

    }

    public void search() {

    }

}
