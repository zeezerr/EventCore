package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import com.example.eventcore.GameEvent;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Base for the mini boss events.
 * Features: custom display-entity model, 3 phases (enrage at 66% and 33% health),
 * an ultimate attack, spawn intro, death shatter + fireworks, boss bar.
 */
public abstract class MiniBossEvent extends GameEvent {

    protected LivingEntity boss;
    protected final List<LivingEntity> minions = new ArrayList<>();
    protected BossModel model;
    protected int phase = 1;

    private BossBar bar;
    private double maxHp;
    private double baseSpeed = 0.25;
    private int ticks, cooldown, abilityUses;

    protected MiniBossEvent(EventCore plugin, String id, String name, Material icon) {
        super(plugin, id, name, icon);
    }

    // ---- to implement ----
    protected abstract EntityType type();
    protected abstract String bossName();
    protected abstract double defaultHealth();
    protected abstract void ability(Player target);
    protected abstract void buildModel(BossModel m);

    // ---- optional hooks ----
    protected double defaultScale() { return 1.0; }
    protected double modelHeight() { return 2.0; }
    protected BossBar.Color barColor() { return BossBar.Color.RED; }
    protected void equip(EntityEquipment eq) { }
    protected void effects(int t, Location l) { }
    protected void ultimate() { shockwave(10, 5); }
    protected void cleanupExtra() { }

    @Override
    protected void onStart() {
        say("A " + bossName() + " will appear in " + getInt("first-spawn-seconds", 10) + " seconds!");
        runTimer(this::trySpawn, getInt("first-spawn-seconds", 10) * 20L, getInt("interval-seconds", 300) * 20L);
        runTimer(this::tick, 2L, 2L);
    }

    // ------------------------------------------------------------------ spawning

    private void trySpawn() {
        if (boss != null && boss.isValid()) return;
        List<Player> ps = players();
        if (ps.isEmpty()) return;
        Player p = ps.get(rnd().nextInt(ps.size()));
        World w = p.getWorld();

        double a = rnd().nextDouble() * Math.PI * 2, d = 15 + rnd().nextDouble() * 10;
        int x = (int) (p.getLocation().getX() + Math.cos(a) * d);
        int z = (int) (p.getLocation().getZ() + Math.sin(a) * d);
        double y = w.getEnvironment() == World.Environment.NORMAL ? w.getHighestBlockYAt(x, z) + 1 : p.getLocation().getY();
        Location loc = new Location(w, x + 0.5, y, z + 0.5);

        Entity en = w.spawnEntity(loc, type());
        if (!(en instanceof LivingEntity le)) { en.remove(); return; }
        boss = le;
        phase = 1;
        ticks = 0;
        abilityUses = 0;
        cooldown = getInt("ability-seconds", 6) * 20;

        maxHp = Math.min(1024.0, getDouble("health", defaultHealth()));
        setBase(le, Attribute.MAX_HEALTH, maxHp);
        le.setHealth(maxHp);
        setBase(le, Attribute.SCALE, getDouble("scale", defaultScale()));
        AttributeInstance sp = le.getAttribute(Attribute.MOVEMENT_SPEED);
        if (sp != null) baseSpeed = sp.getBaseValue();
        le.customName(Component.text(bossName(), NamedTextColor.RED, TextDecoration.BOLD));
        le.setCustomNameVisible(true);
        le.setRemoveWhenFarAway(false);
        if (le instanceof Mob m) m.setTarget(p);

        EntityEquipment eq = le.getEquipment();
        if (eq != null) {
            equip(eq);
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                try { eq.setDropChance(slot, 0f); } catch (IllegalArgumentException ignored) { }
            }
        }

        // ---- model ----
        model = new BossModel(plugin, le, (float) modelHeight());
        String itemModel = plugin.getConfig().getString("events." + id() + ".item-model", "");
        NamespacedKey key = itemModel == null || itemModel.isBlank() ? null : NamespacedKey.fromString(itemModel.trim());
        if (key != null) buildResourcePackModel(key, eq);
        else buildModel(model);
        model.update();

