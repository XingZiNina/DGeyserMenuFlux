package com.fluxcraft.dGeyserMenuFlux.utils;

import com.fluxcraft.dGeyserMenuFlux.DGeyserMenuFlux;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;

public class MenuClockManager implements Listener {
    private final DGeyserMenuFlux plugin;
    private final NamespacedKey clockKey;
    private final String CLOCK_ID = "menu_clock";

    public MenuClockManager(DGeyserMenuFlux plugin) {
        this.plugin = plugin;
        this.clockKey = new NamespacedKey(plugin, CLOCK_ID);
    }

    /**
     * 创建菜单钟表物品
     */
    public ItemStack createMenuClock() {
        ItemStack clock = new ItemStack(Material.CLOCK);
        ItemMeta meta = clock.getItemMeta();

        if (meta != null) {
            // 设置显示名称和描述
            meta.setDisplayName("§6§l菜单钟表 §7(右键打开)");
            List<String> lore = new ArrayList<>();
            lore.add("§7使用此钟表快速打开服务器菜单");
            lore.add("§7死亡不会掉落此物品");
            lore.add("§7不可堆叠");
            meta.setLore(lore);

            // 添加NBT标签标识
            meta.getPersistentDataContainer().set(clockKey, PersistentDataType.STRING, CLOCK_ID);

            // 设置不可破坏（视觉效果）
            meta.setUnbreakable(true);

            clock.setItemMeta(meta);
        }

        return clock;
    }

    /**
     * 检查物品是否是菜单钟表
     */
    public boolean isMenuClock(ItemStack item) {
        if (item == null || item.getType() != Material.CLOCK) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        return meta.getPersistentDataContainer().has(clockKey, PersistentDataType.STRING);
    }

    /**
     * 检查玩家是否拥有菜单钟表
     */
    public boolean hasMenuClock(Player player) {
        PlayerInventory inventory = player.getInventory();

        // 检查主背包
        for (ItemStack item : inventory.getContents()) {
            if (isMenuClock(item)) {
                return true;
            }
        }

        // 检查副手
        if (isMenuClock(inventory.getItemInOffHand())) {
            return true;
        }

        return false;
    }

    /**
     * 给予玩家菜单钟表
     */
    public void giveMenuClock(Player player) {
        if (!hasMenuClock(player)) {
            ItemStack clock = createMenuClock();
            player.getInventory().addItem(clock);
            plugin.getLogger().info("已为玩家 " + player.getName() + " 发放菜单钟表");
        }
    }

    /**
     * 玩家加入服务器事件
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // 延迟执行，确保玩家完全加载
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            giveMenuClock(player);
        }, 20L); // 1秒后执行
    }

    /**
     * 玩家死亡事件 - 防止钟表掉落
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        // 从掉落物中移除菜单钟表
        event.getDrops().removeIf(this::isMenuClock);
    }

    /**
     * 玩家重生事件 - 重新给予钟表
     */
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        // 延迟执行，确保玩家重生完成
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            giveMenuClock(player);
        }, 20L); // 1秒后执行
    }

    /**
     * 打开玩家菜单（通过钟表）
     */
    public void openMenuWithClock(Player player) {
        // 根据玩家类型打开对应菜单
        String defaultMenu = plugin.getConfig().getString("settings.default-menu", "main");

        if (plugin.isBedrockPlayer(player.getUniqueId())) {
            plugin.getBedrockMenuManager().openMenu(player, defaultMenu);
        } else {
            plugin.getJavaMenuManager().openMenu(player, defaultMenu);
        }
    }
}