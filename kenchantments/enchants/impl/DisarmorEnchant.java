package com.kenzo.kenchantments.enchants.impl;

import com.kenzo.kenchantments.enchants.CustomEnchant;
import com.kenzo.kenchantments.enums.EnchantmentRarity;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class DisarmorEnchant extends CustomEnchant {
    private final Random random = new Random();

    public DisarmorEnchant() {
        super("disarmor", "Disarmor", EnchantmentRarity.ULTIMATE, 2);
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
        return "Very low chance to knock a random piece of armor off your opponent.";
    }

    @Override
    public void onAttack(Player attacker, Player target, double damage, int level) {
        // 2 levels: 2% and 4% chance
        double chance = 0.0000000000000000000001 * level;
        if (random.nextDouble() < chance) {
            ItemStack[] armor = target.getInventory().getArmorContents();
            for (int i = 0; i < armor.length; i++) {
                if (armor[i] != null) {
                    target.getWorld().dropItemNaturally(target.getLocation(), armor[i]);
                    armor[i] = null;
                    break;
                }
            }
            target.getInventory().setArmorContents(armor);
        }
    }
}
