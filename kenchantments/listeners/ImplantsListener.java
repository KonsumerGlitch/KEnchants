package com.kenzo.kenchantments.listeners;

import com.kenzo.kenchantments.KEnchantmentsPlugin;
import com.kenzo.kenchantments.enchants.impl.ImplantsEnchant;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class ImplantsListener implements Listener {

    private final KEnchantmentsPlugin plugin;

    public ImplantsListener(KEnchantmentsPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getLogger().info("[KEnchantments] ImplantsListener registered");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // Check if the player actually moved (not just looking around)
        // Use a very small threshold to detect even slight movements
        if (event.getFrom().distanceSquared(event.getTo()) > 0.001) {
            // Update movement tracking for Implants enchantment
        }
    }
}
