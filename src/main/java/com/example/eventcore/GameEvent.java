package com.example.eventcore;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Base class for every event. Extend it, implement onStart/onStop,
 * add @EventHandler methods, and schedule work with runTimer()/runLater()
 * so everything is cleaned up automatically when the event stops.
 */
public abstract class GameEvent implements Listener {

    protected final EventCore plugin;
    private final String id;
    private final String displayName;
    private final Material icon;
    private final List<BukkitTask> tasks = new ArrayList<>();
    private boolean running;

    protected GameEvent(EventCore plugin, String id, String displayName, Material icon) {
        this.plugin = plugin;
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
    }

    protected abstract void onStart();
    protected abstract void onStop();

    public final void start() {
        if (running) return;
        running = true;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        onStart();
    }

    public final void stop() {
        if (!running) return;
        running = false;
        HandlerList.unregisterAll(this);
        tasks.forEach(BukkitTask::cancel);
        tasks.clear();
        onStop();
    }

    protected BukkitTask runTimer(Runnable r, long delayTicks, long periodTicks) {
        BukkitTask t = Bukkit.getScheduler().runTaskTimer(plugin, r, delayTicks, periodTicks);
        tasks.add(t);
        return t;
    }

    protected BukkitTask runLater(Runnable r, long delayTicks) {
        BukkitTask t = Bukkit.getScheduler().runTaskLater(plugin, r, delayTicks);
        tasks.add(t);
        return t;
    }

    // ---- config helpers: events.<id>.<key> ----
    protected int getInt(String key, int def) { return plugin.getConfig().getInt("events." + id + "." + key, def); }
    protected double getDouble(String key, double def) { return plugin.getConfig().getDouble("events." + id + "." + key, def); }
    protected boolean getBool(String key, boolean def) { return plugin.getConfig().getBoolean("events." + id + "." + key, def); }
    protected List<String> getStringList(String key) { return plugin.getConfig().getStringList("events." + id + "." + key); }

    /** Reads a list of material names from config into a set. */
    protected Set<Material> materialSet(String key) {
        Set<Material> out = new HashSet<>();
        for (String s : getStringList(key)) {
            Material m = Material.matchMaterial(s.trim());
            if (m != null) out.add(m);
        }
        return out;
    }

    /** Parses "DIAMOND:3" into an ItemStack (amount optional). */
    protected ItemStack parseItem(String s) {
        String[] p = s.split(":");
        Material m = Material.matchMaterial(p[0].trim());
        if (m == null) return null;
        int amount = 1;
        if (p.length > 1) {
            try { amount = Integer.parseInt(p[1].trim()); } catch (NumberFormatException ignored) { }
        }
        return new ItemStack(m, Math.max(1, amount));
    }

    /** Online players that are actually playing (survival/adventure). Mutable list. */
    protected List<Player> players() {
        return Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    protected void say(String msg) {
        Bukkit.broadcast(Component.text("[" + displayName + "] ", NamedTextColor.AQUA)
                .append(Component.text(msg, NamedTextColor.WHITE)));
    }

    protected static ThreadLocalRandom rnd() { return ThreadLocalRandom.current(); }

    protected static void setBase(LivingEntity e, Attribute attr, double value) {
        AttributeInstance inst = e.getAttribute(attr);
        if (inst != null) inst.setBaseValue(value);
    }


    // ---- locations (set with /event setloc <event>) ----
    protected Location getLocation(String key, Location fallback) {
        ConfigurationSection s = plugin.getConfig().getConfigurationSection("events." + id + "." + key);
        if (s == null) return fallback;
        World w = Bukkit.getWorld(s.getString("world", ""));
        if (w == null) return fallback;
        return new Location(w, s.getDouble("x"), s.getDouble("y"), s.getDouble("z"));
    }

    protected Location spawnLocation() { return Bukkit.getWorlds().get(0).getSpawnLocation(); }

    public void setLocation(String key, Location l) {
        String base = "events." + id + "." + key;
        plugin.getConfig().set(base + ".world", l.getWorld().getName());
        plugin.getConfig().set(base + ".x", l.getBlockX() + 0.5);
        plugin.getConfig().set(base + ".y", (double) l.getBlockY());
        plugin.getConfig().set(base + ".z", l.getBlockZ() + 0.5);
        plugin.saveConfig();
    }

    // ---- item / player helpers ----
    protected void giveItems(Player p, List<String> specs) {
        for (String s : specs) {
            ItemStack it = parseItem(s);
            if (it == null) continue;
            p.getInventory().addItem(it).values()
                    .forEach(left -> p.getWorld().dropItemNaturally(p.getLocation(), left));
        }
    }

    protected static double maxHealth(Player p) {
        AttributeInstance a = p.getAttribute(Attribute.MAX_HEALTH);
        return a == null ? 20.0 : a.getValue();
    }

    protected static String pretty(Material m) { return m.name().toLowerCase().replace('_', ' '); }

    protected static boolean moved(PlayerMoveEvent e) {
        return e.getTo() != null && (e.getFrom().getX() != e.getTo().getX()
                || e.getFrom().getY() != e.getTo().getY() || e.getFrom().getZ() != e.getTo().getZ());
    }

    /** Cancels position change but still lets the player look around. */
    protected static void lockPosition(PlayerMoveEvent e) {
        if (e.getTo() == null) return;
        Location t = e.getFrom().clone();
        t.setYaw(e.getTo().getYaw());
        t.setPitch(e.getTo().getPitch());
        e.setTo(t);
    }

    /** Events that have an arena/zone override this so the settings menu shows a 'set location' button. */
    public boolean usesLocation() { return false; }

    public String id() { return id; }
    public String displayName() { return displayName; }
    public Material icon() { return icon; }
    public boolean isRunning() { return running; }
}
