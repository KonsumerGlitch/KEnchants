package com.kenzo.kenchantments.enchants.impl;

import com.kenzo.kenchantments.enchants.CustomEnchant;
import com.kenzo.kenchantments.enums.EnchantmentRarity;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RageEnchant extends CustomEnchant {

    // Static maps to track rage stacks per player
    private static final ConcurrentHashMap<UUID, Integer> rageStacks = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Long> lastAttackTime = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, BukkitRunnable> rageDecayTasks = new ConcurrentHashMap<>();

    // Track which damage events have been processed to prevent duplicates
    private static final ConcurrentHashMap<UUID, Long> processedDamageEvents = new ConcurrentHashMap<>();

    public RageEnchant() {
        super("rage", "Rage", EnchantmentRarity.LEGENDARY, 6);
    }

    @Override
    public List<Material> getApplicableItems() {
        return Arrays.asList(
                // Swords
                Material.DIAMOND_SWORD, Material.NETHERITE_SWORD, Material.IRON_SWORD,
                Material.GOLDEN_SWORD, Material.STONE_SWORD, Material.WOODEN_SWORD,
                // Axes
                Material.DIAMOND_AXE, Material.NETHERITE_AXE, Material.IRON_AXE,
                Material.GOLDEN_AXE, Material.STONE_AXE, Material.WOODEN_AXE
        );
    }

    @Override
    public String getDescription() {
        return "Build rage stacks by attacking without taking damage. Max 15 stacks = 1.5x damage.";
    }

    @Override
    public void onAttack(Player attacker, Player target, double damage, int level) {
        // Generate a unique ID for this damage event to prevent duplicate processing
        UUID eventId = UUID.randomUUID();
        long now = System.currentTimeMillis();

        // Check if we've recently processed a similar event (within 50ms)
        if (isRecentlyProcessed(attacker.getUniqueId(), now)) {
            return;
        }

        // Mark this event as processed
        processedDamageEvents.put(attacker.getUniqueId(), now);

        // Process the rage stacks
        processRageAttack(attacker, target, damage, level);
    }

    private boolean isRecentlyProcessed(UUID playerId, long currentTime) {
        Long lastProcessed = processedDamageEvents.get(playerId);
        return lastProcessed != null && (currentTime - lastProcessed) < 50; // 50ms threshold
    }

    // Special method to handle mob attacks (called from event listener)
    public static void onMobAttack(Player attacker, LivingEntity mob, double damage, int level) {
        RageEnchant rage = new RageEnchant();

        // Check for duplicate processing
        long now = System.currentTimeMillis();
        if (rage.isRecentlyProcessed(attacker.getUniqueId(), now)) {
            return;
        }

        // Mark this event as processed
        processedDamageEvents.put(attacker.getUniqueId(), now);

        rage.processRageAttack(attacker, mob, damage, level);
    }

    private void processRageAttack(Player attacker, LivingEntity target, double damage, int level) {
        UUID attackerId = attacker.getUniqueId();
        long currentTime = System.currentTimeMillis();

        // Check if last attack was within 8 seconds (NERFED: reduced from 10 seconds)
        Long lastAttack = lastAttackTime.get(attackerId);
        boolean withinTimeLimit = lastAttack != null && (currentTime - lastAttack) <= 8000;

        // NERFED: Increment rage stacks (max 15, reduced from 20)
        int currentStacks = withinTimeLimit ? rageStacks.getOrDefault(attackerId, 0) : 0;
        // NERFED: Only gain 1 stack per hit regardless of enchant level
        currentStacks = Math.min(currentStacks + 1, 15);

        rageStacks.put(attackerId, currentStacks);
        lastAttackTime.put(attackerId, currentTime);

        // NERFED: Calculate damage multiplier (1.0 to 1.5 based on stacks, reduced from 2.0)
        double multiplier = 1.0 + (currentStacks * 0.033); // 3.3% per stack, max 50% = 1.5x damage

        // Send message every 5 stacks
        if (currentStacks % 5 == 0 && currentStacks > 0) {
            attacker.sendMessage(ChatColor.RED + "⚔ Rage Stacks: " + currentStacks + "/15 (" +
                    String.format("%.1f", multiplier) + "x damage)");
        }

        // NERFED: Schedule stack decay (faster decay at 6 seconds, reduced from 10)
        scheduleRageDecay(attackerId);
    }

    // Method called when player takes damage (to reset rage)
    public static void onPlayerTakeDamage(Player victim) {
        UUID victimId = victim.getUniqueId();
        if (rageStacks.containsKey(victimId)) {
            rageStacks.remove(victimId);
            lastAttackTime.remove(victimId);

            // Cancel existing decay task
            BukkitRunnable existingTask = rageDecayTasks.remove(victimId);
            if (existingTask != null) {
                existingTask.cancel();
            }
        }
    }

    private void scheduleRageDecay(UUID playerId) {
        // Cancel existing decay task
        BukkitRunnable existingTask = rageDecayTasks.remove(playerId);
        if (existingTask != null) {
            existingTask.cancel();
        }

        // Schedule new decay task
        BukkitRunnable decayTask = new BukkitRunnable() {
            @Override
            public void run() {
                rageStacks.remove(playerId);
                lastAttackTime.remove(playerId);
                rageDecayTasks.remove(playerId);

                Player player = Bukkit.getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    player.sendMessage(ChatColor.GRAY + "⚔ Rage stacks expired.");
                }
            }
        };

        org.bukkit.plugin.Plugin plugin = Bukkit.getPluginManager().getPlugin("KEnchantments");
        if (plugin != null) {
            // NERFED: Faster decay at 6 seconds (reduced from 10)
            decayTask.runTaskLater(plugin, 120L); // 6 seconds
            rageDecayTasks.put(playerId, decayTask);
        }
    }

    // Cleanup method for plugin disable
    public static void cleanup() {
        rageStacks.clear();
        lastAttackTime.clear();
        processedDamageEvents.clear();
        for (BukkitRunnable task : rageDecayTasks.values()) {
            task.cancel();
        }
        rageDecayTasks.clear();
    }

    // Getter for current rage stacks (for debugging)
    public static int getRageStacks(UUID playerId) {
        return rageStacks.getOrDefault(playerId, 0);
    }

    // NERFED: Getter for current damage multiplier (max 1.5x instead of 2.0x)
    public static double getDamageMultiplier(UUID playerId) {
        int stacks = getRageStacks(playerId);
        return 1.0 + (stacks * 0.033); // 3.3% per stack, max 50%
    }
}
