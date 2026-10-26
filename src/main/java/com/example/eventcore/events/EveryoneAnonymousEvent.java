package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import com.example.eventcore.GameEvent;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class EveryoneAnonymousEvent extends GameEvent {

    private static final String TEAM = "ec_anon";

    public EveryoneAnonymousEvent(EventCore plugin) {
        super(plugin, "everyone_anonymous", "Everyone Anonymous", Material.PLAYER_HEAD);
    }

    private Scoreboard board() { return Bukkit.getScoreboardManager().getMainScoreboard(); }

    private Team team() {
        Team t = board().getTeam(TEAM);
        if (t == null) t = board().registerNewTeam(TEAM);
        t.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        return t;
    }

    private void hide(Player p) {
        team().addEntry(p.getName());
        p.playerListName(Component.text("Anonymous"));
    }

    @Override
    protected void onStart() {
        for (Player p : Bukkit.getOnlinePlayers()) hide(p);
        say("Nobody knows who anyone is...");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) { hide(e.getPlayer()); }

    @EventHandler
    public void onChat(AsyncChatEvent e) {
        e.renderer(ChatRenderer.viewerUnaware((source, name, message) ->
                Component.text("Anonymous", NamedTextColor.GRAY).append(Component.text(": ")).append(message)));
    }

    @Override
    protected void onStop() {
        Team t = board().getTeam(TEAM);
        if (t != null) t.unregister();
        for (Player p : Bukkit.getOnlinePlayers()) p.playerListName(null);
        say("Names are visible again.");
    }
}
