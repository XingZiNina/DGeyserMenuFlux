package com.fluxcraft.dGeyserMenuFlux.javamenu.listeners;

import com.fluxcraft.dGeyserMenuFlux.DGeyserMenuFlux;
import com.fluxcraft.dGeyserMenuFlux.javamenu.JavaMenu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
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

        if (clickedInventory != null && clickedInventory.getHolder() instanceof JavaMenu.MenuHolder) {
            event.setCancelled(true);

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || !clickedItem.hasItemMeta()) return;

            JavaMenu.MenuHolder holder = (JavaMenu.MenuHolder) clickedInventory.getHolder();
            JavaMenu menu = holder.getMenu();

            JavaMenu.MenuItem menuItem = menu.getItemAtSlot(event.getSlot());
            if (menuItem == null) return;

            String clickType;
            if (event.isRightClick()) {
                clickType = "RIGHT";
            } else {
                clickType = "LEFT";
            }

            // 在玩家所在线程执行命令
            player.getScheduler().run(plugin, task -> {
                executeMenuItemCommands(player, menuItem, clickType);
            }, null);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        InventoryView view = event.getView();

        for (Integer slot : event.getRawSlots()) {
            if (slot < view.getTopInventory().getSize()) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // 静默处理关闭事件
    }

    private void executeMenuItemCommands(Player player, JavaMenu.MenuItem menuItem, String clickType) {
        List<String> commands = menuItem.getActions(clickType);
        if (commands.isEmpty()) {
            commands = menuItem.getActions("ALL");
            if (commands.isEmpty()) {
                return;
            }
        }

        for (String command : commands) {
            if (command != null && !command.trim().isEmpty()) {
                executeCommand(player, command.trim());
            }
        }
    }

    private void executeCommand(Player player, String command) {
        try {
            String parsedCommand = parsePlaceholders(player, command);

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
                executePlayerCommand(player, parsedCommand);
            }

        } catch (Exception e) {
            // 静默处理错误
        }
    }

    private void executePlayerCommand(Player player, String command) {
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        player.performCommand(command);
    }

    private void executeConsoleCommand(String command) {
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

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

    private void sendMessage(Player player, String message) {
        String parsedMessage = parsePlaceholders(player, message);
        player.sendMessage(parsedMessage);
    }

    private void broadcastMessage(String message) {
        String parsedMessage = parsePlaceholders(null, message);
        Bukkit.broadcastMessage(parsedMessage);
    }

    private void openMenu(Player player, String menuName) {
        plugin.getJavaMenuManager().openMenu(player, menuName);
    }

    private String parsePlaceholders(Player player, String text) {
        if (text == null) return "";
        text = text.replace('&', '§');

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