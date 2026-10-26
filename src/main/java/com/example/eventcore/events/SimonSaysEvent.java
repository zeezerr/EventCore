package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import com.example.eventcore.GameEvent;

import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SimonSaysEvent extends GameEvent {

    private enum Act {
        JUMP("jump"), SNEAK("sneak"), STAND_STILL("stand still"), SPRINT("sprint"), SWING("swing your arm");
        final String text;
        Act(String t) { text = t; }
    }

    private Act current;
    private boolean simon, active;
    private final Set<UUID> acted = new HashSet<>();
    private final Set<UUID> moved = new HashSet<>();

    public SimonSaysEvent(EventCore plugin) {
        super(plugin, "simon_says", "Simon Says", Material.NOTE_BLOCK);
    }

    @Override
    protected void onStart() {
        long period = getInt("round-seconds", 8) * 20L;
        runTimer(this::round, 60L, period);
    }

    private void round() {
        Act[] all = Act.values();
        current = all[rnd().nextInt(all.length)];
        simon = rnd().nextInt(100) < 60;
        acted.clear();
        moved.clear();
        active = true;
        say((simon ? "Simon says " : "") + current.text + "!");
        runLater(this::judge, 80L);
    }

    private void judge() {
        active = false;
        double dmg = getDouble("damage", 4.0);
        for (Player p : players()) {
            UUID u = p.getUniqueId();
            boolean did = current == Act.STAND_STILL ? !moved.contains(u) : acted.contains(u);
            boolean fail = simon ? !did : did;
            if (fail) {
                p.damage(dmg);
                p.sendMessage(simon ? "You didn't do what Simon said!" : "Simon didn't say that!");
            }
        }
    }

    private void mark(Player p, Act a) {
        if (active && current == a) acted.add(p.getUniqueId());
    }

    @EventHandler public void onJump(PlayerStatisticIncrementEvent e) { if (e.getStatistic() == Statistic.JUMP) mark(e.getPlayer(), Act.JUMP); }
    @EventHandler public void onSneak(PlayerToggleSneakEvent e) { if (e.isSneaking()) mark(e.getPlayer(), Act.SNEAK); }
    @EventHandler public void onSprint(PlayerToggleSprintEvent e) { if (e.isSprinting()) mark(e.getPlayer(), Act.SPRINT); }
    @EventHandler public void onSwing(PlayerAnimationEvent e) { mark(e.getPlayer(), Act.SWING); }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (active && e.getTo() != null && (e.getFrom().getX() != e.getTo().getX() || e.getFrom().getZ() != e.getTo().getZ()))
            moved.add(e.getPlayer().getUniqueId());
    }

    @Override protected void onStop() { active = false; say("Simon Says has ended."); }
}
