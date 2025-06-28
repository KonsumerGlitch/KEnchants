package com.kenzo.kenchantments.enchants.impl;

import com.kenzo.kenchantments.enchants.CustomEnchant;
import com.kenzo.kenchantments.enums.EnchantmentRarity;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;

public class GearsEnchant extends CustomEnchant {

    public GearsEnchant() {
        super("gears", "Gears", EnchantmentRarity.LEGENDARY, 3);
    }

    @Override
    public List<Material> getApplicableItems() {
        return Arrays.asList(
                Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS,
                Material.IRON_BOOTS, Material.GOLDEN_BOOTS,
                Material.LEATHER_BOOTS, Material.CHAINMAIL_BOOTS
        );
    }

    @Override
    public String getDescription() {
        return "Gain speed per level when equipped";
    }

    @Override
    public void onEquip(Player player, int level) {
        // Apply speed effect based on level (Speed I, II, or III)
        int speedLevel = level - 1; // Convert to 0-2 for potion effect amplifier
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, speedLevel, false, false));
    }

    @Override
    public void onUnequip(Player player) {
        player.removePotionEffect(PotionEffectType.SPEED);
    }
}
