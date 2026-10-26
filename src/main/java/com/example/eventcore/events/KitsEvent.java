package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import com.example.eventcore.GameEvent;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

public class KitsEvent extends GameEvent {

    public KitsEvent(EventCore plugin) {
        super(plugin, "kits", "Kits", Material.IRON_CHESTPLATE);
    }

    @Override
    protected void onStart() {
        for (Player p : players()) give(p);
        say("Everyone received the kit!");
    }

    private void give(Player p) {
        if (getBool("clear-first", false)) p.getInventory().clear();
        for (String s : getStringList("items")) {
            ItemStack it = parseItem(s);
            if (it == null) continue;
            String n = it.getType().name();
            if (n.endsWith("_HELMET")) p.getInventory().setHelmet(it);
            else if (n.endsWith("_CHESTPLATE")) p.getInventory().setChestplate(it);
            else if (n.endsWith("_LEGGINGS")) p.getInventory().setLeggings(it);
            else if (n.endsWith("_BOOTS")) p.getInventory().setBoots(it);
            else p.getInventory().addItem(it).values().forEach(l -> p.getWorld().dropItemNaturally(p.getLocation(), l));
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        if (getBool("give-on-respawn", true))
            Bukkit.getScheduler().runTaskLater(plugin, () -> give(e.getPlayer()), 2L);
    }

    @Override protected void onStop() { say("Kits has ended."); }
}
