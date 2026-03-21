package com.example.disabletrapdoors;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.List;

public final class DisableTrapdoors extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {

    private List<String> restrictedWorlds;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadRestrictedWorlds();
        getServer().getPluginManager().registerEvents(this, this);
        var cmd = getCommand("disabletrapdoors");
        if (cmd != null) {
            cmd.setExecutor(this);
            cmd.setTabCompleter(this);
        }
        getLogger().info("DisableTrapdoors enabled. Restricting worlds: " + restrictedWorlds);
    }

    private void loadRestrictedWorlds() {
        restrictedWorlds = getConfig().getStringList("restricted-worlds");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(@NotNull PlayerInteractEvent event) {
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
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NonNull [] args) {
        if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(Component.text("Usage: /" + label + " reload", NamedTextColor.YELLOW));
            return true;
        }
        if (!sender.hasPermission("disabletrapdoors.admin")) {
            sender.sendMessage(Component.text("You don't have permission to do that.", NamedTextColor.RED));
            return true;
        }
        reloadConfig();
        loadRestrictedWorlds();
        sender.sendMessage(Component.text("[DisableTrapdoors] Config reloaded! Restricting worlds: " + restrictedWorlds, NamedTextColor.GREEN));
        getLogger().info("Config reloaded by " + sender.getName() + ". Restricting worlds: " + restrictedWorlds);
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NonNull [] args) {
        if (args.length == 1) {
            return Collections.singletonList("reload");
        }
        return Collections.emptyList();
    }
}