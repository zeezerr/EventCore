package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/** A skeletal marksman with bone wings, a horned skull, a floating scope and orbiting arrows. */
public class MiniBossBoneSniperEvent extends MiniBossEvent {

    public MiniBossBoneSniperEvent(EventCore plugin) {
        super(plugin, "mini_boss_bone_sniper", "Mini Boss: Bone Sniper", Material.BOW);
    }

    @Override protected EntityType type() { return EntityType.SKELETON; }
    @Override protected String bossName() { return "Bone Sniper"; }
    @Override protected double defaultHealth() { return 120; }
    @Override protected double defaultScale() { return 1.4; }
    @Override protected double modelHeight() { return 1.99; }
    @Override protected BossBar.Color barColor() { return BossBar.Color.WHITE; }

    @Override
    protected void equip(EntityEquipment eq) {
        ItemStack bow = new ItemStack(Material.BOW);
        bow.addUnsafeEnchantment(Enchantment.POWER, 5);
        eq.setItemInMainHand(bow);
        eq.setHelmet(new ItemStack(Material.IRON_HELMET));
    }

    @Override
    protected void buildModel(BossModel m) {
        m.wings(Material.BONE_BLOCK, false, 0.18, 0.25);
        m.horns(Material.BONE_BLOCK, false);
        m.orbiters(Material.ARROW, true, 4, 1.0, 0.8, 0.12, 0.8f, false);
        // floating scope in front of the head
        m.item(new ItemStack(Material.SPYGLASS), ItemDisplay.ItemDisplayTransform.FIXED, true, (t, p) -> {
            p.center.set(0.3f, m.h * 0.95f + (float) Math.sin(t * 0.1) * 0.05f, 0.45f);
            p.size.set(0.8f);
            p.rot.rotationY((float) Math.sin(t * 0.05) * 0.3f);
        });
    }

    @Override
    protected void effects(int t, Location l) {
        if (t % 6 == 0) l.getWorld().spawnParticle(Particle.ASH, l.clone().add(0, 1.2, 0), 8, 0.6, 0.8, 0.6, 0.01);
        if (phase >= 2 && t % 10 == 0) l.getWorld().spawnParticle(Particle.CRIT, l.clone().add(0, 2.4, 0), 6, 0.3, 0.3, 0.3, 0.05);
    }

    private void volley(Player target, int n, double speed) {
        Vector dir = target.getEyeLocation().toVector().subtract(boss.getEyeLocation().toVector()).normalize().multiply(speed);
        for (int i = -n / 2; i <= n / 2; i++) {
            Arrow a = boss.launchProjectile(Arrow.class, dir.clone().rotateAroundY(i * 0.1));
            a.addCustomEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1), true);
            a.setDamage(4.0);
            a.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
        }
    }

    @Override
    protected void ability(Player target) {
        volley(target, 2 + 2 * phase, 2.8);
    }

    /** Arrow Rain */
    @Override
    protected void ultimate() {
        forNearby(30, p -> {
            for (int i = 0; i < 10; i++) {
                Location l = p.getLocation().add(rnd().nextInt(-3, 4), 14, rnd().nextInt(-3, 4));
                Arrow a = p.getWorld().spawnArrow(l, new Vector(0, -1, 0), 1.6f, 4f);
                a.setShooter(boss);
                a.setDamage(5.0);
                a.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
            }
        });
    }
}
