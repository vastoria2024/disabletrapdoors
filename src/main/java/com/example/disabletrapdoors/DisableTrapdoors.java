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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Override
    public void onDisable() {
        getLogger().info("DisableTrapdoors disabled.");
    }

    private void loadRestrictedWorlds() {
        restrictedWorlds = getConfig().getStringList("restricted-worlds");
        if (restrictedWorlds.isEmpty()) {
            getLogger().warning("No worlds defined in restricted-worlds! Plugin will not block anything.");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(@NotNull PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.LEFT_CLICK_BLOCK && action != Action.PHYSICAL) return;
        if (event.getClickedBlock() == null) return;
        if (!restrictedWorlds.contains(event.getClickedBlock().getWorld().getName())) return;
        if (event.getPlayer().hasPermission("disabletrapdoors.bypass")) return;
        if (event.getClickedBlock().getType().name().endsWith("_TRAPDOOR")) {
            event.setCancelled(true);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
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
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Stream.of("reload")
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}