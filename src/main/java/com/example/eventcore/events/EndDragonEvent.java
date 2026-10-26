package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import com.example.eventcore.GameEvent;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.ComplexEntityPart;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;

public class EndDragonEvent extends GameEvent {

    private final Map<UUID, Double> damage = new HashMap<>();

    public EndDragonEvent(EventCore plugin) {
        super(plugin, "end_event", "End Event", Material.DRAGON_HEAD);
    }

    @Override
    protected void onStart() {
        damage.clear();
        say("The End Event is live! Whoever deals the most damage to the Ender Dragon wins a prize.");
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        Entity victim = e.getEntity();
        if (victim instanceof ComplexEntityPart part) victim = part.getParent();
        if (!(victim instanceof EnderDragon)) return;
        Player p = null;
        if (e.getDamager() instanceof Player pl) p = pl;
        else if (e.getDamager() instanceof Projectile pr && pr.getShooter() instanceof Player pl) p = pl;
        if (p != null) damage.merge(p.getUniqueId(), e.getFinalDamage(), Double::sum);
    }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        if (!(e.getEntity() instanceof EnderDragon)) return;
        List<Map.Entry<UUID, Double>> list = new ArrayList<>(damage.entrySet());
        list.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        say("The Ender Dragon has fallen!");
        for (int i = 0; i < Math.min(3, list.size()); i++) {
            Map.Entry<UUID, Double> en = list.get(i);
            say("#" + (i + 1) + " " + Bukkit.getOfflinePlayer(en.getKey()).getName() + " - " + Math.round(en.getValue()) + " damage");
        }
        if (!list.isEmpty()) {
            Player top = Bukkit.getPlayer(list.get(0).getKey());
            if (top != null) giveItems(top, getStringList("reward"));
        }
        damage.clear();
    }

    @Override
    protected void onStop() {
        damage.clear();
        say("End Event has ended.");
    }
}
