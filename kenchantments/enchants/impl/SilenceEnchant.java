package com.kenzo.kenchantments.enchants.impl;

import com.kenzo.kenchantments.enchants.CustomEnchant;
import com.kenzo.kenchantments.enums.EnchantmentRarity;
import com.kenzo.kenchantments.managers.SilenceManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SilenceEnchant extends CustomEnchant {
    private final Random random = new Random();

    // Track silenced players and their tasks
    private static final ConcurrentHashMap<UUID, BukkitRunnable> silencedPlayers = new ConcurrentHashMap<>();

    public SilenceEnchant() {
        super("silence", "Silence", EnchantmentRarity.ULTIMATE, 4);
    }

    @Override
    public List<Material> getApplicableItems() {
        return Arrays.asList(
                Material.DIAMOND_SWORD, Material.NETHERITE_SWORD, Material.IRON_SWORD,
                Material.GOLDEN_SWORD, Material.STONE_SWORD, Material.WOODEN_SWORD,
                Material.DIAMOND_AXE, Material.NETHERITE_AXE,
                Material.IRON_AXE, Material.GOLDEN_AXE,
                Material.STONE_AXE, Material.WOODEN_AXE
        );
    }

    @Override
    public String getDescription() {
        return "Chance to silence enemy armor enchantments for 4 seconds.";
    }

    @Override
    public void onAttack(Player attacker, Player target, double damage, int level) {
        double chance = 0.03 * level; // 24% at max level
        if (random.nextDouble() < chance) {
            UUID targetId = target.getUniqueId();

            // Cancel any existing silence task
            BukkitRunnable existingTask = silencedPlayers.remove(targetId);
            if (existingTask != null) {
                existingTask.cancel();
            }

            // Add player to silenced list
            SilenceManager.addSilencedPlayer(targetId);

            // Send bold purple message
            target.sendMessage(ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "Silenced(5s)");

            // Get plugin instance
            org.bukkit.plugin.Plugin kEnchantmentsPlugin = Bukkit.getPluginManager().getPlugin("KEnchantments");
            if (kEnchantmentsPlugin == null) return;

            // Schedule removal of silence after 5 seconds
            BukkitRunnable silenceTask = new BukkitRunnable() {
                @Override
                public void run() {
                    SilenceManager.removeSilencedPlayer(targetId);
                    silencedPlayers.remove(targetId);

                    Player player = Bukkit.getPlayer(targetId);
                    if (player != null && player.isOnline()) {
                        player.sendMessage(ChatColor.GREEN + "Silence ended - Armor enchantments restored!");
                    }
                }
            };

            silenceTask.runTaskLater(kEnchantmentsPlugin, 70L); // 5 seconds
            silencedPlayers.put(targetId, silenceTask);
        }
    }

    // Cleanup method for plugin disable
    public static void cleanup() {
        for (BukkitRunnable task : silencedPlayers.values()) {
            task.cancel();
        }
        silencedPlayers.clear();
        SilenceManager.cleanup();
    }
}
