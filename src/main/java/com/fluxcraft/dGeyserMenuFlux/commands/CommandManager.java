package com.fluxcraft.dGeyserMenuFlux.commands;

import com.fluxcraft.dGeyserMenuFlux.DGeyserMenuFlux;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class CommandManager implements CommandExecutor, TabCompleter {
    private final DGeyserMenuFlux plugin;

    public CommandManager(DGeyserMenuFlux plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        // 处理 /getmenuclock 命令
        if ("getmenuclock".equalsIgnoreCase(command.getName())) {
            return handleGetMenuClockCommand(sender);
        }

        // 处理 /dgeysermenu 命令
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "open":
                return handleOpenCommand(sender, args);
            case "reload":
                return handleReloadCommand(sender, args);
            case "list":
                return handleListCommand(sender);
            case "help":
            default:
                sendHelp(sender);
                return true;
        }
    }

    /**
     * 处理打开菜单命令
     */
    private boolean handleOpenCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家才能执行此命令!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§c用法: /dgeysermenu open <菜单名称> [玩家]");
            return true;
        }

        Player player = (Player) sender;
        String menuName = args[1];
        Player targetPlayer = player;

        // 检查是否有权限打开其他玩家的菜单
        if (args.length >= 3) {
            if (!sender.hasPermission("dgeysermenu.admin")) {
                sender.sendMessage("§c你没有权限为其他玩家打开菜单!");
                return true;
            }
            targetPlayer = plugin.getServer().getPlayer(args[2]);
            if (targetPlayer == null) {
                sender.sendMessage("§c玩家 " + args[2] + " 不在线!");
                return true;
            }
        }

        // 检查菜单权限
        if (!hasMenuPermission(player, menuName)) {
            player.sendMessage("§c你没有权限打开这个菜单!");
            return true;
        }

        // 根据玩家类型打开对应菜单
        try {
            if (plugin.isBedrockPlayer(targetPlayer.getUniqueId())) {
                if (plugin.getBedrockMenuManager().menuExists(menuName)) {
                    plugin.getBedrockMenuManager().openMenu(targetPlayer, menuName);
                    if (!targetPlayer.equals(player)) {
                        player.sendMessage("§a已为基岩玩家 " + targetPlayer.getName() + " 打开菜单: " + menuName);
                    }
                } else {
                    player.sendMessage("§c基岩版菜单不存在: " + menuName);
                }
            } else {
                if (plugin.getJavaMenuManager().menuExists(menuName)) {
                    plugin.getJavaMenuManager().openMenu(targetPlayer, menuName);
                    if (!targetPlayer.equals(player)) {
                        player.sendMessage("§a已为Java玩家 " + targetPlayer.getName() + " 打开菜单: " + menuName);
                    }
                } else {
                    player.sendMessage("§cJava版菜单不存在: " + menuName);
                }
            }
        } catch (Exception e) {
            player.sendMessage("§c打开菜单时发生错误!");
            plugin.getLogger().log(Level.SEVERE, "打开菜单失败: " + menuName, e);
        }

        return true;
    }

    /**
     * 处理重载命令
     */
    private boolean handleReloadCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("dgeysermenu.reload")) {
            sender.sendMessage("§c你没有权限执行此命令!");
            return true;
        }

        String type = "all";
        if (args.length > 1) {
            type = args[1].toLowerCase();
        }

        try {
            switch (type) {
                case "java":
                    plugin.getJavaMenuManager().reloadMenus();
                    sender.sendMessage("§aJava版菜单重载完成! 已加载 " +
                            plugin.getJavaMenuManager().getLoadedMenuCount() + " 个菜单");
                    break;
                case "bedrock":
                    plugin.getBedrockMenuManager().reloadMenus();
                    sender.sendMessage("§a基岩版菜单重载完成! 已加载 " +
                            plugin.getBedrockMenuManager().getLoadedMenuCount() + " 个菜单");
                    break;
                case "all":
                default:
                    plugin.reloadPlugin();
                    sender.sendMessage("§a所有菜单重载完成! Java: " +
                            plugin.getJavaMenuManager().getLoadedMenuCount() +
                            ", 基岩: " + plugin.getBedrockMenuManager().getLoadedMenuCount());
                    break;
            }

            String playerName = sender instanceof Player ? sender.getName() : "控制台";
            plugin.getLogger().info("菜单配置已由 " + playerName + " 重载 (" + type + ")");

        } catch (Exception e) {
            sender.sendMessage("§c重载时发生错误: " + e.getMessage());
            plugin.getLogger().log(Level.SEVERE, "重载菜单时发生错误", e);
        }

        return true;
    }

    /**
     * 处理列出菜单命令
     */
    private boolean handleListCommand(CommandSender sender) {
        if (!sender.hasPermission("dgeysermenu.use")) {
            sender.sendMessage("§c你没有权限执行此命令!");
            return true;
        }

        int javaCount = plugin.getJavaMenuManager().getLoadedMenuCount();
        int bedrockCount = plugin.getBedrockMenuManager().getLoadedMenuCount();

        sender.sendMessage("§6=== 可用菜单列表 ===");
        sender.sendMessage("§eJava版菜单 (§f" + javaCount + "§e): §a" +
                String.join("§7, §a", plugin.getJavaMenuManager().getMenuNames()));
        sender.sendMessage("§e基岩版菜单 (§f" + bedrockCount + "§e): §b" +
                String.join("§7, §b", plugin.getBedrockMenuManager().getMenuNames()));
        sender.sendMessage("§7使用 §f/dgeysermenu open <菜单名> §7打开菜单");

        return true;
    }

    /**
     * 处理获取菜单钟表命令
     */
    private boolean handleGetMenuClockCommand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家才能执行此命令!");
            return true;
        }

        if (!sender.hasPermission("dgeysermenu.admin")) {
            sender.sendMessage("§c你没有权限执行此命令!");
            return true;
        }

        Player player = (Player) sender;
        plugin.getClockManager().giveMenuClock(player);
        player.sendMessage("§a已获得菜单钟表!");

        return true;
    }

    /**
     * 发送帮助信息
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6=== DGeyserMenuFlux 帮助 ===");
        sender.sendMessage("§f/dgeysermenu open <菜单> [玩家] §7- 打开指定菜单");
        sender.sendMessage("§f/dgeysermenu reload [all|java|bedrock] §7- 重载插件配置");
        sender.sendMessage("§f/dgeysermenu list §7- 显示所有可用菜单");
        sender.sendMessage("§f/dgeysermenu help §7- 显示此帮助");

        if (sender.hasPermission("dgeysermenu.admin")) {
            sender.sendMessage("§e管理员权限: §a✓ 可重载配置 §a✓ 可为他人打开菜单");
            sender.sendMessage("§f/getmenuclock §7- 获取菜单钟表 (管理员)");
        }
    }

    /**
     * 检查菜单权限
     */
    private boolean hasMenuPermission(Player player, String menuName) {
        if (player.hasPermission("dgeysermenu.admin") || player.hasPermission("dgeysermenu.*")) {
            return true;
        }

        String menuPermission = "dgeysermenu.menu." + menuName;
        return player.hasPermission(menuPermission) || player.hasPermission("dgeysermenu.use");
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        // 处理 /getmenuclock 命令的Tab补全（无参数）
        if ("getmenuclock".equalsIgnoreCase(command.getName())) {
            return completions; // 无参数可补全
        }

        // 处理 /dgeysermenu 命令的Tab补全
        if (args.length == 1) {
            // 主命令Tab补全
            List<String> commands = Arrays.asList("open", "reload", "help", "list");
            for (String cmd : commands) {
                if (cmd.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(cmd);
                }
            }
        } else if (args.length == 2) {
            // 子命令Tab补全
            switch (args[0].toLowerCase()) {
                case "open":
                    // 动态获取所有可用的菜单名称
                    completions.addAll(plugin.getConfigManager().getAllMenuNames());
                    break;
                case "reload":
                    completions.addAll(Arrays.asList("all", "java", "bedrock"));
                    break;
            }
            // 过滤匹配
            completions.removeIf(s -> !s.toLowerCase().startsWith(args[1].toLowerCase()));
        } else if (args.length == 3 && args[0].equalsIgnoreCase("open")) {
            // 玩家名称补全（仅管理员）
            if (sender.hasPermission("dgeysermenu.admin")) {
                completions.addAll(getOnlinePlayerNames());
                completions.removeIf(s -> !s.toLowerCase().startsWith(args[2].toLowerCase()));
            }
        }

        return completions;
    }

    /**
     * 获取在线玩家名称列表
     */
    private List<String> getOnlinePlayerNames() {
        List<String> names = new ArrayList<>();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            names.add(player.getName());
        }
        return names;
    }
}