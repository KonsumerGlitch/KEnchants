package com.kenzo.kenchantments.enchants.impl;

import com.kenzo.kenchantments.enchants.CustomEnchant;
import com.kenzo.kenchantments.enums.EnchantmentRarity;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class BleedEnchant extends CustomEnchant {

    private final Random random = new Random();

    public BleedEnchant() {
        super("bleed", "Bleed", EnchantmentRarity.ULTIMATE, 6);
    }

    @Override
    public List<Material> getApplicableItems() {
        return Arrays.asList(
                Material.DIAMOND_AXE, Material.NETHERITE_AXE, Material.IRON_AXE,
                Material.GOLDEN_AXE, Material.STONE_AXE, Material.WOODEN_AXE
        );
    }

    @Override
    public String getDescription() {
        return "Chance to inflict bleed (damage over time). At max level, bleed lasts 5 seconds.";
    }

    @Override
    public void onAttack(Player attacker, Player target, double damage, int level) {
        // 5% chance per level, up to 30% at max
        double chance = 0.03 * level;
        if (random.nextDouble() < chance) {
            int durationTicks = 40 + (level - 1) * 19; // Level 1: 40, Level 6: 100
            int ticksPerHit = 20; // 1 second per tick
            double tickDamage = Math.max(1.0, damage * 0.15); // Each tick deals at least 0.5 hearts, or 15% of original hit

            // Notify damage debugger that Bleed procced
            try {
                Class<?> trackerClass = Class.forName("Kenzo.damageDebugger.EnchantmentProcTracker");
                java.lang.reflect.Method addMethod = trackerClass.getMethod("addProccing", java.util.UUID.class, String.class);
                addMethod.invoke(null, attacker.getUniqueId(), "Bleed (PROC)");

                // Schedule removal of proc indicator after 2 seconds
                org.bukkit.plugin.Plugin plugin = Bukkit.getPluginManager().getPlugin("KEnchantments");
                if (plugin != null) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            try {
                                java.lang.reflect.Method removeMethod = trackerClass.getMethod("removeProccing", java.util.UUID.class, String.class);
                                removeMethod.invoke(null, attacker.getUniqueId(), "Bleed (PROC)");
                            } catch (Exception e) {
                                // Silently ignore if debugger is not available
                            }
                        }
                    }.runTaskLater(plugin, 40L);
                }
            } catch (Exception e) {
                // Silently ignore if debugger is not available
            }

            // Start the bleed effect
            new BleedTask(target, durationTicks, ticksPerHit, tickDamage, attacker).runTaskTimer(
                    Bukkit.getPluginManager().getPlugin("KEnchantments"), 0L, ticksPerHit
            );
        }
    }

    private static class BleedTask extends BukkitRunnable {
        private final Player victim;
        private final Player attacker;
        private int ticksLeft;
        private final int ticksPerHit;
        private final double tickDamage;

        BleedTask(Player victim, int totalTicks, int ticksPerHit, double tickDamage, Player attacker) {
            this.victim = victim;
            this.attacker = attacker;
            this.ticksLeft = totalTicks;
            this.ticksPerHit = ticksPerHit;
            this.tickDamage = tickDamage;
        }

        @Override
        public void run() {
            if (victim == null || !victim.isOnline() || victim.isDead()) {
                cancel();
                return;
            }

            // Notify damage debugger that this is a bleed tick
            try {
                Class<?> trackerClass = Class.forName("Kenzo.damageDebugger.EnchantmentProcTracker");
                java.lang.reflect.Method addMethod = trackerClass.getMethod("addProccing", java.util.UUID.class, String.class);
                addMethod.invoke(null, victim.getUniqueId(), "Bleed (TICK)");
            } catch (Exception e) {
                // Silently ignore if debugger is not available
            }

            // Apply damage
            victim.damage(tickDamage);

            // Set metadata for particle detection
            org.bukkit.plugin.Plugin plugin = Bukkit.getPluginManager().getPlugin("KEnchantments");
            if (plugin != null) {
                victim.getWorld().setMetadata("bleed_particles_" + victim.getUniqueId(),
                        new FixedMetadataValue(plugin, true));
            }

            // Spawn redstone particles for 0.5 seconds (10 ticks)
            new BukkitRunnable() {
                int particleTicks = 0;
                @Override
                public void run() {
                    if (particleTicks >= 10 || !victim.isOnline() || victim.isDead()) {
                        // Remove metadata when particles are done
                        if (plugin != null) {
                            victim.getWorld().removeMetadata("bleed_particles_" + victim.getUniqueId(), plugin);
                        }

                        // Remove bleed tick proc
                        try {
                            Class<?> trackerClass = Class.forName("Kenzo.damageDebugger.EnchantmentProcTracker");
                            java.lang.reflect.Method removeMethod = trackerClass.getMethod("removeProccing", java.util.UUID.class, String.class);
                            removeMethod.invoke(null, victim.getUniqueId(), "Bleed (TICK)");
                        } catch (Exception e) {
                            // Silently ignore if debugger is not available
                        }

                        cancel();
                        return;
                    }
                    victim.getWorld().spawnParticle(
                            Particle.REDSTONE,
                            victim.getLocation().add(0, 1, 0),
                            24,
                            0.3, 0.5, 0.3,
                            new Particle.DustOptions(org.bukkit.Color.RED, 1.5f)
                    );
                    particleTicks++;
                }
            }.runTaskTimer(plugin, 0L, 1L);

            // Decrement ticks left and check for end
            ticksLeft -= ticksPerHit;
            if (ticksLeft <= 0) {
                cancel();
            }
        }
    }
}
