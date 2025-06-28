package com.kenzo.kenchantments.enchants.impl;

import com.kenzo.kenchantments.enchants.CustomEnchant;
import com.kenzo.kenchantments.enums.EnchantmentRarity;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;

public class DrunkEnchant extends CustomEnchant {

    public DrunkEnchant() {
        super("drunk", "Drunk", EnchantmentRarity.LEGENDARY, 3);
    }

    @Override
    public List<Material> getApplicableItems() {
        return Arrays.asList(
                Material.DIAMOND_HELMET, Material.NETHERITE_HELMET, Material.IRON_HELMET,
                Material.GOLDEN_HELMET, Material.LEATHER_HELMET, Material.CHAINMAIL_HELMET
        );
    }

    @Override
    public String getDescription() {
        return "Gives strength but applies mining fatigue and slowness";
    }

    @Override
    public void onEquip(Player player, int level) {
        // Apply strength effect (level increases with enchantment level)
        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, level - 1, false, false, true));

        // Apply negative effects (level increases with enchantment level)
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, Math.min(level + 1, 4), false, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, Integer.MAX_VALUE, Math.min(level - 1, 2), false, false, true));
    }

    @Override
    public void onUnequip(Player player) {
        // FIXED: Remove all potion effects when unequipped
        player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
        player.removePotionEffect(PotionEffectType.SLOW);
        player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
    }
}
