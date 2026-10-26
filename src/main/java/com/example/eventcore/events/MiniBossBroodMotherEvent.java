package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

/** A huge spider with pulsing egg sacs, glowing eyes and extra scuttling legs. */
public class MiniBossBroodMotherEvent extends MiniBossEvent {

    private final List<Block> webs = new ArrayList<>();

    public MiniBossBroodMotherEvent(EventCore plugin) {
        super(plugin, "mini_boss_brood_mother", "Mini Boss: Brood Mother", Material.SPIDER_EYE);
    }

    @Override protected EntityType type() { return EntityType.SPIDER; }
    @Override protected String bossName() { return "Brood Mother"; }
    @Override protected double defaultHealth() { return 200; }
    @Override protected double defaultScale() { return 2.5; }
    @Override protected double modelHeight() { return 0.9; }
    @Override protected BossBar.Color barColor() { return BossBar.Color.GREEN; }

    @Override
    protected void buildModel(BossModel m) {
        // pulsing egg sacs on the back
        float[][] sacs = {{0.25f, 1.15f, -0.45f}, {-0.25f, 1.15f, -0.45f}, {0f, 1.35f, -0.65f}, {0.3f, 1.0f, -0.7f}, {-0.3f, 1.0f, -0.7f}};
        for (int i = 0; i < sacs.length; i++) {
            final float[] s = sacs[i];
            final int idx = i;
            m.block(Material.SLIME_BLOCK, true, (t, p) -> {
                float pulse = 0.5f + 0.08f * (float) Math.sin(t * 0.15 + idx);
                p.center.set(s[0], s[1] * m.h, s[2]);
                p.size.set(pulse);
            });
        }
        // four glowing red eyes
        float[][] eyes = {{0.12f, 0.85f}, {-0.12f, 0.85f}, {0.22f, 0.7f}, {-0.22f, 0.7f}};
        for (float[] e : eyes) {
            m.block(Material.REDSTONE_BLOCK, true, (t, p) -> {
                p.center.set(e[0], e[1] * m.h, 0.5f);
                p.size.set(0.1f);
            });
        }
        // extra legs
        for (int side = -1; side <= 1; side += 2) {
            for (int i = 0; i < 3; i++) {
                final int s = side, idx = i;
                m.block(Material.BLACK_CONCRETE, false, (t, p) -> {
                    p.center.set(s * 0.8f, 0.45f * m.h, (idx - 1) * 0.45f);
                    p.size.set(1.2f, 0.09f, 0.09f);
                    p.rot.rotationZ(s * (0.5f + 0.12f * (float) Math.sin(t * 0.4 + idx)));
                });
            }
        }
    }

    @Override
    protected void effects(int t, Location l) {
        if (t % 6 == 0) {
            l.getWorld().spawnParticle(Particle.SPORE_BLOSSOM_AIR, l.clone().add(0, 1.5, 0), 6, 1.2, 0.8, 1.2, 0);
            l.getWorld().spawnParticle(Particle.ITEM_SLIME, l.clone().add(0, 1.0, 0), 3, 1.0, 0.5, 1.0, 0);
        }
    }

    @Override
    protected void ability(Player target) {
        spawnMinions(EntityType.CAVE_SPIDER, 2 + phase, target, 12);
        if (target.getLocation().distanceSquared(boss.getLocation()) <= 100) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 0));
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
        }
    }

    /** Web Trap */
    @Override
    protected void ultimate() {
        spawnMinions(EntityType.CAVE_SPIDER, 6, nearestPlayer(), 16);
        forNearby(25, p -> {
            Block b = p.getLocation().getBlock();
            if (b.getType().isAir()) {
                b.setType(Material.COBWEB);
                webs.add(b);
            }
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1));
        });
        runLater(this::clearWebs, 120L);
    }

    private void clearWebs() {
        for (Block b : webs) if (b.getType() == Material.COBWEB) b.setType(Material.AIR);
        webs.clear();
    }

    @Override protected void cleanupExtra() { clearWebs(); }
}
