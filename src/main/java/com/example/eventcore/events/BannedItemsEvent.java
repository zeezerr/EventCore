package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import com.example.eventcore.GameEvent;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class BannedItemsEvent extends GameEvent {

    private Set<Material> banned;

    public BannedItemsEvent(EventCore plugin) {
        super(plugin, "banned_items", "Banned Items", Material.BARRIER);
    }

    @Override
    protected void onStart() {
        banned = materialSet("banned");
        say("Banned items: " + banned);
    }

    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        ItemStack it = e.getItem();
        if (it != null && banned.contains(it.getType())) e.setCancelled(true);
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent e) {
        if (e.getEntity() instanceof Player && banned.contains(e.getItem().getItemStack().getType())) e.setCancelled(true);
    }

    @EventHandler
    public void onCraft(CraftItemEvent e) {
        ItemStack r = e.getRecipe().getResult();
        if (banned.contains(r.getType())) e.setCancelled(true);
    }

    @Override protected void onStop() { say("Banned Items has ended."); }
}
