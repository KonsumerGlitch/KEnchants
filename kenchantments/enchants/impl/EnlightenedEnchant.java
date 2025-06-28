package com.kenzo.kenchantments.enchants.impl;

import com.kenzo.kenchantments.enchants.CustomEnchant;
import com.kenzo.kenchantments.enums.EnchantmentRarity;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class EnlightenedEnchant extends CustomEnchant {

    private final Random random = new Random();

    public EnlightenedEnchant() {
        super("enlightened", "Enlightened", EnchantmentRarity.LEGENDARY, 3);
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
        return "Chance to heal hearts while taking damage";
    }


    @Override
    public boolean onDefend(Player defender, Player attacker, double damage, int level) {
        // 15% base chance + 10% per level
        double chance = 0.04 + (0.03 * level);

        if (random.nextDouble() < chance) {
            // Get plugin instance safely
            org.bukkit.plugin.Plugin kEnchantmentsPlugin = Bukkit.getPluginManager().getPlugin("KEnchantments");
            if (kEnchantmentsPlugin == null) {
                defender.sendMessage("§cEnlightened failed: Plugin not found!");
                return false;
            }

            // Calculate heal amount (1-2 hearts based on level)
            double healAmount = 1.0 + (level * 0.5);

            // Notify debugger
            notifyDebugger(defender, healAmount, kEnchantmentsPlugin);

            // Schedule healing for next tick
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (defender.isOnline() && !defender.isDead()) {
                        // Get current and max health
                        double currentHealth = defender.getHealth();
                        double maxHealth = defender.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();

                        // Calculate new health (don't exceed max)
                        double newHealth = Math.min(currentHealth + healAmount, maxHealth);

                        // Apply healing if there's actually healing to be done
                        if (newHealth > currentHealth) {
                            // Create and call a custom health regain event
                            EntityRegainHealthEvent healEvent = new EntityRegainHealthEvent(
                                    defender, healAmount, EntityRegainHealthEvent.RegainReason.CUSTOM
                            );

                            // Call the event so other plugins can track it
                            Bukkit.getPluginManager().callEvent(healEvent);

                            if (!healEvent.isCancelled()) {
                                // Apply the actual healing
                                defender.setHealth(newHealth);
                            }
                        }
                    }
                }
            }.runTaskLater(kEnchantmentsPlugin, 1L);
        }

        return false; // Don't cancel damage
    }
    @Override
    public void onEquip(Player player, int level) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, 2, false, false));
    }

    private void notifyDebugger(Player defender, double healAmount, org.bukkit.plugin.Plugin kEnchantmentsPlugin) {
        // Safely notify damage debugger if it exists
        try {
            org.bukkit.plugin.Plugin debuggerPlugin = Bukkit.getPluginManager().getPlugin("DamageDebugger");
            if (debuggerPlugin != null) {
                Class<?> trackerClass = Class.forName("Kenzo.damageDebugger.EnchantmentProcTracker");
                java.lang.reflect.Method addMethod = trackerClass.getMethod("addProccing", java.util.UUID.class, String.class);
                addMethod.invoke(null, defender.getUniqueId(), "Enlightened (PROC)");

                // Schedule removal of proc indicator
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        try {
                            java.lang.reflect.Method removeMethod = trackerClass.getMethod("removeProccing", java.util.UUID.class, String.class);
                            removeMethod.invoke(null, defender.getUniqueId(), "Enlightened (PROC)");
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
