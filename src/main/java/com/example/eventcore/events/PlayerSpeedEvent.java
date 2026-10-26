package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import com.example.eventcore.GameEvent;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerSpeedEvent extends GameEvent {

    private static final double DEFAULT_SPEED = 0.1;

    public PlayerSpeedEvent(EventCore plugin) {
        super(plugin, "player_speed", "Player Speed", Material.SUGAR);
    }

    @Override
    protected void onStart() {
        for (Player p : Bukkit.getOnlinePlayers()) apply(p);
        say("Movement speed x" + getDouble("multiplier", 2.0) + "!");
    }

    private void apply(Player p) {
        setBase(p, Attribute.MOVEMENT_SPEED, DEFAULT_SPEED * getDouble("multiplier", 2.0));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) { apply(e.getPlayer()); }

    @Override
    protected void onStop() {
        for (Player p : Bukkit.getOnlinePlayers()) setBase(p, Attribute.MOVEMENT_SPEED, DEFAULT_SPEED);
        say("Speed back to normal.");
    }
}
