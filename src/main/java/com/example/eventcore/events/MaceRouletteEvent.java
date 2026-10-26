package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import com.example.eventcore.GameEvent;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class MaceRouletteEvent extends GameEvent {

    public MaceRouletteEvent(EventCore plugin) {
        super(plugin, "mace_roulette", "Mace Roulette", Material.MACE);
    }

    @Override
    protected void onStart() {
        long period = getInt("interval-seconds", 90) * 20L;
        runTimer(this::spin, 60L, period);
    }

    private void spin() {
        List<Player> ps = players();
        if (ps.isEmpty()) return;
        Player p = ps.get(rnd().nextInt(ps.size()));
        p.getInventory().addItem(new ItemStack(Material.MACE), new ItemStack(Material.WIND_CHARGE, 8))
                .values().forEach(left -> p.getWorld().dropItemNaturally(p.getLocation(), left));
        say(p.getName() + " won the Mace!");
        runLater(() -> {
            if (p.isOnline()) p.getInventory().remove(Material.MACE);
        }, getInt("hold-seconds", 30) * 20L);
    }

    @Override protected void onStop() { say("Mace Roulette has ended."); }
}
