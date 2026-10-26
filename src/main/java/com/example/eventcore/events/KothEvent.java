package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import com.example.eventcore.GameEvent;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class KothEvent extends GameEvent {

    private Location zone;
    private final Map<UUID, Integer> points = new HashMap<>();
    private BossBar bar;
    private boolean ended;

    public KothEvent(EventCore plugin) {
        super(plugin, "koth", "KOTH", Material.BEACON);
    }

    @Override
    public boolean usesLocation() { return true; }

    @Override
    protected void onStart() {
        ended = false;
        points.clear();
        zone = getLocation("location", spawnLocation());
        bar = BossBar.bossBar(Component.text("KOTH"), 0f, BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS);
        for (Player p : Bukkit.getOnlinePlayers()) p.showBossBar(bar);
        say("Capture the hill at " + zone.getBlockX() + ", " + zone.getBlockY() + ", " + zone.getBlockZ()
                + "! Stand in the zone ALONE to score. First to " + getInt("points-to-win", 60) + " wins.");
        runTimer(this::tick, 20L, 20L);
    }

    private boolean inZone(Player p) {
        if (!p.getWorld().equals(zone.getWorld())) return false;
        double r = getInt("radius", 6);
        double dx = p.getLocation().getX() - zone.getX(), dz = p.getLocation().getZ() - zone.getZ();
        return dx * dx + dz * dz <= r * r && Math.abs(p.getLocation().getY() - zone.getY()) <= 10;
    }

    private void tick() {
        if (ended) return;
        int goal = getInt("points-to-win", 60);

        // particle ring
        double r = getInt("radius", 6);
        for (int i = 0; i < 32; i++) {
            double a = Math.PI * 2 * i / 32;
            zone.getWorld().spawnParticle(Particle.END_ROD, zone.getX() + Math.cos(a) * r, zone.getY() + 1,
                    zone.getZ() + Math.sin(a) * r, 1, 0, 0, 0, 0);
        }

        List<Player> inside = new ArrayList<>();
        for (Player p : players()) if (inZone(p)) inside.add(p);

        if (inside.size() == 1) {
            Player p = inside.get(0);
            int pts = points.merge(p.getUniqueId(), 1, Integer::sum);
            p.sendActionBar(Component.text("Capturing... " + pts + "/" + goal));
            if (pts >= goal) {
                ended = true;
                say(p.getName() + " captured the hill and WINS!");
                giveItems(p, getStringList("reward"));
                Bukkit.getScheduler().runTaskLater(plugin, this::stop, 60L);
                return;
            }
        } else if (inside.size() > 1) {
            for (Player p : inside) p.sendActionBar(Component.text("Contested!"));
        }

        UUID leader = null;
        int best = 0;
        for (Map.Entry<UUID, Integer> en : points.entrySet()) {
            if (en.getValue() > best) { best = en.getValue(); leader = en.getKey(); }
        }
        if (leader != null) {
            bar.name(Component.text("KOTH - Leader: " + Bukkit.getOfflinePlayer(leader).getName() + " (" + best + "/" + goal + ")"));
            bar.progress(Math.min(1f, best / (float) goal));
        }
    }

    @Override
    protected void onStop() {
        if (bar != null) for (Player p : Bukkit.getOnlinePlayers()) p.hideBossBar(bar);
        points.clear();
        say("KOTH has ended.");
    }
}
