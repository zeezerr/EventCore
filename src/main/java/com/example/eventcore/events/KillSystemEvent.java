package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import com.example.eventcore.GameEvent;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class KillSystemEvent extends GameEvent {

    private final Map<UUID, Integer> kills = new HashMap<>();
    private final Map<UUID, Integer> streak = new HashMap<>();

    public KillSystemEvent(EventCore plugin) {
        super(plugin, "kill_system", "Kill System", Material.DIAMOND_SWORD);
    }

    @Override
    protected void onStart() {
        kills.clear(); streak.clear();
        say("Kills are now tracked. Rewards and killstreak announcements are active!");
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player victim = e.getPlayer();
        int lost = streak.getOrDefault(victim.getUniqueId(), 0);
        streak.remove(victim.getUniqueId());
        Player killer = victim.getKiller();
        if (killer == null || killer.equals(victim)) return;

        UUID k = killer.getUniqueId();
        kills.merge(k, 1, Integer::sum);
        int s = streak.merge(k, 1, Integer::sum);

        giveItems(killer, getStringList("kill-reward"));
        double heal = getDouble("heal-on-kill", 4.0);
        if (heal > 0) killer.setHealth(Math.min(maxHealth(killer), killer.getHealth() + heal));

        if (s == 3 || s == 5 || s == 7 || s == 10 || (s > 10 && s % 5 == 0))
            say(killer.getName() + " is on a " + s + " kill streak!");
        if (lost >= 3) say(killer.getName() + " ended " + victim.getName() + "'s " + lost + " kill streak!");
    }

    @Override
    protected void onStop() {
        List<Map.Entry<UUID, Integer>> list = new ArrayList<>(kills.entrySet());
        list.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        say("Top killers:");
        for (int i = 0; i < Math.min(3, list.size()); i++) {
            Map.Entry<UUID, Integer> en = list.get(i);
            say("#" + (i + 1) + " " + Bukkit.getOfflinePlayer(en.getKey()).getName() + " - " + en.getValue() + " kills");
        }
        kills.clear(); streak.clear();
    }
}
