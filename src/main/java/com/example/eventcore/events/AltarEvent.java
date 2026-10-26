package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import com.example.eventcore.GameEvent;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class AltarEvent extends GameEvent {

    private static final PotionEffectType[] BLESSINGS = {
            PotionEffectType.SPEED, PotionEffectType.STRENGTH, PotionEffectType.RESISTANCE,
            PotionEffectType.REGENERATION, PotionEffectType.FIRE_RESISTANCE, PotionEffectType.HASTE,
            PotionEffectType.JUMP_BOOST, PotionEffectType.NIGHT_VISION};

    private Block altar;
    private BlockData previous;
    private Material offering;

    public AltarEvent(EventCore plugin) {
        super(plugin, "altar", "Altar", Material.ENCHANTING_TABLE);
    }

    @Override
    public boolean usesLocation() { return true; }

    @Override
    protected void onStart() {
        altar = getLocation("location", spawnLocation()).getBlock();
        previous = altar.getBlockData();
        altar.setType(Material.ENCHANTING_TABLE, false);
        offering = Material.matchMaterial(plugin.getConfig().getString("events.altar.offering", "DIAMOND"));
        if (offering == null) offering = Material.DIAMOND;
        say("An altar appeared at " + altar.getX() + ", " + altar.getY() + ", " + altar.getZ()
                + ". Right-click it holding " + pretty(offering) + " for a random blessing!");
        runTimer(() -> altar.getWorld().spawnParticle(Particle.ENCHANT, altar.getLocation().add(0.5, 1.5, 0.5), 40, 0.5, 0.5, 0.5, 0.5),
                10L, 10L);
    }

    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getHand() != EquipmentSlot.HAND) return;
        if (e.getClickedBlock() == null || !e.getClickedBlock().equals(altar)) return;
        e.setCancelled(true);
        Player p = e.getPlayer();
        ItemStack hand = p.getInventory().getItemInMainHand();
        if (hand.getType() != offering) {
            p.sendActionBar(net.kyori.adventure.text.Component.text("Offer 1 " + pretty(offering) + " to the altar."));
            return;
        }
        hand.setAmount(hand.getAmount() - 1);
        PotionEffectType t = BLESSINGS[rnd().nextInt(BLESSINGS.length)];
        int seconds = getInt("effect-seconds", 120);
        p.addPotionEffect(new PotionEffect(t, seconds * 20, 1));
        say(p.getName() + " received a blessing from the altar!");
    }

    @Override
    protected void onStop() {
        if (altar != null && previous != null) altar.setBlockData(previous, false);
        say("The altar has vanished.");
    }
}
