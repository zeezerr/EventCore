package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/** A burning blaze with fire wings, a magma crown, a spinning lava core and orbiting fire charges. */
public class MiniBossInfernoBlazeEvent extends MiniBossEvent {

    public MiniBossInfernoBlazeEvent(EventCore plugin) {
        super(plugin, "mini_boss_inferno_blaze", "Mini Boss: Inferno Blaze", Material.BLAZE_ROD);
    }

    @Override protected EntityType type() { return EntityType.BLAZE; }
    @Override protected String bossName() { return "Inferno Blaze"; }
    @Override protected double defaultHealth() { return 150; }
    @Override protected double defaultScale() { return 2.0; }
    @Override protected double modelHeight() { return 1.8; }
    @Override protected BossBar.Color barColor() { return BossBar.Color.RED; }

    @Override
    protected void buildModel(BossModel m) {
        m.wings(Material.MAGMA_BLOCK, true, 0.25, 0.35);
        m.horns(Material.MAGMA_BLOCK, true);
        m.crown(Material.SHROOMLIGHT, 5, true);
        m.orbiters(Material.FIRE_CHARGE, true, 6, 1.8, 0.55, 0.15, 0.8f, true);
        // pulsing lava core
        m.block(Material.MAGMA_BLOCK, true, (t, p) -> {
            float s = 0.7f + 0.12f * (float) Math.sin(t * 0.2);
            p.center.set(0, m.h * 0.5f, 0);
            p.size.set(s);
            p.rot.rotationYXZ(t * 0.07f, t * 0.05f, 0f);
        });
    }

    @Override
    protected void effects(int t, Location l) {
        l.getWorld().spawnParticle(Particle.FLAME, l.clone().add(0, 1.5, 0), 3, 0.7, 0.9, 0.7, 0.01);
        if (t % 8 == 0) l.getWorld().spawnParticle(Particle.LAVA, l.clone().add(0, 2, 0), 2, 0.6, 0.3, 0.6, 0);
        if (phase >= 3 && t % 6 == 0) l.getWorld().spawnParticle(Particle.SMOKE, l.clone().add(0, 2.5, 0), 6, 0.5, 0.5, 0.5, 0.02);
    }

    @Override
    protected void ability(Player target) {
        Vector dir = target.getEyeLocation().toVector().subtract(boss.getEyeLocation().toVector()).normalize();
        for (int i = -phase; i <= phase; i++) {
            Vector d = dir.clone().rotateAroundY(i * 0.14);
            Fireball f = boss.launchProjectile(Fireball.class, d);
            f.setDirection(d);
            f.setYield(1.5f);
            f.setIsIncendiary(true);
        }
        target.setFireTicks(60);
    }

    /** Meteor Shower */
    @Override
    protected void ultimate() {
        forNearby(30, p -> {
            for (int i = 0; i < 4; i++) {
                Location l = p.getLocation().add(rnd().nextInt(-4, 5), 18, rnd().nextInt(-4, 5));
                LargeFireball f = l.getWorld().spawn(l, LargeFireball.class, fb -> {
                    fb.setDirection(new Vector(0, -1, 0));
                    fb.setYield(2f);
                    fb.setIsIncendiary(true);
                });
                f.setShooter(boss);
            }
        });
    }
}
