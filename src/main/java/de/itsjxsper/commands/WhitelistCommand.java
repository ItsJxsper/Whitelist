package de.itsjxsper.commands;

import de.itsjxsper.discord.Colorize;
import de.itsjxsper.whitelist.Whitelist;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Optional;

public class WhitelistCommand implements CommandExecutor {
    private final Whitelist whitelist;

    public WhitelistCommand(Whitelist whitelist) {
        this.whitelist = whitelist;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!commandSender.hasPermission("whitelist.admin") || !commandSender.isOp()) return true;

        if (args.length == 0) {
            commandSender.sendMessage("/" + command.getName() + " <on|off|start|stop|reminder|clear>");
        }

        if (args[0].equals("on")) {
            try {
                whitelist.setWhitelistEnabled(true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            commandSender.sendMessage(Colorize.prefix() + Colorize.color("&aWhitelist wurde aktiviert!"));
        } else if (args[0].equals("off")) {
            try {
                whitelist.setWhitelistEnabled(false);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            commandSender.sendMessage(Colorize.prefix() + Colorize.color("&cWhitelist wurde deaktiviert!"));
        } else if (args[0].equals("start")) {
            TextChannel signupChannel = whitelist.getOrCreateChannel(whitelist.getWhitelistSignupChannelName());
            if (signupChannel != null && whitelist.getDiscordServer()!= null) {
                signupChannel.getManager().setTopic("Schreibe dein Minecraft-Benutzername hier! :watermelon:").complete();

                if (signupChannel.getPermissionOverride(whitelist.getDiscordServer().getPublicRole()) != null) {
                    signupChannel.getPermissionOverride(whitelist.getDiscordServer().getPublicRole())
                            .getManager()
                            .setAllowed(Permission.MESSAGE_SEND)
                            .complete();
                }
            } else {
                commandSender.sendMessage(Colorize.color(Colorize.prefix() + "&cFailed to create signup channel or server is null &7(" + signupChannel + ", " + whitelist.getDiscordServer() + ")"));
            }

            Role playtestingRole = whitelist.getOrCreatePlaytestingRole();
            TextChannel chatChannel = whitelist.getOrCreateChannel(whitelist.getWhitelistChatChannelName());
            if (chatChannel != null && playtestingRole != null && whitelist.getDiscordServer() != null) {
                if (chatChannel.getPermissionOverride(playtestingRole) != null) {
                    chatChannel.getPermissionOverride(playtestingRole)
                            .getManager()
                            .setAllowed(Permission.MESSAGE_SEND)
                            .complete();
                }

                if (chatChannel.getPermissionOverride(whitelist.getDiscordServer().getPublicRole()) != null) {
                    chatChannel.getPermissionOverride(whitelist.getDiscordServer().getPublicRole())
                            .getManager()
                            .complete();
                }
            } else {
                commandSender.sendMessage(Colorize.color(Colorize.prefix() + "&cFailed to create chat channel, playtesting role is null, or server is null &7(" + chatChannel + ", " + playtestingRole + ", " + whitelist.getDiscordServer() + ")"));
            }

            commandSender.sendMessage(Colorize.color(Colorize.prefix() + "&aOpened playtesting signup."));
        } else if (args[0].equalsIgnoreCase("close")) {
            TextChannel signupChannel = whitelist.getOrCreateChannel(whitelist.getWhitelistSignupChannelName());
            if (signupChannel != null) signupChannel.delete().complete();

            commandSender.sendMessage(Colorize.prefix() + Colorize.color("&cClosed playtesting signup."));
        } else if (args[0].equalsIgnoreCase("reminder")) {
            whitelist.sendPlaytestingReminder();
            commandSender.sendMessage(Colorize.prefix() + Colorize.color("&fSent playtesting reminder!"));
        } else if (args[0].equalsIgnoreCase("clear")) {
            Optional<TextChannel> optionalChannel = whitelist.getDiscordBot().getTextChannels().stream()
                    .filter(channel -> channel.getName().equalsIgnoreCase(whitelist.getWhitelistSignupChannelName())).findAny();
            if (!optionalChannel.isPresent()) {
                commandSender.sendMessage("Can't clear Discord channel- doesn't exist");
            } else {
                TextChannel channel = optionalChannel.get();
                channel.deleteMessages(channel.getHistory().retrievePast(100).complete())
                        .complete();
            }

            try {
                whitelist.clearIGNs();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            commandSender.sendMessage(Colorize.prefix() + Colorize.color("Cleared &7#" + whitelist.getWhitelistSignupChannelName() + " &fand reset ign list."));
        } else {
            commandSender.sendMessage(Colorize.prefix() + Colorize.color("&cInvalid argument " + args[0] + "."));
        }
        return true;
    }
}

