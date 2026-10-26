package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import com.example.eventcore.GameEvent;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LastManStandingEvent extends GameEvent {

    private final Set<UUID> alive = new HashSet<>();
    private final Set<UUID> dead = new HashSet<>();

    public LastManStandingEvent(EventCore plugin) {
        super(plugin, "last_man_standing", "Last Man Standing", Material.NETHERITE_SWORD);
    }

    @Override
    protected void onStart() {
        alive.clear();
        dead.clear();
        for (Player p : players()) alive.add(p.getUniqueId());
        say(alive.size() + " players enter. Last one alive wins!");
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getPlayer();
        if (!alive.remove(p.getUniqueId())) return;
        dead.add(p.getUniqueId());
        say(p.getName() + " is out! " + alive.size() + " left.");
        if (alive.size() <= 1) {
            Player winner = alive.stream().findFirst().map(Bukkit::getPlayer).orElse(null);
            say("WINNER: " + (winner != null ? winner.getName() : "nobody") + "!");
            Bukkit.getScheduler().runTaskLater(plugin, this::stop, 100L);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        if (dead.contains(e.getPlayer().getUniqueId()))
            Bukkit.getScheduler().runTaskLater(plugin, () -> e.getPlayer().setGameMode(GameMode.SPECTATOR), 1L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (dead.contains(e.getPlayer().getUniqueId())) e.getPlayer().setGameMode(GameMode.SPECTATOR);
    }

    @Override
    protected void onStop() {
        for (UUID u : dead) {
            Player p = Bukkit.getPlayer(u);
            if (p != null) p.setGameMode(GameMode.SURVIVAL);
        }
        alive.clear();
        dead.clear();
        say("Last Man Standing has ended.");
    }
}
