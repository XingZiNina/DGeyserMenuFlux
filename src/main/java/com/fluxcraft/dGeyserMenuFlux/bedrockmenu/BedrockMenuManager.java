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
import java.util.logging.Level;

public class BedrockMenuManager {
    private final DGeyserMenuFlux plugin;
    private final Map<String, BedrockMenu> menus = new HashMap<>();

    public BedrockMenuManager(DGeyserMenuFlux plugin) {
        this.plugin = plugin;
    }

    /**
     * 加载所有基岩版菜单
     */
    public void loadAllMenus() {
        menus.clear();

        File bedrockMenuDir = new File(plugin.getDataFolder(), "bedrock_menus");
        if (!bedrockMenuDir.exists()) {
            bedrockMenuDir.mkdirs();
            // 静默创建目录
        }

        File[] files = bedrockMenuDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) {
            return; // 静默返回
        }

        int loadedCount = 0;
        int totalItems = 0;

        for (File file : files) {
            String menuName = file.getName().replace(".yml", "");
            try {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                BedrockMenu menu = new BedrockMenu(menuName, config);
                menus.put(menuName, menu);
                loadedCount++;
                totalItems += menu.getMenuItems().size();

            } catch (Exception e) {
                // 静默处理错误
            }
        }

        // 只在调试模式下输出统计信息
        if (plugin.getConfig().getBoolean("settings.debug", false)) {
            plugin.getLogger().info("基岩菜单加载完成: " + loadedCount + "/" + files.length +
                    " 个菜单, 总计 " + totalItems + " 个菜单项");
        }
    }

    /**
     * 打开指定菜单给基岩版玩家
     */
    public void openMenu(Player player, String menuName) {
        // 检查是否是基岩版玩家
        if (!isBedrockPlayer(player)) {
            player.sendMessage("§c此菜单仅适用于基岩版玩家");
            return;
        }

        BedrockMenu menu = menus.get(menuName);
        if (menu == null) {
            player.sendMessage("§c菜单不存在: " + menuName);
            return;
        }

        try {
            // 检查权限
            if (!canOpenMenu(player, menuName)) {
                player.sendMessage("§c你没有权限打开这个菜单!");
                return;
            }

            menu.open(player);
            // 移除日志输出
            // plugin.getLogger().info("为基岩玩家 " + player.getName() + " 打开菜单: " + menuName);

        } catch (Exception e) {
            player.sendMessage("§c打开菜单时发生错误");
        }
    }

    /**
     * 重新加载所有菜单
     */
    public void reloadMenus() {
        // 静默重载
        loadAllMenus();
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
     * 获取已加载的菜单数量
     */
    public int getLoadedMenuCount() {
        return menus.size();
    }

    /**
     * 获取所有菜单名称
     */
    public Set<String> getMenuNames() {
        return menus.keySet();
    }

    /**
     * 检查菜单是否存在
     */
    public boolean menuExists(String menuName) {
        return menus.containsKey(menuName);
    }

    /**
     * 获取指定菜单
     */
    public BedrockMenu getMenu(String menuName) {
        return menus.get(menuName);
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
        // 管理员拥有所有权限
        if (player.hasPermission("dgeysermenu.admin") || player.hasPermission("dgeysermenu.*")) {
            return true;
        }

        // 检查特定菜单权限
        String menuPermission = "dgeysermenu.menu." + menuName;
        return player.hasPermission(menuPermission) || player.hasPermission("dgeysermenu.use");
    }

    /**
     * 获取菜单信息（用于调试）
     */
    public String getMenuInfo(String menuName) {
        BedrockMenu menu = menus.get(menuName);
        if (menu == null) {
            return "基岩菜单不存在: " + menuName;
        }

        return String.format("基岩菜单: %s, 菜单项数: %d",
                menuName, menu.getMenuItems().size());
    }

    /**
     * 获取所有菜单的统计信息
     */
    public String getMenuStatistics() {
        int totalMenus = menus.size();
        int totalItems = 0;

        for (BedrockMenu menu : menus.values()) {
            totalItems += menu.getMenuItems().size();
        }

        return String.format("基岩菜单统计: 总数=%d, 总菜单项数=%d, 平均每菜单=%.1f项",
                totalMenus, totalItems, (double) totalItems / totalMenus);
    }

    /**
     * 检查菜单配置是否有效
     */
    public boolean isMenuValid(String menuName) {
        BedrockMenu menu = menus.get(menuName);
        if (menu == null) {
            return false;
        }

        return !menu.getMenuItems().isEmpty();
    }

    /**
     * 获取所有有效的菜单名称（有菜单项的）
     */
    public Set<String> getValidMenuNames() {
        Set<String> validMenus = new java.util.HashSet<>();
        for (Map.Entry<String, BedrockMenu> entry : menus.entrySet()) {
            if (!entry.getValue().getMenuItems().isEmpty()) {
                validMenus.add(entry.getKey());
            }
        }
        return validMenus;
    }

    /**
     * 清理资源（如果需要）
     */
    public void cleanup() {
        menus.clear();
    }
}
