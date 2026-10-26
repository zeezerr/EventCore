package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import com.example.eventcore.GameEvent;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;

public class UltraHardcoreEvent extends GameEvent {

    public UltraHardcoreEvent(EventCore plugin) {
        super(plugin, "ultra_hardcore", "Ultra Hardcore", Material.GOLDEN_APPLE);
    }

    @Override protected void onStart() { say("No natural regeneration! Use golden apples and potions."); }

    @EventHandler
    public void onRegen(EntityRegainHealthEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        RegainReason r = e.getRegainReason();
        if (r == RegainReason.SATIATED || r == RegainReason.REGEN) e.setCancelled(true);
    }

    @Override protected void onStop() { say("Natural regeneration is back."); }
}
