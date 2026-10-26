package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** A hooded reaper with a tattered cape, a floating scythe and orbiting soul lanterns. */
public class MiniBossGraveKeeperEvent extends MiniBossEvent {

    public MiniBossGraveKeeperEvent(EventCore plugin) {
        super(plugin, "mini_boss_gravekeeper", "Mini Boss: GraveKeeper", Material.WITHER_SKELETON_SKULL);
    }

    @Override protected EntityType type() { return EntityType.WITHER_SKELETON; }
    @Override protected String bossName() { return "GraveKeeper"; }
    @Override protected double defaultHealth() { return 200; }
    @Override protected double defaultScale() { return 1.6; }
    @Override protected double modelHeight() { return 2.4; }
    @Override protected BossBar.Color barColor() { return BossBar.Color.PURPLE; }

    @Override
    protected void equip(EntityEquipment eq) {
        ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
        sword.addUnsafeEnchantment(Enchantment.SHARPNESS, 3);
        eq.setItemInMainHand(sword);
        eq.setHelmet(new ItemStack(Material.NETHERITE_HELMET));
    }

    @Override
    protected void buildModel(BossModel m) {
        // tattered cape that sways
        m.block(Material.BLACK_WOOL, false, (t, p) -> {
            p.center.set(0, m.h * 0.55f, -0.38f);
            p.size.set(1.0f, m.h * 0.95f, 0.08f);
            p.rot.rotationX(0.08f + 0.06f * (float) Math.sin(t * 0.12));
        });
        // bone crown + soul lanterns + scythe
        m.crown(Material.BONE_BLOCK, 7, false);
        m.orbiters(Material.SOUL_LANTERN, false, 5, 1.5, 0.5, 0.07, 0.45f, true);
        m.item(new ItemStack(Material.NETHERITE_HOE), ItemDisplay.ItemDisplayTransform.FIXED, false, (t, p) -> {
            p.center.set(1.0f, m.h * 0.6f, 0.3f);
            p.size.set(2.4f);
            p.rot.rotationZ(-0.7f + 0.25f * (float) Math.sin(t * 0.1));
        });
    }

    @Override
    protected void effects(int t, Location l) {
        World w = l.getWorld();
        w.spawnParticle(Particle.SOUL, l.clone().add(0, 0.3, 0), 2, 0.6, 0.1, 0.6, 0.02);
        if (t % 8 == 0) w.spawnParticle(Particle.SOUL_FIRE_FLAME, l.clone().add(0, 1.5, 0), 6, 0.6, 0.8, 0.6, 0.01);
        if (phase >= 3 && t % 6 == 0) w.spawnParticle(Particle.SCULK_SOUL, l.clone().add(0, 2, 0), 4, 0.5, 0.8, 0.5, 0.02);
    }

    @Override
    protected void ability(Player target) {
        spawnMinions(EntityType.SKELETON, 1 + phase, target, 8);
        // soul drain: hurts nearby players and heals the boss
        forNearby(6, p -> {
            p.damage(3.0, boss);
            heal(3.0);
        });
        boss.getWorld().spawnParticle(Particle.SOUL, boss.getLocation().add(0, 1, 0), 40, 3, 1, 3, 0.05);
    }

    /** Soul Storm */
    @Override
    protected void ultimate() {
        spawnMinions(EntityType.SKELETON, 4, nearestPlayer(), 12);
        forNearby(20, p -> {
            p.damage(4.0, boss);
            p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 0));
        });
        boss.getWorld().spawnParticle(Particle.SOUL, boss.getLocation().add(0, 1, 0), 150, 4, 1.5, 4, 0.05);
    }
}
