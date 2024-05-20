package de.itsjxsper.whitelist;

import de.itsjxsper.commands.WhitelistCommand;
import de.itsjxsper.config.Configuration;
import de.itsjxsper.discord.Bot;
import de.itsjxsper.discord.Colorize;
import de.itsjxsper.listener.PlayerConnectionListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class Whitelist extends JavaPlugin {

    private JDA discordBot;
    private Configuration ignFile;

    private Guild discordServer;
    private List<String> igns = new ArrayList<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        super.onEnable();

        saveConfig();

        String discordToken = getConfig().getString("discordToken");
        if (discordToken == null) {
            getServer().getPluginManager().disablePlugin(this);
            getLogger().severe("Discord Token not set!");
            return;
        }

        this.discordBot = JDABuilder.createDefault(discordToken).build();
        this.discordBot.addEventListener(new Bot(this));

        try {
            this.ignFile = new Configuration(this, "config.yml");
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }

        igns = this.ignFile.getYamlConfiguration().getStringList("igns");
        getCommand("whitelist").setExecutor(new WhitelistCommand(this));

        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);

    }

    @Override
    public void onDisable() {
        discordBot.shutdown();
    }

    public boolean isWhitelistEnabled() {
       return getConfig().getBoolean("whitelist-igns");
    }

    public void setWhitelistEnabled(boolean newValue) throws IOException {
        if (isWhitelistEnabled() == newValue) { return; }

        getConfig().set("whitelist-igns", newValue);
        saveConfig();

        if (!newValue) {
            igns.forEach(ing -> {
                Player player = getServer().getPlayer(ing);
                if (player!= null && !player.hasPermission("whitelist.bypass")) { player.kick(); }
            });

            clearIGNs();
        } else {
            sendPlaytestingReminder();
        }

        updateBotStatus();
    }

    public void updateBotStatus () {
        if (isWhitelistEnabled()) {
            discordBot.getPresence().setActivity(Activity.of(Activity.ActivityType.WATCHING, "Die Whitelist ist aktiv!"));
        } else {
            discordBot.getPresence().setActivity(Activity.of(Activity.ActivityType.WATCHING, "Die Whitelist ist deaktiviert!"));
        }
    }

    public void sendPlaytestingReminder() {
        TextChannel chatChannel = getOrCreateChannel(getWhitelistChatChannelName());
        if (chatChannel == null) return;
        chatChannel.sendMessage("Hello @here! Die server IP ist " + getServer().getIp() + ":" + getServer().getPort() + ". **Nur mit der von Ihnen angegebenen IGN können Sie sich mit dem Server verbinden.**")
                .complete();
    }

    public void registerIGN(String ign) throws IOException {
        if (igns.contains(ign)) return;

        igns.add(ign);
        ignFile.getYamlConfiguration().set("igns", igns);
        ignFile.save();
    }

    public boolean isWhitelisted(String name) {
        return igns.contains(name);
    }

    public void clearIGNs() throws IOException {
        igns.clear();
        ignFile.getYamlConfiguration().set("igns", new ArrayList<String>());
        ignFile.save();
    }

    public TextChannel getOrCreateChannel(String name) {
        List<TextChannel> matches = discordBot.getTextChannelsByName(name, true);
        TextChannel channel;

        if (matches.size() == 0) {
            List<Category> categories = discordBot.getCategoriesByName(getWhitelistCategoryName(), true);
            Category category;

            if (categories.size() == 0) {
                if (discordServer == null) {
                    return null;
                }

                category = discordServer.createCategory(getWhitelistCategoryName()).complete();
            } else {
                category = categories.get(0);
            }

            channel = category.createTextChannel(name).complete();
        } else {
            channel = matches.get(0);
        }

        return channel;
    }

    public Role getOrCreatePlaytestingRole() {
        return getOrCreateRole(getWhitelistRoleName(), "#0fd9b7");
    }

    public Role getOrCreateRole(String name, String hexColor) {
        if (discordServer == null) return null;
        List<Role> matches = discordServer.getRolesByName(name, true);

        return matches.size() <= 0 ? discordServer.createRole()
                .setName(name)
                .setColor(Color.decode(hexColor))
                .complete() :
                matches.get(0);
    }

    public String getWhitelistSignupChannelName() {
        return getConfigOptionOrDefault("whitelist-signup-channel-name", "whitelist-igns");
    }

    public String getWhitelistChatChannelName() {
        return getConfigOptionOrDefault("whitelist-chat-channel-name", "whitelist-chat");
    }

    public String getWhitelistCategoryName() {
        return getConfigOptionOrDefault("whitelist-category-name", "Survival");
    }


    private String getConfigOptionOrDefault(String key, String defaultValue) {
        String name = getConfig().getString(key);
        if (name == null) {
            return defaultValue;
        } else {
            return name;
        }
    }

    public String getDiscordServerName() {
        return getConfigOptionOrDefault("discord-server-name", "");
    }

    public String getWhitelistRoleName() {
        return getConfigOptionOrDefault("whitelist-role-name", "Minecraft verifiziert");
    }

    public boolean isRegistered(String ing) {
        return igns.contains(ing);
    }

    public JDA getDiscordBot() {
        return discordBot;
    }

    public Configuration getIgnFile() {
        return ignFile;
    }

    public Guild getDiscordServer() {
        return discordServer;
    }

    public void setDiscordServer(Guild discordServer) {
        this.discordServer = discordServer;
    }
}
