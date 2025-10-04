package com.fluxcraft.dGeyserMenuFlux;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.command.PluginCommand;
import java.io.File;
import java.util.logging.Level;

import com.fluxcraft.dGeyserMenuFlux.commands.CommandManager;
import com.fluxcraft.dGeyserMenuFlux.config.ConfigManager;
import com.fluxcraft.dGeyserMenuFlux.javamenu.JavaMenuManager;
import com.fluxcraft.dGeyserMenuFlux.bedrockmenu.BedrockMenuManager;
import com.fluxcraft.dGeyserMenuFlux.utils.HotReloadManager;
import com.fluxcraft.dGeyserMenuFlux.javamenu.listeners.JavaMenuListener;
import com.fluxcraft.dGeyserMenuFlux.bedrockmenu.listeners.BedrockMenuListener;
import com.fluxcraft.dGeyserMenuFlux.utils.MenuClockManager;
import com.fluxcraft.dGeyserMenuFlux.listeners.ClockInteractionListener;

import org.geysermc.floodgate.api.FloodgateApi;

public final class DGeyserMenuFlux extends JavaPlugin {

    private static DGeyserMenuFlux instance;
    private ConfigManager configManager;
    private JavaMenuManager javaMenuManager;
    private BedrockMenuManager bedrockMenuManager;
    private HotReloadManager hotReloadManager;
    private CommandManager commandManager;
    private MenuClockManager clockManager;
    private FloodgateApi floodgateApi;

    @Override
    public void onEnable() {
        instance = this;

        if (!initializeFloodgate()) {
            getLogger().severe("Floodgate未找到或初始化失败! 本插件需要Floodgate运行.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        initializeManagers();
        registerListeners();
        registerCommands();
        loadPlugin();
        initializeHotReload();
        initializeClockSystem();
        initializeBStats();

        String version = getPluginMeta().getVersion();
        getLogger().info("DGeyserMenuFlux v" + version + " 已成功启用!");
        getLogger().info("Java版菜单: " + javaMenuManager.getLoadedMenuCount() + " 个");
        getLogger().info("基岩版菜单: " + bedrockMenuManager.getLoadedMenuCount() + " 个");
    }

    @Override
    public void onDisable() {
        if (hotReloadManager != null) {
            hotReloadManager.shutdown();
        }
        getLogger().info("DGeyserMenuFlux 已禁用!");
    }

    private boolean initializeFloodgate() {
        try {
            this.floodgateApi = FloodgateApi.getInstance();
            if (floodgateApi != null) {
                getLogger().info("成功连接到Floodgate API");
                return true;
            }
        } catch (Exception e) {
            getLogger().warning("无法获取Floodgate API实例: " + e.getMessage());
        }
        return false;
    }

    private void initializeManagers() {
        this.configManager = new ConfigManager(this);
        this.javaMenuManager = new JavaMenuManager(this);
        this.bedrockMenuManager = new BedrockMenuManager(this);
        this.hotReloadManager = new HotReloadManager(this);
        this.commandManager = new CommandManager(this);
        getLogger().info("所有管理器初始化完成");
    }

    private void registerListeners() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new JavaMenuListener(this), this);
        pm.registerEvents(new BedrockMenuListener(this), this);
        getLogger().info("事件监听器注册完成");
    }

    private void registerCommands() {
        PluginCommand command = getCommand("dgeysermenu");
        if (command != null) {
            command.setExecutor(commandManager);
            command.setTabCompleter(commandManager);
            getLogger().info("命令注册完成: /dgeysermenu");
        } else {
            getLogger().warning("无法注册命令，请在plugin.yml中检查命令配置");
        }

        PluginCommand clockCommand = getCommand("getmenuclock");
        if (clockCommand != null) {
            clockCommand.setExecutor(commandManager);
            getLogger().info("命令注册完成: /getmenuclock");
        }
    }

    private void loadPlugin() {
        try {
            configManager.loadConfig();
            javaMenuManager.loadAllMenus();
            bedrockMenuManager.loadAllMenus();
            getLogger().info("配置和菜单加载完成");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "加载插件时发生错误", e);
        }
    }

    private void initializeHotReload() {
        if (configManager.isHotReloadEnabled()) {
            hotReloadManager.initialize();
            getLogger().info("热重载系统已启用");
        } else {
            getLogger().info("热重载系统已禁用");
        }
    }

    private void initializeClockSystem() {
        this.clockManager = new MenuClockManager(this);
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(clockManager, this);
        pm.registerEvents(new ClockInteractionListener(this, clockManager), this);
        getLogger().info("菜单钟表系统已初始化");
    }

    /**
     * 简单有效的 bStats 初始化方法
     */
    private void initializeBStats() {
        try {
            // 直接使用重定位后的类名
            Class<?> metricsClass = Class.forName("com.fluxcraft.dGeyserMenuFlux.libs.bstats.bukkit.Metrics");
            Object metrics = metricsClass.getConstructor(JavaPlugin.class, int.class)
                    .newInstance(this, 27455);
            getLogger().info("bStats 统计系统已初始化");
        } catch (Exception e) {
            // 如果失败，简单记录警告，不影响插件运行
            getLogger().warning("bStats 初始化失败，统计功能不可用");
        }
    }

    public void reloadPlugin() {
        getLogger().info("开始重新加载插件...");
        try {
            configManager.reloadAllMenus();
            javaMenuManager.reloadMenus();
            bedrockMenuManager.reloadMenus();
            getLogger().info("插件重载完成!");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "重载插件时发生错误", e);
        }
    }

    public static DGeyserMenuFlux getInstance() { return instance; }
    public ConfigManager getConfigManager() { return configManager; }
    public JavaMenuManager getJavaMenuManager() { return javaMenuManager; }
    public BedrockMenuManager getBedrockMenuManager() { return bedrockMenuManager; }
    public HotReloadManager getHotReloadManager() { return hotReloadManager; }
    public CommandManager getCommandManager() { return commandManager; }
    public MenuClockManager getClockManager() { return clockManager; }
    public FloodgateApi getFloodgateApi() { return floodgateApi; }

    public boolean isBedrockPlayer(java.util.UUID playerUUID) {
        try {
            return floodgateApi != null && floodgateApi.isFloodgatePlayer(playerUUID);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isPlaceholderAPIEnabled() {
        return getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    public File getFile(String path) {
        return new File(getDataFolder(), path);
    }

    public void runTask(Runnable task) {
        getServer().getScheduler().runTask(this, task);
    }

    public void runTaskAsync(Runnable task) {
        getServer().getScheduler().runTaskAsynchronously(this, task);
    }
}
