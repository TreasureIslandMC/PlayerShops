package me.jsbroks.playershops.core.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class Lang extends AConfig{

    public Lang(final JavaPlugin plugin) {
        super("lang.yml", plugin);
        saveDefaultConfiguration();
    }

    @Override
    public String getString(String path){
        return getConfig().getString(path);
    }

    public int getInt(String path){
        return getConfig().getInt(path);
    }

    public boolean getBoolean(String path){
        return getConfig().getBoolean(path);
    }

    public double getDouble(String path){
        return getConfig().getDouble(path);
    }

    public ConfigurationSection getConfigurationSection(String path){
        return getConfig().getConfigurationSection(path);
    }

    public List<String> getStringList(String path) {
        return getConfig().getStringList(path);
    }

    public void reload(){
        reloadConfig();
    }

    public boolean contains(String path){
        return getConfig().contains(path);
    }

    public void set(String path, Object value){
        getConfig().set(path,value);
    }

    public void save(){
        save();
    }

}
