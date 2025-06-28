package com.kenzo.kenchantments.enchants.impl;

import com.kenzo.kenchantments.enchants.CustomEnchant;
import com.kenzo.kenchantments.enums.EnchantmentRarity;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ImplantsEnchant extends CustomEnchant {

    // Track players with Implants enchantment for hunger restoration
    private static final ConcurrentHashMap<UUID, BukkitRunnable> implantsTasks = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Long> lastMovement = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Integer> hungerTickCounters = new ConcurrentHashMap<>();

    public ImplantsEnchant() {
        super("implants", "Implants", EnchantmentRarity.ULTIMATE, 3);
    }

    @Override
    public List<Material> getApplicableItems() {
        return Arrays.asList(
                Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE, Material.IRON_CHESTPLATE,
                Material.GOLDEN_CHESTPLATE, Material.LEATHER_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE,
                Material.DIAMOND_HELMET, Material.NETHERITE_HELMET, Material.IRON_HELMET,
                Material.GOLDEN_HELMET, Material.LEATHER_HELMET, Material.CHAINMAIL_HELMET,
                Material.DIAMOND_LEGGINGS, Material.NETHERITE_LEGGINGS, Material.IRON_LEGGINGS,
                Material.GOLDEN_LEGGINGS, Material.LEATHER_LEGGINGS, Material.CHAINMAIL_LEGGINGS,
                Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS, Material.IRON_BOOTS,
                Material.GOLDEN_BOOTS, Material.LEATHER_BOOTS, Material.CHAINMAIL_BOOTS
        );
    }

    @Override
    public String getDescription() {
        return "Regain hunger while moving";
    }

    @Override
    public void onEquip(Player player, int level) {
        UUID playerId = player.getUniqueId();

        // Cancel any existing task
        BukkitRunnable existingTask = implantsTasks.remove(playerId);
        if (existingTask != null) {
            existingTask.cancel();
        }

        // Initialize hunger tick counter
        hungerTickCounters.put(playerId, 0);

        // Start hunger restoration task
        BukkitRunnable implantsTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    // Player is offline, cancel task
                    implantsTasks.remove(playerId);
                    hungerTickCounters.remove(playerId);
                    lastMovement.remove(playerId);
                    this.cancel();
                    return;
                }

                // Check if player still has the enchantment
                boolean hasEnchant = false;
                for (ItemStack armorPiece : player.getInventory().getArmorContents()) {
                    if (armorPiece != null && !armorPiece.getType().equals(Material.AIR)) {
                        try {
                            org.bukkit.plugin.Plugin kEnchantmentsPlugin = Bukkit.getPluginManager().getPlugin("KEnchantments");
                            if (kEnchantmentsPlugin != null) {
                                java.lang.reflect.Method getManagerMethod = kEnchantmentsPlugin.getClass().getMethod("getEnchantmentManager");
                                Object manager = getManagerMethod.invoke(kEnchantmentsPlugin);

                                java.lang.reflect.Method hasEnchantMethod = manager.getClass().getMethod("hasEnchant", ItemStack.class, String.class);
                                if ((Boolean) hasEnchantMethod.invoke(manager, armorPiece, "implants")) {
                                    hasEnchant = true;
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            // Log the error for debugging
                            Bukkit.getLogger().warning("[KEnchantments] Error checking for Implants enchantment: " + e.getMessage());
                        }
                    }
                }

                if (!hasEnchant) {
                    // Player no longer has the enchantment, cancel task
                    implantsTasks.remove(playerId);
                    hungerTickCounters.remove(playerId);
                    lastMovement.remove(playerId);
                    this.cancel();
                    return;
                }

                // Check if player is moving
                if (isPlayerMoving(playerId)) {
                    int currentHunger = player.getFoodLevel();
                    if (currentHunger < 20) {
                        // Get the current tick counter
                        int tickCounter = hungerTickCounters.getOrDefault(playerId, 0);

                        // Calculate tick interval based on level (Level 1: 60 ticks, Level 2: 40 ticks, Level 3: 20 ticks)
                        int tickInterval = Math.max(80 - (level * 20), 20);

                        // Increment counter
                        tickCounter++;

                        // Check if it's time to restore hunger
                        if (tickCounter >= tickInterval) {
                            // Restore hunger
                            player.setFoodLevel(Math.min(currentHunger + 1, 20));

                            // Also restore saturation slightly
                            player.setSaturation(Math.min(player.getSaturation() + 0.5f, 20.0f));

                            // Debug message
                            Bukkit.getLogger().info("[KEnchantments] Restored hunger for " + player.getName() + " (Implants level " + level + ")");

                            // Reset counter
                            tickCounter = 0;
                        }

                        // Update counter
                        hungerTickCounters.put(playerId, tickCounter);
                    }
                }
            }
        };

        org.bukkit.plugin.Plugin plugin = Bukkit.getPluginManager().getPlugin("KEnchantments");
        if (plugin != null) {
            implantsTask.runTaskTimer(plugin, 0L, 1L); // Run every tick
            implantsTasks.put(playerId, implantsTask);

            // Debug message
            Bukkit.getLogger().info("[KEnchantments] Started Implants task for " + player.getName() + " (level " + level + ")");
        } else {
            Bukkit.getLogger().warning("[KEnchantments] Failed to start Implants task: plugin not found");
        }
    }

    @Override
    public void onUnequip(Player player) {
        UUID playerId = player.getUniqueId();

        // Cancel the hunger restoration task
        BukkitRunnable existingTask = implantsTasks.remove(playerId);
        if (existingTask != null) {
            existingTask.cancel();
            Bukkit.getLogger().info("[KEnchantments] Cancelled Implants task for " + player.getName());
        }

        // Remove movement tracking
        lastMovement.remove(playerId);
        hungerTickCounters.remove(playerId);
    }

    private boolean isPlayerMoving(UUID playerId) {
        Long lastMove = lastMovement.get(playerId);
        if (lastMove == null) return false;

        // Consider player moving if they moved within the last 2 seconds
        boolean isMoving = (System.currentTimeMillis() - lastMove) <= 2000;

        // Debug logging
        if (isMoving) {
            Bukkit.getLogger().info("[KEnchantments] Player " + playerId + " is moving");
        }

        return isMoving;
    }

    // Cleanup method for plugin disable
    public static void cleanup() {
        for (BukkitRunnable task : implantsTasks.values()) {
            task.cancel();
        }
        implantsTasks.clear();
        lastMovement.clear();
        hungerTickCounters.clear();
        Bukkit.getLogger().info("[KEnchantments] Cleaned up all Implants tasks");
    }
}
