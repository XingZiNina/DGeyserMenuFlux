package com.fluxcraft.dGeyserMenuFlux.config;

import com.fluxcraft.dGeyserMenuFlux.DGeyserMenuFlux;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ConfigManager {
    private final DGeyserMenuFlux plugin;
    private FileConfiguration config;
    private final Map<String, FileConfiguration> javaMenus = new HashMap<>();
    private final Map<String, FileConfiguration> bedrockMenus = new HashMap<>();

    public ConfigManager(DGeyserMenuFlux plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        // 创建插件数据文件夹
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        // 加载主配置
        plugin.saveDefaultConfig();
        if (config == null) {
            plugin.reloadConfig();
        }
        this.config = plugin.getConfig();

        // 创建菜单目录并检查是否需要示例
        createMenuDirectories();
        loadAllMenuFiles();
    }

    private void createMenuDirectories() {
        File javaMenuDir = new File(plugin.getDataFolder(), "java_menus");
        File bedrockMenuDir = new File(plugin.getDataFolder(), "bedrock_menus");

        boolean javaDirCreated = false;
        boolean bedrockDirCreated = false;

        if (!javaMenuDir.exists()) {
            javaMenuDir.mkdirs();
            javaDirCreated = true;
        }

        if (!bedrockMenuDir.exists()) {
            bedrockMenuDir.mkdirs();
            bedrockDirCreated = true;
        }

        // 只在目录为空时提供示例
        provideExampleMenus(javaMenuDir, bedrockMenuDir, javaDirCreated, bedrockDirCreated);
    }

    private void provideExampleMenus(File javaMenuDir, File bedrockMenuDir, boolean javaDirCreated, boolean bedrockDirCreated) {
        // 检查Java菜单目录是否为空
        File[] javaFiles = javaMenuDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (javaFiles == null || javaFiles.length == 0) {
            // Java菜单目录为空，提供示例
            saveExampleMenu("java_menus/example.yml", new File(javaMenuDir, "example.yml"));
            plugin.getLogger().info("Java菜单目录为空，已创建示例菜单");
        }

        // 检查基岩菜单目录是否为空
        File[] bedrockFiles = bedrockMenuDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (bedrockFiles == null || bedrockFiles.length == 0) {
            // 基岩菜单目录为空，提供示例
            saveExampleMenu("bedrock_menus/main.yml", new File(bedrockMenuDir, "main.yml"));
            plugin.getLogger().info("基岩菜单目录为空，已创建示例菜单");
        }
    }

    private void saveExampleMenu(String resourcePath, File targetFile) {
        try {
            if (!targetFile.exists()) {
                InputStream inputStream = plugin.getResource(resourcePath);
                if (inputStream != null) {
                    Files.copy(inputStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    plugin.getLogger().info("创建示例菜单: " + targetFile.getName());
                } else {
                    plugin.getLogger().warning("找不到资源文件: " + resourcePath);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "创建示例菜单失败: " + targetFile.getName(), e);
        }
    }

    private void loadAllMenuFiles() {
        javaMenus.clear();
        bedrockMenus.clear();

        loadMenuFiles(new File(plugin.getDataFolder(), "java_menus"), javaMenus, "Java");
        loadMenuFiles(new File(plugin.getDataFolder(), "bedrock_menus"), bedrockMenus, "基岩");

        plugin.getLogger().info("已加载 " + javaMenus.size() + " 个Java菜单和 " + bedrockMenus.size() + " 个基岩菜单");
    }

    private void loadMenuFiles(File menuDir, Map<String, FileConfiguration> menuMap, String menuType) {
        if (!menuDir.exists()) return;

        File[] files = menuDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            String menuName = file.getName().replace(".yml", "");
            try {
                FileConfiguration menuConfig = YamlConfiguration.loadConfiguration(file);
                menuMap.put(menuName, menuConfig);
                // 减少输出：只在调试模式下输出详细信息
                if (plugin.getConfig().getBoolean("settings.debug", false)) {
                    plugin.getLogger().info("成功加载" + menuType + "菜单: " + menuName);
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "加载" + menuType + "菜单失败: " + menuName, e);
            }
        }
    }

    public FileConfiguration getJavaMenu(String menuName) {
        return javaMenus.get(menuName);
    }

    public FileConfiguration getBedrockMenu(String menuName) {
        return bedrockMenus.get(menuName);
    }

    public void reloadAllMenus() {
        loadAllMenuFiles();
    }

    public boolean isHotReloadEnabled() {
        return config.getBoolean("settings.hot-reload.enabled", true);
    }

    // 获取所有可用的菜单名称
    public java.util.Set<String> getJavaMenuNames() {
        return javaMenus.keySet();
    }

    public java.util.Set<String> getBedrockMenuNames() {
        return bedrockMenus.keySet();
    }

    public java.util.Set<String> getAllMenuNames() {
        java.util.Set<String> allMenus = new java.util.HashSet<>();
        allMenus.addAll(javaMenus.keySet());
        allMenus.addAll(bedrockMenus.keySet());
        return allMenus;
    }
}