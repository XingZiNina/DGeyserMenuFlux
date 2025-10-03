package com.fluxcraft.dGeyserMenuFlux.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import com.fluxcraft.dGeyserMenuFlux.DGeyserMenuFlux;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class MenuConfig {
    private final DGeyserMenuFlux plugin;
    private final String menuName;
    private final boolean isJavaMenu;
    private FileConfiguration config;
    private File configFile;

    public MenuConfig(DGeyserMenuFlux plugin, String menuName, boolean isJavaMenu) {
        this.plugin = plugin;
        this.menuName = menuName;
        this.isJavaMenu = isJavaMenu;
        load();
    }

    public void load() {
        String folder = isJavaMenu ? "java_menus" : "bedrock_menus";
        this.configFile = new File(plugin.getDataFolder(), folder + "/" + menuName + ".yml");

        if (!configFile.exists()) {
            // 尝试从resources中复制默认配置
            plugin.saveResource(folder + "/" + menuName + ".yml", false);
        }

        this.config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void save() {
        if (config != null && configFile != null) {
            try {
                config.save(configFile);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "无法保存菜单配置: " + menuName, e);
            }
        }
    }

    public void reload() {
        if (configFile != null) {
            this.config = YamlConfiguration.loadConfiguration(configFile);
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public String getMenuName() {
        return menuName;
    }

    public boolean isJavaMenu() {
        return isJavaMenu;
    }

    public boolean exists() {
        return configFile != null && configFile.exists();
    }

    // 便捷方法获取配置值
    public String getTitle() {
        return config.getString("title", "菜单");
    }

    public int getRows() {
        return config.getInt("rows", 3);
    }

    public Map<String, Object> getItems() {
        Map<String, Object> items = new HashMap<>();
        if (config.contains("items")) {
            items.putAll(config.getConfigurationSection("items").getValues(true));
        }
        return items;
    }
}