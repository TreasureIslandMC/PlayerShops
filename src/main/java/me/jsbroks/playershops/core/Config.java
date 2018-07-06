package me.jsbroks.playershops.core;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static me.jsbroks.playershops.PlayerShops.plugin;

public class Config {

    public static FileConfiguration config;
    static File cfile;
    
    public static FileConfiguration lang;
    static File lfile;

    public static void setup(Plugin p) {
        cfile = new File(p.getDataFolder(), "config.yml");
        lfile = new File(p.getDataFolder(), "lang.yml");
        
        if (!cfile.exists()) {
            cfile.getParentFile().mkdirs();
            p.saveResource("config.yml", false);
        }
        
        if (!lfile.exists()) {
            lfile.getParentFile().mkdirs();
            p.saveResource("lang.yml", false);
        }
        
        config = new YamlConfiguration();
        lang = new YamlConfiguration();
        
        try {
            config.load(cfile);
            lang.load(lfile);
            
            config = YamlConfiguration.loadConfiguration(cfile);
            lang = YamlConfiguration.loadConfiguration(lfile);
            
            config.options().copyDefaults(true);
            lang.options().copyDefaults(true);
            
            p.saveDefaultConfig();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void saveLang() {
        try {
            lang.save(lfile);
        } catch (IOException e) {
            System.out.print("Error saving the lang.yml file");
        }
    }

    public static void reloadLang() {
        if (lang == null) {
            lfile = new File(plugin.getDataFolder(), "lang.yml");
        }

        lang = YamlConfiguration.loadConfiguration(lfile);
        try {
            InputStream defConfigStream = plugin.getResource("lang.yml");
            if (defConfigStream != null) {
                YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
                lang.setDefaults(defConfig);
            }
        }catch (NullPointerException ignored){

        }
    }

    public static void reloadConfig() {
        if (config == null) {
            cfile = new File(plugin.getDataFolder(), "config.yml");
        }

        config = YamlConfiguration.loadConfiguration(cfile);
        try {
            InputStream defConfigStream = plugin.getResource("config.yml");
            if (defConfigStream != null) {
                YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
                config.setDefaults(defConfig);
            }
        }catch (NullPointerException ignored){

        }
    }

    public static void saveConfig() {
        try {
            config.save(cfile);
        } catch (IOException e) {
            System.out.print("Error saving the config.yml file");
        }
    }

    public static String configGetString(String location) {
        if (config.contains(location) || (config.getString(location) != null)) {
            return config.getString(location);
        }
        return "";
    }
}
