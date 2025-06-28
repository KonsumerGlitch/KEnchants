package com.kenzo.kenchantments.managers;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SilenceManager {

    // Track silenced players
    private static final Set<UUID> silencedPlayers = ConcurrentHashMap.newKeySet();

    // Enchantments that are immune to silence
    private static final Set<String> IMMUNE_ENCHANTS = Set.of(
            "healthboost", "gears", "enlightened", "obsidianshield", "rage", "lifesteal", "assassin", "insanity", "doublestrike"
    );

    public static void addSilencedPlayer(UUID playerId) {
        silencedPlayers.add(playerId);
    }

    public static void removeSilencedPlayer(UUID playerId) {
        silencedPlayers.remove(playerId);
    }

    public static boolean isSilenced(UUID playerId) {
        return silencedPlayers.contains(playerId);
    }

    // Check if an enchant on armor should be silenced
    public static boolean isArmorEnchantSilenced(UUID playerId, String enchantName) {
        if (!isSilenced(playerId)) return false;
        return !IMMUNE_ENCHANTS.contains(enchantName.toLowerCase());
    }

    public static void cleanup() {
        silencedPlayers.clear();
    }
}
