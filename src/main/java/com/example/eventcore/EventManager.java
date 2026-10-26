package com.example.eventcore;

import com.example.eventcore.events.*;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class EventManager {

    private final EventCore plugin;
    private final Map<String, GameEvent> events = new LinkedHashMap<>();

    public EventManager(EventCore plugin) {
        this.plugin = plugin;
    }

    /** Register every event here. Add new ones with one line. */
    public void registerDefaults() {
        register(new SupplyDropEvent(plugin));
        register(new TntRainEvent(plugin));
        register(new RedLightGreenLightEvent(plugin));
        register(new RisingLavaEvent(plugin));
        register(new FallingBlocksEvent(plugin));
        register(new MobsApocalypseEvent(plugin));
        register(new GiantMobsEvent(plugin));
        register(new TinyMobsEvent(plugin));
        register(new ExplodingMobsEvent(plugin));
        register(new PlayerSizeEvent(plugin));
        register(new PlayerSpeedEvent(plugin));
        register(new PlayerSwapEvent(plugin));
        register(new InventorySwapEvent(plugin));
        register(new InventoryShuffleEvent(plugin));
        register(new ChatQuestionEvent(plugin));
        register(new LastManStandingEvent(plugin));
        register(new UltraHardcoreEvent(plugin));
        register(new TntTagEvent(plugin));
        register(new DimensionStopEvent(plugin));
        register(new BannedItemsEvent(plugin));
        register(new RecipeDisablerEvent(plugin));
        register(new EveryoneAnonymousEvent(plugin));
        register(new MaceRouletteEvent(plugin));
        register(new JuggernautEvent(plugin));
        register(new HeartCustomizerEvent(plugin));
        register(new FreezePlayerEvent(plugin));
        register(new KothEvent(plugin));
        register(new FreezeTagEvent(plugin));
        register(new FindTheColorEvent(plugin));
        register(new BountyEvent(plugin));
        register(new HideAndSeekEvent(plugin));
        register(new AltarEvent(plugin));
        register(new ScavengerHuntEvent(plugin));
        register(new EndDragonEvent(plugin));
        register(new AssassinEvent(plugin));
        register(new CustomRecipesEvent(plugin));
        register(new KillSystemEvent(plugin));
        register(new KitsEvent(plugin));
        register(new AnnouncementsEvent(plugin));
        register(new SimonSaysEvent(plugin));
        register(new HostileAnimalsEvent(plugin));
        register(new DragonEggPerksEvent(plugin));
        register(new PillarsOfFortuneEvent(plugin));
        register(new MiniBossGraveKeeperEvent(plugin));
        register(new MiniBossBoneSniperEvent(plugin));
        register(new MiniBossBroodMotherEvent(plugin));
        register(new MiniBossEvokerEvent(plugin));
        register(new MiniBossInfernoBlazeEvent(plugin));
        register(new MiniBossFrostStrayEvent(plugin));
    }

    public void register(GameEvent event) {
        events.put(event.id(), event);
    }

    public GameEvent get(String id) { return events.get(id.toLowerCase()); }
    public Collection<GameEvent> all() { return events.values(); }

    public boolean toggle(GameEvent e) {
        if (e.isRunning()) e.stop(); else e.start();
        return e.isRunning();
    }

    public void reloadConfig() { plugin.reloadConfig(); }

    public void stopAll() {
        events.values().forEach(GameEvent::stop);
    }
}
