package com.fluxcraft.dGeyserMenuFlux.bedrockmenu.listeners;

import com.fluxcraft.dGeyserMenuFlux.DGeyserMenuFlux;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;

public class BedrockMenuListener implements Listener {
    private final DGeyserMenuFlux plugin;

    public BedrockMenuListener(DGeyserMenuFlux plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // 检查是否是基岩版玩家
        if (isBedrockPlayer(player)) {
            // 可以在这里执行基岩版玩家特有的逻辑
            // 例如：发送欢迎菜单等
        }
    }

    private boolean isBedrockPlayer(Player player) {
        try {
            return FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId());
        } catch (Exception e) {
            return false;
        }
    }
}