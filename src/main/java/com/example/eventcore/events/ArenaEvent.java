package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import com.example.eventcore.GameEvent;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Base for events that teleport players into an arena and must put them back afterwards. */
public abstract class ArenaEvent extends GameEvent {

    private final Map<UUID, Location> origin = new HashMap<>();
    private final Map<UUID, GameMode> originMode = new HashMap<>();

    protected ArenaEvent(EventCore plugin, String id, String name, Material icon) {
        super(plugin, id, name, icon);
    }

    protected void enter(Player p, Location to) {
        origin.putIfAbsent(p.getUniqueId(), p.getLocation());
        originMode.putIfAbsent(p.getUniqueId(), p.getGameMode());
        p.teleport(to);
    }

    protected void spectate(Player p, Location at) {
        origin.putIfAbsent(p.getUniqueId(), p.getLocation());
        originMode.putIfAbsent(p.getUniqueId(), p.getGameMode());
        p.setGameMode(GameMode.SPECTATOR);
        p.teleport(at);
    }

    protected void restoreAll() {
        for (Map.Entry<UUID, Location> en : origin.entrySet()) {
            Player p = Bukkit.getPlayer(en.getKey());
            if (p == null) continue;
            p.setGameMode(originMode.getOrDefault(en.getKey(), GameMode.SURVIVAL));
            p.teleport(en.getValue());
        }
        origin.clear();
        originMode.clear();
    }
}
