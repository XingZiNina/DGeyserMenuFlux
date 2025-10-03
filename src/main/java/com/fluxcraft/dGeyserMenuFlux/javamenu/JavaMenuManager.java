package com.fluxcraft.dGeyserMenuFlux.javamenu;

import com.fluxcraft.dGeyserMenuFlux.DGeyserMenuFlux;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class JavaMenuManager {
    private final DGeyserMenuFlux plugin;
    private final Map<String, JavaMenu> menus = new HashMap<>();

    public JavaMenuManager(DGeyserMenuFlux plugin) {
        this.plugin = plugin;
    }

    /**
     * 加载所有Java版菜单
     */
    public void loadAllMenus() {
        menus.clear();

        File javaMenuDir = new File(plugin.getDataFolder(), "java_menus");
        if (!javaMenuDir.exists()) {
            javaMenuDir.mkdirs();
            plugin.getLogger().info("创建Java菜单目录: " + javaMenuDir.getAbsolutePath());
            // 不返回，继续检查是否有现有菜单
        }

        File[] files = javaMenuDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) {
            plugin.getLogger().warning("未找到Java菜单配置文件，请将菜单文件放入: " + javaMenuDir.getAbsolutePath());
            return;
        }

        int loadedCount = 0;
        int totalSlots = 0;

        for (File file : files) {
            String menuName = file.getName().replace(".yml", "");
            try {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                JavaMenu menu = new JavaMenu(menuName, config);
                menus.put(menuName, menu);
                loadedCount++;
                totalSlots += menu.getSize();

                plugin.getLogger().info("成功加载Java菜单: " + menuName +
                        " (" + menu.getRows() + "行, " + menu.getSize() + "格, " +
                        menu.getItems().size() + "个物品)");

            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "加载Java菜单失败: " + menuName, e);
            }
        }

        plugin.getLogger().info("Java菜单加载完成: " + loadedCount + "/" + files.length +
                " 个菜单, 总计 " + totalSlots + " 格物品栏");
    }

    /**
     * 打开指定菜单给玩家
     */
    public void openMenu(Player player, String menuName) {
        JavaMenu menu = menus.get(menuName);
        if (menu == null) {
            player.sendMessage("§c菜单不存在: " + menuName);
            plugin.getLogger().warning("玩家 " + player.getName() + " 尝试打开不存在的Java菜单: " + menuName);
            return;
        }

        try {
            // 检查权限
            if (!canOpenMenu(player, menuName)) {
                player.sendMessage("§c你没有权限打开这个菜单!");
                return;
            }

            menu.open(player);
            plugin.getLogger().info("为玩家 " + player.getName() + " 打开Java菜单: " + menuName +
                    " (" + menu.getRows() + "行)");

        } catch (Exception e) {
            player.sendMessage("§c打开菜单时发生错误");
            plugin.getLogger().log(Level.SEVERE, "打开Java菜单失败: " + menuName + " 给玩家 " + player.getName(), e);
        }
    }

    /**
     * 重新加载所有菜单
     */
    public void reloadMenus() {
        plugin.getLogger().info("开始重新加载Java菜单...");
        loadAllMenus();
    }

    /**
     * 重新加载指定菜单
     */
    public boolean reloadMenu(String menuName) {
        File menuFile = new File(plugin.getDataFolder(), "java_menus/" + menuName + ".yml");
        if (!menuFile.exists()) {
            plugin.getLogger().warning("重新加载Java菜单失败: 文件不存在 - " + menuName);
            return false;
        }

        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(menuFile);
            JavaMenu menu = new JavaMenu(menuName, config);
            menus.put(menuName, menu);
            plugin.getLogger().info("重新加载Java菜单: " + menuName + " (" + menu.getRows() + "行)");
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "重新加载Java菜单失败: " + menuName, e);
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
    public JavaMenu getMenu(String menuName) {
        return menus.get(menuName);
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
        JavaMenu menu = menus.get(menuName);
        if (menu == null) {
            return "菜单不存在: " + menuName;
        }

        return String.format("菜单: %s, 行数: %d, 格数: %d, 物品数: %d",
                menuName, menu.getRows(), menu.getSize(), menu.getItems().size());
    }

    /**
     * 获取所有菜单的统计信息
     */
    public String getMenuStatistics() {
        int totalMenus = menus.size();
        int totalSlots = 0;
        int totalItems = 0;

        for (JavaMenu menu : menus.values()) {
            totalSlots += menu.getSize();
            totalItems += menu.getItems().size();
        }

        return String.format("Java菜单统计: 总数=%d, 总格数=%d, 总物品数=%d, 平均填充=%.1f%%",
                totalMenus, totalSlots, totalItems, (totalItems * 100.0 / totalSlots));
    }
}