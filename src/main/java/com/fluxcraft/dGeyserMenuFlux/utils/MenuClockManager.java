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

    public ItemStack createMenuClock() {
        ItemStack clock = new ItemStack(Material.CLOCK);
        ItemMeta meta = clock.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§6§l菜单钟表 §7(右键打开)");
            List<String> lore = new ArrayList<>();
            lore.add("§7使用此钟表快速打开服务器菜单");
            lore.add("§7死亡不会掉落此物品");
            lore.add("§7不可堆叠");
            meta.setLore(lore);
            meta.getPersistentDataContainer().set(clockKey, PersistentDataType.STRING, CLOCK_ID);
            meta.setUnbreakable(true);
            clock.setItemMeta(meta);
        }

        return clock;
    }

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

    public boolean hasMenuClock(Player player) {
        PlayerInventory inventory = player.getInventory();

        for (ItemStack item : inventory.getContents()) {
            if (isMenuClock(item)) {
                return true;
            }
        }

        if (isMenuClock(inventory.getItemInOffHand())) {
            return true;
        }

        return false;
    }

    public void giveMenuClock(Player player) {
        player.getScheduler().run(plugin, task -> {
            if (!hasMenuClock(player)) {
                ItemStack clock = createMenuClock();
                player.getInventory().addItem(clock);
            }
        }, null);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // 延迟执行，确保玩家完全加载
        plugin.runDelayedGlobalTask(() -> {
            giveMenuClock(player);
        }, 20L);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.getDrops().removeIf(this::isMenuClock);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        plugin.runDelayedGlobalTask(() -> {
            giveMenuClock(player);
        }, 20L);
    }

    public void openMenuWithClock(Player player) {
        String defaultMenu = plugin.getConfig().getString("settings.default-menu", "main");

        if (plugin.isBedrockPlayer(player.getUniqueId())) {
            plugin.getBedrockMenuManager().openMenu(player, defaultMenu);
        } else {
            plugin.getJavaMenuManager().openMenu(player, defaultMenu);
        }
    }
}
