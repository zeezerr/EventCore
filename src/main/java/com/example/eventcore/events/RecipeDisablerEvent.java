package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import com.example.eventcore.GameEvent;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class RecipeDisablerEvent extends GameEvent {

    private Set<Material> disabled;

    public RecipeDisablerEvent(EventCore plugin) {
        super(plugin, "recipe_disabler", "Recipe Disabler", Material.CRAFTING_TABLE);
    }

    @Override
    protected void onStart() {
        disabled = materialSet("disabled");
        say("Disabled recipes: " + disabled);
    }

    @EventHandler
    public void onPrepare(PrepareItemCraftEvent e) {
        if (e.getRecipe() != null && disabled.contains(e.getRecipe().getResult().getType()))
            e.getInventory().setResult(new ItemStack(Material.AIR));
    }

    @Override protected void onStop() { say("Recipes restored."); }
}
