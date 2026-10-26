package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/** A golden-crowned mage with a spinning totem, orbiting spellbooks and a rune circle. */
public class MiniBossEvokerEvent extends MiniBossEvent {

    public MiniBossEvokerEvent(EventCore plugin) {
        super(plugin, "mini_boss_evoker", "Mini Boss: The Evoker", Material.TOTEM_OF_UNDYING);
    }

    @Override protected EntityType type() { return EntityType.EVOKER; }
    @Override protected String bossName() { return "The Evoker"; }
    @Override protected double defaultHealth() { return 150; }
    @Override protected double defaultScale() { return 1.4; }
    @Override protected double modelHeight() { return 1.95; }
    @Override protected BossBar.Color barColor() { return BossBar.Color.YELLOW; }

    @Override
    protected void buildModel(BossModel m) {
        m.crown(Material.GOLD_BLOCK, 6, true);
        // spinning totem above the head
        m.item(new ItemStack(Material.TOTEM_OF_UNDYING), ItemDisplay.ItemDisplayTransform.FIXED, true, (t, p) -> {
            p.center.set(0, m.h + 0.9f + (float) Math.sin(t * 0.08) * 0.1f, 0);
            p.size.set(1.4f);
            p.rot.rotationY(t * 0.1f);
        });
        m.orbiters(Material.ENCHANTED_BOOK, true, 3, 1.3, 0.8, 0.09, 0.7f, true);
        m.orbiters(Material.EMERALD, true, 4, 0.8, 1.1, -0.14, 0.4f, true);
    }

    @Override
    protected void effects(int t, Location l) {
        // rotating rune circle on the ground
        for (int i = 0; i < 6; i++) {
            double a = t * 0.05 + i * Math.PI / 3;
            l.getWorld().spawnParticle(Particle.ENCHANT, l.clone().add(Math.cos(a) * 3, 0.1, Math.sin(a) * 3), 1, 0, 0, 0, 0);
        }
        if (t % 8 == 0) l.getWorld().spawnParticle(Particle.WITCH, l.clone().add(0, 2.2, 0), 6, 0.4, 0.4, 0.4, 0);
    }

    @Override
    protected void ability(Player target) {
        LivingEntity b = boss;
        Vector dir = target.getLocation().toVector().subtract(b.getLocation().toVector());
        dir.setY(0);
        if (dir.lengthSquared() == 0) return;
        dir.normalize();
        for (int i = 1; i <= 12; i++) {
            Location l = b.getLocation().add(dir.clone().multiply(i));
            l.setY(target.getLocation().getY());
            b.getWorld().spawn(l, EvokerFangs.class, f -> f.setOwner(b));
        }
        spawnMinions(EntityType.VEX, phase, target, 6);
    }

    /** Judgement: fang rings around every player */
    @Override
    protected void ultimate() {
        LivingEntity b = boss;
        forNearby(30, p -> {
            for (int i = 0; i < 8; i++) {
                double a = Math.PI * 2 * i / 8;
                Location l = p.getLocation().add(Math.cos(a) * 2, 0, Math.sin(a) * 2);
                b.getWorld().spawn(l, EvokerFangs.class, f -> f.setOwner(b));
            }
        });
        spawnMinions(EntityType.VEX, 3, nearestPlayer(), 8);
    }
}
