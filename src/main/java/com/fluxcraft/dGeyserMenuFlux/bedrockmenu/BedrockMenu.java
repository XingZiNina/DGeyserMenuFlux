package com.fluxcraft.dGeyserMenuFlux.bedrockmenu;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.floodgate.api.FloodgateApi;
import me.clip.placeholderapi.PlaceholderAPI;
import com.fluxcraft.dGeyserMenuFlux.DGeyserMenuFlux;

import java.util.ArrayList;
import java.util.List;

public class BedrockMenu {
    private final String name;
    private final FileConfiguration config;
    private final List<BedrockMenuItem> menuItems = new ArrayList<>();
    private DGeyserMenuFlux plugin;

    public BedrockMenu(String name, FileConfiguration config) {
        this.name = name;
        this.config = config;
        this.plugin = DGeyserMenuFlux.getInstance();
        loadMenuItems();
    }

    /**
     * 加载基岩版菜单项 - 支持新格式
     */
    private void loadMenuItems() {
        menuItems.clear();

        if (config.contains("menu.items")) {
            loadNewFormat();
        } else if (config.contains("buttons")) {
            loadLegacyFormat();
        } else if (config.contains("items")) {
            loadSimpleFormat();
        }
    }

    /**
     * 加载新格式菜单
     */
    private void loadNewFormat() {
        List<?> items = config.getList("menu.items");
        if (items == null) return;

        for (int i = 0; i < items.size(); i++) {
            try {
                Object itemObj = items.get(i);
                if (itemObj instanceof org.bukkit.configuration.ConfigurationSection) {
                    org.bukkit.configuration.ConfigurationSection itemSection =
                            (org.bukkit.configuration.ConfigurationSection) itemObj;

                    String text = itemSection.getString("text", "未命名按钮");
                    String icon = itemSection.getString("icon", "");
                    String iconType = itemSection.getString("icon_type", "path");
                    String command = itemSection.getString("command", "");
                    String submenu = itemSection.getString("submenu", "");
                    String executeAs = itemSection.getString("execute_as", "player");

                    if (text == null || text.trim().isEmpty()) {
                        text = "未命名按钮";
                    }

                    BedrockMenuItem menuItem = new BedrockMenuItem(text, icon, iconType, command, submenu, executeAs);
                    menuItems.add(menuItem);

                } else if (itemObj instanceof java.util.Map) {
                    java.util.Map<?, ?> itemMap = (java.util.Map<?, ?>) itemObj;
                    String text = String.valueOf(itemMap.get("text"));
                    String icon = String.valueOf(itemMap.get("icon"));
                    String iconType = String.valueOf(itemMap.get("icon_type"));
                    String command = String.valueOf(itemMap.get("command"));
                    String submenu = String.valueOf(itemMap.get("submenu"));
                    String executeAs = String.valueOf(itemMap.get("execute_as"));

                    if (text == null || text.equals("null") || text.trim().isEmpty()) {
                        text = "未命名按钮";
                    }
                    if (icon == null || icon.equals("null")) icon = "";
                    if (iconType == null || iconType.equals("null")) iconType = "path";
                    if (command == null || command.equals("null")) command = "";
                    if (submenu == null || submenu.equals("null")) submenu = "";
                    if (executeAs == null || executeAs.equals("null")) executeAs = "player";

                    BedrockMenuItem menuItem = new BedrockMenuItem(text, icon, iconType, command, submenu, executeAs);
                    menuItems.add(menuItem);
                }
            } catch (Exception e) {
                // 静默处理错误
            }
        }
    }

    /**
     * 加载旧格式菜单
     */
    private void loadLegacyFormat() {
        List<?> buttons = config.getList("buttons");
        if (buttons == null) return;

        for (int i = 0; i < buttons.size(); i++) {
            try {
                Object buttonObj = buttons.get(i);
                if (buttonObj instanceof org.bukkit.configuration.ConfigurationSection) {
                    org.bukkit.configuration.ConfigurationSection buttonSection =
                            (org.bukkit.configuration.ConfigurationSection) buttonObj;

                    String text = buttonSection.getString("text", "按钮");
                    String icon = "";
                    String iconType = "path";

                    if (buttonSection.contains("image")) {
                        icon = buttonSection.getString("image.data", "");
                        iconType = buttonSection.getString("image.type", "path");
                    }

                    String command = extractCommandFromActions(buttonSection.getStringList("actions"));
                    BedrockMenuItem menuItem = new BedrockMenuItem(text, icon, iconType, command, "", "player");
                    menuItems.add(menuItem);

                } else if (buttonObj instanceof java.util.Map) {
                    java.util.Map<?, ?> buttonMap = (java.util.Map<?, ?>) buttonObj;
                    String text = String.valueOf(buttonMap.get("text"));
                    String icon = "";
                    String iconType = "path";

                    if (buttonMap.containsKey("image")) {
                        Object imageObj = buttonMap.get("image");
                        if (imageObj instanceof java.util.Map) {
                            java.util.Map<?, ?> imageMap = (java.util.Map<?, ?>) imageObj;
                            icon = String.valueOf(imageMap.get("data"));
                            iconType = String.valueOf(imageMap.get("type"));
                        }
                    }

                    BedrockMenuItem menuItem = new BedrockMenuItem(text, icon, iconType, "", "", "player");
                    menuItems.add(menuItem);
                }
            } catch (Exception e) {
                // 静默处理错误
            }
        }
    }

