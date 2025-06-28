package com.kenzo.kenchantments.enchants.impl;

import com.kenzo.kenchantments.enchants.CustomEnchant;
import com.kenzo.kenchantments.enums.EnchantmentRarity;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;

public class LifestealEnchant extends CustomEnchant {

    public LifestealEnchant() {
        super("lifesteal", "Lifesteal", EnchantmentRarity.LEGENDARY, 5);
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
        return "Heal % of damage dealt per level";
    }

    @Override
    public void onAttack(Player attacker, Player target, double damage, int level) {
            double chance = 0.01 * level;
        double healAmount = damage * 0.6 * level;

        // Get plugin instance safely
        org.bukkit.plugin.Plugin kEnchantmentsPlugin = Bukkit.getPluginManager().getPlugin("KEnchantments");
        if (kEnchantmentsPlugin == null) {
            attacker.sendMessage("§cLifesteal failed: Plugin not found!");
            return;
        }

        // FIXED: Use proper healing method with health event
        new BukkitRunnable() {
            @Override
            public void run() {
                if (attacker.isOnline() && !attacker.isDead()) {
                    // Get current and max health
                    double currentHealth = attacker.getHealth();
                    double maxHealth = attacker.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();

                    // Calculate new health (don't exceed max)
                    double newHealth = Math.min(currentHealth + healAmount, maxHealth);

                    // Apply healing if there's actually healing to be done
                    if (newHealth > currentHealth) {
                        // Create and call a custom health regain event
                        EntityRegainHealthEvent healEvent = new EntityRegainHealthEvent(
                                attacker, healAmount, EntityRegainHealthEvent.RegainReason.CUSTOM
                        );

                        // Call the event so other plugins can track it
                        Bukkit.getPluginManager().callEvent(healEvent);

                        if (!healEvent.isCancelled()) {
                            // Apply the actual healing
                            attacker.setHealth(newHealth);

                            // Notify debugger that Lifesteal actually procced (if available)
                            notifyDebugger(attacker, healAmount, kEnchantmentsPlugin);

                            attacker.sendMessage("§c❤ §7Lifesteal healed you for §c" +
                                    String.format("%.1f", healAmount) + " §7hearts!");
                        }
                    }
                }
            }
        }.runTaskLater(kEnchantmentsPlugin, 1L);
    }

    private void notifyDebugger(Player attacker, double healAmount, org.bukkit.plugin.Plugin kEnchantmentsPlugin) {
        // Safely notify damage debugger if it exists
        try {
            org.bukkit.plugin.Plugin debuggerPlugin = Bukkit.getPluginManager().getPlugin("DamageDebugger");
            if (debuggerPlugin != null) {
                Class<?> trackerClass = Class.forName("Kenzo.damageDebugger.EnchantmentProcTracker");
                java.lang.reflect.Method addMethod = trackerClass.getMethod("addProccing", java.util.UUID.class, String.class);
                addMethod.invoke(null, attacker.getUniqueId(), "Lifesteal (PROC)");

                // Schedule removal of proc indicator
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        try {
                            java.lang.reflect.Method removeMethod = trackerClass.getMethod("removeProccing", java.util.UUID.class, String.class);
                            removeMethod.invoke(null, attacker.getUniqueId(), "Lifesteal (PROC)");
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
