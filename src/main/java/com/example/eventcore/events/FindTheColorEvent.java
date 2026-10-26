package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import com.example.eventcore.GameEvent;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class FindTheColorEvent extends ArenaEvent {

    private static final Material[] WOOLS = {
            Material.RED_WOOL, Material.ORANGE_WOOL, Material.YELLOW_WOOL, Material.LIME_WOOL,
            Material.CYAN_WOOL, Material.BLUE_WOOL, Material.PURPLE_WOOL, Material.PINK_WOOL};
    private static final String[] NAMES = {"RED", "ORANGE", "YELLOW", "LIME", "CYAN", "BLUE", "PURPLE", "PINK"};

    private final Set<UUID> alive = new HashSet<>();
    private World world;
    private int cx, cz, floorY, half, round, target;
    private int[][] grid;
    private boolean ended;

    public FindTheColorEvent(EventCore plugin) {
        super(plugin, "find_the_color", "Find The Color", Material.LIME_WOOL);
    }

    @Override
    public boolean usesLocation() { return true; }

    @Override
    protected void onStart() {
        ended = false; round = 0; alive.clear();
        Location c = getLocation("location", spawnLocation());
        world = c.getWorld();
        cx = c.getBlockX(); cz = c.getBlockZ();
        floorY = c.getBlockY() + getInt("height", 25);
        half = Math.max(4, getInt("size", 24) / 2);

        newPattern();
        paint();
        List<Player> ps = players();
        for (Player p : ps) {
            alive.add(p.getUniqueId());
            int r = half - 2;
            enter(p, new Location(world, cx + 0.5 + rnd().nextInt(-r, r + 1), floorY + 1, cz + 0.5 + rnd().nextInt(-r, r + 1)));
        }
        say("Find the colour before the floor disappears! Last one standing wins.");
        runLater(this::nextRound, 80L);
    }

    private void newPattern() {
        int cells = half + 1;
        grid = new int[cells][cells];
        for (int[] row : grid) for (int i = 0; i < row.length; i++) row[i] = rnd().nextInt(WOOLS.length);
    }

    private Material colorAt(int x, int z) {
        return WOOLS[grid[(x + half) / 2][(z + half) / 2]];
    }

    private void paint() {
        for (int x = -half; x < half; x++)
            for (int z = -half; z < half; z++)
                world.getBlockAt(cx + x, floorY, cz + z).setType(colorAt(x, z), false);
    }

    private void nextRound() {
        if (!isRunning() || ended) return;
        round++;
        newPattern();
        paint();
        target = rnd().nextInt(WOOLS.length);
        int time = Math.max(2, getInt("start-seconds", 6) - (round - 1) / 2);
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showTitle(Title.title(Component.text("FIND: " + NAMES[target]), Component.text("Round " + round + " - " + time + "s")));
        }
        runLater(this::clearOthers, time * 20L);
        runLater(this::nextRound, time * 20L + 80L);
    }

    private void clearOthers() {
        for (int x = -half; x < half; x++)
            for (int z = -half; z < half; z++) {
                Material m = colorAt(x, z);
                if (m != WOOLS[target]) world.getBlockAt(cx + x, floorY, cz + z).setType(Material.AIR, false);
            }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (!alive.contains(p.getUniqueId()) || e.getTo() == null || e.getTo().getY() >= floorY - 3) return;
        alive.remove(p.getUniqueId());
        spectate(p, new Location(world, cx + 0.5, floorY + 6, cz + 0.5));
        say(p.getName() + " fell! " + alive.size() + " left.");
        if (alive.size() <= 1 && !ended) {
            ended = true;
            Player w = alive.stream().findFirst().map(Bukkit::getPlayer).orElse(null);
            say(w != null ? "WINNER: " + w.getName() + "!" : "Nobody won.");
            if (w != null) giveItems(w, getStringList("reward"));
            Bukkit.getScheduler().runTaskLater(plugin, this::stop, 80L);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player p && alive.contains(p.getUniqueId())) e.setCancelled(true);
    }

    @Override
    protected void onStop() {
        if (world != null) {
            for (int x = -half; x < half; x++)
                for (int z = -half; z < half; z++)
                    world.getBlockAt(cx + x, floorY, cz + z).setType(Material.AIR, false);
        }
        restoreAll();
        alive.clear();
        say("Find The Color has ended.");
    }
}
