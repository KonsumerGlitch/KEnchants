package com.kenzo.kenchantments.listeners;

import com.kenzo.kenchantments.KEnchantmentsPlugin;
import com.kenzo.kenchantments.enchants.CustomEnchant;
import com.kenzo.kenchantments.enchants.impl.RageEnchant;
import com.kenzo.kenchantments.managers.EnchantmentManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;

public class RageEnchantListener implements Listener {

    private final KEnchantmentsPlugin plugin;
    private final EnchantmentManager enchantManager;

    public RageEnchantListener(KEnchantmentsPlugin plugin) {
        this.plugin = plugin;
        this.enchantManager = plugin.getEnchantmentManager();
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker) {
            ItemStack weapon = attacker.getInventory().getItemInMainHand();
            Map<CustomEnchant, Integer> weaponEnchants = enchantManager.getItemEnchants(weapon);

            for (Map.Entry<CustomEnchant, Integer> entry : weaponEnchants.entrySet()) {
                if (entry.getKey() instanceof RageEnchant) {
                    int level = entry.getValue();

                    // Apply the multiplier to the original damage
                    UUID attackerId = attacker.getUniqueId();
                    double multiplier = RageEnchant.getDamageMultiplier(attackerId);

                    // Apply the multiplier to the original damage
                    double originalDamage = event.getDamage();
                    double newDamage = originalDamage * multiplier;
                    event.setDamage(newDamage);

                    // Check if attacking a mob (not player)
                    if (event.getEntity() instanceof LivingEntity && !(event.getEntity() instanceof Player)) {
                        LivingEntity mob = (LivingEntity) event.getEntity();
                        RageEnchant.onMobAttack(attacker, mob, event.getDamage(), level);
                    }

                    break; // Only process once if multiple rage enchants exist
                }
            }
        }

        // Handle player taking damage (reset rage stacks)
        if (event.getEntity() instanceof Player victim) {
            RageEnchant.onPlayerTakeDamage(victim);
        }
    }
}
