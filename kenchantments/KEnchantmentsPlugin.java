package com.kenzo.kenchantments;

import com.kenzo.kenchantments.commands.KEnchantCommands;
import com.kenzo.kenchantments.enchants.impl.ImplantsEnchant;
import com.kenzo.kenchantments.enchants.impl.RageEnchant;
import com.kenzo.kenchantments.enchants.impl.SilenceEnchant;
import com.kenzo.kenchantments.listeners.*;
import com.kenzo.kenchantments.listeners.PlayerDeathListener;
import com.kenzo.kenchantments.listeners.PlayerRespawnListener;
import com.kenzo.kenchantments.managers.EnchantmentManager;
import com.kenzo.kenchantments.managers.PlayerEffectManager;
import org.bukkit.plugin.java.JavaPlugin;

public class KEnchantmentsPlugin extends JavaPlugin {

    private EnchantmentManager enchantmentManager;
    private PlayerEffectManager playerEffectManager;

    @Override
    public void onEnable() {
        // Initialize managers
        this.enchantmentManager = new EnchantmentManager(this);
        this.playerEffectManager = new PlayerEffectManager();

        // Register event listeners
        getServer().getPluginManager().registerEvents(new EnchantApplyListener(this), this);
        getServer().getPluginManager().registerEvents(new EnchantEffectListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);

        // Register equipment change listener
        getServer().getPluginManager().registerEvents(new EquipmentChangeListener(this), this);

        // Register Rage enchant listener with NORMAL priority to prevent hit duplication
        getServer().getPluginManager().registerEvents(new RageEnchantListener(this), this);

        // Register Immortal enchant listener
        getServer().getPluginManager().registerEvents(new ImmortalEnchantListener(this), this);

        // Register Clarity enchant listener
        getServer().getPluginManager().registerEvents(new ClarityListener(this), this);

        // NEW: Register Implants enchant listener
        getServer().getPluginManager().registerEvents(new ImplantsListener(this), this);

        // Register death and respawn listeners for HealthBoost fix
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerRespawnListener(this), this);

        // Register commands
        KEnchantCommands commandExecutor = new KEnchantCommands(this);
        getCommand("cebooks").setExecutor(commandExecutor);
        getCommand("kegen").setExecutor(commandExecutor);
        getCommand("kereload").setExecutor(commandExecutor);

        // Save default config
        saveDefaultConfig();

        getLogger().info("KEnchantments has been enabled!");
    }

    @Override
    public void onDisable() {
        // Clean up player effects to prevent memory leaks
        if (playerEffectManager != null) {
            playerEffectManager.cleanup();
        }

        // Clean up rage stacks to prevent memory leaks
        RageEnchant.cleanup();

        // Clean up silence effects
        SilenceEnchant.cleanup();

        // NEW: Clean up implants effects
        ImplantsEnchant.cleanup();

        getLogger().info("KEnchantments has been disabled!");
    }

    public EnchantmentManager getEnchantmentManager() {
        return enchantmentManager;
    }

    public PlayerEffectManager getPlayerEffectManager() {
        return playerEffectManager;
    }
}
