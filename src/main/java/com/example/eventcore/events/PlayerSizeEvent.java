package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import com.example.eventcore.GameEvent;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

public class PlayerSizeEvent extends GameEvent {

    public PlayerSizeEvent(EventCore plugin) {
        super(plugin, "player_size", "Player Size", Material.RED_MUSHROOM);
    }

    @Override
    protected void onStart() {
        roll();
        long period = getInt("reroll-seconds", 60) * 20L;
        runTimer(this::roll, period, period);
    }

    private void roll() {
        double min = getDouble("min-scale", 0.5), max = getDouble("max-scale", 2.0);
        for (Player p : Bukkit.getOnlinePlayers()) {
            setBase(p, Attribute.SCALE, min + rnd().nextDouble() * (max - min));
        }
        say("Everyone's size has been re-rolled!");
    }

    @Override
    protected void onStop() {
        for (Player p : Bukkit.getOnlinePlayers()) setBase(p, Attribute.SCALE, 1.0);
        say("Sizes reset.");
    }
}
