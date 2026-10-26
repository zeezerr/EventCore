package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import com.example.eventcore.GameEvent;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class FallingBlocksEvent extends GameEvent {

    public FallingBlocksEvent(EventCore plugin) {
        super(plugin, "falling_blocks", "Falling Blocks", Material.ANVIL);
    }

    @Override
    protected void onStart() {
        say("Blocks are falling from the sky!");
        List<Material> mats = new ArrayList<>(materialSet("blocks"));
        if (mats.isEmpty()) mats.add(Material.ANVIL);
        int per = getInt("per-player", 2), height = getInt("height", 20), spread = getInt("spread", 8);

        runTimer(() -> {
            for (Player p : players()) {
                for (int i = 0; i < per; i++) {
                    Location loc = p.getLocation().add(rnd().nextInt(-spread, spread + 1), height, rnd().nextInt(-spread, spread + 1));
                    Material m = mats.get(rnd().nextInt(mats.size()));
                    FallingBlock fb = p.getWorld().spawnFallingBlock(loc, m.createBlockData());
                    fb.setDropItem(false);
                    fb.setHurtEntities(true);
                }
            }
        }, 20L, getInt("interval-ticks", 30));
    }

    @Override
    protected void onStop() { say("Falling Blocks has ended."); }
}
