package com.fluxcraft.dGeyserMenuFlux.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.fluxcraft.dGeyserMenuFlux.DGeyserMenuFlux;
import org.geysermc.floodgate.api.FloodgateApi;

public class MenuCommand {
    private final DGeyserMenuFlux plugin;

    public MenuCommand(DGeyserMenuFlux plugin) {
        this.plugin = plugin;
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家才能执行此命令!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage("§c用法: /dgeysermenu open <菜单名称> [玩家]");
            return true;
        }

        String menuName = args[1];
        Player targetPlayer = player;

        // 检查是否有权限打开其他玩家的菜单
        if (args.length >= 3 && sender.hasPermission("dgeysermenu.admin")) {
            targetPlayer = plugin.getServer().getPlayer(args[2]);
            if (targetPlayer == null) {
                sender.sendMessage("§c玩家 " + args[2] + " 不在线!");
                return true;
            }
        }

        // 检查权限
        if (!hasMenuPermission(player, menuName)) {
            player.sendMessage("§c你没有权限打开这个菜单!");
            return true;
        }

        try {
            // 根据玩家类型打开对应菜单
            if (isBedrockPlayer(targetPlayer)) {
                plugin.getBedrockMenuManager().openMenu(targetPlayer, menuName);
            } else {
                plugin.getJavaMenuManager().openMenu(targetPlayer, menuName);
            }

            if (!targetPlayer.equals(player)) {
                player.sendMessage("§a已为玩家 " + targetPlayer.getName() + " 打开菜单: " + menuName);
            }

        } catch (Exception e) {
            player.sendMessage("§c打开菜单时发生错误: " + e.getMessage());
            plugin.getLogger().severe("打开菜单 " + menuName + " 时发生错误: " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }

    private boolean isBedrockPlayer(Player player) {
        try {
            return FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId());
        } catch (Exception e) {
            return false;
        }
    }

    private boolean hasMenuPermission(Player player, String menuName) {
        // 默认权限
        String permission = "dgeysermenu.menu." + menuName;

        if (player.hasPermission("dgeysermenu.admin") || player.hasPermission("dgeysermenu.*")) {
            return true;
        }

        return player.hasPermission(permission);
    }
}