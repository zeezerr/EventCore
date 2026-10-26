package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import com.example.eventcore.GameEvent;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayerSwapEvent extends GameEvent {

    public PlayerSwapEvent(EventCore plugin) {
        super(plugin, "player_swap", "Player Swap", Material.ENDER_PEARL);
    }

    @Override
    protected void onStart() {
        long period = getInt("interval-seconds", 60) * 20L;
        runTimer(() -> {
            if (players().size() < 2) return;
            say("Swapping positions in 5 seconds!");
            runLater(this::swap, 100L);
        }, period, period);
    }

    private void swap() {
        List<Player> l = players();
        if (l.size() < 2) return;
        Collections.shuffle(l);
        List<Location> locs = new ArrayList<>();
        for (Player p : l) locs.add(p.getLocation());
        for (int i = 0; i < l.size(); i++) l.get(i).teleport(locs.get((i + 1) % l.size()));
        say("Positions swapped!");
    }

    @Override protected void onStop() { say("Player Swap has ended."); }
}
