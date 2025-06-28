package com.kenzo.kenchantments.listeners;

import com.kenzo.kenchantments.KEnchantmentsPlugin;
import com.kenzo.kenchantments.enchants.CustomEnchant;
import com.kenzo.kenchantments.managers.EnchantmentManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class EquipmentChangeListener implements Listener {

    private final KEnchantmentsPlugin plugin;
    private final EnchantmentManager enchantManager;

    public EquipmentChangeListener(KEnchantmentsPlugin plugin) {
        this.plugin = plugin;
        this.enchantManager = plugin.getEnchantmentManager();
    }

    // Handle armor slot clicks (equipping/unequipping armor)
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Check if it's an armor slot (36-39 in player inventory)
        int slot = event.getSlot();
        boolean isArmorSlot = (slot >= 36 && slot <= 39) && event.getInventory().getType() == InventoryType.PLAYER;

        // FIXED: Also check if the clicked item has enchantments that need to be removed
        if (isArmorSlot || (event.getCurrentItem() != null && isArmorPiece(event.getCurrentItem()))) {
            // Get the item being removed (if any)
            ItemStack removedItem = isArmorSlot ? event.getCurrentItem() : null;

            // If an item is being removed, remove its effects
            if (removedItem != null && !removedItem.getType().isAir()) {
                Map<CustomEnchant, Integer> enchants = enchantManager.getItemEnchants(removedItem);
                for (CustomEnchant enchant : enchants.keySet()) {
                    plugin.getPlayerEffectManager().removeArmorEffects(player, enchant);
                }
            }

            // Schedule task to update effects after the click is processed
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                updatePlayerArmorEffects(player);
            }, 1L);
        }
    }

    // Handle dragging items to/from armor slots
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Check if any armor slots are affected by the drag
        boolean affectsArmor = event.getInventorySlots().stream()
                .anyMatch(slot -> slot >= 36 && slot <= 39);

        if (affectsArmor) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                updatePlayerArmorEffects(player);
            }, 1L);
        }
    }

    // Handle dropping items (including armor)
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack droppedItem = event.getItemDrop().getItemStack();

        // Check if the dropped item has custom enchantments
        Map<CustomEnchant, Integer> enchants = enchantManager.getItemEnchants(droppedItem);
        if (!enchants.isEmpty() && isArmorPiece(droppedItem)) {
            // Remove effects from the dropped armor piece
            for (Map.Entry<CustomEnchant, Integer> entry : enchants.entrySet()) {
                plugin.getPlayerEffectManager().removeArmorEffects(player, entry.getKey());
            }

            // Update remaining armor effects
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                updatePlayerArmorEffects(player);
            }, 1L);
        }
    }

    // Handle player disconnect (cleanup all effects)
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getPlayerEffectManager().removeAllEffects(event.getPlayer());
    }

    private void updatePlayerArmorEffects(Player player) {
        // Remove all current armor effects
        plugin.getPlayerEffectManager().removeAllEffects(player);

        // Reapply effects from currently equipped armor
        ItemStack[] armorContents = player.getInventory().getArmorContents();
        for (ItemStack armorPiece : armorContents) {
            if (armorPiece != null && !armorPiece.getType().isAir()) {
                Map<CustomEnchant, Integer> armorEnchants = enchantManager.getItemEnchants(armorPiece);
                plugin.getPlayerEffectManager().applyArmorEffects(player, armorEnchants);
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
