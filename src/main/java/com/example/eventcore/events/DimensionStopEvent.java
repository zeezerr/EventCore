package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import com.example.eventcore.GameEvent;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerPortalEvent;

public class DimensionStopEvent extends GameEvent {

    public DimensionStopEvent(EventCore plugin) {
        super(plugin, "dimension_stop", "Dimension Stop", Material.OBSIDIAN);
    }

    @Override protected void onStart() { say("Portals are disabled!"); }

    @EventHandler
    public void onPortal(PlayerPortalEvent e) {
        e.setCancelled(true);
        e.getPlayer().sendActionBar(Component.text("Dimension travel is disabled right now.", NamedTextColor.RED));
    }

    @Override protected void onStop() { say("Portals are enabled again."); }
}
