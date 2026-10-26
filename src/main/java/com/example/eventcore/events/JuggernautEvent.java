package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import com.example.eventcore.GameEvent;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class JuggernautEvent extends GameEvent {

    private Player jug;

    public JuggernautEvent(EventCore plugin) {
        super(plugin, "juggernaut", "Juggernaut", Material.NETHERITE_CHESTPLATE);
    }

    @Override
    protected void onStart() {
        List<Player> ps = players();
        if (ps.isEmpty()) return;
        make(ps.get(rnd().nextInt(ps.size())));
    }

    private void make(Player p) {
        jug = p;
        double hp = getInt("health", 60);
        setBase(p, Attribute.MAX_HEALTH, hp);
        p.setHealth(hp);
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, 1));
        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, PotionEffect.INFINITE_DURATION, 0));
        p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, PotionEffect.INFINITE_DURATION, 0));
        say(p.getName() + " is the JUGGERNAUT! Take them down!");
    }

    private void clear(Player p) {
        setBase(p, Attribute.MAX_HEALTH, 20.0);
        p.removePotionEffect(PotionEffectType.RESISTANCE);
        p.removePotionEffect(PotionEffectType.STRENGTH);
        p.removePotionEffect(PotionEffectType.GLOWING);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getPlayer();
        if (!p.equals(jug)) return;
        clear(p);
        jug = null;
        Player killer = p.getKiller();
        if (killer != null) make(killer);
        else {
            List<Player> ps = players();
            ps.remove(p);
            if (!ps.isEmpty()) make(ps.get(rnd().nextInt(ps.size())));
        }
    }

    @Override
    protected void onStop() {
        if (jug != null) clear(jug);
        jug = null;
        say("Juggernaut has ended.");
    }
}
