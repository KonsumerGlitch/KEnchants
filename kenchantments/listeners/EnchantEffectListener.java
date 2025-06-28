package com.kenzo.kenchantments.listeners;

import com.kenzo.kenchantments.KEnchantmentsPlugin;
import com.kenzo.kenchantments.enchants.CustomEnchant;
import com.kenzo.kenchantments.enchants.impl.SwordDanceEnchant;  // ADD THIS IMPORT
import com.kenzo.kenchantments.managers.EnchantmentManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class EnchantEffectListener implements Listener {

    private final KEnchantmentsPlugin plugin;
    private final EnchantmentManager enchantManager;

    public EnchantEffectListener(KEnchantmentsPlugin plugin) {
        this.plugin = plugin;
        this.enchantManager = plugin.getEnchantmentManager();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player target)) return;

        ItemStack weapon = attacker.getInventory().getItemInMainHand();
        Map<CustomEnchant, Integer> weaponEnchants = enchantManager.getItemEnchants(weapon);

        // Get the initial damage value
        double initialDamage = event.getFinalDamage();
        double finalDamage = initialDamage;

        // Process weapon enchants BEFORE damage calculation
        for (Map.Entry<CustomEnchant, Integer> entry : weaponEnchants.entrySet()) {
            entry.getKey().onAttack(attacker, target, initialDamage, entry.getValue());
        }

        // Apply Sword Dance damage multiplier if active
        if (SwordDanceEnchant.hasActiveSwordDance(attacker.getUniqueId())) {
            double swordDanceMultiplier = SwordDanceEnchant.getDamageMultiplier(attacker.getUniqueId());
            finalDamage *= swordDanceMultiplier;
        }

        // Set the modified damage
        event.setDamage(finalDamage);

        // Process target's ALL armor enchants (not just chestplate)
        ItemStack[] armorContents = target.getInventory().getArmorContents();
        for (ItemStack armorPiece : armorContents) {
            if (armorPiece != null) {
                Map<CustomEnchant, Integer> armorEnchants = enchantManager.getItemEnchants(armorPiece);
                for (Map.Entry<CustomEnchant, Integer> entry : armorEnchants.entrySet()) {
                    if (entry.getKey().onDefend(target, attacker, event.getFinalDamage(), entry.getValue())) {
                        event.setCancelled(true);
                        return; // Exit early if damage was cancelled
                    }
                }
            }
        }
    }

    @EventHandler
    public void onArmorChange(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Check if armor slot was modified
        int slot = event.getSlot();
        if (slot >= 36 && slot <= 39) { // Armor slots
            // Schedule effect update for next tick
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                updatePlayerEffects(player);
            }, 1L);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getPlayerEffectManager().removeAllEffects(event.getPlayer());
    }

    private void updatePlayerEffects(Player player) {
        // Remove all current effects
        plugin.getPlayerEffectManager().removeAllEffects(player);

        // Apply new effects from current armor (all pieces)
        ItemStack[] armorContents = player.getInventory().getArmorContents();

        // Check each armor piece for enchantments
        for (ItemStack armorPiece : armorContents) {
            if (armorPiece != null) {
                Map<CustomEnchant, Integer> armorEnchants = enchantManager.getItemEnchants(armorPiece);
                plugin.getPlayerEffectManager().applyArmorEffects(player, armorEnchants);
            }
        }
    }
}
