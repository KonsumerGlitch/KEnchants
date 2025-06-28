package com.kenzo.kenchantments.listeners;

import com.kenzo.kenchantments.KEnchantmentsPlugin;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerRespawnListener implements Listener {

    private final KEnchantmentsPlugin plugin;

    public PlayerRespawnListener(KEnchantmentsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        // Check if player died (has the metadata)
        if (player.hasMetadata("kenchant_died")) {
            // Remove the metadata
            player.removeMetadata("kenchant_died", plugin);

            // Reset max health to default (20.0)
            AttributeInstance maxHealthAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (maxHealthAttribute != null) {
                maxHealthAttribute.setBaseValue(20.0);
            }

            // Schedule a task to reapply enchantments after respawn
            // This is necessary because armor is re-equipped after respawn
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isOnline()) {
                        plugin.getEnchantmentManager().reapplyArmorEnchantments(player);
                    }
                }
            }.runTaskLater(plugin, 1L);
        }
    }
}
