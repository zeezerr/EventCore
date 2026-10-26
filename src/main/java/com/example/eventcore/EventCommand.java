package com.example.eventcore;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class EventCommand implements CommandExecutor, TabCompleter {

    private final EventManager manager;
    private final EventGUI gui;

    public EventCommand(EventManager manager, EventGUI gui) {
        this.manager = manager;
        this.gui = gui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("gui")) {
            if (sender instanceof Player p) gui.open(p);
            else sender.sendMessage("Players only.");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "list" -> {
                sender.sendMessage(Component.text("Events:", NamedTextColor.AQUA));
                for (GameEvent e : manager.all()) {
                    sender.sendMessage(Component.text(" - " + e.id() + (e.isRunning() ? " (running)" : ""),
                            e.isRunning() ? NamedTextColor.GREEN : NamedTextColor.GRAY));
                }
            }
            case "stopall" -> {
                manager.stopAll();
                sender.sendMessage(Component.text("Stopped all events.", NamedTextColor.YELLOW));
            }
            case "reload" -> {
                manager.reloadConfig();
                sender.sendMessage(Component.text("config.yml reloaded.", NamedTextColor.GREEN));
            }
            case "setloc" -> {
                if (!(sender instanceof Player pl)) { sender.sendMessage("Players only."); return true; }
                GameEvent e = args.length > 1 ? manager.get(args[1]) : null;
                if (e == null) {
                    sender.sendMessage(Component.text("Usage: /event setloc <event>", NamedTextColor.RED));
                    return true;
                }
                e.setLocation("location", pl.getLocation());
                sender.sendMessage(Component.text("Location saved for " + e.displayName() + ".", NamedTextColor.GREEN));
            }
            case "start", "stop" -> {
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Usage: /event " + args[0] + " <event>", NamedTextColor.RED));
                    return true;
                }
                GameEvent e = manager.get(args[1]);
                if (e == null) {
                    sender.sendMessage(Component.text("Unknown event.", NamedTextColor.RED));
                    return true;
                }
                if (args[0].equalsIgnoreCase("start")) e.start(); else e.stop();
                sender.sendMessage(Component.text(e.displayName() + (e.isRunning() ? " started." : " stopped."),
                        NamedTextColor.GREEN));
            }
            default -> sender.sendMessage(Component.text("Usage: /event <gui|list|start|stop|stopall|setloc|reload> [event]", NamedTextColor.RED));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            for (String s : List.of("gui", "list", "start", "stop", "stopall", "setloc", "reload"))
                if (s.startsWith(args[0].toLowerCase())) out.add(s);
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("start") || args[0].equalsIgnoreCase("stop") || args[0].equalsIgnoreCase("setloc"))) {
            for (GameEvent e : manager.all())
                if (e.id().startsWith(args[1].toLowerCase())) out.add(e.id());
        }
        return out;
    }
}
