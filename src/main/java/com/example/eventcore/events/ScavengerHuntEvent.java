package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import com.example.eventcore.GameEvent;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ScavengerHuntEvent extends GameEvent {

    private final List<Material> targets = new ArrayList<>();
    private final Map<UUID, Set<Material>> found = new HashMap<>();
    private boolean ended;

    public ScavengerHuntEvent(EventCore plugin) {
        super(plugin, "scavenger_hunt", "Scavenger Hunt", Material.COMPASS);
    }

    @Override
    protected void onStart() {
        ended = false; targets.clear(); found.clear();
        List<Material> pool = new ArrayList<>(materialSet("pool"));
        if (pool.isEmpty()) pool.add(Material.DIAMOND);
        Collections.shuffle(pool);
        int count = Math.min(getInt("count", 5), pool.size());
        targets.addAll(pool.subList(0, count));
        StringBuilder sb = new StringBuilder();
        for (Material m : targets) sb.append(pretty(m)).append(", ");
        say("Collect: " + sb.substring(0, sb.length() - 2) + ". First to have them all wins!");
        runTimer(this::check, 20L, 20L);
    }

    private void check() {
        if (ended) return;
        for (Player p : players()) {
            Set<Material> f = found.computeIfAbsent(p.getUniqueId(), k -> new HashSet<>());
            for (Material m : targets) if (p.getInventory().contains(m)) f.add(m);
            p.sendActionBar(Component.text("Scavenger Hunt: " + f.size() + "/" + targets.size()));
            if (f.size() == targets.size()) {
                ended = true;
                say(p.getName() + " completed the scavenger hunt and WINS!");
                giveItems(p, getStringList("reward"));
                Bukkit.getScheduler().runTaskLater(plugin, this::stop, 60L);
                return;
            }
        }
    }

    @Override
    protected void onStop() {
        targets.clear(); found.clear();
        say("Scavenger Hunt has ended.");
    }
}
