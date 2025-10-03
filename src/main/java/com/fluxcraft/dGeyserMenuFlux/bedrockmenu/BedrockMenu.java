package com.fluxcraft.dGeyserMenuFlux.bedrockmenu;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.floodgate.api.FloodgateApi;
import me.clip.placeholderapi.PlaceholderAPI;

// 添加缺失的导入
import com.fluxcraft.dGeyserMenuFlux.DGeyserMenuFlux;

import java.util.ArrayList;
import java.util.List;

public class BedrockMenu {
    private final String name;
    private final FileConfiguration config;
    private final List<BedrockMenuItem> menuItems = new ArrayList<>();

    public BedrockMenu(String name, FileConfiguration config) {
        this.name = name;
        this.config = config;
        loadMenuItems();
    }

    /**
     * 加载基岩版菜单项 - 支持新格式
     */
    private void loadMenuItems() {
        menuItems.clear();

        // 首先尝试加载新的菜单格式
        if (config.contains("menu.items")) {
            loadNewFormat();
        }
        // 然后尝试加载旧格式
        else if (config.contains("buttons")) {
            loadLegacyFormat();
        }
        // 如果都没有，尝试加载最简格式
        else if (config.contains("items")) {
            loadSimpleFormat();
        } else {
            Bukkit.getLogger().warning("基岩菜单 '" + name + "' 没有找到有效的菜单项配置");
        }

        Bukkit.getLogger().info("基岩菜单 '" + name + "' 加载了 " + menuItems.size() + " 个菜单项");

        // 调试：打印所有加载的菜单项
        for (int i = 0; i < menuItems.size(); i++) {
            BedrockMenuItem item = menuItems.get(i);
            Bukkit.getLogger().info("菜单项 " + i + ": " + item.getText() + " | 图标: " + item.getIcon());
        }
    }

