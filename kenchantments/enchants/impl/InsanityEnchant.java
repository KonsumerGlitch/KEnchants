package com.kenzo.kenchantments.enchants.impl;

import com.kenzo.kenchantments.enchants.CustomEnchant;
import com.kenzo.kenchantments.enums.EnchantmentRarity;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;

public class InsanityEnchant extends CustomEnchant {

    public InsanityEnchant() {
        super("insanity", "Insanity", EnchantmentRarity.LEGENDARY, 8);
    }

    @Override
    public List<Material> getApplicableItems() {
        return Arrays.asList(
                Material.DIAMOND_AXE, Material.NETHERITE_AXE,
                Material.IRON_AXE, Material.GOLDEN_AXE,
                Material.STONE_AXE, Material.WOODEN_AXE
        );
    }

    @Override
    public String getDescription() {
        return "Multiplies damage against players wielding swords.";
    }

    @Override
    public void onAttack(Player attacker, Player target, double damage, int level) {
        ItemStack targetWeapon = target.getInventory().getItemInMainHand();
        if (!isSword(targetWeapon)) return;
        org.bukkit.plugin.Plugin kEnchantmentsPlugin = Bukkit.getPluginManager().getPlugin("KEnchantments");
        if (kEnchantmentsPlugin == null) return;
        double multiplier = 1.2 + (0.1 * level); // 1.2x to 2.0x
        new BukkitRunnable() {
            @Override
            public void run() {
                if (target.isOnline() && !target.isDead() && attacker.isOnline()) {
                    double additionalDamage = damage * (multiplier - 1.0);
                    target.damage(additionalDamage, attacker);
                }
            }
        }.runTaskLater(kEnchantmentsPlugin, 1L);
    }

    private boolean isSword(ItemStack item) {
        if (item == null) return false;
        Material type = item.getType();
        return type == Material.DIAMOND_SWORD || type == Material.NETHERITE_SWORD ||
                type == Material.IRON_SWORD || type == Material.GOLDEN_SWORD ||
                type == Material.STONE_SWORD || type == Material.WOODEN_SWORD;
    }
}
