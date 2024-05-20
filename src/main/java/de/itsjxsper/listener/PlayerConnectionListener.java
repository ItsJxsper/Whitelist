package de.itsjxsper.listener;

import de.itsjxsper.discord.Colorize;
import de.itsjxsper.whitelist.Whitelist;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class PlayerConnectionListener implements Listener {
    private final Whitelist whitelist;

    public PlayerConnectionListener(Whitelist whitelist) {
        this.whitelist = whitelist;
    }

    @EventHandler
    private void onLogin(AsyncPlayerPreLoginEvent event) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(event.getUniqueId());
        if (offlinePlayer.isOp()) return;

        if (!whitelist.isWhitelistEnabled()) {
            if (whitelist.isWhitelisted(event.getName())) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, Colorize.color("&cDie Whitelist in noch nicht offen! Der Whitelist-Bot wird Sie benachrichtigen, sobald es beginnt."));
            } else {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, Colorize.color("&cDie Whitelist in noch nicht offen! Tragt euch in die Whitelist ein, indem ihr in den Community-Discord (http://discord.themineattack.de) geht und euren IGN unter #whitelist-igns\" hinzufügt."));
            }
        }
    }
}
