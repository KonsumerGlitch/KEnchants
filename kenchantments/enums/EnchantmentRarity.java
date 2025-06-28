package com.kenzo.kenchantments.enums;

import org.bukkit.ChatColor;

public enum EnchantmentRarity {
    COMMON(ChatColor.GRAY, "Common"),
    UNCOMMON(ChatColor.GRAY, "Uncommon"),
    RARE(ChatColor.GREEN, "Rare"),
    ELITE(ChatColor.AQUA, "Elite"),
    ULTIMATE(ChatColor.YELLOW, "Ultimate"),
    LEGENDARY(ChatColor.GOLD, "Legendary");

    private final ChatColor color;
    private final String displayName;

    EnchantmentRarity(ChatColor color, String displayName) {
        this.color = color;
        this.displayName = displayName;
    }

    public ChatColor getColor() {
        return color;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return color + displayName;
    }
}
