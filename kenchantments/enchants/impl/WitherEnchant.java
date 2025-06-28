package com.kenzo.kenchantments.enchants.impl;

import com.kenzo.kenchantments.enchants.CustomEnchant;
import com.kenzo.kenchantments.enums.EnchantmentRarity;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class WitherEnchant extends CustomEnchant {
    private final Random random = new Random();

    public WitherEnchant() {
        super("wither", "Wither", EnchantmentRarity.ELITE, 5);
    }

    @Override
    public List<Material> getApplicableItems() {
        return Arrays.asList(
                Material.DIAMOND_SWORD, Material.NETHERITE_SWORD, Material.IRON_SWORD,
                Material.GOLDEN_SWORD, Material.STONE_SWORD, Material.WOODEN_SWORD,
                Material.DIAMOND_AXE, Material.NETHERITE_AXE, Material.IRON_AXE,
                Material.GOLDEN_AXE, Material.STONE_AXE, Material.WOODEN_AXE
        );
    }

    @Override
    public String getDescription() {
        return "Chance to inflict wither on your opponent.";
    }

    @Override
    public void onAttack(Player attacker, Player target, double damage, int level) {
        double chance = 0.02 * level; // 25% at max level
        if (random.nextDouble() < chance) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 40 + (20 * level), 0, false, false));
        }
    }
}
