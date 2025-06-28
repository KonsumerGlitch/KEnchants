package com.kenzo.kenchantments.enchants.impl;

import com.kenzo.kenchantments.enchants.CustomEnchant;
import com.kenzo.kenchantments.enums.EnchantmentRarity;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class AngelicEnchant extends CustomEnchant {

    private final Random random = new Random();
    // Only proc when defender's health is at or below this fraction of max health
    private static final double LOW_HEALTH_THRESHOLD = 0.20; // 30% HP

    public AngelicEnchant() {
        super("angelic", "Angelic", EnchantmentRarity.RARE, 5);
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
        return "Small chance to gain Regeneration when taking damage at low HP";
    }

    @Override
    public boolean onDefend(Player defender, Player attacker, double damage, int level) {
        // Only trigger if defender is at or below LOW_HEALTH_THRESHOLD of max health
        double currentHealth = defender.getHealth();
        double maxHealth = defender.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        if (currentHealth / maxHealth > LOW_HEALTH_THRESHOLD) {
            return false;
        }

        // Chance scales with level: base 0.5% + 0.5% per level
        double chance = 0.005 + 0.005 * level;
        if (random.nextDouble() < chance) {
            // Apply Regeneration V (amplifier 4) for 3 seconds (60 ticks)
            defender.addPotionEffect(new PotionEffect(
                    PotionEffectType.REGENERATION,
                    60,  // 3 seconds = 60 ticks
                    4,   // Amplifier 4 = Regeneration V
                    false, // No ambient particles
                    false, // No particles
                    true   // Show icon
            ));
        }

        return false; // Do not cancel the damage
    }
}
