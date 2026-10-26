package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import com.example.eventcore.GameEvent;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PillarsOfFortuneEvent extends ArenaEvent {

    private static final Set<String> BAD = Set.of("BEDROCK", "BARRIER", "STRUCTURE_BLOCK", "STRUCTURE_VOID", "JIGSAW",
            "DEBUG_STICK", "KNOWLEDGE_BOOK", "LIGHT", "SPAWNER", "TRIAL_SPAWNER", "VAULT", "REINFORCED_DEEPSLATE",
            "END_PORTAL_FRAME", "TEST_BLOCK", "TEST_INSTANCE_BLOCK");

    private final Set<UUID> alive = new HashSet<>();
    private final Set<UUID> participants = new HashSet<>();
    private final Map<UUID, ItemStack[]> savedInv = new HashMap<>();
    private final List<Block> pillarBlocks = new ArrayList<>();
    private final List<Material> pool = new ArrayList<>();
    private Location spectateLoc;
    private boolean ended;

    public PillarsOfFortuneEvent(EventCore plugin) {
        super(plugin, "pillars_of_fortune", "Pillars Of Fortune", Material.QUARTZ_PILLAR);
    }

    @Override
    public boolean usesLocation() { return true; }

    @Override
    protected void onStart() {
        ended = false;
        alive.clear(); participants.clear(); savedInv.clear(); pillarBlocks.clear(); pool.clear();
        for (Material m : Material.values()) {
            if (m.isLegacy() || !m.isItem() || m.isAir() || BAD.contains(m.name()) || m.name().contains("COMMAND_BLOCK")) continue;
            pool.add(m);
        }

        List<Player> ps = players();
        if (ps.isEmpty()) { Bukkit.getScheduler().runTask(plugin, this::stop); return; }

        Location c = getLocation("location", spawnLocation());
        World w = c.getWorld();
        int height = getInt("height", 25);
        int radius = getInt("radius", Math.max(6, ps.size() * 2));
        int baseY = c.getBlockY();
        spectateLoc = new Location(w, c.getX(), baseY + height + 10, c.getZ());

        Collections.shuffle(ps);
        for (int i = 0; i < ps.size(); i++) {
            double a = Math.PI * 2 * i / ps.size();
            int x = c.getBlockX() + (int) Math.round(Math.cos(a) * radius);
            int z = c.getBlockZ() + (int) Math.round(Math.sin(a) * radius);
            for (int y = baseY; y <= baseY + height; y++) {
                Block b = w.getBlockAt(x, y, z);
                b.setType(Material.QUARTZ_BLOCK, false);
                pillarBlocks.add(b);
            }
            Player p = ps.get(i);
            alive.add(p.getUniqueId());
            participants.add(p.getUniqueId());
            savedInv.put(p.getUniqueId(), copy(p.getInventory().getContents()));
            p.getInventory().clear();
            p.setFoodLevel(20);
            enter(p, new Location(w, x + 0.5, baseY + height + 1, z + 0.5));
        }

        say("Pillars of Fortune! You get a random item every " + getInt("item-seconds", 10) + "s. Last one standing wins.");
        long period = getInt("item-seconds", 10) * 20L;
        runTimer(() -> {
            for (UUID u : alive) {
                Player p = Bukkit.getPlayer(u);
                if (p == null) continue;
                Material m = pool.get(rnd().nextInt(pool.size()));
                int amt = m.getMaxStackSize() > 1 ? rnd().nextInt(1, Math.min(8, m.getMaxStackSize()) + 1) : 1;
                p.getInventory().addItem(new ItemStack(m, amt)).values()
                        .forEach(left -> p.getWorld().dropItemNaturally(p.getLocation(), left));
            }
        }, period, period);
    }

    private static ItemStack[] copy(ItemStack[] in) {
        ItemStack[] out = new ItemStack[in.length];
        for (int i = 0; i < in.length; i++) out[i] = in[i] == null ? null : in[i].clone();
        return out;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getPlayer();
        if (!alive.remove(p.getUniqueId())) return;
        e.getDrops().clear();
        e.setDroppedExp(0);
        say(p.getName() + " is out! " + alive.size() + " left.");
        if (alive.size() <= 1 && !ended) {
            ended = true;
            Player w = alive.stream().findFirst().map(Bukkit::getPlayer).orElse(null);
            say(w != null ? "WINNER: " + w.getName() + "!" : "Nobody won.");
            Bukkit.getScheduler().runTaskLater(plugin, this::stop, 100L);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        UUID u = e.getPlayer().getUniqueId();
        if (participants.contains(u) && !alive.contains(u)) {
            e.setRespawnLocation(spectateLoc);
            Bukkit.getScheduler().runTaskLater(plugin, () -> e.getPlayer().setGameMode(GameMode.SPECTATOR), 1L);
        }
    }

    @Override
    protected void onStop() {
        for (Block b : pillarBlocks) b.setType(Material.AIR, false);
        pillarBlocks.clear();
        restoreAll();
        for (Map.Entry<UUID, ItemStack[]> en : savedInv.entrySet()) {
            Player p = Bukkit.getPlayer(en.getKey());
            if (p != null) { p.getInventory().clear(); p.getInventory().setContents(en.getValue()); }
        }
        savedInv.clear(); alive.clear(); participants.clear();
        say("Pillars Of Fortune has ended.");
    }
}
