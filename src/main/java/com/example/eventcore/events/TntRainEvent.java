package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import com.example.eventcore.GameEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;

import java.util.concurrent.ThreadLocalRandom;

public class TntRainEvent extends GameEvent {

    public TntRainEvent(EventCore plugin) {
        super(plugin, "tnt_rain", "TNT Rain", Material.TNT);
    }

    @Override
    protected void onStart() {
        Bukkit.broadcast(Component.text("TNT is falling from the sky! Take cover!", NamedTextColor.RED));

        int perPlayer = getInt("tnt-per-player", 2);
        int height = getInt("height", 18);
        int spread = getInt("spread", 10);
        int fuse = getInt("fuse-ticks", 60);

        runTimer(() -> {
            ThreadLocalRandom r = ThreadLocalRandom.current();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getGameMode() != GameMode.SURVIVAL && p.getGameMode() != GameMode.ADVENTURE) continue;
                World w = p.getWorld();
                for (int i = 0; i < perPlayer; i++) {
                    Location loc = p.getLocation().add(
                            r.nextInt(-spread, spread + 1), height, r.nextInt(-spread, spread + 1));
                    w.spawn(loc, TNTPrimed.class, tnt -> tnt.setFuseTicks(fuse));
                }
            }
        }, 20L, getInt("interval-ticks", 20));
    }

    @Override
    protected void onStop() {
        Bukkit.broadcast(Component.text("TNT Rain has ended.", NamedTextColor.GREEN));
    }
}
