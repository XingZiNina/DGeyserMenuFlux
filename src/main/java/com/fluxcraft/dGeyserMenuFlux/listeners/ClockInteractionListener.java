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
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (!clockManager.isMenuClock(event.getItem())) {
            return;
        }

        event.setCancelled(true);

        // 在玩家线程中打开菜单
        event.getPlayer().getScheduler().run(plugin, task -> {
            clockManager.openMenuWithClock(event.getPlayer());
        }, null);
    }
}
