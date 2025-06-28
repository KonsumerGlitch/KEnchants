package com.kenzo.kenchantments.enchants.impl;

import com.kenzo.kenchantments.enchants.CustomEnchant;
import com.kenzo.kenchantments.enums.EnchantmentRarity;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class DeathbringerEnchant extends CustomEnchant {

    private final Random random = new Random();

    public DeathbringerEnchant() {
        super("deathbringer", "Deathbringer", EnchantmentRarity.LEGENDARY, 3);
    }

    @Override
    public List<Material> getApplicableItems() {
        return Arrays.asList(
                Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE,
                Material.IRON_CHESTPLATE, Material.GOLDEN_CHESTPLATE,
                Material.LEATHER_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE,
                Material.DIAMOND_HELMET, Material.NETHERITE_HELMET,
                Material.IRON_HELMET, Material.GOLDEN_HELMET,
                Material.LEATHER_HELMET, Material.CHAINMAIL_HELMET,
                Material.DIAMOND_LEGGINGS, Material.NETHERITE_LEGGINGS,
                Material.IRON_LEGGINGS, Material.GOLDEN_LEGGINGS,
                Material.LEATHER_LEGGINGS, Material.CHAINMAIL_LEGGINGS,
                Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS,
                Material.IRON_BOOTS, Material.GOLDEN_BOOTS,
                Material.LEATHER_BOOTS, Material.CHAINMAIL_BOOTS
        );
    }

    @Override
    public String getDescription() {
        return "Chance to deal double damage when attacking";
    }

    @Override
    public void onEquip(Player player, int level) {
        // No passive effects needed - double damage is handled when attacking
    }

    @Override
    public void onUnequip(Player player) {
        // No cleanup needed
    }

    @Override
    public void onAttack(Player attacker, Player target, double damage, int level) {
        // Calculate chance based on level (8% base + 4% per level)
        double chance = 0.08 + (0.04 * level);

        if (random.nextDouble() < chance) {
            // Get plugin instance safely
            org.bukkit.plugin.Plugin kEnchantmentsPlugin = Bukkit.getPluginManager().getPlugin("KEnchantments");
            if (kEnchantmentsPlugin == null) {
                return;
            }

            // Notify debugger that Deathbringer procced (if available)
            notifyDebugger(attacker, kEnchantmentsPlugin);

            // Schedule additional damage for next tick (to double the damage)
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
                addMethod.invoke(null, attacker.getUniqueId(), "Deathbringer (PROC)");

                // Schedule removal of proc indicator
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        try {
                            java.lang.reflect.Method removeMethod = trackerClass.getMethod("removeProccing", java.util.UUID.class, String.class);
                            removeMethod.invoke(null, attacker.getUniqueId(), "Deathbringer (PROC)");
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
