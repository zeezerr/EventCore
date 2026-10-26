package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import com.example.eventcore.GameEvent;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class FreezeTagEvent extends GameEvent {

    private final Set<UUID> taggers = new HashSet<>();
    private final Set<UUID> runners = new HashSet<>();
    private final Set<UUID> frozen = new HashSet<>();
    private int timeLeft;
    private boolean ended;

    public FreezeTagEvent(EventCore plugin) {
        super(plugin, "freeze_tag", "Freeze Tag", Material.ICE);
    }

    @Override
    protected void onStart() {
        taggers.clear(); runners.clear(); frozen.clear(); ended = false;
        List<Player> ps = players();
        if (ps.size() < 2) {
            say("Need at least 2 players.");
            Bukkit.getScheduler().runTask(plugin, this::stop);
            return;
        }
        Collections.shuffle(ps);
        int tagCount = Math.max(1, ps.size() / getInt("players-per-tagger", 4));
        for (int i = 0; i < ps.size(); i++) {
            Player p = ps.get(i);
            if (i < tagCount) { taggers.add(p.getUniqueId()); p.setGlowing(true); }
            else runners.add(p.getUniqueId());
        }
        timeLeft = getInt("time-seconds", 300);
        say(tagCount + " tagger(s) glow. Taggers freeze runners by hitting them. Runners unfreeze teammates with right-click!");
        runTimer(this::tick, 20L, 20L);
    }

    private void tick() {
        if (ended) return;
        timeLeft--;
        for (UUID u : frozen) {
            Player p = Bukkit.getPlayer(u);
            if (p != null) p.setFreezeTicks(p.getMaxFreezeTicks());
        }
        if (timeLeft <= 0) finish(false);
    }

    private void finish(boolean taggersWin) {
        if (ended) return;
        ended = true;
        say(taggersWin ? "All runners are frozen - TAGGERS WIN!" : "Time is up - RUNNERS WIN!");
        Bukkit.getScheduler().runTaskLater(plugin, this::stop, 60L);
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player a) || !(e.getEntity() instanceof Player v)) return;
        UUID au = a.getUniqueId(), vu = v.getUniqueId();
        boolean aIn = taggers.contains(au) || runners.contains(au), vIn = taggers.contains(vu) || runners.contains(vu);
        if (!aIn || !vIn) return;
        e.setCancelled(true);
        if (taggers.contains(au) && runners.contains(vu) && !frozen.contains(vu)) {
            frozen.add(vu);
            say(v.getName() + " was frozen by " + a.getName() + "!");
            boolean allFrozen = true;
            for (UUID r : runners) if (!frozen.contains(r)) { allFrozen = false; break; }
            if (allFrozen) finish(true);
        }
    }

    @EventHandler
    public void onUnfreeze(PlayerInteractEntityEvent e) {
        if (e.getHand() != EquipmentSlot.HAND || !(e.getRightClicked() instanceof Player target)) return;
        UUID c = e.getPlayer().getUniqueId();
        if (runners.contains(c) && !frozen.contains(c) && frozen.remove(target.getUniqueId())) {
            target.setFreezeTicks(0);
            say(e.getPlayer().getName() + " unfroze " + target.getName() + "!");
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (frozen.contains(e.getPlayer().getUniqueId()) && moved(e)) lockPosition(e);
    }

    @Override
    protected void onStop() {
        for (UUID u : taggers) { Player p = Bukkit.getPlayer(u); if (p != null) p.setGlowing(false); }
        for (UUID u : frozen) { Player p = Bukkit.getPlayer(u); if (p != null) p.setFreezeTicks(0); }
        taggers.clear(); runners.clear(); frozen.clear();
        say("Freeze Tag has ended.");
    }
}
