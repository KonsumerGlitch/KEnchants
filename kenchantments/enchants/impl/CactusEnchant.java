package com.kenzo.kenchantments.enchants.impl;

import com.kenzo.kenchantments.enchants.CustomEnchant;
import com.kenzo.kenchantments.enums.EnchantmentRarity;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class CactusEnchant extends CustomEnchant {
    private final Random random = new Random();

    public CactusEnchant() {
        super("cactus", "Cactus", EnchantmentRarity.ELITE, 2);
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
        return "Chance to deal thorns damage to attackers.";
    }

    @Override
    public boolean onDefend(Player defender, Player attacker, double damage, int level) {
        double chance = 0.09 * level;
        if (attacker != null && random.nextDouble() < chance) {
            attacker.damage(1.0 * level, defender); // 1 heart per level
        }
        return false;
    }
}
