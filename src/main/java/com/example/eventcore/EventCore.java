package com.example.eventcore;

import org.bukkit.plugin.java.JavaPlugin;

public final class EventCore extends JavaPlugin {

    private EventManager eventManager;
    private EventGUI gui;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        eventManager = new EventManager(this);
        eventManager.registerDefaults();

        gui = new EventGUI(this, eventManager);
        SettingsGUI settingsGui = new SettingsGUI(this, eventManager, gui);
        gui.setSettings(settingsGui);
        getServer().getPluginManager().registerEvents(gui, this);
        getServer().getPluginManager().registerEvents(settingsGui, this);

        EventCommand cmd = new EventCommand(eventManager, gui);
        getCommand("event").setExecutor(cmd);
        getCommand("event").setTabCompleter(cmd);

        getLogger().info("EventCore enabled with " + eventManager.all().size() + " events.");
    }

    @Override
    public void onDisable() {
        if (eventManager != null) eventManager.stopAll();
    }
}
