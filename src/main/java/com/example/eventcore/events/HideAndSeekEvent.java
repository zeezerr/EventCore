package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import com.example.eventcore.GameEvent;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class HideAndSeekEvent extends GameEvent {

    private final Set<UUID> seekers = new HashSet<>();
    private final Set<UUID> hiders = new HashSet<>();
    private boolean seeking, ended;
    private int timeLeft;

    public HideAndSeekEvent(EventCore plugin) {
        super(plugin, "hide_and_seek", "Hide And Seek", Material.ENDER_EYE);
    }

    @Override
    protected void onStart() {
        seekers.clear(); hiders.clear(); seeking = false; ended = false;
        List<Player> ps = players();
        if (ps.size() < 2) {
            say("Need at least 2 players.");
            Bukkit.getScheduler().runTask(plugin, this::stop);
            return;
        }
        Collections.shuffle(ps);
        int seekerCount = Math.max(1, ps.size() / getInt("players-per-seeker", 5));
        int hide = getInt("hide-seconds", 30);
        for (int i = 0; i < ps.size(); i++) {
            Player p = ps.get(i);
            if (i < seekerCount) {
                seekers.add(p.getUniqueId());
                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, hide * 20, 0));
            } else hiders.add(p.getUniqueId());
        }
        say(seekerCount + " seeker(s) are blinded and frozen. Hiders have " + hide + " seconds to hide!");
        timeLeft = getInt("seek-seconds", 300);
        runLater(this::release, hide * 20L);
    }

    private void release() {
        seeking = true;
        say("Seekers released! Find the hiders!");
        runTimer(() -> {
            if (ended) return;
            timeLeft--;
            if (timeLeft > 0 && timeLeft % 60 == 0) {
                for (UUID u : hiders) {
                    Player p = Bukkit.getPlayer(u);
                    if (p != null) p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 60, 0));
                }
                say("Hint: hiders glow for 3 seconds!");
            }
            if (timeLeft <= 0) finish("Time is up - the HIDERS win!");
        }, 20L, 20L);
    }

    private void finish(String msg) {
        if (ended) return;
        ended = true;
        say(msg);
        Bukkit.getScheduler().runTaskLater(plugin, this::stop, 60L);
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player a) || !(e.getEntity() instanceof Player v)) return;
        if (!seeking) return;
        if (seekers.contains(a.getUniqueId()) && hiders.contains(v.getUniqueId())) {
            e.setCancelled(true);
            hiders.remove(v.getUniqueId());
            seekers.add(v.getUniqueId());
            say(v.getName() + " was found by " + a.getName() + "! " + hiders.size() + " hider(s) left.");
            if (hiders.isEmpty()) finish("All hiders were found - the SEEKERS win!");
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (!seeking && seekers.contains(e.getPlayer().getUniqueId()) && moved(e)) lockPosition(e);
    }

    @Override
    protected void onStop() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.removePotionEffect(PotionEffectType.BLINDNESS);
            p.removePotionEffect(PotionEffectType.GLOWING);
        }
        seekers.clear(); hiders.clear();
        say("Hide And Seek has ended.");
    }
}
