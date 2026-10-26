package com.example.eventcore;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Per-event settings menu. It is generated from events.<id> in config.yml,
 * so every setting you add to the config automatically gets a button.
 *  - numbers: left/right click = +/-, shift = bigger step
 *  - booleans: click to toggle
 *  - item names (e.g. altar offering): click with an item on your cursor
 *  - lists / text: shown read-only (edit in config.yml)
 */
public class SettingsGUI implements Listener {

    private static class Holder implements InventoryHolder {
        Inventory inv;
        GameEvent event;
        final List<String> keys = new ArrayList<>();
        int backSlot, toggleSlot, locSlot = -1;
        @Override public Inventory getInventory() { return inv; }
    }

    private final EventCore plugin;
    private final EventManager manager;
    private final EventGUI mainGui;

    public SettingsGUI(EventCore plugin, EventManager manager, EventGUI mainGui) {
        this.plugin = plugin;
        this.manager = manager;
        this.mainGui = mainGui;
    }

    public void open(Player p, GameEvent e) {
        Holder h = new Holder();
        h.event = e;
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("events." + e.id());
        if (sec != null) {
            for (String k : sec.getKeys(false)) {
                if (sec.get(k) instanceof ConfigurationSection) continue; // e.g. saved location
                h.keys.add(k);
            }
        }
        while (h.keys.size() > 45) h.keys.remove(h.keys.size() - 1);

        int rows = Math.max(2, Math.min(6, (int) Math.ceil(h.keys.size() / 9.0) + 1));
        int size = rows * 9;
        h.backSlot = size - 9;
        h.toggleSlot = size - 5;
        if (e.usesLocation()) h.locSlot = size - 1;

        Inventory inv = Bukkit.createInventory(h, size, Component.text("Settings - " + e.displayName(), NamedTextColor.AQUA));
        h.inv = inv;
        render(h);
        p.openInventory(inv);
    }

    // ------------------------------------------------------------------ rendering

    private Component txt(String s, NamedTextColor c) {
        return Component.text(s, c).decoration(TextDecoration.ITALIC, false);
    }

    private String prettyKey(String key) {
        StringBuilder sb = new StringBuilder();
        for (String w : key.replace('-', ' ').replace('_', ' ').split(" ")) {
            if (w.isEmpty()) continue;
            sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(' ');
        }
        return sb.toString().trim();
    }

    private Material iconFor(String key, Object v) {
        if (v instanceof Boolean b) return b ? Material.LIME_DYE : Material.GRAY_DYE;
        if (v instanceof List) return Material.BOOK;
        if (v instanceof String s) {
            Material m = Material.matchMaterial(s);
            return (m != null && m.isItem() && !m.isAir()) ? m : Material.PAPER;
        }
        String k = key.toLowerCase();
        if (k.contains("second") || k.contains("tick") || k.contains("interval")) return Material.CLOCK;
        if (k.contains("damage")) return Material.IRON_SWORD;
        if (k.contains("health") || k.contains("heart")) return Material.APPLE;
        if (k.contains("radius") || k.contains("spread") || k.contains("size")) return Material.COMPASS;
        if (k.contains("scale") || k.contains("multiplier")) return Material.SLIME_BALL;
        if (k.contains("height") || k.endsWith("-y")) return Material.LADDER;
        if (k.contains("count") || k.contains("per-") || k.contains("max")) return Material.HOPPER;
        if (k.contains("point")) return Material.EMERALD;
        if (k.contains("xp")) return Material.EXPERIENCE_BOTTLE;
        return Material.REPEATER;
    }

    private ItemStack buildItem(Holder h, String key) {
        Object v = plugin.getConfig().get("events." + h.event.id() + "." + key);
        ItemStack item = new ItemStack(iconFor(key, v));
        List<Component> lore = new ArrayList<>();

        if (v instanceof Boolean b) {
            lore.add(txt("Value: " + b, b ? NamedTextColor.GREEN : NamedTextColor.RED));
            lore.add(txt("Click to toggle", NamedTextColor.GRAY));
        } else if (v instanceof Integer || v instanceof Long) {
            lore.add(txt("Value: " + v, NamedTextColor.YELLOW));
            lore.add(txt("Left click: +1   Right click: -1", NamedTextColor.GRAY));
            lore.add(txt("Shift: +10 / -10", NamedTextColor.GRAY));
        } else if (v instanceof Number) {
            lore.add(txt("Value: " + v, NamedTextColor.YELLOW));
            lore.add(txt("Left click: +0.5   Right click: -0.5", NamedTextColor.GRAY));
            lore.add(txt("Shift: +5 / -5", NamedTextColor.GRAY));
        } else if (v instanceof String s) {
            lore.add(txt("Value: " + (s.isEmpty() ? "(empty)" : s), NamedTextColor.YELLOW));
            if (Material.matchMaterial(s) != null) lore.add(txt("Click with an item on your cursor to change", NamedTextColor.GRAY));
            else lore.add(txt("Edit in config.yml", NamedTextColor.DARK_GRAY));
        } else if (v instanceof List<?> list) {
            lore.add(txt(list.size() + " entries (edit in config.yml):", NamedTextColor.YELLOW));
            for (int i = 0; i < Math.min(8, list.size()); i++) lore.add(txt(" - " + list.get(i), NamedTextColor.GRAY));
            if (list.size() > 8) lore.add(txt(" ...", NamedTextColor.GRAY));
        }
        lore.add(txt("Some settings fully apply on the next start", NamedTextColor.DARK_GRAY));

        item.editMeta(m -> {
            m.displayName(txt(prettyKey(key), NamedTextColor.WHITE));
            m.lore(lore);
        });
        return item;
    }

