package com.example.eventcore.events;

import org.bukkit.Material;
import org.bukkit.Bukkit;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.Location;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A "custom model" built out of Block/Item Display entities that follow a boss.
 * Each part has an animation callback that is run every 2 ticks and sets the part's
 * centre (local space: x = right, y = up, z = forward), size and rotation.
 * Model units are "unscaled"; everything is multiplied by the boss' SCALE attribute.
 */
public final class BossModel {

    @FunctionalInterface
    public interface Anim { void apply(int t, Part p); }

    public static final class Part {
        final Display display;
        final boolean block;
        final Anim anim;
        public final Vector3f center = new Vector3f();
        public final Vector3f size = new Vector3f(1, 1, 1);
        public final Quaternionf rot = new Quaternionf();

        Part(Display display, boolean block, Anim anim) {
            this.display = display;
            this.block = block;
            this.anim = anim;
        }

        void reset() {
            center.set(0, 0, 0);
            size.set(1, 1, 1);
            rot.identity();
        }
    }

    private final JavaPlugin plugin;
    private final LivingEntity boss;
    /** Unscaled height of the boss body, used to place heads/crowns. */
    public final float h;
    private final List<Part> parts = new ArrayList<>();
    private int t;

    public BossModel(JavaPlugin plugin, LivingEntity boss, float baseHeight) {
        this.plugin = plugin;
        this.boss = boss;
        this.h = baseHeight;
    }

    private static void setup(Display d, boolean glow) {
        d.setPersistent(false);
        d.setTeleportDuration(3);
        d.setInterpolationDelay(0);
        d.setInterpolationDuration(3);
        if (glow) d.setBrightness(new Display.Brightness(15, 15));
    }

    public Part block(Material mat, boolean glow, Anim anim) {
        BlockDisplay d = boss.getWorld().spawn(boss.getLocation(), BlockDisplay.class, e -> {
            e.setBlock(mat.createBlockData());
            setup(e, glow);
        });
        Part p = new Part(d, true, anim);
        parts.add(p);
        return p;
    }

    public Part item(ItemStack stack, ItemDisplay.ItemDisplayTransform mode, boolean glow, Anim anim) {
        ItemDisplay d = boss.getWorld().spawn(boss.getLocation(), ItemDisplay.class, e -> {
            e.setItemStack(stack);
            e.setItemDisplayTransform(mode);
            setup(e, glow);
        });
        Part p = new Part(d, false, anim);
        parts.add(p);
        return p;
    }

    private float scale() {
        AttributeInstance a = boss.getAttribute(Attribute.SCALE);
        return a == null ? 1f : (float) Math.max(0.0625, a.getValue());
    }

    /** Advance animations by 2 ticks and push everything to the clients. */
    public void update() {
        t += 2;
        float k = scale();
        Location l = boss.getLocation();
        l.setPitch(0);
        for (Part p : parts) {
            if (!p.display.isValid()) continue;
            p.reset();
            p.anim.apply(t, p);

            Vector3f size = new Vector3f(p.size).mul(k);
            Vector3f translation = new Vector3f(p.center).mul(k);
            if (p.block) {
                // rotate around the centre of the box instead of its corner
                Vector3f half = new Vector3f(size).mul(0.5f);
                p.rot.transform(half);
                translation.sub(half);
            }
            p.display.setInterpolationDelay(0);
            p.display.setInterpolationDuration(3);
            p.display.setTransformation(new Transformation(translation, new Quaternionf(p.rot), size, new Quaternionf()));
            p.display.teleport(l);
        }
    }

    public void remove() {
        for (Part p : parts) p.display.remove();
        parts.clear();
    }

    /** Death animation: every part flies outwards while shrinking, then disappears. */
    public void shatter() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        for (Part p : parts) {
            if (!p.display.isValid()) continue;
            Transformation cur = p.display.getTransformation();
            Vector3f away = new Vector3f(cur.getTranslation())
                    .add((float) r.nextDouble(-3, 3), (float) r.nextDouble(0.5, 3), (float) r.nextDouble(-3, 3));
            p.display.setInterpolationDelay(0);
            p.display.setInterpolationDuration(20);
            p.display.setTransformation(new Transformation(away, cur.getLeftRotation(), new Vector3f(0, 0, 0), cur.getRightRotation()));
        }
        List<Part> copy = new ArrayList<>(parts);
        parts.clear();
        Bukkit.getScheduler().runTaskLater(plugin, () -> copy.forEach(p -> p.display.remove()), 24L);
    }

    // ------------------------------------------------------------------
    // Reusable model pieces
    // ------------------------------------------------------------------

    /** Objects circling the boss. */
    public void orbiters(Material mat, boolean asItem, int count, double radius, double yFrac,
                         double speed, float size, boolean glow) {
        for (int i = 0; i < count; i++) {
            final double off = Math.PI * 2 * i / count;
            Anim a = (tt, p) -> {
                double ang = off + tt * speed;
                p.center.set((float) (Math.cos(ang) * radius),
                        (float) (h * yFrac + Math.sin(tt * 0.1 + off) * 0.15),
                        (float) (Math.sin(ang) * radius));
                p.size.set(size);
                p.rot.rotationY((float) (tt * 0.15));
            };
            if (asItem) item(new ItemStack(mat), ItemDisplay.ItemDisplayTransform.FIXED, glow, a);
            else block(mat, glow, a);
        }
    }

    /** Two flapping wings, three panels each. */
    public void wings(Material mat, boolean glow, double speed, double amp) {
        final float[] lens = {0.9f, 0.8f, 0.6f};
        final float[] widths = {0.8f, 0.6f, 0.4f};
        for (int side = -1; side <= 1; side += 2) {
            for (int i = 0; i < 3; i++) {
                final int s = side, idx = i;
                block(mat, glow, (tt, p) -> {
                    Quaternionf q = new Quaternionf().rotateY(s * 0.5f)
                            .rotateZ((float) (s * (0.3 + amp * Math.sin(tt * speed))));
                    Vector3f d = new Vector3f(s, 0, 0);
                    q.transform(d);
                    float along = lens[idx] / 2f;
                    for (int j = 0; j < idx; j++) along += lens[j];
                    p.center.set(s * 0.25f, h * 0.75f, -0.3f).add(d.x * along, d.y * along, d.z * along);
                    p.size.set(lens[idx], 0.06f, widths[idx]);
                    p.rot.set(q);
                });
            }
        }
    }

    /** Two horns on the head. */
    public void horns(Material mat, boolean glow) {
        for (int side = -1; side <= 1; side += 2) {
            final int s = side;
            block(mat, glow, (tt, p) -> {
                p.center.set(s * 0.32f, h * 0.98f + 0.25f, 0.05f);
                p.size.set(0.12f, 0.7f, 0.12f);
                p.rot.rotationZ(-s * 0.5f);
            });
        }
    }

    /** A ring of spikes above the head. */
    public void crown(Material mat, int spikes, boolean glow) {
        for (int i = 0; i < spikes; i++) {
            final double a = Math.PI * 2 * i / spikes;
            block(mat, glow, (tt, p) -> {
                p.center.set((float) Math.cos(a) * 0.32f, h + 0.2f, (float) Math.sin(a) * 0.32f);
                p.size.set(0.1f, 0.45f + 0.06f * (float) Math.sin(tt * 0.1 + a * 2), 0.1f);
                p.rot.rotationAxis(0.35f, (float) Math.sin(a), 0f, (float) -Math.cos(a));
            });
        }
    }
}
