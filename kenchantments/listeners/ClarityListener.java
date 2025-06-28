package com.kenzo.kenchantments.listeners;

import com.kenzo.kenchantments.KEnchantmentsPlugin;
import com.kenzo.kenchantments.enchants.CustomEnchant;
import com.kenzo.kenchantments.managers.EnchantmentManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.inventory.ItemStack;

public class ClarityListener implements Listener {

    private final KEnchantmentsPlugin plugin;
    private final EnchantmentManager enchantManager;

    public ClarityListener(KEnchantmentsPlugin plugin) {
        this.plugin = plugin;
        this.enchantManager = plugin.getEnchantmentManager();
    }

    private boolean hasClarity(Player player) {
        ItemStack helmet = player.getInventory().getHelmet();
        if (helmet == null) return false;
        return enchantManager.hasEnchant(helmet, "clarity");
    }

    private void cleanse(Player player) {
        if (player.hasPotionEffect(PotionEffectType.BLINDNESS)) {
            player.removePotionEffect(PotionEffectType.BLINDNESS);
        }
        if (player.hasPotionEffect(PotionEffectType.WITHER)) {
            player.removePotionEffect(PotionEffectType.WITHER);
        }
    }

    // Cleanse on movement (runs every tick for active players)
    @EventHandler(priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (hasClarity(player)) {
            cleanse(player);
        }
    }

    // Cleanse on hotbar change (in case helmet is swapped)
    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        if (hasClarity(player)) {
            cleanse(player);
        }
    }
}

