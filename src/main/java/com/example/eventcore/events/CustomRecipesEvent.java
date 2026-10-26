package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import com.example.eventcore.GameEvent;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomRecipesEvent extends GameEvent {

    private final List<NamespacedKey> keys = new ArrayList<>();

    public CustomRecipesEvent(EventCore plugin) {
        super(plugin, "custom_recipes", "Custom Recipes", Material.CRAFTING_TABLE);
    }

    @Override
    protected void onStart() {
        for (Map<?, ?> m : plugin.getConfig().getMapList("events.custom_recipes.recipes")) {
            try {
                register(m);
            } catch (Exception ex) {
                plugin.getLogger().warning("Bad custom recipe: " + ex.getMessage());
            }
        }
        say(keys.size() + " custom recipe(s) unlocked! Check your recipe book.");
    }

    private void register(Map<?, ?> m) {
        String key = String.valueOf(m.get("key")).toLowerCase();
        ItemStack result = parseItem(String.valueOf(m.get("result")));
        if (result == null) throw new IllegalArgumentException("unknown result for " + key);
        List<?> shape = (List<?>) m.get("shape");
        Map<?, ?> ing = (Map<?, ?>) m.get("ingredients");

        NamespacedKey nk = new NamespacedKey(plugin, key);
        ShapedRecipe r = new ShapedRecipe(nk, result);
        r.shape(shape.stream().map(String::valueOf).toArray(String[]::new));
        for (Map.Entry<?, ?> en : ing.entrySet()) {
            Material mat = Material.matchMaterial(String.valueOf(en.getValue()));
            if (mat == null) throw new IllegalArgumentException("unknown ingredient " + en.getValue());
            r.setIngredient(String.valueOf(en.getKey()).charAt(0), mat);
        }
        Bukkit.addRecipe(r);
        keys.add(nk);
        for (Player p : Bukkit.getOnlinePlayers()) p.discoverRecipe(nk);
    }

    @Override
    protected void onStop() {
        for (NamespacedKey k : keys) Bukkit.removeRecipe(k);
        keys.clear();
        say("Custom recipes removed.");
    }
}
