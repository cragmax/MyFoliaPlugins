package com.glyn.myfirstplugin;

import org.bukkit.plugin.java.JavaPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class MyFirstPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        Component enableMessage = Component.text()
                .append(Component.text("[", NamedTextColor.DARK_GRAY))
                .append(Component.text("✓", NamedTextColor.GREEN, TextDecoration.BOLD))
                .append(Component.text("] ", NamedTextColor.DARK_GRAY))
                .append(Component.text("MyFirstPlugin ", NamedTextColor.AQUA))
                .append(Component.text("enabled!", NamedTextColor.GREEN))
                .build();

        getComponentLogger().info(enableMessage);
    }

    @Override
    public void onDisable() {
        Component disableMessage = Component.text()
                .append(Component.text("[", NamedTextColor.DARK_GRAY))
                .append(Component.text("✗", NamedTextColor.RED, TextDecoration.BOLD))
                .append(Component.text("] ", NamedTextColor.DARK_GRAY))
                .append(Component.text("MyFirstPlugin ", NamedTextColor.AQUA))
                .append(Component.text("disabled!", NamedTextColor.RED))
                .build();

        getComponentLogger().info(disableMessage);
    }
}