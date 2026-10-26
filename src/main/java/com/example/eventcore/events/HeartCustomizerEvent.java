package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import com.example.eventcore.GameEvent;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class HeartCustomizerEvent extends GameEvent {

    public HeartCustomizerEvent(EventCore plugin) {
        super(plugin, "heart_customizer", "Heart Customizer", Material.APPLE);
    }

    @Override
    protected void onStart() {
        for (Player p : Bukkit.getOnlinePlayers()) apply(p);
        say("Everyone now has " + getInt("hearts", 5) + " hearts!");
    }

    private void apply(Player p) {
        double max = getInt("hearts", 5) * 2.0;
        setBase(p, Attribute.MAX_HEALTH, max);
        if (p.getHealth() > max) p.setHealth(max);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) { apply(e.getPlayer()); }

    @Override
    protected void onStop() {
        for (Player p : Bukkit.getOnlinePlayers()) setBase(p, Attribute.MAX_HEALTH, 20.0);
        say("Hearts reset to 10.");
    }
}
