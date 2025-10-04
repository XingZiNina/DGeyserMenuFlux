package com.fluxcraft.dGeyserMenuFlux.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.fluxcraft.dGeyserMenuFlux.DGeyserMenuFlux;

public class ReloadCommand {
    private final DGeyserMenuFlux plugin;

    public ReloadCommand(DGeyserMenuFlux plugin) {
        this.plugin = plugin;
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("dgeysermenu.reload")) {
            sender.sendMessage("§c你没有权限执行此命令!");
            return true;
        }

        String type = "all";
        if (args.length > 1) {
            type = args[1].toLowerCase();
        }

        final String finalType = type;
        final CommandSender finalSender = sender;

        // 使用 Folia 的全局调度器执行重载
        plugin.runGlobalTask(() -> {
            try {
                switch (finalType) {
                    case "java":
                        plugin.getJavaMenuManager().reloadMenus();
                        finalSender.sendMessage("§aJava版菜单重载完成!");
                        break;
                    case "bedrock":
                        plugin.getBedrockMenuManager().reloadMenus();
                        finalSender.sendMessage("§a基岩版菜单重载完成!");
                        break;
                    case "all":
                    default:
                        plugin.reloadPlugin();
                        finalSender.sendMessage("§a所有菜单重载完成!");
                        break;
                }

                // 记录到控制台
                String playerName = finalSender instanceof Player ? finalSender.getName() : "控制台";
                plugin.getLogger().info("菜单配置已由 " + playerName + " 重载 (" + finalType + ")");

            } catch (Exception e) {
                finalSender.sendMessage("§c重载时发生错误: " + e.getMessage());
                plugin.getLogger().severe("重载菜单时发生错误: " + e.getMessage());
            }
        });

        return true;
    }
}