    /**
     * 加载简单格式菜单
     */
    private void loadSimpleFormat() {
        List<?> items = config.getList("items");
        if (items == null) return;

        for (int i = 0; i < items.size(); i++) {
            try {
                Object itemObj = items.get(i);
                if (itemObj instanceof org.bukkit.configuration.ConfigurationSection) {
                    org.bukkit.configuration.ConfigurationSection itemSection =
                            (org.bukkit.configuration.ConfigurationSection) itemObj;

                    String text = itemSection.getString("text", "按钮");
                    String icon = itemSection.getString("icon", "");
                    String iconType = itemSection.getString("icon_type", "path");
                    String command = itemSection.getString("command", "");
                    String submenu = itemSection.getString("submenu", "");

                    BedrockMenuItem menuItem = new BedrockMenuItem(text, icon, iconType, command, submenu, "player");
                    menuItems.add(menuItem);
                }
            } catch (Exception e) {
                // 静默处理错误
            }
        }
    }

    /**
     * 从actions列表中提取命令
     */
    private String extractCommandFromActions(List<String> actions) {
        if (actions == null) return "";

        for (String action : actions) {
            if (action.startsWith("[command]")) {
                return action.substring(9).trim();
            } else if (action.startsWith("[menu]")) {
                return "dgeysermenu open " + action.substring(6).trim();
            }
        }
        return "";
    }

