package com.cragmax.regioninfo;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RegionInfoCommand implements CommandExecutor {

    private final RegionInfo plugin;

    public RegionInfoCommand(RegionInfo plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be used by players!",
                    NamedTextColor.RED));
            return true;
        }

        // SINGLE FOLIA SCHEDULER CALL
        // Schedule task to run on the player's region thread
        player.getScheduler().run(plugin, (task) -> {
            // Everything in here runs on the region thread that owns this player

            player.sendMessage(Component.text()
                    .append(Component.text("═══════════════════════════", NamedTextColor.DARK_GRAY))
                    .append(Component.text("\n"))
                    .append(Component.text("Region Information", NamedTextColor.AQUA, TextDecoration.BOLD))
                    .append(Component.text("\n"))
                    .append(Component.text("═══════════════════════════", NamedTextColor.DARK_GRAY))
            );

            var location = player.getLocation();
            String threadName = Thread.currentThread().getName();

            player.sendMessage(Component.text("\nRegion Thread: ", NamedTextColor.GRAY)
                    .append(Component.text(threadName, NamedTextColor.GREEN, TextDecoration.BOLD)));

            player.sendMessage(Component.text("Your Location: ", NamedTextColor.GRAY)
                    .append(Component.text(String.format("X: %.1f, Y: %.1f, Z: %.1f",
                            location.x(), location.y(), location.z()), NamedTextColor.WHITE)));

            player.sendMessage(Component.text("World: ", NamedTextColor.GRAY)
                    .append(Component.text(location.getWorld().getName(), NamedTextColor.WHITE)));

            player.sendMessage(Component.text("Chunk: ", NamedTextColor.GRAY)
                    .append(Component.text(String.format("%d, %d",
                            location.getChunk().getX(), location.getChunk().getZ()), NamedTextColor.WHITE)));

        }, () -> {});

        return true;
    }
}