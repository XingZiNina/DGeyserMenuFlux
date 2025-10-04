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
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class JavaMenuManager {
    private final DGeyserMenuFlux plugin;
    private final Map<String, JavaMenu> menus = new HashMap<>();

    public JavaMenuManager(DGeyserMenuFlux plugin) {
        this.plugin = plugin;
    }

    public void loadAllMenus() {
        menus.clear();

        File javaMenuDir = new File(plugin.getDataFolder(), "java_menus");
        if (!javaMenuDir.exists()) {
            javaMenuDir.mkdirs();
        }

        File[] files = javaMenuDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) {
            return;
        }

        for (File file : files) {
            String menuName = file.getName().replace(".yml", "");
            try {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                JavaMenu menu = new JavaMenu(menuName, config);
                menus.put(menuName, menu);
            } catch (Exception e) {
                // 静默处理错误
            }
        }
    }

    public CompletableFuture<Void> openMenu(Player player, String menuName) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        player.getScheduler().run(plugin, task -> {
            JavaMenu menu = menus.get(menuName);
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

    public void reloadMenus() {
        plugin.runGlobalTask(this::loadAllMenus);
    }

    public boolean reloadMenu(String menuName) {
        File menuFile = new File(plugin.getDataFolder(), "java_menus/" + menuName + ".yml");
        if (!menuFile.exists()) {
            return false;
        }

        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(menuFile);
            JavaMenu menu = new JavaMenu(menuName, config);
            menus.put(menuName, menu);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public int getLoadedMenuCount() {
        return menus.size();
    }

    public Set<String> getMenuNames() {
        return menus.keySet();
    }

    public boolean menuExists(String menuName) {
        return menus.containsKey(menuName);
    }

    public JavaMenu getMenu(String menuName) {
        return menus.get(menuName);
    }

    public boolean canOpenMenu(Player player, String menuName) {
        if (player.hasPermission("dgeysermenu.admin") || player.hasPermission("dgeysermenu.*")) {
            return true;
        }

        String menuPermission = "dgeysermenu.menu." + menuName;
        return player.hasPermission(menuPermission) || player.hasPermission("dgeysermenu.use");
    }

    public JavaMenu findMenuByTitle(String title) {
        for (JavaMenu menu : menus.values()) {
            String menuTitle = menu.getTitle().replace("&", "§");
            if (menuTitle.equals(title)) {
                return menu;
            }
        }
        return null;
    }

    public boolean isMenuTitle(String title) {
        return findMenuByTitle(title) != null;
    }
}