        // ---- boss bar ----
        bar = BossBar.bossBar(Component.text(bossName(), NamedTextColor.WHITE, TextDecoration.BOLD), 1f, barColor(), BossBar.Overlay.NOTCHED_10);
        for (Player pl : Bukkit.getOnlinePlayers()) pl.showBossBar(bar);

        // ---- intro ----
        w.strikeLightningEffect(loc);
        w.playSound(loc, Sound.ENTITY_WITHER_SPAWN, 2f, 0.9f);
        for (Player pl : Bukkit.getOnlinePlayers()) {
            pl.showTitle(Title.title(Component.text(bossName().toUpperCase(), NamedTextColor.RED, TextDecoration.BOLD),
                    Component.text("has awakened", NamedTextColor.GRAY)));
        }
        say(bossName() + " has spawned near " + x + ", " + (int) y + ", " + z + "!");
        le.setInvulnerable(true);
        runLater(() -> { if (boss != null) boss.setInvulnerable(false); }, 60L);
    }

    /** Uses an item model from a resource pack (item_model component) instead of the built-in parts. */
    private void buildResourcePackModel(NamespacedKey key, EntityEquipment eq) {
        ItemStack it = new ItemStack(Material.PAPER);
        it.editMeta(m -> m.setItemModel(key));
        float sc = (float) getDouble("model-scale", 2.0);
        ItemDisplay.ItemDisplayTransform mode = ItemDisplay.ItemDisplayTransform.FIXED;
        try {
            mode = ItemDisplay.ItemDisplayTransform.valueOf(
                    plugin.getConfig().getString("events." + id() + ".model-transform", "FIXED").toUpperCase());
        } catch (IllegalArgumentException ignored) { }
        model.item(it, mode, false, (t, p) -> {
            p.center.set(0, model.h * 0.5f + (float) Math.sin(t * 0.08) * 0.05f, 0);
            p.size.set(sc);
        });
        boss.setInvisible(true);
        if (eq != null) eq.clear();
    }

    // ------------------------------------------------------------------ main loop (every 2 ticks)

    private void tick() {
        if (boss == null) return;
        if (!boss.isValid()) { cleanup(true); return; }
        ticks += 2;

        if (model != null) model.update();
        effects(ticks, boss.getLocation());

        double ratio = Math.max(0.0, Math.min(1.0, boss.getHealth() / maxHp));
        if (bar != null) bar.progress((float) ratio);
        int np = ratio < 0.33 ? 3 : ratio < 0.66 ? 2 : 1;
        if (np > phase) enterPhase(np);

        cooldown -= 2;
        if (cooldown <= 0) {
            cooldown = (int) (getInt("ability-seconds", 6) * 20 / (1.0 + 0.4 * (phase - 1)));
            useAbility();
        }
    }

    private void enterPhase(int np) {
        phase = np;
        Location l = boss.getLocation();
        l.getWorld().strikeLightningEffect(l);
        l.getWorld().playSound(l, Sound.ENTITY_ENDER_DRAGON_GROWL, 2f, 0.8f);
        say(bossName() + " is ENRAGED! (Phase " + phase + "/3)");
        if (bar != null) {
            bar.color(BossBar.Color.RED);
            bar.name(Component.text(bossName() + " - Phase " + phase, NamedTextColor.DARK_RED, TextDecoration.BOLD));
        }
        AttributeInstance sp = boss.getAttribute(Attribute.MOVEMENT_SPEED);
        if (sp != null) sp.setBaseValue(baseSpeed * (1 + 0.25 * (phase - 1)));
        ultimate();
    }

    private void useAbility() {
        Player nearest = nearestPlayer();
        if (nearest == null) return;
        if (boss instanceof Mob m) m.setTarget(nearest);
        abilityUses++;
        if (phase >= 3 && abilityUses % 4 == 0) ultimate();
        else ability(nearest);
    }

    // ------------------------------------------------------------------ helpers for subclasses

    protected Player nearestPlayer() {
        Player nearest = null;
        double best = 40 * 40;
        for (Player p : players()) {
            if (boss == null || !p.getWorld().equals(boss.getWorld())) continue;
            double d = p.getLocation().distanceSquared(boss.getLocation());
            if (d < best) { best = d; nearest = p; }
        }
        return nearest;
    }

    protected void forNearby(double radius, Consumer<Player> action) {
        if (boss == null) return;
        for (Player p : players()) {
            if (p.getWorld().equals(boss.getWorld()) && p.getLocation().distanceSquared(boss.getLocation()) <= radius * radius)
                action.accept(p);
        }
    }

    protected void shockwave(double radius, double damage) {
        Location c = boss.getLocation();
        c.getWorld().spawnParticle(Particle.EXPLOSION, c, 1);
        c.getWorld().playSound(c, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.7f);
        forNearby(radius, p -> {
            p.damage(damage, boss);
            Vector v = p.getLocation().toVector().subtract(c.toVector());
            v.setY(0);
            if (v.lengthSquared() < 0.01) v = new Vector(0, 0, 0.1);
            v.normalize().multiply(1.4).setY(0.6);
            p.setVelocity(v);
        });
    }

    protected void heal(double amount) {
        if (boss != null && boss.isValid()) boss.setHealth(Math.min(maxHp, boss.getHealth() + amount));
    }

    protected void spawnMinions(EntityType t, int count, Player target, int cap) {
        minions.removeIf(m -> !m.isValid());
        for (int i = 0; i < count && minions.size() < cap; i++) {
            Location l = boss.getLocation().add(rnd().nextInt(-3, 4), 0, rnd().nextInt(-3, 4));
            Entity en = boss.getWorld().spawnEntity(l, t);
            if (en instanceof LivingEntity le) {
                minions.add(le);
                if (le instanceof Mob m && target != null) m.setTarget(target);
            }
        }
    }

    // ------------------------------------------------------------------ lifecycle

    private void cleanup(boolean removeBoss) {
        cleanupExtra();
        if (model != null) { model.remove(); model = null; }
        if (boss != null && removeBoss) boss.remove();
        boss = null;
        minions.forEach(Entity::remove);
        minions.clear();
        if (bar != null) for (Player p : Bukkit.getOnlinePlayers()) p.hideBossBar(bar);
        bar = null;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (bar != null) e.getPlayer().showBossBar(bar);
    }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        LivingEntity dead = e.getEntity();
        if (minions.remove(dead)) { e.getDrops().clear(); return; }
        if (boss == null || !dead.getUniqueId().equals(boss.getUniqueId())) return;

        Location loc = dead.getLocation().add(0, 1, 0);
        World w = loc.getWorld();

        e.getDrops().clear();
        e.setDroppedExp(getInt("xp", 500));
        for (String s : getStringList("rewards")) {
            ItemStack it = parseItem(s);
            if (it != null) e.getDrops().add(it);
        }

        // death show
        if (model != null) { model.shatter(); model = null; }
        w.spawnParticle(Particle.EXPLOSION_EMITTER, loc, 3, 1, 1, 1, 0);
        w.spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 120, 1.5, 1.5, 1.5, 0.4);
        w.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2f, 0.6f);
        Firework fw = w.spawn(loc, Firework.class, f -> {
            FireworkMeta fm = f.getFireworkMeta();
            fm.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE)
                    .withColor(Color.RED, Color.ORANGE).withFade(Color.YELLOW).flicker(true).trail(true).build());
            f.setFireworkMeta(fm);
        });
        fw.detonate();

        Player killer = dead.getKiller();
        say(bossName() + " was slain" + (killer != null ? " by " + killer.getName() : "") + "!");
        cleanup(false);
    }

    @Override
    protected void onStop() {
        cleanup(true);
        say(displayName() + " has ended.");
    }
}
