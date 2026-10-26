package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/** An ice wraith with a crystal crown, frozen wings and orbiting ice shards. */
public class MiniBossFrostStrayEvent extends MiniBossEvent {

    public MiniBossFrostStrayEvent(EventCore plugin) {
        super(plugin, "mini_boss_frost_stray", "Mini Boss: Frost Stray", Material.BLUE_ICE);
    }

    @Override protected EntityType type() { return EntityType.STRAY; }
    @Override protected String bossName() { return "Frost Stray"; }
    @Override protected double defaultHealth() { return 150; }
    @Override protected double defaultScale() { return 1.5; }
    @Override protected double modelHeight() { return 1.99; }
    @Override protected BossBar.Color barColor() { return BossBar.Color.BLUE; }

    @Override
    protected void equip(EntityEquipment eq) {
        eq.setItemInMainHand(new ItemStack(Material.BOW));
        eq.setHelmet(new ItemStack(Material.DIAMOND_HELMET));
    }

    @Override
    protected void buildModel(BossModel m) {
        m.crown(Material.BLUE_ICE, 7, true);
        m.wings(Material.PACKED_ICE, false, 0.1, 0.15);
        m.orbiters(Material.BLUE_ICE, false, 4, 1.2, 0.6, 0.08, 0.35f, true);
        m.orbiters(Material.SNOWBALL, true, 5, 0.9, 1.0, -0.12, 0.5f, true);
    }

    @Override
    protected void effects(int t, Location l) {
        // frost ring around the feet
        for (int i = 0; i < 8; i++) {
            double a = t * 0.04 + i * Math.PI / 4;
            l.getWorld().spawnParticle(Particle.SNOWFLAKE, l.clone().add(Math.cos(a) * 2.2, 0.2, Math.sin(a) * 2.2), 1, 0, 0, 0, 0);
        }
        if (t % 8 == 0) l.getWorld().spawnParticle(Particle.CLOUD, l.clone().add(0, 0.2, 0), 4, 0.8, 0.1, 0.8, 0.01);
    }

    @Override
    protected void ability(Player target) {
        // frost nova
        boss.getWorld().spawnParticle(Particle.SNOWFLAKE, boss.getLocation().add(0, 1, 0), 120, 4, 1.5, 4, 0.05);
        forNearby(10, p -> {
            p.setFreezeTicks(240);
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 2));
            p.damage(2.0, boss);
        });
        // frost arrows
        Vector dir = target.getEyeLocation().toVector().subtract(boss.getEyeLocation().toVector()).normalize().multiply(2.8);
        for (int i = 0; i < phase; i++) {
            Arrow a = boss.launchProjectile(Arrow.class, dir.clone().rotateAroundY((i - (phase - 1) / 2.0) * 0.12));
            a.addCustomEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 2), true);
            a.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
        }
    }

    /** Blizzard */
    @Override
    protected void ultimate() {
        boss.getWorld().spawnParticle(Particle.SNOWFLAKE, boss.getLocation().add(0, 2, 0), 300, 8, 3, 8, 0.1);
        forNearby(25, p -> {
            p.setFreezeTicks(400);
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 3));
            p.damage(4.0, boss);
        });
        spawnMinions(EntityType.STRAY, 2, nearestPlayer(), 4);
    }
}
