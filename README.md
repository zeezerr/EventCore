# EventCore
Minecraft (Paper 1.21.11) events plugin. 26 events, GUI menu, `/event` command.

## Build (no local tools needed)
1. Push this repo to GitHub.
2. Open the **Actions** tab -> **Build EventCore** -> latest run.
3. Download the **EventCore** artifact (zip containing the jar) and put the jar in `plugins/`.

## Commands
`/event gui` · `/event list` · `/event start <id>` · `/event stop <id>` · `/event stopall`
Permission: `eventcore.admin`

## Settings menu
`/event gui` -> **right-click** any event to open its settings. Numbers: left/right click = +/-,
shift = bigger step. Booleans toggle. Item-name settings (e.g. Altar offering): click with an item on your cursor.
Events with an arena/zone have a **Set location here** button (same as `/event setloc <event>`).
`/event reload` re-reads config.yml after you edit it by hand.

## Mini boss models
Bosses use a built-in model made of display entities (wings, crowns, orbiting parts, particles) and have
3 phases: they enrage at 66% and 33% health, speed up, and use an ultimate attack.

To use a **real custom model from a resource pack** instead:
1. Make a model in Blockbench and put it in your resource pack as an item model, e.g. `assets/eventcore/items/gravekeeper.json`.
2. In `config.yml` set, for example:
   ```yaml
   mini_boss_gravekeeper:
     item-model: "eventcore:gravekeeper"
     model-scale: 2.5
     model-transform: FIXED   # NONE / HEAD / FIXED / GROUND ...
   ```
3. The vanilla body becomes invisible and your model follows the boss.
