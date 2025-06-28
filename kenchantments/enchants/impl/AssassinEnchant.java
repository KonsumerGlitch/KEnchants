package com.kenzo.kenchantments.enchants.impl;

import com.kenzo.kenchantments.enchants.CustomEnchant;
import com.kenzo.kenchantments.enums.EnchantmentRarity;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;

public class AssassinEnchant extends CustomEnchant {

    public AssassinEnchant() {
        super("assassin", "Assassin", EnchantmentRarity.ULTIMATE, 5);
    }

    @Override
    public List<Material> getApplicableItems() {
        return Arrays.asList(
                Material.DIAMOND_SWORD, Material.NETHERITE_SWORD,
                Material.IRON_SWORD, Material.GOLDEN_SWORD,
                Material.STONE_SWORD, Material.WOODEN_SWORD
        );
    }

    @Override
    public String getDescription() {
        return "Distance-based damage: closer = more damage, farther = less damage";
    }

    @Override
    public void onAttack(Player attacker, Player target, double damage, int level) {
        // Calculate distance between attacker and target
        double distance = attacker.getLocation().distance(target.getLocation());

        // Get plugin instance safely
        org.bukkit.plugin.Plugin kEnchantmentsPlugin = Bukkit.getPluginManager().getPlugin("KEnchantments");
        if (kEnchantmentsPlugin == null) {
            return;
        }

        // Calculate damage multiplier based on distance
        double multiplier;
        if (distance <= 1.0) {
            // Very close range - maximum damage boost
            multiplier = 2.0 + (0.25 * (level / 5.0)); // Up to 1.25x at max level
        } else if (distance <= 2.0) {
            // Medium range - reduced bonus
            multiplier = 1.3 + (0.15 * (level / 5.0) * (2.0 - distance)); // Scaling down
        } else {
            // Far range - damage penalty
            multiplier = 0.75 - (0.05 * Math.min(distance - 2.0, 3.0)); // Minimum 0.6x damage
        }

        // Apply the damage modification
        if (multiplier != 1.0) {
            notifyDebugger(attacker, distance, multiplier, kEnchantmentsPlugin);

            // Schedule damage modification for next tick
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (target.isOnline() && !target.isDead() && attacker.isOnline()) {
                        double additionalDamage = damage * (multiplier - 1.0);
                        if (additionalDamage > 0) {
                            target.damage(additionalDamage, attacker);
                        } else if (additionalDamage < 0) {
                            // Heal some damage back to simulate reduced damage
                            double healBack = Math.abs(additionalDamage);
                            double newHealth = Math.min(target.getHealth() + healBack, target.getMaxHealth());
                            target.setHealth(newHealth);
                        }
                    }
                }
            }.runTaskLater(kEnchantmentsPlugin, 1L);
        }
    }

    private void notifyDebugger(Player attacker, double distance, double multiplier, org.bukkit.plugin.Plugin kEnchantmentsPlugin) {
        // Safely notify damage debugger if it exists
        try {
            org.bukkit.plugin.Plugin debuggerPlugin = Bukkit.getPluginManager().getPlugin("DamageDebugger");
            if (debuggerPlugin != null) {
                Class<?> trackerClass = Class.forName("Kenzo.damageDebugger.EnchantmentProcTracker");
                java.lang.reflect.Method addMethod = trackerClass.getMethod("addProccing", java.util.UUID.class, String.class);
                addMethod.invoke(null, attacker.getUniqueId(), "Assassin (PROC)");

                // Schedule removal of proc indicator
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        try {
                            java.lang.reflect.Method removeMethod = trackerClass.getMethod("removeProccing", java.util.UUID.class, String.class);
                            removeMethod.invoke(null, attacker.getUniqueId(), "Assassin (PROC)");
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
