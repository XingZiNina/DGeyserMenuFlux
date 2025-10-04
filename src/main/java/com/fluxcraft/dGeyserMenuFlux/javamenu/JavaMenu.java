package com.fluxcraft.dGeyserMenuFlux.javamenu;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import me.clip.placeholderapi.PlaceholderAPI;
import com.fluxcraft.dGeyserMenuFlux.DGeyserMenuFlux;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class JavaMenu {
    private final String name;
    private final FileConfiguration config;
    private final List<MenuItem> items = new ArrayList<>();
    private String title;
    private int rows;
    private int size;
    private DGeyserMenuFlux plugin;

    public JavaMenu(String name, FileConfiguration config) {
        this.name = name;
        this.config = config;
        this.plugin = DGeyserMenuFlux.getInstance();
        loadMenuConfig();
        loadItems();
    }

    private void loadMenuConfig() {
        if (config.contains("menu_title")) {
            this.title = config.getString("menu_title", "&6菜单");
        } else if (config.contains("title")) {
            this.title = config.getString("title", "&6菜单");
        } else {
            this.title = "&6菜单";
        }

        this.rows = config.getInt("rows", 3);
        if (rows < 1) rows = 1;
        if (rows > 6) rows = 6;

        this.size = rows * 9;
    }

    private void loadItems() {
        if (!config.contains("items")) {
            return;
        }

        for (String itemKey : config.getConfigurationSection("items").getKeys(false)) {
            String path = "items." + itemKey;

            try {
                Object slotObj = config.get(path + ".slot");
                List<Integer> slots = parseSlots(slotObj);

                String materialName = getMaterialName(config, path);
                String displayName = getDisplayName(config, path);
                List<String> lore = getLore(config, path);

                Map<String, List<String>> commands = new HashMap<>();
                loadDeluxeMenusCommands(config, path, commands);

                for (int slot : slots) {
                    MenuItem menuItem = new MenuItem(slot, materialName, displayName, lore, commands);
                    items.add(menuItem);
                }

            } catch (Exception e) {
                // 静默处理错误
            }
        }
    }

    private List<Integer> parseSlots(Object slotObj) {
        List<Integer> slots = new ArrayList<>();

        if (slotObj instanceof Integer) {
            slots.add((Integer) slotObj);
        } else if (slotObj instanceof String) {
            String slotStr = (String) slotObj;
            if (slotStr.contains("-")) {
                String[] range = slotStr.split("-");
                if (range.length == 2) {
                    try {
                        int start = Integer.parseInt(range[0]);
                        int end = Integer.parseInt(range[1]);
                        for (int i = start; i <= end; i++) {
                            if (i >= 0 && i < 54) {
                                slots.add(i);
                            }
                        }
                    } catch (NumberFormatException e) {
                        // 静默处理
                    }
                }
            } else if (slotStr.contains(",")) {
                String[] slotArray = slotStr.split(",");
                for (String slot : slotArray) {
                    try {
                        int slotNum = Integer.parseInt(slot.trim());
                        if (slotNum >= 0 && slotNum < 54) {
                            slots.add(slotNum);
                        }
                    } catch (NumberFormatException e) {
                        // 静默处理
                    }
                }
            } else {
                try {
                    int slotNum = Integer.parseInt(slotStr);
                    if (slotNum >= 0 && slotNum < 54) {
                        slots.add(slotNum);
                    }
                } catch (NumberFormatException e) {
                    // 静默处理
                }
            }
        }

        if (slots.isEmpty()) {
            slots.add(0);
        }

        return slots;
    }

    private String getMaterialName(FileConfiguration config, String path) {
        if (config.contains(path + ".material")) {
            return config.getString(path + ".material");
        } else if (config.contains(path + ".type")) {
            return config.getString(path + ".type");
        } else if (config.contains(path + ".item")) {
            return config.getString(path + ".item");
        }
        return "STONE";
    }

    private String getDisplayName(FileConfiguration config, String path) {
        if (config.contains(path + ".display_name")) {
            return config.getString(path + ".display_name");
        } else if (config.contains(path + ".name")) {
            return config.getString(path + ".name");
        } else if (config.contains(path + ".display-name")) {
            return config.getString(path + ".display-name");
        } else if (config.contains(path + ".display_name")) {
            return config.getString(path + ".display_name");
        }
        return "&f未命名";
    }

    private List<String> getLore(FileConfiguration config, String path) {
        if (config.contains(path + ".lore")) {
            return config.getStringList(path + ".lore");
        } else if (config.contains(path + ".description")) {
            return config.getStringList(path + ".description");
        } else if (config.contains(path + ".desc")) {
            return config.getStringList(path + ".desc");
        }
        return new ArrayList<>();
    }

    private void loadDeluxeMenusCommands(FileConfiguration config, String path, Map<String, List<String>> commands) {
        // 左键点击命令
        if (config.contains(path + ".left_click_commands")) {
            commands.put("LEFT", config.getStringList(path + ".left_click_commands"));
        } else if (config.contains(path + ".left-click-commands")) {
            commands.put("LEFT", config.getStringList(path + ".left-click-commands"));
        } else if (config.contains(path + ".actions.LEFT")) {
            commands.put("LEFT", config.getStringList(path + ".actions.LEFT"));
        } else if (config.contains(path + ".left_click_action")) {
            List<String> leftActions = new ArrayList<>();
            String action = config.getString(path + ".left_click_action");
            if (action != null && !action.trim().isEmpty()) {
                leftActions.add(action);
            }
            commands.put("LEFT", leftActions);
        }

        // 右键点击命令
        if (config.contains(path + ".right_click_commands")) {
            commands.put("RIGHT", config.getStringList(path + ".right_click_commands"));
        } else if (config.contains(path + ".right-click-commands")) {
            commands.put("RIGHT", config.getStringList(path + ".right-click-commands"));
        } else if (config.contains(path + ".actions.RIGHT")) {
            commands.put("RIGHT", config.getStringList(path + ".actions.RIGHT"));
        } else if (config.contains(path + ".right_click_action")) {
            List<String> rightActions = new ArrayList<>();
            String action = config.getString(path + ".right_click_action");
            if (action != null && !action.trim().isEmpty()) {
                rightActions.add(action);
            }
            commands.put("RIGHT", rightActions);
        }

        // 通用命令
        if (config.contains(path + ".commands")) {
            commands.put("ALL", config.getStringList(path + ".commands"));
        } else if (config.contains(path + ".action")) {
            List<String> allActions = new ArrayList<>();
            String action = config.getString(path + ".action");
            if (action != null && !action.trim().isEmpty()) {
                allActions.add(action);
            }
            commands.put("ALL", allActions);
        } else if (config.contains(path + ".click_commands")) {
            commands.put("ALL", config.getStringList(path + ".click_commands"));
        }
    }

    public void open(Player player) {
        player.getScheduler().run(plugin, task -> {
            try {
                String parsedTitle = parsePlaceholders(player, title);
                if (parsedTitle.length() > 32) {
                    parsedTitle = parsedTitle.substring(0, 32);
                }

                MenuHolder holder = new MenuHolder(this);
                Inventory inventory = Bukkit.createInventory(holder, size, parsedTitle);

                for (MenuItem item : items) {
                    ItemStack itemStack = item.createItemStack(player);
                    if (itemStack != null && item.getSlot() < size) {
                        inventory.setItem(item.getSlot(), itemStack);
                    }
                }

                player.openInventory(inventory);

            } catch (Exception e) {
                player.sendMessage("§c打开菜单时发生错误");
            }
        }, null);
    }

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
    public String getTitle() { return title; }
    public int getRows() { return rows; }
    public int getSize() { return size; }
    public List<MenuItem> getItems() { return new ArrayList<>(items); }

    public MenuItem getItemAtSlot(int slot) {
        for (MenuItem item : items) {
            if (item.getSlot() == slot) {
                return item;
            }
        }
        return null;
    }

    public String getRawTitle() {
        return title;
    }

    public static class MenuItem {
        private final int slot;
        private final String materialName;
        private final String displayName;
        private final List<String> lore;
        private final Map<String, List<String>> commands;

        public MenuItem(int slot, String materialName, String displayName, List<String> lore, Map<String, List<String>> commands) {
            this.slot = slot;
            this.materialName = materialName;
            this.displayName = displayName;
            this.lore = lore != null ? new ArrayList<>(lore) : new ArrayList<>();
            this.commands = commands != null ? new HashMap<>(commands) : new HashMap<>();
        }

        public ItemStack createItemStack(Player player) {
            try {
                Material material = Material.getMaterial(materialName.toUpperCase());
                if (material == null) {
                    material = Material.STONE;
                }

                ItemStack item = new ItemStack(material);
                ItemMeta meta = item.getItemMeta();

                if (meta != null) {
                    String parsedName = parsePlaceholders(player, displayName);
                    meta.setDisplayName(parsedName);

                    List<String> parsedLore = new ArrayList<>();
                    for (String line : lore) {
                        parsedLore.add(parsePlaceholders(player, line));
                    }
                    meta.setLore(parsedLore);

                    item.setItemMeta(meta);
                }

                return item;

            } catch (Exception e) {
                return new ItemStack(Material.STONE);
            }
        }

        private String parsePlaceholders(Player player, String text) {
            if (text == null) return "";
            text = text.replace('&', '§');

            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                try {
                    text = PlaceholderAPI.setPlaceholders(player, text);
                } catch (Exception e) {
                    // 静默处理错误
                }
            }

            return text;
        }

        // Getter 方法
        public int getSlot() { return slot; }
        public String getMaterialName() { return materialName; }
        public String getDisplayName() { return displayName; }
        public List<String> getLore() { return new ArrayList<>(lore); }
        public Map<String, List<String>> getCommands() { return new HashMap<>(commands); }

        public List<String> getActions(String clickType) {
            return commands.getOrDefault(clickType.toUpperCase(), new ArrayList<>());
        }

        public boolean hasActions(String clickType) {
            return commands.containsKey(clickType.toUpperCase()) &&
                    !commands.get(clickType.toUpperCase()).isEmpty();
        }

        public boolean hasAnyActions() {
            for (List<String> actionList : commands.values()) {
                if (!actionList.isEmpty()) {
                    return true;
                }
            }
            return false;
        }
    }

    public static class MenuHolder implements org.bukkit.inventory.InventoryHolder {
        private final JavaMenu menu;

        public MenuHolder(JavaMenu menu) {
            this.menu = menu;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }

        public JavaMenu getMenu() {
            return menu;
        }

        public String getMenuName() {
            return menu.getName();
        }
    }
}
