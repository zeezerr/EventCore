package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import com.example.eventcore.GameEvent;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InventorySwapEvent extends GameEvent {

    public InventorySwapEvent(EventCore plugin) {
        super(plugin, "inventory_swap", "Inventory Swap", Material.CHEST);
    }

    @Override
    protected void onStart() {
        long period = getInt("interval-seconds", 90) * 20L;
        runTimer(() -> {
            if (players().size() < 2) return;
            say("Swapping inventories in 5 seconds!");
            runLater(this::swap, 100L);
        }, period, period);
    }

    private ItemStack[] copy(ItemStack[] in) {
        ItemStack[] out = new ItemStack[in.length];
        for (int i = 0; i < in.length; i++) out[i] = in[i] == null ? null : in[i].clone();
        return out;
    }

    private void swap() {
        List<Player> l = players();
        if (l.size() < 2) return;
        Collections.shuffle(l);
        List<ItemStack[]> snapshots = new ArrayList<>();
        for (Player p : l) snapshots.add(copy(p.getInventory().getContents()));
        for (int i = 0; i < l.size(); i++) l.get(i).getInventory().setContents(snapshots.get((i + 1) % l.size()));
        say("Inventories swapped!");
    }

    @Override protected void onStop() { say("Inventory Swap has ended."); }
}
