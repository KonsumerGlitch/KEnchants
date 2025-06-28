package com.kenzo.kenchantments.enchants.impl;

import com.kenzo.kenchantments.enchants.CustomEnchant;
import com.kenzo.kenchantments.enums.EnchantmentRarity;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HexEnchant extends CustomEnchant {

    private final Random random = new Random();

    // Track players affected by Hex
    private static final ConcurrentHashMap<UUID, HexEffect> hexedPlayers = new ConcurrentHashMap<>();

    public HexEnchant() {
        super("hex", "Hex", EnchantmentRarity.LEGENDARY, 6);
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
        return "Applies a hex to your target, reflecting their damage back to them";
    }

    @Override
    public void onAttack(Player attacker, Player target, double damage, int level) {
        // Calculate chance to apply Hex effect (2% base + 2% per level = 8% at max level)
        double chance = 0.02 + ((level - 1) * 0.02);

        if (random.nextDouble() < chance) {
            // Apply Hex effect to target
            UUID targetId = target.getUniqueId();

            // Calculate reflection percentage (15% base + 5% per level, max 30% at level 4)
            double reflectionPercentage = 0.15 + ((level - 1) * 0.05);

            // Remove any existing hex on this player
            HexEffect existingHex = hexedPlayers.remove(targetId);
            if (existingHex != null && existingHex.expirationTask != null) {
                existingHex.expirationTask.cancel();
            }

            // Get plugin instance
            org.bukkit.plugin.Plugin kEnchantmentsPlugin = Bukkit.getPluginManager().getPlugin("KEnchantments");
            if (kEnchantmentsPlugin == null) return;

            // Create expiration task
            BukkitRunnable expirationTask = new BukkitRunnable() {
                @Override
                public void run() {
                    hexedPlayers.remove(targetId);
                    Player hexedPlayer = Bukkit.getPlayer(targetId);
                    if (hexedPlayer != null && hexedPlayer.isOnline()) {
                        hexedPlayer.sendMessage(ChatColor.GREEN + "Hex has worn off.");
                    }
                }
            };

            // Create Hex effect with 10-second duration
            HexEffect hexEffect = new HexEffect(attacker.getUniqueId(), reflectionPercentage, expirationTask);
            hexedPlayers.put(targetId, hexEffect);

            // Schedule expiration
            expirationTask.runTaskLater(kEnchantmentsPlugin, 80L); // 10 seconds

            // Notify players
            target.sendMessage(ChatColor.DARK_PURPLE + "Hexed! Your damage will reflect back to you!");
            attacker.sendMessage(ChatColor.DARK_PURPLE + "Hexed!" + target.getName() + "!");

            // Notify damage debugger if available
            try {
                org.bukkit.plugin.Plugin debuggerPlugin = Bukkit.getPluginManager().getPlugin("DamageDebugger");
                if (debuggerPlugin != null) {
                    Class<?> trackerClass = Class.forName("Kenzo.damageDebugger.EnchantmentProcTracker");
                    java.lang.reflect.Method addMethod = trackerClass.getMethod("addProccing", java.util.UUID.class, String.class);
                    addMethod.invoke(null, attacker.getUniqueId(), "Hex (PROC)");

                    // Schedule removal of proc indicator
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            try {
                                java.lang.reflect.Method removeMethod = trackerClass.getMethod("removeProccing", java.util.UUID.class, String.class);
                                removeMethod.invoke(null, attacker.getUniqueId(), "Hex (PROC)");
                            } catch (Exception e) {
                                // Silently ignore if debugger is not available
                            }
                        }
                    }.runTaskLater(kEnchantmentsPlugin, 40L);
                }
            } catch (Exception e) {
                // Silently ignore if debugger is not available
            }
        }
    }

    // Method to check if a player is hexed and process damage reflection
    public static void processOutgoingDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;

        Player damager = (Player) event.getDamager();
        UUID damagerId = damager.getUniqueId();

        // Check if damager is hexed
        HexEffect hexEffect = hexedPlayers.get(damagerId);
        if (hexEffect != null) {
            // Calculate reflected damage
            double originalDamage = event.getFinalDamage();
            double reflectedDamage = originalDamage * hexEffect.reflectionPercentage;

            // Apply reflected damage to the hexed player
            damager.damage(reflectedDamage);

            // Send feedback message
            damager.sendMessage(ChatColor.DARK_PURPLE + "The hex reflects " +
                    String.format("%.1f", reflectedDamage) + " damage back to you!");
        }
    }

    // Check if a player is currently hexed
    public static boolean isHexed(UUID playerId) {
        return hexedPlayers.containsKey(playerId);
    }

    // Remove hex from a player (for silence or other effects)
    public static void removeHex(UUID playerId) {
        HexEffect hexEffect = hexedPlayers.remove(playerId);
        if (hexEffect != null && hexEffect.expirationTask != null) {
            hexEffect.expirationTask.cancel();
        }
    }

    // Cleanup method for plugin disable
    public static void cleanup() {
        for (HexEffect hexEffect : hexedPlayers.values()) {
            if (hexEffect.expirationTask != null) {
                hexEffect.expirationTask.cancel();
            }
        }
        hexedPlayers.clear();
    }

    // Inner class to store Hex effect data
    private static class HexEffect {
        private final UUID casterUUID;
        private final double reflectionPercentage;
        private final BukkitRunnable expirationTask;

        public HexEffect(UUID casterUUID, double reflectionPercentage, BukkitRunnable expirationTask) {
            this.casterUUID = casterUUID;
            this.reflectionPercentage = reflectionPercentage;
            this.expirationTask = expirationTask;
        }
    }
}
