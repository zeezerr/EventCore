package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import com.example.eventcore.GameEvent;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class MobsApocalypseEvent extends GameEvent {

    public MobsApocalypseEvent(EventCore plugin) {
        super(plugin, "mobs_apocalypse", "Mobs Apocalypse", Material.SKELETON_SKULL);
    }

    @Override
    protected void onStart() {
        say("The mobs are coming. Survive!");
        List<EntityType> types = new ArrayList<>();
        for (String s : getStringList("mobs")) {
            try { types.add(EntityType.valueOf(s.trim().toUpperCase())); } catch (IllegalArgumentException ignored) { }
        }
        if (types.isEmpty()) types.add(EntityType.ZOMBIE);
        int per = getInt("per-player", 3);
        long period = getInt("interval-seconds", 10) * 20L;

        runTimer(() -> {
            for (Player p : players()) {
                World w = p.getWorld();
                if (w.getEnvironment() != World.Environment.NORMAL) continue;
                for (int i = 0; i < per; i++) {
                    double angle = rnd().nextDouble() * Math.PI * 2;
                    double dist = 12 + rnd().nextDouble() * 8;
                    int x = (int) (p.getLocation().getX() + Math.cos(angle) * dist);
                    int z = (int) (p.getLocation().getZ() + Math.sin(angle) * dist);
                    int y = w.getHighestBlockYAt(x, z) + 1;
                    w.spawnEntity(new Location(w, x + 0.5, y, z + 0.5), types.get(rnd().nextInt(types.size())));
                }
            }
        }, 40L, period);
    }

    @Override
    protected void onStop() { say("The apocalypse is over."); }
}
