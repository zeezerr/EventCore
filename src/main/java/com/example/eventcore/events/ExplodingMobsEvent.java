package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import com.example.eventcore.GameEvent;

import org.bukkit.Material;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;

public class ExplodingMobsEvent extends GameEvent {

    public ExplodingMobsEvent(EventCore plugin) {
        super(plugin, "exploding_mobs", "Exploding Mobs", Material.CREEPER_HEAD);
    }

    @Override protected void onStart() { say("Hostile mobs now explode when they die!"); }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        if (!(e.getEntity() instanceof Monster m)) return;
        m.getWorld().createExplosion(m.getLocation(), (float) getDouble("power", 2.0), false, getBool("break-blocks", false));
    }

    @Override protected void onStop() { say("Mobs no longer explode."); }
}
