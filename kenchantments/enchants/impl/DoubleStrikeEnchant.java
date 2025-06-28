package com.kenzo.kenchantments.enchants.impl;

import com.kenzo.kenchantments.enchants.CustomEnchant;
import com.kenzo.kenchantments.enums.EnchantmentRarity;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class DoubleStrikeEnchant extends CustomEnchant {

    private final Random random = new Random();

    public DoubleStrikeEnchant() {
        super("doublestrike", "Double Strike", EnchantmentRarity.ULTIMATE, 2);
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
        return "8% chance per level to strike twice";
    }

    @Override
    public void onAttack(Player attacker, Player target, double damage, int level) {
        double chance = 0.04 * level;
        if (random.nextDouble() < chance) {
            // Get plugin instance safely
            org.bukkit.plugin.Plugin kEnchantmentsPlugin = Bukkit.getPluginManager().getPlugin("KEnchantments");
            if (kEnchantmentsPlugin == null) {
                attacker.sendMessage("§cDouble Strike failed: Plugin not found!");
                return;
            }

            // Notify debugger that Double Strike actually procced (if available)
            notifyDebugger(attacker, kEnchantmentsPlugin);

            attacker.sendMessage("§e⚔ §7Double Strike activated!");

            // FIXED: Double the damage instead of applying a second hit
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (target.isOnline() && !target.isDead() && attacker.isOnline()) {
                        // Apply additional damage equal to the original damage (effectively doubling it)
                        target.damage(damage, attacker);
                    }
                }
            }.runTaskLater(kEnchantmentsPlugin, 1L);
        }
    }

    private void notifyDebugger(Player attacker, org.bukkit.plugin.Plugin kEnchantmentsPlugin) {
        // Safely notify damage debugger if it exists
        try {
            org.bukkit.plugin.Plugin debuggerPlugin = Bukkit.getPluginManager().getPlugin("DamageDebugger");
            if (debuggerPlugin != null) {
                Class<?> trackerClass = Class.forName("Kenzo.damageDebugger.EnchantmentProcTracker");
                java.lang.reflect.Method addMethod = trackerClass.getMethod("addProccing", java.util.UUID.class, String.class);
                addMethod.invoke(null, attacker.getUniqueId(), "Double Strike (PROC)");

                // Schedule removal of proc indicator
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        try {
                            java.lang.reflect.Method removeMethod = trackerClass.getMethod("removeProccing", java.util.UUID.class, String.class);
                            removeMethod.invoke(null, attacker.getUniqueId(), "Double Strike (PROC)");
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
