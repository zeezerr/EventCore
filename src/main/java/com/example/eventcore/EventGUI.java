package com.example.eventcore;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EventGUI implements Listener {

    private static final int PER_PAGE = 45;
    private static final int PREV_SLOT = 45;
    private static final int NEXT_SLOT = 53;
    private static final int INFO_SLOT = 49;

    /** Marker holder so we can recognise our own inventory. */
    private static class Holder implements InventoryHolder {
        Inventory inv;
        int page = 0;
        final List<GameEvent> pageEvents = new ArrayList<>();
        @Override public Inventory getInventory() { return inv; }
    }

    private final EventCore plugin;
    private final EventManager manager;
    private SettingsGUI settings;

    public EventGUI(EventCore plugin, EventManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    public void setSettings(SettingsGUI settings) { this.settings = settings; }

    public void open(Player p) {
        Holder holder = new Holder();
        Inventory inv = Bukkit.createInventory(holder, 54, Component.text("EventCore", NamedTextColor.AQUA));
        holder.inv = inv;
        render(holder);
        p.openInventory(inv);
    }

    private int pageCount() {
        return Math.max(1, (int) Math.ceil(manager.all().size() / (double) PER_PAGE));
    }

    private void render(Holder h) {
        h.inv.clear();
        h.pageEvents.clear();

        List<GameEvent> all = new ArrayList<>(manager.all());
        int start = h.page * PER_PAGE;
        for (int i = 0; i < PER_PAGE && start + i < all.size(); i++) {
            GameEvent e = all.get(start + i);
            h.pageEvents.add(e);
            h.inv.setItem(i, buildItem(e));
        }

        if (h.page > 0) h.inv.setItem(PREV_SLOT, button(Material.ARROW, "Previous page"));
        if (h.page < pageCount() - 1) h.inv.setItem(NEXT_SLOT, button(Material.ARROW, "Next page"));
        h.inv.setItem(INFO_SLOT, button(Material.PAPER, "Page " + (h.page + 1) + "/" + pageCount()));
    }

    private ItemStack button(Material m, String name) {
        ItemStack item = new ItemStack(m);
        item.editMeta(meta -> meta.displayName(
                Component.text(name, NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false)));
        return item;
    }

    private ItemStack buildItem(GameEvent e) {
        ItemStack item = new ItemStack(e.icon());
        item.editMeta(m -> {
            m.displayName(Component.text(e.displayName(), e.isRunning() ? NamedTextColor.GREEN : NamedTextColor.WHITE)
                    .decoration(TextDecoration.ITALIC, false));
            m.lore(List.of(
                    Component.text("Status: " + (e.isRunning() ? "RUNNING" : "stopped"),
                            e.isRunning() ? NamedTextColor.GREEN : NamedTextColor.RED).decoration(TextDecoration.ITALIC, false),
                    Component.text("Left click: " + (e.isRunning() ? "stop" : "start"), NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("Right click: settings", NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));
        });
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent ev) {
        if (!(ev.getView().getTopInventory().getHolder() instanceof Holder h)) return;
        ev.setCancelled(true);
        if (ev.getClickedInventory() != ev.getView().getTopInventory()) return;

        int slot = ev.getRawSlot();
        if (slot == PREV_SLOT && h.page > 0) {
            h.page--;
            render(h);
        } else if (slot == NEXT_SLOT && h.page < pageCount() - 1) {
            h.page++;
            render(h);
        } else if (slot >= 0 && slot < h.pageEvents.size()) {
            GameEvent target = h.pageEvents.get(slot);
            if (ev.isRightClick() && settings != null && ev.getWhoClicked() instanceof Player pl) {
                settings.open(pl, target);
            } else {
                manager.toggle(target);
                render(h);
            }
        }
    }
}
