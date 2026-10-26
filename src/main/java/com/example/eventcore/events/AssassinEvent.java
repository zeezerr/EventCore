package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import com.example.eventcore.GameEvent;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AssassinEvent extends GameEvent {

    private final Map<UUID, UUID> targets = new HashMap<>();
    private final Set<UUID> out = new HashSet<>();

    public AssassinEvent(EventCore plugin) {
        super(plugin, "assassin_event", "Assassin Event", Material.IRON_SWORD);
    }

    @Override
    protected void onStart() {
        targets.clear(); out.clear();
        List<Player> ps = players();
        if (ps.size() < 2) {
            say("Need at least 2 players.");
            Bukkit.getScheduler().runTask(plugin, this::stop);
            return;
        }
        Collections.shuffle(ps);
        for (int i = 0; i < ps.size(); i++) {
            Player p = ps.get(i);
            targets.put(p.getUniqueId(), ps.get((i + 1) % ps.size()).getUniqueId());
            p.getInventory().addItem(new ItemStack(Material.COMPASS));
        }
        say("Everyone has a secret target. Kill yours - your compass points to them. Last assassin standing wins!");
        runTimer(this::updateHud, 20L, 20L);
    }

    private void updateHud() {
        for (Map.Entry<UUID, UUID> en : targets.entrySet()) {
            Player hunter = Bukkit.getPlayer(en.getKey());
            Player prey = Bukkit.getPlayer(en.getValue());
            if (hunter == null) continue;
            if (prey == null || !prey.getWorld().equals(hunter.getWorld())) {
                hunter.sendActionBar(Component.text("Target: " + (prey == null ? "offline" : prey.getName() + " (other dimension)"), NamedTextColor.RED));
                continue;
            }
            hunter.setCompassTarget(prey.getLocation());
            hunter.sendActionBar(Component.text("Target: " + prey.getName() + " - " + (int) hunter.getLocation().distance(prey.getLocation()) + "m", NamedTextColor.RED));
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player victim = e.getPlayer();
        UUID vu = victim.getUniqueId();
        if (!targets.containsKey(vu)) return;
        UUID victimTarget = targets.remove(vu);
        out.add(vu);

        UUID hunter = null;
        for (Map.Entry<UUID, UUID> en : targets.entrySet()) if (en.getValue().equals(vu)) hunter = en.getKey();
        if (hunter != null && !hunter.equals(victimTarget)) targets.put(hunter, victimTarget);

        Player killer = victim.getKiller();
        if (killer != null && hunter != null && killer.getUniqueId().equals(hunter)) {
            giveItems(killer, getStringList("kill-reward"));
        }
        say("An assassin was eliminated! " + targets.size() + " left.");

        if (targets.size() <= 1) {
            UUID w = targets.keySet().stream().findFirst().orElse(null);
            say("WINNER: " + (w == null ? "nobody" : Bukkit.getOfflinePlayer(w).getName()) + "!");
            Bukkit.getScheduler().runTaskLater(plugin, this::stop, 100L);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        if (out.contains(e.getPlayer().getUniqueId()))
            Bukkit.getScheduler().runTaskLater(plugin, () -> e.getPlayer().setGameMode(GameMode.SPECTATOR), 1L);
    }

    @Override
    protected void onStop() {
        for (UUID u : out) {
            Player p = Bukkit.getPlayer(u);
            if (p != null) p.setGameMode(GameMode.SURVIVAL);
        }
        targets.clear(); out.clear();
        say("Assassin Event has ended.");
    }
}