    /**
     * 为基岩版玩家打开菜单 - Folia 版本（已修复 lambda 问题）
     */
    public void open(Player player) {
        // 使用 Folia 的 EntityScheduler 在玩家所在线程执行
        player.getScheduler().run(plugin, task -> {
            if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) {
                player.sendMessage("§c此菜单仅适用于基岩版玩家");
                return;
            }

            try {
                String title = parsePlaceholders(player, getMenuTitle());
                String subtitle = parsePlaceholders(player, getMenuSubtitle());
                String footer = parsePlaceholders(player, getMenuFooter());

                StringBuilder contentBuilder = new StringBuilder();
                if (subtitle != null && !subtitle.isEmpty()) {
                    contentBuilder.append(subtitle).append("\n");
                }

                String content = contentBuilder.toString();

                SimpleForm.Builder form = SimpleForm.builder()
                        .title(title)
                        .content(content);

                if (menuItems.isEmpty()) {
                    form.button("§c没有可用的菜单项");
                } else {
                    for (BedrockMenuItem menuItem : menuItems) {
                        String buttonText = parsePlaceholders(player, menuItem.getText());

                        if (menuItem.hasIcon()) {
                            FormImage.Type imageType = getImageType(menuItem.getIconType());
                            form.button(buttonText, FormImage.of(imageType, menuItem.getIcon()));
                        } else {
                            form.button(buttonText);
                        }
                    }
                }

                if (footer != null && !footer.isEmpty()) {
                    String finalContent = content + "\n§8" + footer;
                    form.content(finalContent);
                }

                // 修复 lambda 问题：创建 final 副本
                final Player finalPlayer = player;
                final List<BedrockMenuItem> finalMenuItems = new ArrayList<>(menuItems);

                form.validResultHandler(response -> {
                    // 在玩家线程中处理点击事件
                    finalPlayer.getScheduler().run(plugin, clickTask -> {
                        try {
                            int clickedIndex = response.clickedButtonId();
                            if (clickedIndex >= 0 && clickedIndex < finalMenuItems.size()) {
                                BedrockMenuItem clickedItem = finalMenuItems.get(clickedIndex);
                                handleMenuItemClick(finalPlayer, clickedItem);
                            }
                        } catch (Exception e) {
                            // 静默处理错误
                        }
                    }, null);
                });

                FloodgateApi.getInstance().sendForm(player.getUniqueId(), form.build());

            } catch (Exception e) {
                player.sendMessage("§c打开菜单时发生错误");
            }
        }, null);
    }

    /**
     * 处理菜单项点击 - Folia 版本（已修复 lambda 问题）
     */
    private void handleMenuItemClick(Player player, BedrockMenuItem menuItem) {
        try {
            if (menuItem.getCommand() != null && !menuItem.getCommand().isEmpty()) {
                String command = parsePlaceholders(player, menuItem.getCommand());

                if (command.startsWith("/")) {
                    command = command.substring(1);
                }

                // 修复 lambda 问题：创建 final 副本
                final String finalCommand = command;
                final Player finalPlayer = player;

                if ("console".equalsIgnoreCase(menuItem.getExecuteAs())) {
                    // 使用 Folia 的全局调度器执行控制台命令
                    plugin.runGlobalTask(() -> {
                        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), finalCommand);
                        finalPlayer.sendMessage("§a命令执行成功!");
                    });
                } else {
                    // 在玩家线程中执行玩家命令
                    finalPlayer.getScheduler().run(plugin, task -> {
                        finalPlayer.performCommand(finalCommand);
                    }, null);
                }
                return;
            }

            if (menuItem.getSubmenu() != null && !menuItem.getSubmenu().isEmpty()) {
                String submenuName = menuItem.getSubmenu().replace(".yml", "");
                plugin.getBedrockMenuManager().openMenu(player, submenuName);
                return;
            }

            player.sendMessage("§a你点击了: " + parsePlaceholders(player, menuItem.getText()));

        } catch (Exception e) {
            player.sendMessage("§c执行命令时发生错误");
        }
    }

    /**
     * 获取菜单标题
     */
    private String getMenuTitle() {
        if (config.contains("menu.title")) {
            return config.getString("menu.title", "菜单");
        }
        return config.getString("title", "菜单");
    }

    /**
     * 获取菜单副标题
     */
    private String getMenuSubtitle() {
        if (config.contains("menu.subtitle")) {
            return config.getString("menu.subtitle", "");
        }
        return config.getString("subtitle", "");
    }

    /**
     * 获取菜单页脚
     */
    private String getMenuFooter() {
        if (config.contains("menu.footer")) {
            return config.getString("menu.footer", "");
        }
        return config.getString("footer", "");
    }

    /**
     * 获取图片类型
     */
    private FormImage.Type getImageType(String type) {
        if (type == null) {
            return FormImage.Type.PATH;
        }

        switch (type.toLowerCase()) {
            case "url":
                return FormImage.Type.URL;
            case "path":
            case "bedrock":
            default:
                return FormImage.Type.PATH;
        }
    }

    /**
     * 解析占位符
     */
    private String parsePlaceholders(Player player, String text) {
        if (text == null) return "";

        text = text.replace('&', '§');

        if (plugin.isPlaceholderAPIEnabled()) {
            try {
                text = PlaceholderAPI.setPlaceholders(player, text);
            } catch (Exception e) {
                // 静默处理错误
            }
        }

        return text;
    }

    // Getter 方法
    public String getName() { return name; }
    public List<BedrockMenuItem> getMenuItems() { return new ArrayList<>(menuItems); }
    public int getMenuItemCount() { return menuItems.size(); }
    public boolean hasValidItems() { return !menuItems.isEmpty(); }

    /**
     * 基岩版菜单项类
     */
    public static class BedrockMenuItem {
        private final String text;
        private final String icon;
        private final String iconType;
        private final String command;
        private final String submenu;
        private final String executeAs;

        public BedrockMenuItem(String text, String icon, String iconType, String command, String submenu, String executeAs) {
            this.text = text != null ? text : "未命名按钮";
            this.icon = icon != null ? icon : "";
            this.iconType = iconType != null ? iconType : "path";
            this.command = command != null ? command : "";
            this.submenu = submenu != null ? submenu : "";
            this.executeAs = executeAs != null ? executeAs : "player";
        }

        public String getText() { return text; }
        public String getIcon() { return icon; }
        public String getIconType() { return iconType; }
        public String getCommand() { return command; }
        public String getSubmenu() { return submenu; }
        public String getExecuteAs() { return executeAs; }
        public boolean hasIcon() {
            return icon != null && !icon.isEmpty() && !icon.equals("null");
        }
    }
}
