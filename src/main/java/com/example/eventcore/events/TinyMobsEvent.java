package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import com.example.eventcore.GameEvent;

import org.bukkit.Material;

public class TinyMobsEvent extends MobScaleEvent {
    public TinyMobsEvent(EventCore plugin) {
        super(plugin, "tiny_mobs", "Tiny Mobs", Material.TURTLE_SPAWN_EGG, 0.4);
    }
}
