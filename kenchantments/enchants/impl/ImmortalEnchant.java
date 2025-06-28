package com.kenzo.kenchantments.enchants.impl;

import com.kenzo.kenchantments.enchants.CustomEnchant;
import com.kenzo.kenchantments.enums.EnchantmentRarity;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class ImmortalEnchant extends CustomEnchant {

    public ImmortalEnchant() {
        super("immortal", "Immortal", EnchantmentRarity.ULTIMATE, 1);
    }

    @Override
    public List<Material> getApplicableItems() {
        // All armor pieces
        return Arrays.asList(
                Material.DIAMOND_HELMET, Material.NETHERITE_HELMET, Material.IRON_HELMET,
                Material.GOLDEN_HELMET, Material.LEATHER_HELMET, Material.CHAINMAIL_HELMET,
                Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE, Material.IRON_CHESTPLATE,
                Material.GOLDEN_CHESTPLATE, Material.LEATHER_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE,
                Material.DIAMOND_LEGGINGS, Material.NETHERITE_LEGGINGS, Material.IRON_LEGGINGS,
                Material.GOLDEN_LEGGINGS, Material.LEATHER_LEGGINGS, Material.CHAINMAIL_LEGGINGS,
                Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS, Material.IRON_BOOTS,
                Material.GOLDEN_BOOTS, Material.LEATHER_BOOTS, Material.CHAINMAIL_BOOTS
        );
    }

    @Override
    public String getDescription() {
        return "Prevents all equipped armor from taking durability damage";
    }
}
