package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import com.example.eventcore.GameEvent;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class FreezePlayerEvent extends GameEvent {

    private final Set<UUID> frozen = new HashSet<>();

    public FreezePlayerEvent(EventCore plugin) {
        super(plugin, "freeze_player", "Freeze Player", Material.BLUE_ICE);
    }

    @Override
    protected void onStart() {
        long period = getInt("interval-seconds", 45) * 20L;
        runTimer(() -> {
            List<Player> ps = players();
            if (ps.isEmpty()) return;
            Player p = ps.get(rnd().nextInt(ps.size()));
            int secs = getInt("freeze-seconds", 8);
            frozen.add(p.getUniqueId());
            p.setFreezeTicks(secs * 20);
            say(p.getName() + " is frozen for " + secs + "s!");
            runLater(() -> frozen.remove(p.getUniqueId()), secs * 20L);
        }, period, period);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (!frozen.contains(e.getPlayer().getUniqueId()) || e.getTo() == null) return;
        if (e.getFrom().getX() != e.getTo().getX() || e.getFrom().getZ() != e.getTo().getZ()) {
            e.setTo(e.getFrom());
        }
    }

    @Override
    protected void onStop() {
        frozen.clear();
        say("Freeze Player has ended.");
    }
}
