package com.kenzo.kenchantments.enchants.impl;

import com.kenzo.kenchantments.enchants.CustomEnchant;
import com.kenzo.kenchantments.enums.EnchantmentRarity;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BlessedEnchant extends CustomEnchant {

    private final Random random = new Random();

    public BlessedEnchant() {
        super("blessed", "Blessed", EnchantmentRarity.ULTIMATE, 3);
    }

    @Override
    public List<Material> getApplicableItems() {
        return Arrays.asList(
                Material.DIAMOND_SWORD, Material.NETHERITE_SWORD,
                Material.IRON_SWORD, Material.GOLDEN_SWORD,
                Material.STONE_SWORD, Material.WOODEN_SWORD,
                Material.DIAMOND_AXE, Material.NETHERITE_AXE,
                Material.IRON_AXE, Material.GOLDEN_AXE,
                Material.STONE_AXE, Material.WOODEN_AXE
        );
    }

    @Override
    public String getDescription() {
        return "Chance to cleanse all negative effects when damaging enemies";
    }

    @Override
    public void onAttack(Player attacker, Player target, double damage, int level) {
        // 15% base chance + 5% per level
        double chance = 0.02 + (0.01 * level);

        if (random.nextDouble() < chance) {
            // Get plugin instance safely
            org.bukkit.plugin.Plugin kEnchantmentsPlugin = Bukkit.getPluginManager().getPlugin("KEnchantments");
            if (kEnchantmentsPlugin == null) {
                attacker.sendMessage("§cBlessed failed: Plugin not found!");
                return;
            }

            // Notify debugger that Blessed actually procced (if available)
            notifyDebugger(attacker, kEnchantmentsPlugin);

            // Schedule cleansing for next tick to ensure proper execution
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (attacker.isOnline() && !attacker.isDead()) {
                        cleanseNegativeEffects(attacker);
                        attacker.sendMessage("§eBlessed! All negative effects cleansed!");
                    }
                }
            }.runTaskLater(kEnchantmentsPlugin, 1L);
        }
    }

    private void cleanseNegativeEffects(Player player) {
        // List of negative potion effects to remove
        PotionEffectType[] negativeEffects = {
                PotionEffectType.POISON,
                PotionEffectType.WEAKNESS,
                PotionEffectType.SLOW,
                PotionEffectType.SLOW_DIGGING,
                PotionEffectType.HARM,
                PotionEffectType.CONFUSION,
                PotionEffectType.BLINDNESS,
                PotionEffectType.HUNGER,
                PotionEffectType.WITHER,
                PotionEffectType.LEVITATION,
                PotionEffectType.UNLUCK,
                PotionEffectType.SLOW_FALLING,
                PotionEffectType.BAD_OMEN,
        };

        // Remove all negative effects
        for (PotionEffectType effect : negativeEffects) {
            if (player.hasPotionEffect(effect)) {
                player.removePotionEffect(effect);
            }
        }
    }

    private void notifyDebugger(Player attacker, org.bukkit.plugin.Plugin kEnchantmentsPlugin) {
        // Safely notify damage debugger if it exists
        try {
            org.bukkit.plugin.Plugin debuggerPlugin = Bukkit.getPluginManager().getPlugin("DamageDebugger");
            if (debuggerPlugin != null) {
                Class<?> trackerClass = Class.forName("Kenzo.damageDebugger.EnchantmentProcTracker");
                java.lang.reflect.Method addMethod = trackerClass.getMethod("addProccing", java.util.UUID.class, String.class);
                addMethod.invoke(null, attacker.getUniqueId(), "Blessed (PROC)");

                // Schedule removal of proc indicator
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        try {
                            java.lang.reflect.Method removeMethod = trackerClass.getMethod("removeProccing", java.util.UUID.class, String.class);
                            removeMethod.invoke(null, attacker.getUniqueId(), "Blessed (PROC)");
                        } catch (Exception e) {
                            // Silently ignore if debugger is not available
                        }
                    }
                }.runTaskLater(kEnchantmentsPlugin, 40L);
            }
        } catch (Exception e) {
            // Silently ignore if debugger is not available or fails
        }
    }
}
