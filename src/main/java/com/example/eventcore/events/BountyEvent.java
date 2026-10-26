package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import com.example.eventcore.GameEvent;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.List;

public class BountyEvent extends GameEvent {

    private Player target;

    public BountyEvent(EventCore plugin) {
        super(plugin, "bounty", "Bounty", Material.GOLD_INGOT);
    }

    @Override
    protected void onStart() {
        long period = getInt("interval-seconds", 120) * 20L;
        runTimer(this::assign, 40L, period);
    }

    private void assign() {
        if (target != null) target.setGlowing(false);
        List<Player> ps = players();
        if (ps.isEmpty()) { target = null; return; }
        target = ps.get(rnd().nextInt(ps.size()));
        target.setGlowing(true);
        say("A bounty is on " + target.getName() + "! Kill them for a reward. They glow for everyone.");
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getPlayer();
        if (target == null || !p.equals(target)) return;
        p.setGlowing(false);
        target = null;
        Player killer = p.getKiller();
        if (killer != null && !killer.equals(p)) {
            say(killer.getName() + " claimed the bounty!");
            giveItems(killer, getStringList("reward"));
        } else {
            say("The bounty target died - nobody claimed it.");
        }
    }

    @Override
    protected void onStop() {
        if (target != null) target.setGlowing(false);
        target = null;
        say("Bounty has ended.");
    }
}
