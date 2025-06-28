package com.kenzo.kenchantments.enchants.impl;

import com.kenzo.kenchantments.enchants.CustomEnchant;
import com.kenzo.kenchantments.enums.EnchantmentRarity;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClarityEnchant extends CustomEnchant {

    private static final ConcurrentHashMap<UUID, BukkitRunnable> clarityTasks = new ConcurrentHashMap<>();

    public ClarityEnchant() {
        super("clarity", "Clarity", EnchantmentRarity.ULTIMATE, 1);
    }

    @Override
    public List<Material> getApplicableItems() {
        return Arrays.asList(
                Material.DIAMOND_HELMET, Material.NETHERITE_HELMET,
                Material.IRON_HELMET, Material.GOLDEN_HELMET,
                Material.LEATHER_HELMET, Material.CHAINMAIL_HELMET
        );
    }

    @Override
    public String getDescription() {
        return "Immune to blindness and wither.";
    }

    @Override
    public void onEquip(Player player, int level) {
        UUID playerId = player.getUniqueId();
        BukkitRunnable existingTask = clarityTasks.remove(playerId);
        if (existingTask != null) existingTask.cancel();

        // Cleanse immediately
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        player.removePotionEffect(PotionEffectType.WITHER);

        // Start a repeating task to continuously cleanse
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !hasClarity(player)) {
                    clarityTasks.remove(playerId);
                    this.cancel();
                    return;
                }
                if (player.hasPotionEffect(PotionEffectType.BLINDNESS)) {
                    player.removePotionEffect(PotionEffectType.BLINDNESS);
                }
                if (player.hasPotionEffect(PotionEffectType.WITHER)) {
                    player.removePotionEffect(PotionEffectType.WITHER);
                }
            }
        };

        org.bukkit.plugin.Plugin plugin = Bukkit.getPluginManager().getPlugin("KEnchantments");
        if (plugin != null) {
            task.runTaskTimer(plugin, 0L, 1L); // Every tick
            clarityTasks.put(playerId, task);
        }
    }

    @Override
    public void onUnequip(Player player) {
        UUID playerId = player.getUniqueId();
        BukkitRunnable existingTask = clarityTasks.remove(playerId);
        if (existingTask != null) existingTask.cancel();
    }

    private boolean hasClarity(Player player) {
        ItemStack helmet = player.getInventory().getHelmet();
        if (helmet == null) return false;
        try {
            org.bukkit.plugin.Plugin kEnchantmentsPlugin = Bukkit.getPluginManager().getPlugin("KEnchantments");
            if (kEnchantmentsPlugin != null) {
                java.lang.reflect.Method getManagerMethod = kEnchantmentsPlugin.getClass().getMethod("getEnchantmentManager");
                Object manager = getManagerMethod.invoke(kEnchantmentsPlugin);
                java.lang.reflect.Method hasEnchantMethod = manager.getClass().getMethod("hasEnchant", ItemStack.class, String.class);
                return (Boolean) hasEnchantMethod.invoke(manager, helmet, "clarity");
            }
        } catch (Exception e) {
            // Ignore
        }
        return false;
    }

    public static void cleanup() {
        for (BukkitRunnable task : clarityTasks.values()) {
            task.cancel();
        }
        clarityTasks.clear();
    }
}
