package com.example.eventcore.events;

import com.example.eventcore.EventCore;
import com.example.eventcore.GameEvent;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ChatQuestionEvent extends GameEvent {

    private volatile String answer;

    public ChatQuestionEvent(EventCore plugin) {
        super(plugin, "chat_question", "Chat Question", Material.OAK_SIGN);
    }

    @Override
    protected void onStart() {
        long period = getInt("interval-seconds", 120) * 20L;
        runTimer(this::ask, 40L, period);
    }

    private void ask() {
        int a = rnd().nextInt(10, 100), b = rnd().nextInt(2, 30);
        if (rnd().nextBoolean()) {
            answer = String.valueOf(a + b);
            say("First to answer wins! What is " + a + " + " + b + "?");
        } else {
            answer = String.valueOf(a * b);
            say("First to answer wins! What is " + a + " x " + b + "?");
        }
    }

    @EventHandler
    public void onChat(AsyncChatEvent e) {
        String expected = answer;
        if (expected == null) return;
        String msg = PlainTextComponentSerializer.plainText().serialize(e.message()).trim();
        if (!msg.equals(expected)) return;
        synchronized (this) {
            if (answer == null) return;
            answer = null;
        }
        Player p = e.getPlayer();
        Bukkit.getScheduler().runTask(plugin, () -> reward(p));
    }

    private void reward(Player p) {
        List<String> rewards = getStringList("rewards");
        say(p.getName() + " answered correctly!");
        if (rewards.isEmpty()) return;
        ItemStack item = parseItem(rewards.get(rnd().nextInt(rewards.size())));
        if (item == null) return;
        p.getInventory().addItem(item).values().forEach(left -> p.getWorld().dropItemNaturally(p.getLocation(), left));
    }

    @Override protected void onStop() { answer = null; say("Chat Question has ended."); }
}
