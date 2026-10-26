package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import com.example.eventcore.GameEvent;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class RisingLavaEvent extends GameEvent {

    private int level;
    private final List<Block> placed = new ArrayList<>();

    public RisingLavaEvent(EventCore plugin) {
        super(plugin, "rising_lava", "Rising Lava", Material.LAVA_BUCKET);
    }

    @Override
    protected void onStart() {
        World w = Bukkit.getWorlds().get(0);
        level = getInt("start-y", -55);
        say("The lava is rising! Get to high ground!");
        long period = getInt("rise-seconds", 15) * 20L;
        runTimer(() -> rise(w), period, period);
    }

    private void rise(World w) {
        level++;
        if (level >= w.getMaxHeight()) { stop(); return; }
        int radius = getInt("radius", 40);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.getWorld().equals(w)) continue;
            int cx = p.getLocation().getBlockX(), cz = p.getLocation().getBlockZ();
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    Block b = w.getBlockAt(cx + dx, level, cz + dz);
                    if (b.isPassable() && b.getType() != Material.LAVA) {
                        b.setType(Material.LAVA, false);
                        placed.add(b);
                    }
                }
            }
        }
        say("Lava level: Y=" + level);
    }

    @Override
    protected void onStop() {
        for (Block b : placed) {
            if (b.getType() == Material.LAVA) b.setType(Material.AIR, false);
        }
        placed.clear();
        say("The lava has drained.");
    }
}
