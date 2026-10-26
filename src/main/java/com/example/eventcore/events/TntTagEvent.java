package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import com.example.eventcore.GameEvent;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.List;

public class TntTagEvent extends GameEvent {

    private Player it;
    private int remaining;

    public TntTagEvent(EventCore plugin) {
        super(plugin, "tnt_tag", "TNT Tag", Material.TNT_MINECART);
    }

    @Override
    protected void onStart() {
        pickNew(null);
        runTimer(this::tick, 20L, 20L);
    }

    private void pickNew(Player exclude) {
        if (it != null) it.setGlowing(false);
        List<Player> ps = players();
        ps.remove(exclude);
        if (ps.isEmpty()) { it = null; return; }
        it = ps.get(rnd().nextInt(ps.size()));
        it.setGlowing(true);
        remaining = getInt("time-seconds", 30);
        say(it.getName() + " is IT! Hit someone to pass the TNT!");
    }

    private void tick() {
        if (it == null || !it.isOnline() || it.isDead()) { pickNew(null); return; }
        remaining--;
        it.sendActionBar(Component.text("TNT explodes in " + remaining + "s - hit someone!", NamedTextColor.RED));
        if (remaining <= 0) {
            Player victim = it;
            Location l = victim.getLocation();
            victim.setGlowing(false);
            it = null;
            l.getWorld().createExplosion(l, 2f, false, false);
            if (!victim.isDead()) victim.setHealth(0);
            say(victim.getName() + " blew up!");
            pickNew(victim);
        }
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent e) {
        if (it == null || e.getDamager() != it) return;
        if (e.getEntity() instanceof Player target && target.getGameMode() != GameMode.SPECTATOR) {
            it.setGlowing(false);
            it = target;
            target.setGlowing(true);
            say(target.getName() + " is now IT!");
        }
    }

    @Override
    protected void onStop() {
        if (it != null) it.setGlowing(false);
        it = null;
        say("TNT Tag has ended.");
    }
}
