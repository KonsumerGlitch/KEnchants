package com.kenzo.kenchantments.enchants.impl;

import com.kenzo.kenchantments.enchants.CustomEnchant;
import com.kenzo.kenchantments.enums.EnchantmentRarity;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class DodgeEnchant extends CustomEnchant {

    private final Random random = new Random();

    public DodgeEnchant() {
        super("dodge", "Dodge", EnchantmentRarity.ELITE, 3);
    }

    @Override
    public List<Material> getApplicableItems() {
        return Arrays.asList(
                Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE,
                Material.IRON_CHESTPLATE, Material.GOLDEN_CHESTPLATE,
                Material.LEATHER_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE
        );
    }

    @Override
    public String getDescription() {
        return "5% chance per level to completely avoid damage";
    }

    @Override
    public boolean onDefend(Player defender, Player attacker, double damage, int level) {
        double chance = 0.01 * level;
        if (random.nextDouble() < chance) {
            defender.sendMessage("§b⚡ §7Dodge activated! Avoided §c" + String.format("%.1f", damage) + " §7damage!");
            if (attacker != null) {
                attacker.sendMessage("§b⚡ §7Your attack was dodged!");
            }
            return true; // Cancel damage
        }
        return false;
    }
}