    private ItemStack button(Material m, String name, NamedTextColor color, String... lines) {
        ItemStack item = new ItemStack(m);
        item.editMeta(meta -> {
            meta.displayName(txt(name, color));
            List<Component> lore = new ArrayList<>();
            for (String l : lines) lore.add(txt(l, NamedTextColor.GRAY));
            meta.lore(lore);
        });
        return item;
    }

    private void render(Holder h) {
        h.inv.clear();
        if (h.keys.isEmpty()) {
            h.inv.setItem(0, button(Material.BARRIER, "No configurable settings", NamedTextColor.RED));
        }
        for (int i = 0; i < h.keys.size(); i++) h.inv.setItem(i, buildItem(h, h.keys.get(i)));

        h.inv.setItem(h.backSlot, button(Material.ARROW, "Back", NamedTextColor.YELLOW));
        boolean on = h.event.isRunning();
        h.inv.setItem(h.toggleSlot, on
                ? button(Material.RED_DYE, "Running - click to stop", NamedTextColor.RED)
                : button(Material.LIME_DYE, "Stopped - click to start", NamedTextColor.GREEN));
        if (h.locSlot >= 0) {
            h.inv.setItem(h.locSlot, button(Material.COMPASS, "Set location here", NamedTextColor.AQUA,
                    "Saves where you are standing", "as this event's location"));
        }
    }

    // ------------------------------------------------------------------ clicks

    private static double round2(double d) { return Math.round(d * 100.0) / 100.0; }

    @EventHandler
    public void onClick(InventoryClickEvent ev) {
        if (!(ev.getView().getTopInventory().getHolder() instanceof Holder h)) return;
        ev.setCancelled(true);
        if (ev.getClickedInventory() != ev.getView().getTopInventory()) return;
        if (!(ev.getWhoClicked() instanceof Player p)) return;

        int slot = ev.getRawSlot();
        if (slot == h.backSlot) { mainGui.open(p); return; }
        if (slot == h.toggleSlot) { manager.toggle(h.event); render(h); return; }
        if (slot == h.locSlot) {
            h.event.setLocation("location", p.getLocation());
            p.sendActionBar(Component.text("Location saved!", NamedTextColor.GREEN));
            return;
        }
        if (slot < 0 || slot >= h.keys.size()) return;

        String key = h.keys.get(slot);
        String path = "events." + h.event.id() + "." + key;
        FileConfiguration cfg = plugin.getConfig();
        Object v = cfg.get(path);
        boolean right = ev.isRightClick(), shift = ev.isShiftClick();

        if (v instanceof Boolean b) {
            cfg.set(path, !b);
        } else if (v instanceof Integer || v instanceof Long) {
            int cur = ((Number) v).intValue();
            int step = shift ? 10 : 1;
            int min = key.equals("start-y") ? -64 : 1;
            cfg.set(path, Math.max(min, Math.min(100000, cur + (right ? -step : step))));
        } else if (v instanceof Number n) {
            double cur = n.doubleValue();
            double step = shift ? 5.0 : 0.5;
            double min = key.contains("scale") ? 0.1 : 0.0;
            cfg.set(path, round2(Math.max(min, cur + (right ? -step : step))));
        } else if (v instanceof String s) {
            ItemStack cursor = ev.getCursor();
            if (!cursor.getType().isAir() && Material.matchMaterial(s) != null) {
                cfg.set(path, cursor.getType().name());
            } else {
                p.sendActionBar(Component.text("Hold an item on your cursor to change this.", NamedTextColor.RED));
                return;
            }
        } else {
            p.sendActionBar(Component.text("Edit this one in config.yml", NamedTextColor.GRAY));
            return;
        }

        plugin.saveConfig();
        render(h);
    }
}
