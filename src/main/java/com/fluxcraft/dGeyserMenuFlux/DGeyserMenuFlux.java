package com.fluxcraft.dGeyserMenuFlux;

// Java & Bukkit/Paper 相关导入
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.command.PluginCommand;
import java.io.File;
import java.util.logging.Level;

// 插件内部管理器导入
import com.fluxcraft.dGeyserMenuFlux.commands.CommandManager;
import com.fluxcraft.dGeyserMenuFlux.config.ConfigManager;
import com.fluxcraft.dGeyserMenuFlux.javamenu.JavaMenuManager;
import com.fluxcraft.dGeyserMenuFlux.bedrockmenu.BedrockMenuManager;
import com.fluxcraft.dGeyserMenuFlux.utils.HotReloadManager;
import com.fluxcraft.dGeyserMenuFlux.javamenu.listeners.JavaMenuListener;
import com.fluxcraft.dGeyserMenuFlux.bedrockmenu.listeners.BedrockMenuListener;
import com.fluxcraft.dGeyserMenuFlux.utils.MenuClockManager;
import com.fluxcraft.dGeyserMenuFlux.listeners.ClockInteractionListener;

// Floodgate API 导入
import org.geysermc.floodgate.api.FloodgateApi;

/**
 * DGeyserMenuFlux 主类
 * 一个支持Java版(箱子菜单)和基岩版(表单)的轻量级菜单插件
 */
public final class DGeyserMenuFlux extends JavaPlugin {

    // 静态实例，便于其他类访问插件主实例
    private static DGeyserMenuFlux instance;

    // 插件管理器
    private ConfigManager configManager;
    private JavaMenuManager javaMenuManager;
    private BedrockMenuManager bedrockMenuManager;
    private HotReloadManager hotReloadManager;
    private CommandManager commandManager;
    private MenuClockManager clockManager;

    // Floodgate API 实例
    private FloodgateApi floodgateApi;

    /**
     * 插件启用时调用
     */
    @Override
    public void onEnable() {
        // 设置实例
        instance = this;

        // 检查并获取Floodgate API
        if (!initializeFloodgate()) {
            getLogger().severe("Floodgate未找到或初始化失败! 本插件需要Floodgate运行.");
            getLogger().severe("请确保已安装Floodgate并重启服务器.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // 初始化所有管理器
        initializeManagers();

        // 注册事件监听器
        registerListeners();

        // 注册命令
        registerCommands();

        // 加载配置和菜单
        loadPlugin();

        // 初始化热重载
        initializeHotReload();

        // 初始化钟表系统
        initializeClockSystem();

        getLogger().info("§aDGeyserMenuFlux v" + getDescription().getVersion() + " 已成功启用!");
        getLogger().info("§7Java版菜单: " + javaMenuManager.getLoadedMenuCount() + " 个");
        getLogger().info("§7基岩版菜单: " + bedrockMenuManager.getLoadedMenuCount() + " 个");
    }

    /**
     * 插件禁用时调用
     */
    @Override
    public void onDisable() {
        // 关闭热重载
        if (hotReloadManager != null) {
            hotReloadManager.shutdown();
        }

        getLogger().info("§cDGeyserMenuFlux 已禁用!");
    }

    /**
     * 初始化Floodgate
     * @return 初始化是否成功
     */
    private boolean initializeFloodgate() {
        try {
            this.floodgateApi = FloodgateApi.getInstance();
            if (floodgateApi != null) {
                getLogger().info("§a成功连接到Floodgate API");
                return true;
            }
        } catch (Exception e) {
            getLogger().warning("无法获取Floodgate API实例: " + e.getMessage());
        }
        return false;
    }

    /**
     * 初始化所有管理器
     */
    private void initializeManagers() {
        this.configManager = new ConfigManager(this);
        this.javaMenuManager = new JavaMenuManager(this);
        this.bedrockMenuManager = new BedrockMenuManager(this);
        this.hotReloadManager = new HotReloadManager(this);
        this.commandManager = new CommandManager(this);

        getLogger().info("§7所有管理器初始化完成");
    }

    /**
     * 注册事件监听器
     */
    private void registerListeners() {
        PluginManager pm = getServer().getPluginManager();

        // 注册Java版菜单监听器
        pm.registerEvents(new JavaMenuListener(this), this);

        // 注册基岩版菜单监听器
        pm.registerEvents(new BedrockMenuListener(this), this);

        getLogger().info("§7事件监听器注册完成");
    }

    /**
     * 注册命令
     */
    private void registerCommands() {
        PluginCommand command = getCommand("dgeysermenu");
        if (command != null) {
            command.setExecutor(commandManager);
            command.setTabCompleter(commandManager);
            getLogger().info("§7命令注册完成: /dgeysermenu");
        } else {
            getLogger().warning("§c无法注册命令，请在plugin.yml中检查命令配置");
        }

        // 注册获取钟表命令
        PluginCommand clockCommand = getCommand("getmenuclock");
        if (clockCommand != null) {
            clockCommand.setExecutor(commandManager);
            getLogger().info("§7命令注册完成: /getmenuclock");
        }
    }

    /**
     * 加载插件配置和菜单
     */
    private void loadPlugin() {
        try {
            // 加载主配置
            configManager.loadConfig();

            // 加载Java版菜单
            javaMenuManager.loadAllMenus();

            // 加载基岩版菜单
            bedrockMenuManager.loadAllMenus();

            getLogger().info("§7配置和菜单加载完成");

        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "§c加载插件时发生错误", e);
        }
    }

    /**
     * 初始化热重载系统
     */
    private void initializeHotReload() {
        if (configManager.isHotReloadEnabled()) {
            hotReloadManager.initialize();
            getLogger().info("§7热重载系统已启用");
        } else {
            getLogger().info("§7热重载系统已禁用");
        }
    }

    /**
     * 初始化钟表系统
     */
    private void initializeClockSystem() {
        this.clockManager = new MenuClockManager(this);

        // 注册钟表交互监听器
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(clockManager, this);
        pm.registerEvents(new ClockInteractionListener(this, clockManager), this);

        getLogger().info("§7菜单钟表系统已初始化");
    }

    /**
     * 重新加载整个插件
     */
    public void reloadPlugin() {
        getLogger().info("§7开始重新加载插件...");

        try {
            // 重载配置
            configManager.reloadAllMenus();

            // 重载菜单
            javaMenuManager.reloadMenus();
            bedrockMenuManager.reloadMenus();

            getLogger().info("§a插件重载完成!");

        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "§c重载插件时发生错误", e);
        }
    }

