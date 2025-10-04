package com.fluxcraft.dGeyserMenuFlux;

import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.command.PluginCommand;
import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import com.fluxcraft.dGeyserMenuFlux.commands.CommandManager;
import com.fluxcraft.dGeyserMenuFlux.commands.MenuCommand;
import com.fluxcraft.dGeyserMenuFlux.commands.ReloadCommand;
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
    private MenuCommand menuCommand;
    private ReloadCommand reloadCommand;
    private FloodgateApi floodgateApi;
    private GlobalRegionScheduler globalScheduler;

    @Override
    public void onEnable() {
        instance = this;
        this.globalScheduler = getServer().getGlobalRegionScheduler();

        this.configManager = new ConfigManager(this);

        if (!initializeFloodgate()) {
            getLogger().severe("Floodgate未找到或初始化失败! 本插件需要Floodgate运行.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        initializeManagers();
        registerListeners();
        registerCommands();

        loadPlugin();
        initializeBStats();

        getLogger().info("§aDGeyserMenuFlux-Folia v" + getPluginMeta().getVersion() + " 已成功启用!");
    }

    @Override
    public void onDisable() {
        if (hotReloadManager != null) {
            hotReloadManager.shutdown();
        }
        getLogger().info("§cDGeyserMenuFlux-Folia 已禁用!");
    }

    private boolean initializeFloodgate() {
        try {
            this.floodgateApi = FloodgateApi.getInstance();
            if (floodgateApi == null) {
                getLogger().warning("Floodgate API 为 null");
                return false;
            }
            getLogger().info("§aFloodgate API 初始化成功");
            return true;
        } catch (Exception e) {
            getLogger().warning("Floodgate初始化失败: " + e.getMessage());
            return false;
        }
    }

    private void initializeManagers() {
        this.javaMenuManager = new JavaMenuManager(this);
        this.bedrockMenuManager = new BedrockMenuManager(this);
        this.hotReloadManager = new HotReloadManager(this);
        this.commandManager = new CommandManager(this);
        this.menuCommand = new MenuCommand(this);
        this.reloadCommand = new ReloadCommand(this);
    }

    private void registerListeners() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new JavaMenuListener(this), this);
        pm.registerEvents(new BedrockMenuListener(this), this);
    }

    private void registerCommands() {
        PluginCommand command = getCommand("dgeysermenu");
        if (command != null) {
            command.setExecutor(commandManager);
            command.setTabCompleter(commandManager);
            getLogger().info("§a已注册命令: dgeysermenu");
        } else {
            getLogger().warning("§c无法注册命令: dgeysermenu");
        }

        PluginCommand clockCommand = getCommand("getmenuclock");
        if (clockCommand != null) {
            clockCommand.setExecutor(commandManager);
            getLogger().info("§a已注册命令: getmenuclock");
        } else {
            getLogger().warning("§c无法注册命令: getmenuclock");
        }
    }

    private void loadPlugin() {
        globalScheduler.run(this, task -> {
            try {
                if (configManager.getConfig() == null) {
                    getLogger().warning("§c配置为空，重新加载配置");
                    configManager.loadConfig();
                }

                javaMenuManager.loadAllMenus();
                bedrockMenuManager.loadAllMenus();

                getLogger().info("§a插件配置加载完成!");
                getLogger().info("§aJava菜单: " + javaMenuManager.getLoadedMenuCount() + " 个");
                getLogger().info("§a基岩菜单: " + bedrockMenuManager.getLoadedMenuCount() + " 个");

                initializeHotReload();
                initializeClockSystem();

            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "§c加载插件时发生错误", e);
            }
        });
    }

    private void initializeHotReload() {
        try {
            if (configManager.isHotReloadEnabled()) {
                hotReloadManager.initialize();
                getLogger().info("§a热重载系统已启用 (检查间隔: " + configManager.getHotReloadInterval() + "秒)");
            } else {
                getLogger().info("§7热重载系统已禁用");
            }
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "§c初始化热重载系统失败", e);
        }
    }

    private void initializeClockSystem() {
        try {
            this.clockManager = new MenuClockManager(this);
            PluginManager pm = getServer().getPluginManager();
            pm.registerEvents(clockManager, this);
            pm.registerEvents(new ClockInteractionListener(this, clockManager), this);

            getLogger().info("§a智能钟表系统已启用");
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "§c初始化钟表系统失败", e);
        }
    }

    /**
     * 初始化 bStats 统计 - Folia 兼容版本
     */
    private void initializeBStats() {
        try {
            // 使用反射来避免重定位问题
            Class<?> metricsClass = Class.forName("org.bstats.bukkit.Metrics");
            Object metrics = metricsClass.getConstructor(JavaPlugin.class, int.class)
                    .newInstance(this, 23116);

            getLogger().info("§abStats 统计系统已初始化");

        } catch (ClassNotFoundException e) {
            getLogger().warning("§c未找到 bStats 类，请检查依赖配置");
        } catch (Exception e) {
            getLogger().warning("§c初始化 bStats 统计失败: " + e.getMessage());
        }
    }

    public void reloadPlugin() {
        globalScheduler.run(this, task -> {
            try {
                configManager.reloadConfig();
                configManager.reloadAllMenus();
                javaMenuManager.reloadMenus();
                bedrockMenuManager.reloadMenus();

                getLogger().info("§a插件重载完成!");
                getLogger().info("§aJava菜单: " + javaMenuManager.getLoadedMenuCount() + " 个");
                getLogger().info("§a基岩菜单: " + bedrockMenuManager.getLoadedMenuCount() + " 个");

            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "§c重载插件时发生错误", e);
            }
        });
    }

    public void runGlobalTask(Runnable task) {
        globalScheduler.run(this, scheduledTask -> task.run());
    }

    public void runDelayedGlobalTask(Runnable task, long delayTicks) {
        globalScheduler.runDelayed(this, scheduledTask -> task.run(), delayTicks);
    }

    public void runAtLocation(org.bukkit.Location location, Runnable task) {
        getServer().getRegionScheduler().run(this, location, scheduledTask -> task.run());
    }

    public void runDelayedAtLocation(org.bukkit.Location location, Runnable task, long delayTicks) {
        getServer().getRegionScheduler().runDelayed(this, location, scheduledTask -> task.run(), delayTicks);
    }

    public void runAtPlayer(org.bukkit.entity.Player player, Runnable task) {
        player.getScheduler().run(this, scheduledTask -> task.run(), null);
    }

    public void runDelayedAtPlayer(org.bukkit.entity.Player player, Runnable task, long delayTicks) {
        player.getScheduler().runDelayed(this, scheduledTask -> task.run(), null, delayTicks);
    }

    public void runAsyncTask(Runnable task) {
        getServer().getAsyncScheduler().runNow(this, scheduledTask -> task.run());
    }

    public void runDelayedAsyncTask(Runnable task, long delayTicks) {
        getServer().getAsyncScheduler().runDelayed(this, scheduledTask -> task.run(),
                delayTicks * 50, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    public static DGeyserMenuFlux getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public JavaMenuManager getJavaMenuManager() {
        return javaMenuManager;
    }

    public BedrockMenuManager getBedrockMenuManager() {
        return bedrockMenuManager;
    }

    public HotReloadManager getHotReloadManager() {
        return hotReloadManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public MenuClockManager getClockManager() {
        return clockManager;
    }

    public MenuCommand getMenuCommand() {
        return menuCommand;
    }

    public ReloadCommand getReloadCommand() {
        return reloadCommand;
    }

    public FloodgateApi getFloodgateApi() {
        return floodgateApi;
    }

    public GlobalRegionScheduler getGlobalScheduler() {
        return globalScheduler;
    }

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

    public String getDefaultMenu() {
        return configManager.getDefaultMenu();
    }

    public boolean isDebugEnabled() {
        return configManager.isDebugEnabled();
    }

    public void debug(String message) {
        if (isDebugEnabled()) {
            getLogger().info("§7[DEBUG] " + message);
        }
    }

    public java.util.Set<String> getAllMenuNames() {
        return configManager.getAllMenuNames();
    }

    public String getPluginStatistics() {
        return String.format(
                "DGeyserMenuFlux-Folia 统计: Java菜单=%d, 基岩菜单=%d, 热重载=%s, 调试=%s",
                javaMenuManager.getLoadedMenuCount(),
                bedrockMenuManager.getLoadedMenuCount(),
                configManager.isHotReloadEnabled() ? "启用" : "禁用",
                configManager.isDebugEnabled() ? "启用" : "禁用"
        );
    }

    public void runSafeTask(org.bukkit.entity.Player player, Runnable task) {
        if (player != null && player.isOnline()) {
            runAtPlayer(player, task);
        } else {
            runGlobalTask(task);
        }
    }

    public void runBatchPlayerTasks(java.util.List<org.bukkit.entity.Player> players, java.util.function.Consumer<org.bukkit.entity.Player> task) {
        for (org.bukkit.entity.Player player : players) {
            if (player.isOnline()) {
                runAtPlayer(player, () -> task.accept(player));
            }
        }
    }
}
