package com.kenzo.kenchantments.managers;

import com.kenzo.kenchantments.KEnchantmentsPlugin;
import com.kenzo.kenchantments.enchants.CustomEnchant;
import com.kenzo.kenchantments.enchants.impl.*;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class EnchantmentManager {

    private final KEnchantmentsPlugin plugin;
    private final Map<String, CustomEnchant> registeredEnchants = new LinkedHashMap<>();

    public EnchantmentManager(KEnchantmentsPlugin plugin) {
        this.plugin = plugin;
        registerEnchants();
    }

    private void registerEnchants() {
        // --- SWORDS ---
        register(new LifestealEnchant());
        register(new DoubleStrikeEnchant());
        register(new AssassinEnchant());
        register(new BlessedEnchant());
        register(new ThunderingBlowEnchant());
        register(new SilenceEnchant());
        register(new DisarmorEnchant());
        register(new RageEnchant());
        register(new SwordDanceEnchant());

        // --- AXES ---
        register(new InsanityEnchant());
        register(new BleedEnchant());
        register(new DisarmorEnchant()); // Also applies to axes
        register(new HexEnchant());      // Hex enchantment for axes

        // --- ARMOR ---
        register(new ObsidianShieldEnchant());
        register(new DodgeEnchant());
        register(new EnlightenedEnchant());
        register(new DrunkEnchant());
        register(new GearsEnchant());
        register(new HealthBoostEnchant());
        register(new TankEnchant());
        register(new AngelicEnchant());
        register(new CactusEnchant());
        register(new WitherEnchant());
        register(new ArmoredEnchant());
        register(new ClarityEnchant());
        register(new ImmortalEnchant());
        register(new DeathbringerEnchant());
        register(new ImplantsEnchant());
    }

    private void register(CustomEnchant enchant) {
        registeredEnchants.put(enchant.getName().toLowerCase(), enchant);
    }

    public CustomEnchant getEnchant(String name) {
        return registeredEnchants.get(name.toLowerCase());
    }

    public Collection<CustomEnchant> getAllEnchants() {
        return registeredEnchants.values();
    }

    public boolean hasEnchant(ItemStack item, String enchantName) {
        if (item == null || !item.hasItemMeta()) return false;
        NamespacedKey key = new NamespacedKey(plugin, "ke_" + enchantName.toLowerCase());
        return item.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.INTEGER);
    }

    public int getEnchantLevel(ItemStack item, String enchantName) {
        if (item == null || !item.hasItemMeta()) return 0;
        NamespacedKey key = new NamespacedKey(plugin, "ke_" + enchantName.toLowerCase());
        return item.getItemMeta().getPersistentDataContainer().getOrDefault(key, PersistentDataType.INTEGER, 0);
    }

    public Map<CustomEnchant, Integer> getItemEnchants(ItemStack item) {
        Map<CustomEnchant, Integer> enchants = new HashMap<>();
        if (item == null || !item.hasItemMeta()) return enchants;
        for (CustomEnchant enchant : registeredEnchants.values()) {
            int level = getEnchantLevel(item, enchant.getName());
            if (level > 0) enchants.put(enchant, level);
        }
        return enchants;
    }

    public int getCustomEnchantCount(ItemStack item) {
        return getItemEnchants(item).size();
    }

    public boolean canApplyEnchant(ItemStack item, CustomEnchant enchant) {
        if (item == null || enchant == null) return false;
        if (!enchant.getApplicableItems().contains(item.getType())) return false;
        if (hasEnchant(item, enchant.getName())) return false;
        return getCustomEnchantCount(item) < plugin.getConfig().getInt("max-enchants-per-item", 9);
    }

    public ItemStack createEnchantBook(CustomEnchant enchant, int level, int successRate, int destroyRate) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();
        meta.setDisplayName(enchant.getRarity().getColor() + enchant.getDisplayName() + " " + toRoman(level));
        List<String> lore = new ArrayList<>();
        lore.add("§7" + enchant.getDescription());
        lore.add("");
        lore.add("§7Max Level: §f" + enchant.getMaxLevel());
        lore.add("§7Rarity: " + enchant.getRarity().toString());
        lore.add("");
        lore.add("§aSuccess Rate: §f" + successRate + "%");
        lore.add("§cDestroy Rate: §f" + destroyRate + "%");
        lore.add("");
        lore.add("§eClick on an item to apply!");
        meta.setLore(lore);
        NamespacedKey enchantKey = new NamespacedKey(plugin, "ke_book_enchant");
        NamespacedKey levelKey = new NamespacedKey(plugin, "ke_book_level");
        NamespacedKey successKey = new NamespacedKey(plugin, "ke_book_success");
        NamespacedKey destroyKey = new NamespacedKey(plugin, "ke_book_destroy");
        meta.getPersistentDataContainer().set(enchantKey, PersistentDataType.STRING, enchant.getName());
        meta.getPersistentDataContainer().set(levelKey, PersistentDataType.INTEGER, level);
        meta.getPersistentDataContainer().set(successKey, PersistentDataType.INTEGER, successRate);
        meta.getPersistentDataContainer().set(destroyKey, PersistentDataType.INTEGER, destroyRate);
        book.setItemMeta(meta);
        return book;
    }

    public void reapplyArmorEnchantments(Player player) {
        for (ItemStack armorPiece : player.getInventory().getArmorContents()) {
            if (armorPiece != null) {
                Map<CustomEnchant, Integer> enchants = getItemEnchants(armorPiece);
                for (Map.Entry<CustomEnchant, Integer> entry : enchants.entrySet()) {
                    CustomEnchant enchant = entry.getKey();
                    int level = entry.getValue();
                    enchant.onEquip(player, level);
                }
            }
        }
    }

    private String toRoman(int number) {
        String[] romanNumerals = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return number > 0 && number <= 10 ? romanNumerals[number - 1] : String.valueOf(number);
    }
}
