package com.kenzo.kenchantments.commands;

import com.kenzo.kenchantments.KEnchantmentsPlugin;
import com.kenzo.kenchantments.listeners.GUIListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KEnchantCommands implements CommandExecutor {

    private final KEnchantmentsPlugin plugin;
    private final GUIListener guiListener;

    public KEnchantCommands(KEnchantmentsPlugin plugin) {
        this.plugin = plugin;
        this.guiListener = new GUIListener(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("cebooks")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cOnly players can use this command!");
                return true;
            }
            guiListener.openBooksGUI(player);
            return true;
        }
        // ... handle other commands as before
        return false;
    }
}