    // ==================== Getter 方法 ====================

    /**
     * 获取插件实例
     * @return 插件实例
     */
    public static DGeyserMenuFlux getInstance() {
        return instance;
    }

    /**
     * 获取配置管理器
     * @return ConfigManager 实例
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * 获取Java版菜单管理器
     * @return JavaMenuManager 实例
     */
    public JavaMenuManager getJavaMenuManager() {
        return javaMenuManager;
    }

    /**
     * 获取基岩版菜单管理器
     * @return BedrockMenuManager 实例
     */
    public BedrockMenuManager getBedrockMenuManager() {
        return bedrockMenuManager;
    }

    /**
     * 获取热重载管理器
     * @return HotReloadManager 实例
     */
    public HotReloadManager getHotReloadManager() {
        return hotReloadManager;
    }

    /**
     * 获取命令管理器
     * @return CommandManager 实例
     */
    public CommandManager getCommandManager() {
        return commandManager;
    }

    /**
     * 获取钟表管理器
     * @return MenuClockManager 实例
     */
    public MenuClockManager getClockManager() {
        return clockManager;
    }

    /**
     * 获取Floodgate API实例
     * @return FloodgateApi 实例
     */
    public FloodgateApi getFloodgateApi() {
        return floodgateApi;
    }

    /**
     * 检查玩家是否是基岩版玩家
     * @param playerUUID 玩家UUID
     * @return 如果是基岩版玩家返回true
     */
    public boolean isBedrockPlayer(java.util.UUID playerUUID) {
        try {
            return floodgateApi != null && floodgateApi.isFloodgatePlayer(playerUUID);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查PlaceholderAPI是否可用
     * @return 如果可用返回true
     */
    public boolean isPlaceholderAPIEnabled() {
        return getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    /**
     * 获取数据文件夹中的文件路径
     * @param path 相对路径
     * @return 完整文件路径
     */
    public File getFile(String path) {
        return new File(getDataFolder(), path);
    }

    /**
     * 在主线程运行任务
     * @param task 要运行的任务
     */
    public void runTask(Runnable task) {
        getServer().getScheduler().runTask(this, task);
    }

    /**
     * 异步运行任务
     * @param task 要运行的任务
     */
    public void runTaskAsync(Runnable task) {
        getServer().getScheduler().runTaskAsynchronously(this, task);
    }
}