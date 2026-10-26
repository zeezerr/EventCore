package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import com.example.eventcore.GameEvent;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.CreatureSpawnEvent;

public abstract class MobScaleEvent extends GameEvent {

    private final double defaultScale;

    protected MobScaleEvent(EventCore plugin, String id, String name, Material icon, double defaultScale) {
        super(plugin, id, name, icon);
        this.defaultScale = defaultScale;
    }

    private void apply(LivingEntity le, double scale) {
        if (le instanceof Player) return;
        setBase(le, Attribute.SCALE, Math.max(0.0625, Math.min(16.0, scale)));
    }

    @Override
    protected void onStart() {
        double s = getDouble("scale", defaultScale);
        for (World w : Bukkit.getWorlds())
            for (LivingEntity le : w.getLivingEntities()) apply(le, s);
        say("All mobs are now scaled to x" + s + "!");
    }

    @EventHandler
    public void onSpawn(CreatureSpawnEvent e) {
        apply(e.getEntity(), getDouble("scale", defaultScale));
    }

    @Override
    protected void onStop() {
        for (World w : Bukkit.getWorlds())
            for (LivingEntity le : w.getLivingEntities()) apply(le, 1.0);
        say("Mob sizes are back to normal.");
    }
}
