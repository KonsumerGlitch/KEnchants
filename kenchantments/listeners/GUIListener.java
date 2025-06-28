package com.kenzo.kenchantments.listeners;

import com.kenzo.kenchantments.KEnchantmentsPlugin;
import com.kenzo.kenchantments.enchants.CustomEnchant;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GUIListener implements Listener {

    private final KEnchantmentsPlugin plugin;
    private static final String GUI_TITLE = "§6Custom Enchant Books";

    public GUIListener(KEnchantmentsPlugin plugin) {
        this.plugin = plugin;
    }

    public void openBooksGUI(Player player) {
        List<CustomEnchant> enchants = new ArrayList<>(plugin.getEnchantmentManager().getAllEnchants());
        int size = Math.max(54, ((enchants.size() + 8) / 9) * 9); // Always at least 54 slots
        size = Math.min(54, size); // Cap at 54 (double chest)
        Inventory gui = Bukkit.createInventory(null, size, GUI_TITLE);

        for (int i = 0; i < enchants.size() && i < size; i++) {
            CustomEnchant enchant = enchants.get(i);
            ItemStack book = plugin.getEnchantmentManager().createEnchantBook(
                    enchant, enchant.getMaxLevel(), 100, 0
            );
            gui.setItem(i, book);
        }

        player.openInventory(gui);
    }

    @EventHandler
    public void onGUIClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;
        player.getInventory().addItem(clicked.clone());
        player.closeInventory();
    }
}
