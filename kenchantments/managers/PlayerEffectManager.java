package com.kenzo.kenchantments.managers;

import com.kenzo.kenchantments.enchants.CustomEnchant;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerEffectManager {

    // Thread-safe map to track active effects per player
    private final Map<UUID, Map<String, CustomEnchant>> activeEffects = new ConcurrentHashMap<>();

    public void applyArmorEffects(Player player, Map<CustomEnchant, Integer> armorEnchants) {
        UUID playerId = player.getUniqueId();
        Map<String, CustomEnchant> currentEffects = activeEffects.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>());

        // Apply new effects
        for (Map.Entry<CustomEnchant, Integer> entry : armorEnchants.entrySet()) {
            CustomEnchant enchant = entry.getKey();
            int level = entry.getValue();

            // Check if the armor enchant is silenced before applying
            if (!SilenceManager.isArmorEnchantSilenced(playerId, enchant.getName()) &&
                    !currentEffects.containsKey(enchant.getName())) {
                enchant.onEquip(player, level);
                currentEffects.put(enchant.getName(), enchant);
            }
        }
    }

    public void removeArmorEffects(Player player, CustomEnchant enchant) {
        UUID playerId = player.getUniqueId();
        Map<String, CustomEnchant> currentEffects = activeEffects.get(playerId);

        if (currentEffects != null && currentEffects.containsKey(enchant.getName())) {
            // FIXED: Properly call onUnequip to remove potion effects
            enchant.onUnequip(player);
            currentEffects.remove(enchant.getName());
        }
    }

    public void removeAllEffects(Player player) {
        UUID playerId = player.getUniqueId();

        // Get current effects before clearing
        Map<String, CustomEnchant> currentEffects = activeEffects.get(playerId);
        if (currentEffects != null) {
            // Call onUnequip for all active enchantments
            for (CustomEnchant enchant : new ArrayList<>(currentEffects.values())) {
                // FIXED: Properly call onUnequip for each enchantment
                enchant.onUnequip(player);
            }

            // Reset player's max health to default when removing all effects
            try {
                org.bukkit.attribute.AttributeInstance maxHealthAttribute = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH);
                if (maxHealthAttribute != null) {
                    maxHealthAttribute.setBaseValue(20.0); // Reset to default
                    if (player.getHealth() > 20.0) {
                        player.setHealth(20.0);
                    }
                }
            } catch (Exception e) {
                // Silently handle any attribute errors
            }
        }

        activeEffects.remove(playerId);
    }

    public Set<String> getActiveEffects(Player player) {
        Map<String, CustomEnchant> effects = activeEffects.get(player.getUniqueId());
        return effects != null ? effects.keySet() : Collections.emptySet();
    }

    public boolean hasActiveEffect(Player player, String enchantName) {
        Map<String, CustomEnchant> effects = activeEffects.get(player.getUniqueId());
        return effects != null && effects.containsKey(enchantName);
    }

    public void cleanup() {
        activeEffects.clear();
    }
}
