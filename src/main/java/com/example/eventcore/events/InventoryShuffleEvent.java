package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import com.example.eventcore.GameEvent;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class InventoryShuffleEvent extends GameEvent {

    public InventoryShuffleEvent(EventCore plugin) {
        super(plugin, "inventory_shuffle", "Inventory Shuffle", Material.HOPPER);
    }

    @Override
    protected void onStart() {
        long period = getInt("interval-seconds", 90) * 20L;
        runTimer(() -> {
            for (Player p : players()) {
                ItemStack[] contents = p.getInventory().getStorageContents();
                List<ItemStack> list = Arrays.asList(contents);
                Collections.shuffle(list);
                p.getInventory().setStorageContents(contents);
            }
            say("Everyone's inventory was shuffled!");
        }, period, period);
    }

    @Override protected void onStop() { say("Inventory Shuffle has ended."); }
}
