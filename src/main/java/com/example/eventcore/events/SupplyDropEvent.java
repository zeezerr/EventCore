package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import com.example.eventcore.GameEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class SupplyDropEvent extends GameEvent {

    public SupplyDropEvent(EventCore plugin) {
        super(plugin, "supply_drop", "Supply Drop", Material.CHEST);
    }

    @Override
    protected void onStart() {
        long period = getInt("interval-seconds", 120) * 20L;
        runTimer(this::drop, 20L, period);
    }

    private void drop() {
        World w = Bukkit.getWorlds().get(0);
        int radius = getInt("radius", 300);
        ThreadLocalRandom r = ThreadLocalRandom.current();

        int x = w.getSpawnLocation().getBlockX() + r.nextInt(-radius, radius + 1);
        int z = w.getSpawnLocation().getBlockZ() + r.nextInt(-radius, radius + 1);
        int y = w.getHighestBlockYAt(x, z) + 1;

        Block block = w.getBlockAt(x, y, z);
        block.setType(Material.CHEST);
        if (!(block.getState() instanceof Chest chest)) return;

        List<String> loot = getStringList("loot");
        for (String entry : loot) {
            String[] parts = entry.split(":");
            Material mat = Material.matchMaterial(parts[0]);
            if (mat == null) continue;
            int max = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
            int amount = Math.max(1, r.nextInt(1, max + 1));
            chest.getBlockInventory().setItem(r.nextInt(27), new org.bukkit.inventory.ItemStack(mat, amount));
        }

        Location loc = block.getLocation().add(0.5, 1, 0.5);
        // Visual marker: particle column for 60 seconds
        int[] ticks = {0};
        runTimer(() -> {
            if (ticks[0]++ > 60) return;
            w.spawnParticle(Particle.END_ROD, loc, 30, 0.1, 20, 0.1, 0.01);
        }, 0L, 20L);

        if (getBool("announce", true)) {
            Bukkit.broadcast(Component.text("A supply drop landed at " + x + ", " + y + ", " + z + "!",
                    NamedTextColor.GOLD));
        }
    }

    @Override
    protected void onStop() {
        Bukkit.broadcast(Component.text("Supply drops have stopped.", NamedTextColor.YELLOW));
    }
}
