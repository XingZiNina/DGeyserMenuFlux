package com.fluxcraft.dGeyserMenuFlux.javamenu.listeners;

import com.fluxcraft.dGeyserMenuFlux.DGeyserMenuFlux;
import com.fluxcraft.dGeyserMenuFlux.javamenu.JavaMenu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class JavaMenuListener implements Listener {
    private final DGeyserMenuFlux plugin;

    public JavaMenuListener(DGeyserMenuFlux plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        InventoryView view = event.getView();
        Inventory clickedInventory = event.getClickedInventory();

        // 检查点击的库存是否是菜单库存（通过持有者检测）
        if (clickedInventory != null && clickedInventory.getHolder() instanceof JavaMenu.MenuHolder) {
            // 取消所有点击事件，防止物品被移动
            event.setCancelled(true);

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || !clickedItem.hasItemMeta()) return;

            JavaMenu.MenuHolder holder = (JavaMenu.MenuHolder) clickedInventory.getHolder();
            JavaMenu menu = holder.getMenu();

            JavaMenu.MenuItem menuItem = menu.getItemAtSlot(event.getSlot());
            if (menuItem == null) return;

            // 确定点击类型
            String clickType = "LEFT";
            if (event.isRightClick()) {
                clickType = "RIGHT";
            }

            // 执行菜单项的命令
            executeMenuItemCommands(player, menuItem, clickType);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        InventoryView view = event.getView();

        // 检查拖拽是否涉及菜单库存
        for (Integer slot : event.getRawSlots()) {
            if (slot < view.getTopInventory().getSize()) {
                // 拖拽涉及顶部库存（菜单），取消事件
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * 执行菜单项的命令
     */
    private void executeMenuItemCommands(Player player, JavaMenu.MenuItem menuItem, String clickType) {
        // 获取对应点击类型的命令
        List<String> commands = menuItem.getActions(clickType);
        if (commands.isEmpty()) {
            // 如果没有特定类型的命令，尝试通用命令
            commands = menuItem.getActions("ALL");
            if (commands.isEmpty()) {
                return; // 静默返回，不发送消息
            }
        }

        // 执行每个命令
        for (String command : commands) {
            if (command != null && !command.trim().isEmpty()) {
                executeCommand(player, command.trim());
            }
        }
    }

    /**
     * 执行单个命令
     */
    private void executeCommand(Player player, String command) {
        try {
            // 解析占位符
            String parsedCommand = parsePlaceholders(player, command);

            // 根据命令前缀执行不同的操作
            if (parsedCommand.startsWith("[player]")) {
                String actualCommand = parsedCommand.substring(8).trim();
                executePlayerCommand(player, actualCommand);

            } else if (parsedCommand.startsWith("[console]")) {
                String actualCommand = parsedCommand.substring(9).trim();
                executeConsoleCommand(actualCommand);

            } else if (parsedCommand.startsWith("[op]")) {
                String actualCommand = parsedCommand.substring(4).trim();
                executeOpCommand(player, actualCommand);

            } else if (parsedCommand.startsWith("[close]")) {
                player.closeInventory();

            } else if (parsedCommand.startsWith("[message]")) {
                String message = parsedCommand.substring(9).trim();
                sendMessage(player, message);

            } else if (parsedCommand.startsWith("[broadcast]") || parsedCommand.startsWith("[bc]")) {
                String message = parsedCommand.replace("[broadcast]", "").replace("[bc]", "").trim();
                broadcastMessage(message);

            } else if (parsedCommand.startsWith("[menu]")) {
                String menuName = parsedCommand.substring(6).trim();
                openMenu(player, menuName);

            } else {
                // 默认情况下，以玩家身份执行命令
                executePlayerCommand(player, parsedCommand);
            }

        } catch (Exception e) {
            // 静默处理错误
        }
    }

    /**
     * 以玩家身份执行命令
     */
    private void executePlayerCommand(Player player, String command) {
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        player.performCommand(command);
    }

    /**
     * 以控制台身份执行命令
     */
    private void executeConsoleCommand(String command) {
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    /**
     * 以OP身份执行命令
     */
    private void executeOpCommand(Player player, String command) {
        if (command.startsWith("/")) {
            command = command.substring(1);
        }

        boolean wasOp = player.isOp();
        try {
            player.setOp(true);
            player.performCommand(command);
        } finally {
            player.setOp(wasOp);
        }
    }

    /**
     * 发送消息给玩家
     */
    private void sendMessage(Player player, String message) {
        String parsedMessage = parsePlaceholders(player, message);
        player.sendMessage(parsedMessage);
    }

    /**
     * 广播消息
     */
    private void broadcastMessage(String message) {
        String parsedMessage = parsePlaceholders(null, message);
        Bukkit.broadcastMessage(parsedMessage);
    }

    /**
     * 打开其他菜单
     */
    private void openMenu(Player player, String menuName) {
        plugin.getJavaMenuManager().openMenu(player, menuName);
    }

    /**
     * 解析占位符
     */
    private String parsePlaceholders(Player player, String text) {
        if (text == null) return "";

        // 首先转换颜色代码
        text = text.replace('&', '§');

        // 如果有玩家且PlaceholderAPI可用，解析占位符
        if (player != null && plugin.isPlaceholderAPIEnabled()) {
            try {
                text = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
            } catch (Exception e) {
                // 静默处理错误
            }
        }

        return text;
    }
}