    /**
     * 加载新格式菜单
     */
    private void loadNewFormat() {
        List<?> items = config.getList("menu.items");
        if (items == null) {
            Bukkit.getLogger().warning("基岩菜单 '" + name + "' 的 menu.items 不是有效的列表");
            return;
        }

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

                    // 确保文本不为空
                    if (text == null || text.trim().isEmpty()) {
                        text = "未命名按钮";
                    }

                    BedrockMenuItem menuItem = new BedrockMenuItem(text, icon, iconType, command, submenu, executeAs);
                    menuItems.add(menuItem);

                } else if (itemObj instanceof java.util.Map) {
                    // 处理Map格式的配置
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
                Bukkit.getLogger().warning("加载基岩菜单 '" + name + "' 第 " + i + " 个物品失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * 加载旧格式菜单
     */
    private void loadLegacyFormat() {
        List<?> buttons = config.getList("buttons");
        if (buttons == null) {
            Bukkit.getLogger().warning("基岩菜单 '" + name + "' 的 buttons 不是有效的列表");
            return;
        }

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

                    // 从actions中提取命令
                    String command = extractCommandFromActions(buttonSection.getStringList("actions"));

                    BedrockMenuItem menuItem = new BedrockMenuItem(text, icon, iconType, command, "", "player");
                    menuItems.add(menuItem);

                } else if (buttonObj instanceof java.util.Map) {
                    // 处理Map格式的按钮
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
                Bukkit.getLogger().warning("加载基岩菜单 '" + name + "' 旧格式按钮 " + i + " 失败: " + e.getMessage());
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
                Bukkit.getLogger().warning("加载基岩菜单 '" + name + "' 简单格式项 " + i + " 失败: " + e.getMessage());
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
     * 为基岩版玩家打开菜单 - 修复选项显示问题
     */
    public void open(Player player) {
        if (!FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId())) {
            player.sendMessage("§c此菜单仅适用于基岩版玩家");
            return;
        }

        try {
            // 解析菜单元数据
            String title = parsePlaceholders(player, getMenuTitle());
            String subtitle = parsePlaceholders(player, getMenuSubtitle());
            String footer = parsePlaceholders(player, getMenuFooter());

            Bukkit.getLogger().info("=== 开始构建基岩菜单 ===");
            Bukkit.getLogger().info("菜单名称: " + name);
            Bukkit.getLogger().info("标题: " + title);
            Bukkit.getLogger().info("副标题: " + subtitle);
            Bukkit.getLogger().info("页脚: " + footer);
            Bukkit.getLogger().info("菜单项数量: " + menuItems.size());

            // 构建内容 - 修复：确保内容正确构建
            StringBuilder contentBuilder = new StringBuilder();
            if (subtitle != null && !subtitle.isEmpty()) {
                contentBuilder.append(subtitle).append("\n\n");
            }

            String content = contentBuilder.toString();

            Bukkit.getLogger().info("表单内容: " + content);

            // 构建表单
            SimpleForm.Builder form = SimpleForm.builder()
                    .title(title)
                    .content(content);

            // 添加菜单项到表单 - 修复：确保每个菜单项都正确添加
            if (menuItems.isEmpty()) {
                Bukkit.getLogger().warning("菜单 '" + name + "' 没有可用的菜单项，添加默认按钮");
                form.button("§c没有可用的菜单项");
            } else {
                for (int i = 0; i < menuItems.size(); i++) {
                    BedrockMenuItem menuItem = menuItems.get(i);
                    String buttonText = parsePlaceholders(player, menuItem.getText());

                    Bukkit.getLogger().info("添加按钮 " + i + ": " + buttonText);

                    if (menuItem.hasIcon()) {
                        FormImage.Type imageType = getImageType(menuItem.getIconType());
                        String iconPath = menuItem.getIcon();
                        Bukkit.getLogger().info("按钮图标: " + iconPath + " (类型: " + imageType + ")");

                        try {
                            form.button(buttonText, FormImage.of(imageType, iconPath));
                            Bukkit.getLogger().info("✓ 成功添加带图标按钮");
                        } catch (Exception e) {
                            Bukkit.getLogger().warning("✗ 添加图标按钮失败，使用无图标按钮: " + e.getMessage());
                            form.button(buttonText);
                        }
                    } else {
                        form.button(buttonText);
                        Bukkit.getLogger().info("✓ 成功添加无图标按钮");
                    }
                }
            }

            // 添加页脚信息 - 修复：正确添加页脚
            if (footer != null && !footer.isEmpty()) {
                // 重新构建包含页脚的内容
                String finalContent = content + "\n\n§8" + footer;
                form.content(finalContent);
                Bukkit.getLogger().info("最终内容(含页脚): " + finalContent);
            }

            // 构建表单
            SimpleForm simpleForm = form.build();

            // 处理按钮点击事件
            form.validResultHandler(response -> {
                try {
                    int clickedIndex = response.clickedButtonId();
                    Bukkit.getLogger().info("玩家点击了按钮索引: " + clickedIndex);

                    if (clickedIndex >= 0 && clickedIndex < menuItems.size()) {
                        BedrockMenuItem clickedItem = menuItems.get(clickedIndex);
                        Bukkit.getLogger().info("处理菜单项点击: " + clickedItem.getText());
                        handleMenuItemClick(player, clickedItem);
                    } else {
                        Bukkit.getLogger().warning("无效的按钮索引: " + clickedIndex);
                        player.sendMessage("§c无效的选择");
                    }
                } catch (Exception e) {
                    Bukkit.getLogger().severe("处理菜单点击时发生错误: " + e.getMessage());
                    e.printStackTrace();
                    player.sendMessage("§c处理选择时发生错误");
                }
            });

            // 发送表单给玩家
            FloodgateApi.getInstance().sendForm(player.getUniqueId(), simpleForm);
            Bukkit.getLogger().info("✓ 成功为基岩玩家 " + player.getName() + " 发送菜单: " + name);
            Bukkit.getLogger().info("=== 菜单构建完成 ===");

        } catch (Exception e) {
            player.sendMessage("§c打开菜单时发生错误");
            Bukkit.getLogger().severe("✗ 打开基岩菜单 '" + name + "' 失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 处理菜单项点击 - 修复：移除对DGeyserMenuFlux的直接引用
     */
    private void handleMenuItemClick(Player player, BedrockMenuItem menuItem) {
        try {
            Bukkit.getLogger().info("开始处理菜单项点击: " + menuItem.getText());

            // 执行命令
            if (menuItem.getCommand() != null && !menuItem.getCommand().isEmpty()) {
                String command = parsePlaceholders(player, menuItem.getCommand());
                Bukkit.getLogger().info("执行命令: '" + command + "', 执行方式: " + menuItem.getExecuteAs());

                // 清理命令格式
                if (command.startsWith("/")) {
                    command = command.substring(1);
                }

                if ("console".equalsIgnoreCase(menuItem.getExecuteAs())) {
                    // 以控制台身份执行
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                    player.sendMessage("§a命令执行成功!");
                } else {
                    // 以玩家身份执行
                    player.performCommand(command);
                }
                return;
            }

            // 打开子菜单 - 修复：通过Bukkit获取插件实例
            if (menuItem.getSubmenu() != null && !menuItem.getSubmenu().isEmpty()) {
                String submenuName = menuItem.getSubmenu().replace(".yml", "");
                Bukkit.getLogger().info("打开子菜单: " + submenuName);

                // 通过Bukkit获取插件实例，而不是直接引用
                org.bukkit.plugin.Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("DGeyserMenuFlux");
                if (plugin instanceof DGeyserMenuFlux) {
                    DGeyserMenuFlux menuPlugin = (DGeyserMenuFlux) plugin;
                    menuPlugin.getBedrockMenuManager().openMenu(player, submenuName);
                } else {
                    player.sendMessage("§c无法打开子菜单，插件未正确加载");
                    Bukkit.getLogger().warning("无法获取DGeyserMenuFlux插件实例");
                }
                return;
            }

            // 如果没有设置命令和子菜单，显示点击消息
            player.sendMessage("§a你点击了: " + parsePlaceholders(player, menuItem.getText()));

        } catch (Exception e) {
            player.sendMessage("§c执行菜单操作时发生错误");
            Bukkit.getLogger().severe("处理基岩菜单点击失败: " + e.getMessage());
            e.printStackTrace();
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

        // 颜色代码转换
        text = text.replace('&', '§');

        // PlaceholderAPI 支持
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            text = PlaceholderAPI.setPlaceholders(player, text);
        }

        return text;
    }

    // Getter 方法
    public String getName() {
        return name;
    }

    public List<BedrockMenuItem> getMenuItems() {
        return new ArrayList<>(menuItems);
    }

    public int getMenuItemCount() {
        return menuItems.size();
    }

    public boolean hasValidItems() {
        return !menuItems.isEmpty();
    }

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

        @Override
        public String toString() {
            return "BedrockMenuItem{" +
                    "text='" + text + '\'' +
                    ", icon='" + icon + '\'' +
                    ", command='" + command + '\'' +
                    ", submenu='" + submenu + '\'' +
                    '}';
        }
    }
}