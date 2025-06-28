package com.kenzo.kenchantments.enchants;

import com.kenzo.kenchantments.enums.EnchantmentRarity;
import com.kenzo.kenchantments.managers.SilenceManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class CustomEnchant {

    protected final String name;
    protected final String displayName;
    protected final EnchantmentRarity rarity;
    protected final int maxLevel;

    public CustomEnchant(String name, String displayName, EnchantmentRarity rarity, int maxLevel) {
        this.name = name;
        this.displayName = displayName;
        this.rarity = rarity;
        this.maxLevel = maxLevel;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public EnchantmentRarity getRarity() {
        return rarity;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public abstract List<Material> getApplicableItems();
    public abstract String getDescription();

    // Check if armor enchantment is silenced
    protected boolean isArmorEnchantSilenced(Player player) {
        return SilenceManager.isArmorEnchantSilenced(player.getUniqueId(), this.name);
    }

    // For passive effects (like armor enchants)
    public void onEquip(Player player, int level) {}
    public void onUnequip(Player player) {}

    // For combat effects
    public void onAttack(Player attacker, Player target, double damage, int level) {}
    public boolean onDefend(Player defender, Player attacker, double damage, int level) {
        return false; // Return true if damage should be cancelled
    }
}
