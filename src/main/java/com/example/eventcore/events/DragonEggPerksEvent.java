package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import com.example.eventcore.GameEvent;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class DragonEggPerksEvent extends GameEvent {

    private int sinceBroadcast;

    public DragonEggPerksEvent(EventCore plugin) {
        super(plugin, "dragon_egg_perks", "Dragon Egg Perks", Material.DRAGON_EGG);
    }

    @Override
    protected void onStart() {
        sinceBroadcast = 0;
        boolean anyone = false;
        for (Player p : players()) if (p.getInventory().contains(Material.DRAGON_EGG)) anyone = true;
        if (!anyone && getBool("give-to-random", true)) {
            List<Player> ps = players();
            if (!ps.isEmpty()) {
                Player p = ps.get(rnd().nextInt(ps.size()));
                p.getInventory().addItem(new ItemStack(Material.DRAGON_EGG));
                say(p.getName() + " received the Dragon Egg!");
            }
        }
        say("Hold the Dragon Egg in your inventory for powerful perks - but everyone will hunt you!");

        runTimer(() -> {
            int broadcast = getInt("broadcast-seconds", 60);
            sinceBroadcast++;
            for (Player p : players()) {
                if (!p.getInventory().contains(Material.DRAGON_EGG)) continue;
                p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 80, 0, true, false));
                p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 80, 0, true, false));
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 80, 0, true, false));
                p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 80, 0, true, false));
                if (sinceBroadcast >= broadcast) {
                    say(p.getName() + " holds the Dragon Egg at " + p.getLocation().getBlockX() + ", "
                            + p.getLocation().getBlockY() + ", " + p.getLocation().getBlockZ());
                    sinceBroadcast = 0;
                }
            }
        }, 20L, 20L);
    }

    @Override protected void onStop() { say("Dragon Egg Perks has ended."); }
}
