package com.kenzo.kenchantments.listeners;

import com.kenzo.kenchantments.KEnchantmentsPlugin;
import com.kenzo.kenchantments.enchants.CustomEnchant;
import com.kenzo.kenchantments.managers.EnchantmentManager;
import com.kenzo.kenchantments.utils.RomanNumerals;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EnchantApplyListener implements Listener {

    private final KEnchantmentsPlugin plugin;
    private final EnchantmentManager enchantManager;
    private final Random random = new Random();

    public EnchantApplyListener(KEnchantmentsPlugin plugin) {
        this.plugin = plugin;
        this.enchantManager = plugin.getEnchantmentManager();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack cursor = event.getCursor();
        ItemStack clicked = event.getCurrentItem();

        if (cursor == null || clicked == null) return;
        if (cursor.getType() != Material.ENCHANTED_BOOK) return;
        if (!isEnchantBook(cursor)) return;

        event.setCancelled(true);

        // Get book data
        String enchantName = getBookEnchant(cursor);
        int level = getBookLevel(cursor);
        int successRate = getBookSuccessRate(cursor);
        int destroyRate = getBookDestroyRate(cursor);

        CustomEnchant enchant = enchantManager.getEnchant(enchantName);
        if (enchant == null) {
            player.sendMessage("§cInvalid enchantment book!");
            return;
        }

        // Check if enchant can be applied
        if (!enchantManager.canApplyEnchant(clicked, enchant)) {
            if (!enchant.getApplicableItems().contains(clicked.getType())) {
                player.sendMessage("§c" + enchant.getDisplayName() + " cannot be applied to this item!");
            } else if (enchantManager.hasEnchant(clicked, enchant.getName())) {
                player.sendMessage("§cThis item already has " + enchant.getDisplayName() + "!");
            } else {
                player.sendMessage("§cThis item has too many custom enchantments! (Max: " +
                        plugin.getConfig().getInt("max-enchants-per-item", 9) + ")");
            }
            return;
        }

        // Apply success/destroy logic
        int roll = random.nextInt(100) + 1;

        if (roll <= successRate) {
            // Success - apply enchant
            applyEnchant(clicked, enchant, level);
            player.sendMessage("§a✓ Successfully applied " + enchant.getRarity().getColor() +
                    enchant.getDisplayName() + " " + RomanNumerals.toRoman(level) + "§a!");
        } else if (roll <= successRate + destroyRate) {
            // Destroy item
            clicked.setType(Material.AIR);
            player.sendMessage("§c✗ Enchantment failed and destroyed the item!");
        } else {
            // Fail but don't destroy
            player.sendMessage("§e⚠ Enchantment failed but the item was saved!");
        }

        // Remove book from cursor
        cursor.setAmount(cursor.getAmount() - 1);
        if (cursor.getAmount() <= 0) {
            player.setItemOnCursor(null);
        }
    }

    private boolean isEnchantBook(ItemStack book) {
        if (!book.hasItemMeta()) return false;
        NamespacedKey key = new NamespacedKey(plugin, "ke_book_enchant");
        return book.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.STRING);
    }

    private String getBookEnchant(ItemStack book) {
        NamespacedKey key = new NamespacedKey(plugin, "ke_book_enchant");
        return book.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
    }

    private int getBookLevel(ItemStack book) {
        NamespacedKey key = new NamespacedKey(plugin, "ke_book_level");
        return book.getItemMeta().getPersistentDataContainer().getOrDefault(key, PersistentDataType.INTEGER, 1);
    }

    private int getBookSuccessRate(ItemStack book) {
        NamespacedKey key = new NamespacedKey(plugin, "ke_book_success");
        return book.getItemMeta().getPersistentDataContainer().getOrDefault(key, PersistentDataType.INTEGER, 100);
    }

    private int getBookDestroyRate(ItemStack book) {
        NamespacedKey key = new NamespacedKey(plugin, "ke_book_destroy");
        return book.getItemMeta().getPersistentDataContainer().getOrDefault(key, PersistentDataType.INTEGER, 0);
    }

    private void applyEnchant(ItemStack item, CustomEnchant enchant, int level) {
        ItemMeta meta = item.getItemMeta();

        // Store enchant data
        NamespacedKey key = new NamespacedKey(plugin, "ke_" + enchant.getName().toLowerCase());
        meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, level);

        // Update lore
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.add(enchant.getRarity().getColor() + enchant.getDisplayName() + " " + RomanNumerals.toRoman(level));
        meta.setLore(lore);

        item.setItemMeta(meta);
    }
}
