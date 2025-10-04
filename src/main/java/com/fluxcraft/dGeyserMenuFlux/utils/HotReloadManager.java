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

            javaMenusPath.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            bedrockMenusPath.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "DGeyserMenuFlux-HotReload");
                t.setDaemon(true);
                return t;
            });

            executor.scheduleAtFixedRate(this::checkForChanges, 0, 2, TimeUnit.SECONDS);

        } catch (Exception e) {
            // 静默处理错误
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
                        // 使用 Folia 的全局调度器重载
                        plugin.runDelayedGlobalTask(() -> {
                            plugin.getConfigManager().reloadAllMenus();
                            plugin.getJavaMenuManager().reloadMenus();
                            plugin.getBedrockMenuManager().reloadMenus();
                        }, 20L);
                    }
                }
                key.reset();
            }
        } catch (Exception e) {
            // 静默处理错误
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
