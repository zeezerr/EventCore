package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import com.example.eventcore.GameEvent;

import org.bukkit.Material;

public class GiantMobsEvent extends MobScaleEvent {
    public GiantMobsEvent(EventCore plugin) {
        super(plugin, "giant_mobs", "Giant Mobs", Material.ZOMBIE_SPAWN_EGG, 3.0);
    }
}
