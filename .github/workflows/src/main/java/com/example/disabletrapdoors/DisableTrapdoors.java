package com.example.disabletrapdoors;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;

public final class DisableTrapdoors extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {

    private String restrictedWorld;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadRestrictedWorld();
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("disabletrapdoors").setExecutor(this);
        getCommand("disabletrapdoors").setTabCompleter(this);
        getLogger().info("DisableTrapdoors enabled. Restricting world: " + restrictedWorld);
    }

    private List<String> restrictedWorlds;

    private void loadRestrictedWorld() {
        restrictedWorlds = getConfig().getStringList("restricted-worlds");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.LEFT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        if (!restrictedWorlds.contains(event.getClickedBlock().getWorld().getName())) return;
        if (event.getPlayer().hasPermission("disabletrapdoors.bypass")) return;
        if (event.getClickedBlock().getType().name().endsWith("_TRAPDOOR")) {
            event.setCancelled(true);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /" + label + " reload");
            return true;
        }
        if (!sender.hasPermission("disabletrapdoors.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to do that.");
            return true;
        }
        reloadConfig();
        loadRestrictedWorld();
        sender.sendMessage(ChatColor.GREEN + "[DisableTrapdoors] Config reloaded! Restricting world: "
                + ChatColor.AQUA + restrictedWorld);
        getLogger().info("Config reloaded by " + sender.getName() + ". Restricting world: " + restrictedWorld);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return Collections.singletonList("reload");
        }
        return Collections.emptyList();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.LEFT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        if (!event.getClickedBlock().getWorld().getName().equals(restrictedWorld)) return;
        if (event.getPlayer().hasPermission("disabletrapdoors.bypass")) return;
        if (event.getClickedBlock().getType().name().endsWith("_TRAPDOOR")) {
            event.setCancelled(true);
        }
    }
}