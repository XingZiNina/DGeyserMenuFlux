package com.fluxcraft.dGeyserMenuFlux.config;

import com.fluxcraft.dGeyserMenuFlux.DGeyserMenuFlux;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

public class ConfigManager {
    private final DGeyserMenuFlux plugin;
    private FileConfiguration config;

    public ConfigManager(DGeyserMenuFlux plugin) {
        this.plugin = plugin;
        // 立即加载配置，避免 null
        loadConfig();
    }

    public void loadConfig() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }

            File configFile = new File(plugin.getDataFolder(), "config.yml");
            if (!configFile.exists()) {
                plugin.saveResource("config.yml", false);
                plugin.getLogger().info("§a已创建默认配置文件");
            }

            this.config = YamlConfiguration.loadConfiguration(configFile);
            createDefaultConfig();

            plugin.getLogger().info("§a配置文件加载成功");

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "§c加载配置文件失败", e);
            // 创建默认配置作为后备
            createFallbackConfig();
        }
    }

    private void createDefaultConfig() {
        if (config == null) {
            createFallbackConfig();
            return;
        }

        if (!config.contains("settings")) {
            config.set("settings.debug", false);
            config.set("settings.default-menu", "main");
            config.set("settings.hot-reload.enabled", true);
            config.set("settings.hot-reload.check-interval", 2);
            saveConfig();
        }

        createExampleMenus();
    }

    private void createFallbackConfig() {
        plugin.getLogger().warning("§e使用后备配置");
        // 创建内存中的默认配置
        this.config = new YamlConfiguration();
        config.set("settings.debug", false);
        config.set("settings.default-menu", "main");
        config.set("settings.hot-reload.enabled", false);
        config.set("settings.hot-reload.check-interval", 2);

        // 确保目录存在
        createExampleMenus();
    }

    private void createExampleMenus() {
        try {
            createJavaExampleMenu();
            createBedrockExampleMenu();
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "创建示例菜单失败", e);
        }
    }

    private void createJavaExampleMenu() {
        File javaMenuDir = new File(plugin.getDataFolder(), "java_menus");
        if (!javaMenuDir.exists()) {
            javaMenuDir.mkdirs();
        }

        File exampleFile = new File(javaMenuDir, "main.yml");
        if (!exampleFile.exists()) {
            try {
                FileConfiguration exampleConfig = new YamlConfiguration();
                exampleConfig.set("menu_title", "&6&l主菜单 &7| &f服务器名称");
                exampleConfig.set("rows", 6);

                exampleConfig.set("items.server_info.slot", 10);
                exampleConfig.set("items.server_info.material", "KNOWLEDGE_BOOK");
                exampleConfig.set("items.server_info.display_name", "&e&l服务器信息");
                exampleConfig.set("items.server_info.lore", java.util.Arrays.asList(
                        "&7点击查看服务器信息",
                        "",
                        "&f在线玩家: &a%server_online%&f/&a%server_max_players%",
                        "&fTPS: &a%server_tps%",
                        "",
                        "&e点击查看详细信息"
                ));
                exampleConfig.set("items.server_info.left_click_commands", java.util.Arrays.asList(
                        "[message] &6=== 服务器信息 ===",
                        "[message] &f在线玩家: &a%server_online%&f/&a%server_max_players%",
                        "[message] &f服务器版本: &a%server_version%"
                ));

                exampleConfig.save(exampleFile);
                plugin.getLogger().info("§a已创建Java示例菜单");
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "创建Java示例菜单失败", e);
            }
        }
    }

    private void createBedrockExampleMenu() {
        File bedrockMenuDir = new File(plugin.getDataFolder(), "bedrock_menus");
        if (!bedrockMenuDir.exists()) {
            bedrockMenuDir.mkdirs();
        }

        File exampleFile = new File(bedrockMenuDir, "main.yml");
        if (!exampleFile.exists()) {
            try {
                FileConfiguration exampleConfig = new YamlConfiguration();
                exampleConfig.set("menu.title", "§6§l主菜单");
                exampleConfig.set("menu.subtitle", "§7欢迎来到服务器!");
                exampleConfig.set("menu.footer", "§8服务器版本 1.21.x");

                java.util.List<java.util.Map<String, Object>> items = new java.util.ArrayList<>();

                java.util.Map<String, Object> item1 = new java.util.HashMap<>();
                item1.put("text", "§e§l服务器信息\n§7点击查看服务器状态");
                item1.put("icon", "textures/items/book_enchanted");
                item1.put("icon_type", "path");
                item1.put("command", "dgeysermenu open info");
                item1.put("execute_as", "player");
                items.add(item1);

                exampleConfig.set("menu.items", items);
                exampleConfig.save(exampleFile);
                plugin.getLogger().info("§a已创建基岩版示例菜单");
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "创建基岩示例菜单失败", e);
            }
        }
    }

    public void saveConfig() {
        if (config == null) {
            plugin.getLogger().warning("§c配置为null，无法保存");
            return;
        }

        try {
            config.save(new File(plugin.getDataFolder(), "config.yml"));
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "保存配置文件失败", e);
        }
    }

    public FileConfiguration getConfig() {
        if (config == null) {
            plugin.getLogger().warning("§c配置为null，返回空配置");
            return new YamlConfiguration();
        }
        return config;
    }

    public boolean isHotReloadEnabled() {
        if (config == null) {
            plugin.getLogger().warning("§c配置为null，热重载已禁用");
            return false;
        }
        return config.getBoolean("settings.hot-reload.enabled", false);
    }

    public int getHotReloadInterval() {
        if (config == null) {
            return 2;
        }
        return config.getInt("settings.hot-reload.check-interval", 2);
    }

    public boolean isDebugEnabled() {
        if (config == null) {
            return false;
        }
        return config.getBoolean("settings.debug", false);
    }

    public String getDefaultMenu() {
        if (config == null) {
            return "main";
        }
        return config.getString("settings.default-menu", "main");
    }

    public void reloadConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (configFile.exists()) {
            this.config = YamlConfiguration.loadConfiguration(configFile);
        } else {
            plugin.getLogger().warning("§c配置文件不存在，使用默认配置");
            createFallbackConfig();
        }
    }

    public Set<String> getAllMenuNames() {
        Set<String> menuNames = new HashSet<>();

        // Java 版菜单
        File javaMenuDir = new File(plugin.getDataFolder(), "java_menus");
        if (javaMenuDir.exists()) {
            File[] javaFiles = javaMenuDir.listFiles((dir, name) -> name.endsWith(".yml"));
            if (javaFiles != null) {
                for (File file : javaFiles) {
                    menuNames.add(file.getName().replace(".yml", ""));
                }
            }
        }

        // 基岩版菜单
        File bedrockMenuDir = new File(plugin.getDataFolder(), "bedrock_menus");
        if (bedrockMenuDir.exists()) {
            File[] bedrockFiles = bedrockMenuDir.listFiles((dir, name) -> name.endsWith(".yml"));
            if (bedrockFiles != null) {
                for (File file : bedrockFiles) {
                    menuNames.add(file.getName().replace(".yml", ""));
                }
            }
        }

        return menuNames;
    }

    public void reloadAllMenus() {
        // 静默重载，不输出日志
    }
}
