package com.kenzo.kenchantments.enchants.impl;

import com.kenzo.kenchantments.enchants.CustomEnchant;
import com.kenzo.kenchantments.enums.EnchantmentRarity;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class HealthBoostEnchant extends CustomEnchant {

    public HealthBoostEnchant() {
        super("healthboost", "Health Boost", EnchantmentRarity.LEGENDARY, 3);
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
        return "For every level gain 2 extra hearts";
    }

    @Override
    public void onEquip(Player player, int level) {
        // Calculate total health boost from all currently equipped armor
        double totalHealthBoost = calculateTotalHealthBoost(player);
        AttributeInstance maxHealthAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttribute != null) {
            maxHealthAttribute.setBaseValue(20.0 + totalHealthBoost);
            if (player.getHealth() > maxHealthAttribute.getValue()) {
                player.setHealth(maxHealthAttribute.getValue());
            }
        }
    }

    @Override
    public void onUnequip(Player player) {
        // Recalculate total health boost from remaining armor
        double totalHealthBoost = calculateTotalHealthBoost(player);
        AttributeInstance maxHealthAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttribute != null) {
            maxHealthAttribute.setBaseValue(20.0 + totalHealthBoost);
            if (player.getHealth() > maxHealthAttribute.getValue()) {
                player.setHealth(maxHealthAttribute.getValue());
            }
        }
    }

    private double calculateTotalHealthBoost(Player player) {
        double totalBoost = 0.0;
        ItemStack[] armorContents = player.getInventory().getArmorContents();
        for (ItemStack armorPiece : armorContents) {
            if (armorPiece != null && armorPiece.hasItemMeta()) {
                int level = getHealthBoostLevel(armorPiece);
                if (level > 0) {
                    totalBoost += level * 4.0; // 4 health points per level (2 hearts)
                }
            }
        }
        return totalBoost;
    }

    private int getHealthBoostLevel(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0;
        try {
            return item.getItemMeta().getPersistentDataContainer().getOrDefault(
                    new org.bukkit.NamespacedKey("kenchantments", "ke_healthboost"),
                    org.bukkit.persistence.PersistentDataType.INTEGER, 0);
        } catch (Exception e) {
            return 0;
        }
    }
}
