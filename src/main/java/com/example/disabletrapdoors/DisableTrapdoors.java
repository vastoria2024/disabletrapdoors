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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public final class DisableTrapdoors extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {

    private Set<String> restrictedWorlds;

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
        List<String> list = getConfig().getStringList("restricted-worlds");
        restrictedWorlds = new HashSet<>();
        for (String world : list) {
            if (world != null && !world.isBlank()) {
                restrictedWorlds.add(world.toLowerCase());
            }
        }
        if (restrictedWorlds.isEmpty()) {
            getLogger().warning("No worlds defined in restricted-worlds! Plugin will not block anything.");
        }
    }

    private boolean isRestrictedWorld(@NotNull String worldName) {
        return restrictedWorlds.contains(worldName.toLowerCase());
    }

    private boolean isTrapdoor(@NotNull String materialName) {
        return materialName.endsWith("_TRAPDOOR");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(@NotNull PlayerInteractEvent event) {
        if (event.getPlayer().hasPermission("disabletrapdoors.bypass")) return;
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.LEFT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        if (!isRestrictedWorld(event.getClickedBlock().getWorld().getName())) return;
        if (isTrapdoor(event.getClickedBlock().getType().name())) {
            event.setCancelled(true);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("disabletrapdoors.admin")) {
            sender.sendMessage(Component.text("You don't have permission to do that.", NamedTextColor.RED));
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage: /" + label + " <reload|list>", NamedTextColor.YELLOW));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "reload" -> {
                reloadConfig();
                loadRestrictedWorlds();
                sender.sendMessage(Component.text("[DisableTrapdoors] Config reloaded! Restricting worlds: " + restrictedWorlds, NamedTextColor.GREEN));
                getLogger().info("Config reloaded by " + sender.getName() + ". Restricting worlds: " + restrictedWorlds);
            }
            case "list" -> {
                if (restrictedWorlds.isEmpty()) {
                    sender.sendMessage(Component.text("[DisableTrapdoors] No worlds are currently restricted.", NamedTextColor.YELLOW));
                } else {
                    sender.sendMessage(Component.text("[DisableTrapdoors] Restricted worlds: " + restrictedWorlds, NamedTextColor.AQUA));
                }
            }
            default -> sender.sendMessage(Component.text("Usage: /" + label + " <reload|list>", NamedTextColor.YELLOW));
        }
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Stream.of("reload", "list")
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}