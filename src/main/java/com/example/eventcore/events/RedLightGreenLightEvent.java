package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import com.example.eventcore.GameEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.concurrent.ThreadLocalRandom;

public class RedLightGreenLightEvent extends GameEvent {

    private boolean green;

    public RedLightGreenLightEvent(EventCore plugin) {
        super(plugin, "red_light_green_light", "Red Light Green Light", Material.RED_CONCRETE);
    }

    @Override
    protected void onStart() {
        setGreen();
    }

    private void setGreen() {
        green = true;
        Bukkit.broadcast(Component.text("GREEN LIGHT - run!", NamedTextColor.GREEN));
        Bukkit.getOnlinePlayers().forEach(p -> p.showTitle(net.kyori.adventure.title.Title.title(
                Component.text("GREEN", NamedTextColor.GREEN), Component.empty())));

        int min = getInt("green-min-seconds", 4);
        int max = Math.max(min + 1, getInt("green-max-seconds", 9));
        int seconds = ThreadLocalRandom.current().nextInt(min, max + 1);
        runLater(this::setRed, seconds * 20L);
    }

    private void setRed() {
        green = false;
        Bukkit.broadcast(Component.text("RED LIGHT - freeze!", NamedTextColor.RED));
        Bukkit.getOnlinePlayers().forEach(p -> p.showTitle(net.kyori.adventure.title.Title.title(
                Component.text("RED", NamedTextColor.RED), Component.empty())));
        runLater(this::setGreen, getInt("red-seconds", 4) * 20L);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (green) return;
        Player p = e.getPlayer();
        if (p.getGameMode() != GameMode.SURVIVAL) return;

        Location from = e.getFrom(), to = e.getTo();
        if (to == null) return;
        // Only punish actual position change, not head rotation
        if (from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ()) return;

        p.damage(getDouble("damage", 4.0));
    }

    @Override
    protected void onStop() {
        Bukkit.broadcast(Component.text("Red Light Green Light has ended.", NamedTextColor.YELLOW));
    }
}
