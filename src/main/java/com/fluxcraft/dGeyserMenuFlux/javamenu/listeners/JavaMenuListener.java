package com.fluxcraft.dGeyserMenuFlux.javamenu.listeners;

import com.fluxcraft.dGeyserMenuFlux.DGeyserMenuFlux;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

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

        // 修复：使用getTitle()方法
        String title = view.getTitle();

        // 检查是否是菜单库存
        if (isMenuInventory(title)) {
            event.setCancelled(true); // 取消点击事件，防止玩家移动物品

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || !clickedItem.hasItemMeta()) return;

            int slot = event.getRawSlot();
            if (slot >= 0 && slot < view.getTopInventory().getSize()) {
                // 处理菜单点击
                handleMenuClick(player, title, slot, event.getClick());
            }
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        String title = event.getView().getTitle();

        if (isMenuInventory(title)) {
            plugin.getLogger().info("玩家 " + player.getName() + " 打开了菜单: " + title);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        String title = event.getView().getTitle();

        if (isMenuInventory(title)) {
            plugin.getLogger().info("玩家 " + player.getName() + " 关闭了菜单: " + title);
        }
    }

    private boolean isMenuInventory(String title) {
        // 检查这个库存是否是我们的菜单
        // 这里需要实现识别逻辑，可以根据标题特征判断
        return title != null && (title.contains("菜单") || title.contains("Menu"));
    }

    private void handleMenuClick(Player player, String menuTitle, int slot, org.bukkit.event.inventory.ClickType clickType) {
        // 处理菜单点击逻辑
        // 这里需要根据点击的槽位执行对应的动作

        String clickTypeName = clickType.name();

        // 查找对应的菜单和动作
        // 执行配置的动作，比如打开另一个菜单、执行命令等

        player.sendMessage("§a你点击了槽位: " + slot + ", 点击类型: " + clickTypeName);

        // 示例：根据槽位执行不同动作
        switch (slot) {
            case 0:
                player.sendMessage("§e你点击了第一个物品！");
                break;
            case 1:
                player.sendMessage("§e你点击了第二个物品！");
                break;
            default:
                // 其他槽位的处理
                break;
        }
    }
}