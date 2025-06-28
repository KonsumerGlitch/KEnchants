package com.kenzo.kenchantments.listeners;

import com.kenzo.kenchantments.KEnchantmentsPlugin;
import com.kenzo.kenchantments.enchants.CustomEnchant;
import com.kenzo.kenchantments.managers.EnchantmentManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class ImmortalEnchantListener implements Listener {

    private final KEnchantmentsPlugin plugin;
    private final EnchantmentManager enchantManager;

    public ImmortalEnchantListener(KEnchantmentsPlugin plugin) {
        this.plugin = plugin;
        this.enchantManager = plugin.getEnchantmentManager();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemDamage(PlayerItemDamageEvent event) {
        Player player = event.getPlayer();
        ItemStack damagedItem = event.getItem();

        // Check if the damaged item has the Immortal enchantment
        if (enchantManager.hasEnchant(damagedItem, "immortal")) {
            event.setCancelled(true);
            return;
        }

        // Also check if player has Immortal on ANY equipped armor piece
        ItemStack[] armorContents = player.getInventory().getArmorContents();
        for (ItemStack armorPiece : armorContents) {
            if (armorPiece != null && enchantManager.hasEnchant(armorPiece, "immortal")) {
                // Check if the damaged item is armor
                if (isArmorPiece(damagedItem)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    private boolean isArmorPiece(ItemStack item) {
        if (item == null) return false;
        String itemName = item.getType().name();
        return itemName.contains("HELMET") || itemName.contains("CHESTPLATE") ||
                itemName.contains("LEGGINGS") || itemName.contains("BOOTS");
    }
}
