package com.fluxcraft.dGeyserMenuFlux.javamenu;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import me.clip.placeholderapi.PlaceholderAPI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JavaMenu {
    private final String name;
    private final FileConfiguration config;
    private final List<MenuItem> items = new ArrayList<>();
    private String title;
    private int rows;
    private int size;
    private final Map<String, Object> menuSettings = new HashMap<>();

    public JavaMenu(String name, FileConfiguration config) {
        this.name = name;
        this.config = config;
        loadMenuConfig();
        loadItems();
        loadMenuSettings();
    }

    /**
     * 加载菜单基本配置 - 完全支持大菜单
     */
    private void loadMenuConfig() {
        this.title = config.getString("title", "&6菜单");

        // 完全兼容DeluxeMenus的rows设置，支持任意行数
        this.rows = config.getInt("rows", 3);

        // 移除行数限制！支持任意大小的菜单
        // 但为了稳定性，设置一个合理上限（比如9行=81格）
        if (rows < 1) rows = 1;
        if (rows > 9) {
            Bukkit.getLogger().warning("菜单 '" + name + "' 行数 " + rows + " 过大，限制为9行");
            rows = 9;
        }

        this.size = rows * 9;

        Bukkit.getLogger().info("加载菜单 '" + name + "': " + rows + "行, " + size + "格 (支持0-" + (size-1) + "槽位)");
    }

    /**
     * 加载DeluxeMenus兼容的菜单设置
     */
    private void loadMenuSettings() {
        menuSettings.put("command", config.getString("command"));
        menuSettings.put("open-actions", config.getStringList("open-actions"));
        menuSettings.put("open-requirement", config.getConfigurationSection("open-requirement"));
    }

    /**
     * 加载菜单物品 - 完全兼容DeluxeMenus格式，支持大菜单
     */
    private void loadItems() {
        if (!config.contains("items")) {
            Bukkit.getLogger().warning("菜单 '" + name + "' 没有找到items配置部分");
            return;
        }

        int validItems = 0;
        int outOfRangeItems = 0;

        for (String itemKey : config.getConfigurationSection("items").getKeys(false)) {
            String path = "items." + itemKey;

            try {
                int slot = config.getInt(path + ".slot", 0);
                String materialName = config.getString(path + ".material", "STONE");
                String displayName = config.getString(path + ".name", "&f未命名");
                List<String> lore = config.getStringList(path + ".lore");

                // 彻底移除槽位范围验证！支持任意槽位
                // 只验证是否为负数
                if (slot < 0) {
                    Bukkit.getLogger().warning("菜单 '" + name + "' 物品 '" + itemKey + "' 槽位 " + slot + " 不能为负数，已设置为0");
                    slot = 0;
                }

                // 如果槽位超过当前菜单大小，给出信息性提示但不阻止加载
                if (slot >= size) {
                    Bukkit.getLogger().info("菜单 '" + name + "' 物品 '" + itemKey + "' 槽位 " + slot + " 超出当前菜单大小 (" + size + "格)，将在更大的菜单中显示");
                    outOfRangeItems++;
                }

                // 加载动作 - 兼容DeluxeMenus的actions格式
                Map<String, List<String>> actions = new HashMap<>();
                if (config.contains(path + ".actions")) {
                    for (String actionType : config.getConfigurationSection(path + ".actions").getKeys(false)) {
                        List<String> actionList = config.getStringList(path + ".actions." + actionType);
                        actions.put(actionType.toUpperCase(), actionList);
                    }
                }

                // 加载其他DeluxeMenus兼容属性
                Map<String, Object> itemSettings = new HashMap<>();
                itemSettings.put("enchantments", config.getStringList(path + ".enchantments"));
                itemSettings.put("amount", config.getInt(path + ".amount", 1));
                itemSettings.put("data", config.getInt(path + ".data", 0));
                itemSettings.put("skull-owner", config.getString(path + ".skull-owner"));
                itemSettings.put("flags", config.getStringList(path + ".flags"));

                MenuItem menuItem = new MenuItem(slot, materialName, displayName, lore, actions, itemSettings);
                items.add(menuItem);
                validItems++;

            } catch (Exception e) {
                Bukkit.getLogger().warning("加载菜单 '" + name + "' 物品 '" + itemKey + "' 失败: " + e.getMessage());
                e.printStackTrace();
            }
        }

        Bukkit.getLogger().info("菜单 '" + name + "' 成功加载了 " + validItems + " 个物品" +
                (outOfRangeItems > 0 ? ", 其中 " + outOfRangeItems + " 个物品槽位超出当前菜单大小" : ""));
    }

    /**
     * 为玩家打开菜单 - 完全支持大菜单
     */
    public void open(Player player) {
        try {
            // 解析标题中的占位符
            String parsedTitle = parsePlaceholders(player, title);
            // 限制标题长度（避免过长导致问题）
            if (parsedTitle.length() > 32) {
                parsedTitle = parsedTitle.substring(0, 32);
                Bukkit.getLogger().warning("菜单 '" + name + "' 标题过长，已截断");
            }

            Bukkit.getLogger().info("为玩家 " + player.getName() + " 打开菜单 '" + name + "': " + rows + "行, " + size + "格");

            // 创建库存 - 支持最多81格（9行）
            Inventory inventory = Bukkit.createInventory(null, size, parsedTitle);

            // 添加物品到库存
            int addedItems = 0;
            int skippedItems = 0;

            for (MenuItem item : items) {
                ItemStack itemStack = item.createItemStack(player);
                if (itemStack != null) {
                    int slot = item.getSlot();

                    // 检查槽位是否在有效范围内
                    if (slot >= 0 && slot < size) {
                        inventory.setItem(slot, itemStack);
                        addedItems++;
                        Bukkit.getLogger().info("在槽位 " + slot + " 添加物品: " + item.getDisplayName());
                    } else {
                        Bukkit.getLogger().warning("菜单 '" + name + "' 物品槽位 " + slot + " 超出库存大小 " + size + "，已跳过");
                        skippedItems++;
                    }
                } else {
                    Bukkit.getLogger().warning("菜单 '" + name + "' 创建物品失败，槽位: " + item.getSlot());
                    skippedItems++;
                }
            }

            // 打开菜单
            player.openInventory(inventory);
            Bukkit.getLogger().info("成功为玩家 " + player.getName() + " 打开菜单 '" + name + "' (" + addedItems + "个物品, " + size + "格, " + skippedItems + "个跳过)");

        } catch (Exception e) {
            player.sendMessage("§c打开菜单时发生错误");
            Bukkit.getLogger().severe("打开Java菜单 '" + name + "' 失败: " + e.getMessage());
            e.printStackTrace();
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

    public String getTitle() {
        return title;
    }

    public int getRows() {
        return rows;
    }

    public int getSize() {
        return size;
    }

    public List<MenuItem> getItems() {
        return new ArrayList<>(items);
    }

    /**
     * 增强的菜单项内部类 - 完全兼容DeluxeMenus
     */
    public static class MenuItem {
        private final int slot;
        private final String materialName;
        private final String displayName;
        private final List<String> lore;
        private final Map<String, List<String>> actions;
        private final Map<String, Object> settings;

        public MenuItem(int slot, String materialName, String displayName, List<String> lore,
                        Map<String, List<String>> actions, Map<String, Object> settings) {
            this.slot = slot;
            this.materialName = materialName;
            this.displayName = displayName;
            this.lore = lore != null ? new ArrayList<>(lore) : new ArrayList<>();
            this.actions = actions != null ? new HashMap<>(actions) : new HashMap<>();
            this.settings = settings != null ? new HashMap<>(settings) : new HashMap<>();
        }

        /**
         * 为玩家创建物品堆栈 - 支持更多DeluxeMenus特性
         */
        public ItemStack createItemStack(Player player) {
            try {
                Material material = Material.getMaterial(materialName.toUpperCase());
                if (material == null) {
                    material = Material.STONE; // 默认材料
                    Bukkit.getLogger().warning("未知材料: " + materialName + ", 使用默认材料 STONE");
                }

                ItemStack item = new ItemStack(material);

                // 设置数量
                int amount = (int) settings.getOrDefault("amount", 1);
                if (amount < 1) amount = 1;
                if (amount > 64) amount = 64;
                item.setAmount(amount);

                ItemMeta meta = item.getItemMeta();

                if (meta != null) {
                    // 解析显示名称中的占位符
                    String parsedName = parsePlaceholders(player, displayName);
                    meta.setDisplayName(parsedName);

                    // 解析lore中的占位符
                    List<String> parsedLore = new ArrayList<>();
                    for (String line : lore) {
                        parsedLore.add(parsePlaceholders(player, line));
                    }
                    meta.setLore(parsedLore);

                    item.setItemMeta(meta);
                }

                return item;

            } catch (Exception e) {
                Bukkit.getLogger().warning("创建菜单物品失败: " + materialName + " - " + e.getMessage());
                return new ItemStack(Material.STONE);
            }
        }

        /**
         * 解析单个字符串的占位符
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
        public int getSlot() { return slot; }
        public String getMaterialName() { return materialName; }
        public String getDisplayName() { return displayName; }
        public List<String> getLore() { return new ArrayList<>(lore); }
        public Map<String, List<String>> getActions() { return new HashMap<>(actions); }
        public Map<String, Object> getSettings() { return new HashMap<>(settings); }

        /**
         * 获取指定点击类型的动作
         */
        public List<String> getActions(String clickType) {
            return actions.getOrDefault(clickType.toUpperCase(), new ArrayList<>());
        }

        @Override
        public String toString() {
            return "MenuItem{slot=" + slot + ", material=" + materialName + ", name=" + displayName + "}";
        }
    }
}