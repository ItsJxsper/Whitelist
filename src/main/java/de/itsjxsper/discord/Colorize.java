package de.itsjxsper.discord;

import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.Nullable;

public class Colorize {
    public static String color(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static String prefix() {
        return color("&f&lPlaytesting &r&7> &r&f");
    }

}
