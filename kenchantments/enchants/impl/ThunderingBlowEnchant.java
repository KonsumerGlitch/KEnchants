package com.kenzo.kenchantments.enchants.impl;

import com.kenzo.kenchantments.enchants.CustomEnchant;
import com.kenzo.kenchantments.enums.EnchantmentRarity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ThunderingBlowEnchant extends CustomEnchant {

    private final Random random = new Random();

    public ThunderingBlowEnchant() {
        super("thunderingblow", "Thundering Blow", EnchantmentRarity.COMMON, 3);
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
        return "Chance to strike lightning at the opponent";
    }

    @Override
    public void onAttack(Player attacker, Player target, double damage, int level) {
        // 10% base chance + 5% per level
        double chance = 0.03 + (0.02 * level);

        if (random.nextDouble() < chance) {
            // Get plugin instance safely
            org.bukkit.plugin.Plugin kEnchantmentsPlugin = Bukkit.getPluginManager().getPlugin("KEnchantments");
            if (kEnchantmentsPlugin == null) {
                attacker.sendMessage("§cThundering Blow failed: Plugin not found!");
                return;
            }

            // Notify debugger
            notifyDebugger(attacker, kEnchantmentsPlugin);

            attacker.sendMessage("§b⚡ §7Thundering Blow activated!");

            // Strike lightning at target's location
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (target.isOnline() && !target.isDead()) {
                        Location targetLoc = target.getLocation();
                        // Strike lightning but don't cause fire (cosmetic + damage)
                        target.getWorld().strikeLightning(targetLoc);

                        // Apply additional lightning damage (2 hearts + 0.5 per level)
                        double lightningDamage = 4.0 + (level * 1.0);
                        target.damage(lightningDamage, attacker);
                    }
                }
            }.runTaskLater(kEnchantmentsPlugin, 5L); // Small delay for dramatic effect
        }
    }

    private void notifyDebugger(Player attacker, org.bukkit.plugin.Plugin kEnchantmentsPlugin) {
        // Safely notify damage debugger if it exists
        try {
            org.bukkit.plugin.Plugin debuggerPlugin = Bukkit.getPluginManager().getPlugin("DamageDebugger");
            if (debuggerPlugin != null) {
                Class<?> trackerClass = Class.forName("Kenzo.damageDebugger.EnchantmentProcTracker");
                java.lang.reflect.Method addMethod = trackerClass.getMethod("addProccing", java.util.UUID.class, String.class);
                addMethod.invoke(null, attacker.getUniqueId(), "Thundering Blow (PROC)");

                // Schedule removal of proc indicator
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        try {
                            java.lang.reflect.Method removeMethod = trackerClass.getMethod("removeProccing", java.util.UUID.class, String.class);
                            removeMethod.invoke(null, attacker.getUniqueId(), "Thundering Blow (PROC)");
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
