package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import com.example.eventcore.GameEvent;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;

import java.util.ArrayList;
import java.util.List;

public class HostileAnimalsEvent extends GameEvent {

    private final List<Entity> spawned = new ArrayList<>();

    public HostileAnimalsEvent(EventCore plugin) {
        super(plugin, "hostile_animals", "Hostile Animals", Material.BONE);
    }

    @Override
    protected void onStart() {
        say("The animals have turned on you!");
        List<EntityType> types = new ArrayList<>();
        for (String s : getStringList("animals")) {
            try { types.add(EntityType.valueOf(s.trim().toUpperCase())); } catch (IllegalArgumentException ignored) { }
        }
        if (types.isEmpty()) types.add(EntityType.WOLF);
        int per = getInt("per-player", 2), cap = getInt("max-alive", 40);
        long period = getInt("interval-seconds", 15) * 20L;

        runTimer(() -> {
            spawned.removeIf(en -> !en.isValid());
            for (Player p : players()) {
                World w = p.getWorld();
                if (w.getEnvironment() != World.Environment.NORMAL) continue;
                for (int i = 0; i < per && spawned.size() < cap; i++) {
                    double a = rnd().nextDouble() * Math.PI * 2, d = 10 + rnd().nextDouble() * 8;
                    int x = (int) (p.getLocation().getX() + Math.cos(a) * d);
                    int z = (int) (p.getLocation().getZ() + Math.sin(a) * d);
                    Entity en = w.spawnEntity(new Location(w, x + 0.5, w.getHighestBlockYAt(x, z) + 1, z + 0.5),
                            types.get(rnd().nextInt(types.size())));
                    if (en instanceof Wolf wolf) wolf.setAngry(true);
                    if (en instanceof Mob m) m.setTarget(p);
                    spawned.add(en);
                }
            }
        }, 40L, period);
    }

    @Override
    protected void onStop() {
        spawned.forEach(Entity::remove);
        spawned.clear();
        say("The animals calmed down.");
    }
}
