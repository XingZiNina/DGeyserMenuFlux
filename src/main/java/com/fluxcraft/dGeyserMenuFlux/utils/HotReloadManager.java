package com.fluxcraft.dGeyserMenuFlux.utils;

import com.fluxcraft.dGeyserMenuFlux.DGeyserMenuFlux;
import java.nio.file.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HotReloadManager {
    private final DGeyserMenuFlux plugin;
    private WatchService watchService;
    private ScheduledExecutorService executor;

    public HotReloadManager(DGeyserMenuFlux plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        try {
            this.watchService = FileSystems.getDefault().newWatchService();
            Path javaMenusPath = Paths.get(plugin.getDataFolder().getAbsolutePath(), "java_menus");
            Path bedrockMenusPath = Paths.get(plugin.getDataFolder().getAbsolutePath(), "bedrock_menus");

            // 注册监听
            javaMenusPath.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            bedrockMenusPath.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            this.executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleAtFixedRate(this::checkForChanges, 0, 2, TimeUnit.SECONDS);

        } catch (Exception e) {
            plugin.getLogger().warning("无法初始化热重载: " + e.getMessage());
        }
    }

    private void checkForChanges() {
        try {
            WatchKey key = watchService.poll();
            if (key != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    Path changedFile = (Path) event.context();
                    String fileName = changedFile.toString();

                    if (fileName.endsWith(".yml")) {
                        plugin.getLogger().info("检测到菜单文件变化: " + fileName + ", 重新加载...");
                        // 延迟重载以避免文件写入中
                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                            plugin.getConfigManager().reloadAllMenus();
                            plugin.getJavaMenuManager().reloadMenus();
                            plugin.getBedrockMenuManager().reloadMenus();
                        }, 20L);
                    }
                }
                key.reset();
            }
        } catch (Exception e) {
            plugin.getLogger().warning("热重载检查错误: " + e.getMessage());
        }
    }

    public void shutdown() {
        if (executor != null) {
            executor.shutdown();
        }
        if (watchService != null) {
            try { watchService.close(); } catch (Exception e) { }
        }
    }
}