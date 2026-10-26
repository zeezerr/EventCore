package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import com.example.eventcore.GameEvent;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.List;

public class AnnouncementsEvent extends GameEvent {

    private int index;

    public AnnouncementsEvent(EventCore plugin) {
        super(plugin, "announcements", "Announcements", Material.BELL);
    }

    @Override
    protected void onStart() {
        index = 0;
        long period = getInt("interval-seconds", 300) * 20L;
        runTimer(() -> {
            List<String> msgs = getStringList("messages");
            if (msgs.isEmpty()) return;
            String prefix = plugin.getConfig().getString("events.announcements.prefix", "<aqua>[Announcement]</aqua> ");
            Bukkit.broadcast(MiniMessage.miniMessage().deserialize(prefix + msgs.get(index % msgs.size())));
            index++;
        }, 20L * 10, period);
    }

    @Override protected void onStop() { }
}
