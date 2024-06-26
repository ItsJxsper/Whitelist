package de.itsjxsper.discord;

import de.itsjxsper.whitelist.Whitelist;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static javax.swing.text.html.HTML.Tag.U;

public class Bot extends ListenerAdapter {
    private final Whitelist whitelist;
    private final List<Message> repliedTo = new ArrayList<>();

    public Bot(Whitelist whitelist) {
        this.whitelist = whitelist;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        super.onReady(event);

        List<Guild> guilds = whitelist.getDiscordBot().getGuildsByName(whitelist.getDiscordServerName(), true);
        if (guilds.size() == 0) {
            Bukkit.getLogger().info("The bot hasn't been added to your server- please visit https://discord.com/oauth2/authorize?client_id=753778272851984474&scope=bot&permissions=268446800 to add it. The bot will only work on the server you've setup in the config.yml file.");
        } else {
            whitelist.setDiscordServer(guilds.get(0));
        }
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        if (!event.getGuild().getName().equalsIgnoreCase(whitelist.getDiscordServerName())) {
            event.getGuild().leave().complete();
        } else {
            whitelist.setDiscordServer(event.getGuild());
        }
    }

    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        if (event.getGuild().getName().equalsIgnoreCase(whitelist.getDiscordServerName())) {
            whitelist.setDiscordServer(null); // server is gone
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        MessageChannel channel = event.getChannel();

        if (channel.getName().equalsIgnoreCase(whitelist.getWhitelistSignupChannelName()) &&
                !event.getAuthor().isBot() &&
                !repliedTo.contains(event.getMessage())) {
            String messageToSend;

            String message = event.getMessage().getContentRaw();
            boolean validIGN = message.length() <= 16 && message.split(" ").length == 1;

            if (!validIGN) {
                messageToSend = "Invalid Minecraft IGN. Try again in a bit.";
            } else {
                if (whitelist.isRegistered(message)) {
                    messageToSend = "You've already registered!";
                } else {
                    // Valid IGN
                    messageToSend = "You've registered for playtesting! Thanks. :watermelon:";
                    try {
                        whitelist.registerIGN(message);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            repliedTo.add(event.getMessage());

            channel.sendMessage(event.getMember().getAsMention() + ", " + messageToSend)
                    .queue();

            channel.addReactionById(event.getMessageId(), Emoji.fromUnicode("✅")).complete();

            Role playtestingRole = whitelist.getOrCreatePlaytestingRole();
            event.getGuild().addRoleToMember(event.getMember(), playtestingRole)
                    .queue();

            if (whitelist.isWhitelistEnabled() && validIGN) {
                // DM them the IP
                event.getAuthor().openPrivateChannel()
                        .complete()
                        .sendMessage(":watermelon: Playtesting has already started- please join us on the server: " + Bukkit.getServer().getIp() + ":" + Bukkit.getServer().getPort())
                        .complete();
            }
        }
    }
}
