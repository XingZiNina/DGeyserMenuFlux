package com.fluxcraft.dGeyserMenuFlux.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.fluxcraft.dGeyserMenuFlux.DGeyserMenuFlux;
import org.geysermc.floodgate.api.FloodgateApi;
import java.util.concurrent.CompletableFuture;

public class MenuCommand {
    private final DGeyserMenuFlux plugin;

    public MenuCommand(DGeyserMenuFlux plugin) {
        this.plugin = plugin;
    }

    public CompletableFuture<Boolean> execute(CommandSender sender, String[] args) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();

        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家才能执行此命令!");
            result.complete(true);
            return result;
        }

        final Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage("§c用法: /dgeysermenu open <菜单名称> [玩家]");
            result.complete(true);
            return result;
        }

        final String menuName = args[1];
        Player targetPlayer = player;

        // 检查是否有权限打开其他玩家的菜单
        if (args.length >= 3 && sender.hasPermission("dgeysermenu.admin")) {
            targetPlayer = plugin.getServer().getPlayer(args[2]);
            if (targetPlayer == null) {
                sender.sendMessage("§c玩家 " + args[2] + " 不在线!");
                result.complete(true);
                return result;
            }
        }

        final Player finalTargetPlayer = targetPlayer;

        // 检查权限
        if (!hasMenuPermission(player, menuName)) {
            player.sendMessage("§c你没有权限打开这个菜单!");
            result.complete(true);
            return result;
        }

        try {
            // 根据玩家类型打开对应菜单
            CompletableFuture<Void> future;
            if (isBedrockPlayer(finalTargetPlayer)) {
                if (plugin.getBedrockMenuManager().menuExists(menuName)) {
                    future = plugin.getBedrockMenuManager().openMenu(finalTargetPlayer, menuName);
                } else {
                    player.sendMessage("§c基岩版菜单不存在: " + menuName);
                    result.complete(true);
                    return result;
                }
            } else {
                if (plugin.getJavaMenuManager().menuExists(menuName)) {
                    future = plugin.getJavaMenuManager().openMenu(finalTargetPlayer, menuName);
                } else {
                    player.sendMessage("§cJava版菜单不存在: " + menuName);
                    result.complete(true);
                    return result;
                }
            }

            final boolean isDifferentPlayer = !finalTargetPlayer.equals(player);

            future.thenRun(() -> {
                if (isDifferentPlayer) {
                    player.sendMessage("§a已为玩家 " + finalTargetPlayer.getName() + " 打开菜单: " + menuName);
                }
                result.complete(true);
            }).exceptionally(throwable -> {
                player.sendMessage("§c打开菜单时发生错误: " + throwable.getMessage());
                plugin.getLogger().severe("打开菜单 " + menuName + " 时发生错误: " + throwable.getMessage());
                result.complete(true);
                return null;
            });

        } catch (Exception e) {
            player.sendMessage("§c打开菜单时发生错误: " + e.getMessage());
            plugin.getLogger().severe("打开菜单 " + menuName + " 时发生错误: " + e.getMessage());
            result.complete(true);
        }

        return result;
    }

    private boolean isBedrockPlayer(Player player) {
        try {
            return FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId());
        } catch (Exception e) {
            return false;
        }
    }

    private boolean hasMenuPermission(Player player, String menuName) {
        if (player.hasPermission("dgeysermenu.admin") || player.hasPermission("dgeysermenu.*")) {
            return true;
        }

        String menuPermission = "dgeysermenu.menu." + menuName;
        return player.hasPermission(menuPermission) || player.hasPermission("dgeysermenu.use");
    }
}
