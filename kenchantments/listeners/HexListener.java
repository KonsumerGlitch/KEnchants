package com.kenzo.kenchantments.listeners;

import com.kenzo.kenchantments.KEnchantmentsPlugin;
import com.kenzo.kenchantments.enchants.impl.HexEnchant;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class HexListener implements Listener {

    private final KEnchantmentsPlugin plugin;

    public HexListener(KEnchantmentsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Process outgoing damage for hexed players
        // This runs at LOW priority to ensure damage modifications are calculated first
        HexEnchant.processOutgoingDamage(event);
    }
}
