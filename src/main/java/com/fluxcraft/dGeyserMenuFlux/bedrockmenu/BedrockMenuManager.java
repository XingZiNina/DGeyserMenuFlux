package com.fluxcraft.dGeyserMenuFlux.bedrockmenu;

import com.fluxcraft.dGeyserMenuFlux.DGeyserMenuFlux;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class BedrockMenuManager {
    private final DGeyserMenuFlux plugin;
    private final Map<String, BedrockMenu> menus = new HashMap<>();

    public BedrockMenuManager(DGeyserMenuFlux plugin) {
        this.plugin = plugin;
    }

    /**
     * 加载所有基岩版菜单 - Folia 版本
     */
    public void loadAllMenus() {
        // 使用 Folia 的全局调度器异步加载
        plugin.runGlobalTask(() -> {
            menus.clear();

            File bedrockMenuDir = new File(plugin.getDataFolder(), "bedrock_menus");
            if (!bedrockMenuDir.exists()) {
                bedrockMenuDir.mkdirs();
            }

            File[] files = bedrockMenuDir.listFiles((dir, name) -> name.endsWith(".yml"));
            if (files == null || files.length == 0) {
                return;
            }

            for (File file : files) {
                String menuName = file.getName().replace(".yml", "");
                try {
                    FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                    BedrockMenu menu = new BedrockMenu(menuName, config);
                    menus.put(menuName, menu);
                } catch (Exception e) {
                    // 静默处理错误
                }
            }
        });
    }

    /**
     * 打开指定菜单给基岩版玩家 - Folia 版本
     */
    public CompletableFuture<Void> openMenu(Player player, String menuName) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        // 使用玩家调度器确保在正确的线程执行
        player.getScheduler().run(plugin, task -> {
            if (!isBedrockPlayer(player)) {
                player.sendMessage("§c此菜单仅适用于基岩版玩家");
                future.complete(null);
                return;
            }

            BedrockMenu menu = menus.get(menuName);
            if (menu == null) {
                player.sendMessage("§c菜单不存在: " + menuName);
                future.complete(null);
                return;
            }

            try {
                if (!canOpenMenu(player, menuName)) {
                    player.sendMessage("§c你没有权限打开这个菜单!");
                    future.complete(null);
                    return;
                }

                menu.open(player);
                future.complete(null);
            } catch (Exception e) {
                player.sendMessage("§c打开菜单时发生错误");
                future.completeExceptionally(e);
            }
        }, null);

        return future;
    }

    /**
     * 重新加载所有菜单 - Folia 版本
     */
    public void reloadMenus() {
        plugin.runGlobalTask(this::loadAllMenus);
    }

    /**
     * 重新加载指定菜单
     */
    public boolean reloadMenu(String menuName) {
        File menuFile = new File(plugin.getDataFolder(), "bedrock_menus/" + menuName + ".yml");
        if (!menuFile.exists()) {
            return false;
        }

        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(menuFile);
            BedrockMenu menu = new BedrockMenu(menuName, config);
            menus.put(menuName, menu);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查玩家是否是基岩版玩家
     */
    private boolean isBedrockPlayer(Player player) {
        try {
            return FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查玩家是否可以打开菜单
     */
    public boolean canOpenMenu(Player player, String menuName) {
        if (player.hasPermission("dgeysermenu.admin") || player.hasPermission("dgeysermenu.*")) {
            return true;
        }

        String menuPermission = "dgeysermenu.menu." + menuName;
        return player.hasPermission(menuPermission) || player.hasPermission("dgeysermenu.use");
    }

    // Getter 方法
    public int getLoadedMenuCount() { return menus.size(); }
    public Set<String> getMenuNames() { return menus.keySet(); }
    public boolean menuExists(String menuName) { return menus.containsKey(menuName); }
    public BedrockMenu getMenu(String menuName) { return menus.get(menuName); }

    public String getMenuInfo(String menuName) {
        BedrockMenu menu = menus.get(menuName);
        if (menu == null) {
            return "基岩菜单不存在: " + menuName;
        }
        return String.format("基岩菜单: %s, 菜单项数: %d", menuName, menu.getMenuItems().size());
    }

    public void cleanup() {
        menus.clear();
    }
}
