package me.jsbroks.playershops.core.data;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import me.jsbroks.playershops.util.InventoryUtil;
import me.jsbroks.playershops.util.ItemUtil;
import me.jsbroks.playershops.util.MapUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static me.jsbroks.playershops.PlayerShops.plugin;

class FileManager {

    String prefix = "[PlayerShops-File] ";

    public FileManager() {
        System.out.println(prefix + "Loading files...");
    }


    public void createPlayer(UUID uuid) {
        ShopFile shopFile = new ShopFile(uuid);
        shopFile.createPlayer();
    }

    public void createPlayer(String uuid, String name, String lastseen, String inventory) {
        ShopFile shopFile = new ShopFile(uuid, name, lastseen, inventory);
        shopFile.createPlayer();
    }

    public String getInventory(UUID uuid) {
        ShopFile shopFile = new ShopFile(uuid);
        return shopFile.getInventory();
    }

    public void setInventory(UUID uuid, Inventory inv) {
        ShopFile shopFile = new ShopFile(uuid);
        shopFile.setInventory(inv);
    }

    public void deletePlayer(OfflinePlayer uuid) {
        ShopFile shopFile = new ShopFile(uuid.getUniqueId());
        shopFile.delete();
    }

    public Multimap<Double, OfflinePlayer> search(Material material) {
        Multimap<Double, OfflinePlayer> results = ArrayListMultimap.create();

        File dir = new File(plugin.getDataFolder() + "/data/");
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".yml");
            }
        });

        try {

            for (File file : files) {
                YamlConfiguration yamlConfig = YamlConfiguration.loadConfiguration(file);
                String data = yamlConfig.getString("Inventory");
                if (!data.equalsIgnoreCase("Not Set")) {
                    Inventory inv = InventoryUtil.fromBase64("Search", data);
                    if (inv.contains(material)) {
                        for (int i = 0; i < inv.getSize(); i++) {
                            ItemStack item = inv.getItem(i);

                            if (item != null) {
                                if(item.getType() == material) {
                                    results.put(ItemUtil.getItemPriceEach(item), Bukkit.getOfflinePlayer(UUID.fromString(yamlConfig.getString("Player.UUID"))));
                                }
                            }
                        }
                    }
                }
            }

            return results;
        } catch (Exception ignore) {
        }

        return null;
    }

    public boolean playerExists(UUID uuid) {
        ShopFile shopFile = new ShopFile(uuid);
        if (shopFile == null) {
            return false;
        } else {
            return shopFile.exists();
        }
    }

    public int clean(int days) {
        File dir = new File(plugin.getDataFolder() + "/data/");

        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".yml");
            }
        });

        Date date = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");

        try {
            int counter = 0;
            for (File file : files) {
                YamlConfiguration yamlConfig = YamlConfiguration.loadConfiguration(file);
                Date date2 = ft.parse(yamlConfig.getString("Player.LastSeen"));

                Map<TimeUnit, Long> timeUnitDiff = MapUtil.computeDiff(date2, date);
                if (timeUnitDiff.get(TimeUnit.DAYS) >= days) {
                    UUID uuid = UUID.fromString(yamlConfig.getString("Player.UUID"));
                    ShopFile shopFile = new ShopFile(uuid);
                    shopFile.delete();
                    counter++;
                }
            }

            return counter;
        } catch (Exception ignore) {
        }

        return 0;
    }
}
