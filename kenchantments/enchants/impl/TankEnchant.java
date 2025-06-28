package com.kenzo.kenchantments.enchants.impl;

import com.kenzo.kenchantments.enchants.CustomEnchant;
import com.kenzo.kenchantments.enums.EnchantmentRarity;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class TankEnchant extends CustomEnchant {

    public TankEnchant() {
        super("tank", "Tank", EnchantmentRarity.ULTIMATE, 4);
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
        return "Reduces damage taken from axes per level";
    }

    @Override
    public boolean onDefend(Player defender, Player attacker, double damage, int level) {
        // Check if armor enchantment is silenced
        if (isArmorEnchantSilenced(defender)) return false;

        if (attacker != null) {
            ItemStack weapon = attacker.getInventory().getItemInMainHand();
            if (isAxe(weapon)) {
                // Reduce damage by 5% per level (max 20% at level 4)
                double reduction = damage * (0.05 * level);
                double newHealth = Math.min(defender.getHealth() + reduction, defender.getMaxHealth());
                defender.setHealth(newHealth);
            }
        }
        return false;
    }

    private boolean isAxe(ItemStack item) {
        if (item == null) return false;
        Material type = item.getType();
        return type == Material.DIAMOND_AXE || type == Material.NETHERITE_AXE ||
                type == Material.IRON_AXE || type == Material.GOLDEN_AXE ||
                type == Material.STONE_AXE || type == Material.WOODEN_AXE;
    }
}
