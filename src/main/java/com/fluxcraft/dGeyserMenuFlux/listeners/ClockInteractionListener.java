package com.fluxcraft.dGeyserMenuFlux.listeners;

import com.fluxcraft.dGeyserMenuFlux.DGeyserMenuFlux;
import com.fluxcraft.dGeyserMenuFlux.utils.MenuClockManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class ClockInteractionListener implements Listener {
    private final DGeyserMenuFlux plugin;
    private final MenuClockManager clockManager;

    public ClockInteractionListener(DGeyserMenuFlux plugin, MenuClockManager clockManager) {
        this.plugin = plugin;
        this.clockManager = clockManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // 只处理右手交互（避免重复触发）
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        // 检查是否是右键点击空气或方块
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // 检查手持物品是否是菜单钟表
        if (!clockManager.isMenuClock(event.getItem())) {
            return;
        }

        // 取消事件（防止放置钟表等操作）
        event.setCancelled(true);

        // 打开菜单
        clockManager.openMenuWithClock(event.getPlayer());

        plugin.getLogger().info("玩家 " + event.getPlayer().getName() + " 使用钟表打开菜单");
    }
}