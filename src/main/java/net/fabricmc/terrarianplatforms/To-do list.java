    /* TODO:
     * 
     * If there is a number before a to-do list item, that is its priority. 1 is highest, oo is lowest.
     * 
     * 1 Make mod compatibility with Hookshot (Hooshots will be able to grab onto platforms.)
     * 
     * 2 Platforms make the game really laggy (TPS) when placed in the world, possibly due to the fact that every single tick they check for
     * anything that is currently interacting with them, and also check the stacktrace. Fix me.
     * 
	 * 3 When held, Swords do not make the Outline Shape empty. Fix me.
     * 
     * 4 Make mod compatibility with BoMD (In case the bosses do not understand platforms.)
     * 
     * 5 On the released .jar, mobs fail to see through platforms. Wither/zombie cannot see pig/villager directly below.
     *   This does not occur in local debug mode.
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * Making other mods after creating Terrarian Platforms:
     * Make other mods, such as Spin Attack, Priceless Dash, Better Sweeping, Crit Overhaul, More Bows, More Arrows, More Shields,
	 * Terrarian Weather, Better Time, and Shockwaves.
	 * 
     * Spin Attack is probably possible
     * Priceless Dash is possible (Origins:Rammling adds a dash keybind to an origin (Origins:Rammling is an Origins mod addon))
     * Better Sweeping is probably possible
     * Crit Overhaul might be possible
     * More Bows is probably possible
     * More Arrows is probably possible
     * More Shields is definitely possible (copy+paste old mod then add axe nerf, and add credit to original mod)
     * Terrarian Weather might be possible
     * Better Time is probably possible
     * Shockwaves is probably possible
     * 
	 * Spin Attack will make a keybind for an AoE cooldown attack that only works if you have a sword, and depends on the sword's damage.
     * Spin Attack will also need new animations (spinning animation and spin attack slashes) and a cooldown bar.
     * (When the cooldown is active, the XP bar is replaced by a red XP bar that shows how much time is left until the cooldown is over,
     * and replaces the level display with a spin attack icon.)
	 * 
	 * Priceless Dash will make a keybind for a dash with a cooldown. (5 second cooldown) The dash will go about 2 blocks.
     * The reason it is called "Priceless" Dash is because it is extremely good and does not cost you anything to have dash.
     * By "cost" I mean... *being slowed down in warm biomes with 14 HP instead of 20 HP, not being able to sprint, and/or being in
     * Forge 1.12.2/RLCraft just to be able to dodge intensifies*
	 * 
	 * Better Sweeping will increase the range and damage of the sweeping attack, will increase the chance of getting the enchantment
     * Sweeping Edge, and will add an enchantment called "Air Slash" that gives you a 5% chance shoot an air slash projectile that
     * goes 4 blocks (level 1) 5.5 blocks (level 2) and 7 blocks (level 3) when doing a sweeping attack. When paired with Spin Attack,
     * doing a Spin Attack will have a 5% chance to shoot out 4 Air Slash projectiles in a randomly rotated cross pattern.
	 * 
	 * Crit Overhaul will remove the jump attack crit from minecraft and add a crit chance, defaulting to 10%. Crit chance can
     * be increased with weapon tier, as a way to rubber-band players and make them stop craving Netherite. (Wood by 2.5%,
     * Gold by 15%, Stone by 3.75%, Iron by 12.5%, Diamond by 10%, Netherite by 8.75%.)
	 * 
	 * More Bows will add Iron, Gold, Diamond, and Netherite reinforced bows. (smithing table with normal bow to make Iron, Gold, and
     * Diamond bows.)
	 * 
	 * More Arrows will add Iron, Gold, and Flaming Arrows. (crafted with a nugget or torch, not flint.)
     * (adds a new recipe for arrows, replacing flint with cobblestone.)
	 * 
	 * More Shields will add Wooden, Iron, Diamond, Golden, and Netherite shields, along with reducing the shield cooldown for axes by 50%.
	 * 
	 * Terrarian Weather will tweak the chances of different types of weather in the weather cycle,
     * as well as adding Windy Days, Blizzards, Storms, and Sandstorms from Terraria.
     * Blizzards
     * Blizzards will reduce the light level provided by the sun by 3, spawn 2 new mob types, (Frost Husk and Cryoarachnid)
     * and adds snow particles that fall from the sky and drift a little, But they can only happen in snowy biomes, and are caused by rain.
     * Windy Days
     * Windy Days will add wind, a subtle effect which adds a wind direction, a wind speed, a weather effect that
     * spawns very rare particles above Y=50 that move in the direction of the wind and change speed with the speed of the wind,
     * and a very subtle acceleration in the direction of the wind, depending on the speed of the wind, that effects all items and projectiles.
     * Storms
     * Storms (Terrarian Storms) will be caused by Windy Days being at the same time as Rain, and will act like normal minecraft storms
     * (which get removed) but have Windy Day effects, rain effects, and have a higher chance of lightning.
     * Sandstorms
     * Sandstorms will occur when in a Desert there is a Windy Day. Sandstorms will add the "subtle acceleration" to every single entity above Y=50,
     * including items and projectiles, meaning items and projectiles now have 2x the "subtle acceleration".
     * Sanstorms will also add 2 new enemies, (Antlion and Antlion Swarmer) can have lightning strikes, reduce the light level
     * provided by the sun by 3, and adds sand particles that drift with the wind and fall from the sky.
     * Optional (priority)
     *  1 Add terraria's weather music for Blizzards, Windy Days, Storms, Sandstorms, and Slime Rains.
     *  2 Add Weather Detector item that changes description depending on the weather and weather forecast.
     *  3 Add Slime Rain weather that can happen if you have Terrarian Slimes installed.
	 * 
	 * Better Time will slightly slow down daytime (not nighttime) by 1.25x, and will reduce normal minecraft hostile mob spawning by 1.05x.
     * (multiply by 0.95)
	 * 
	 * Shockwaves will add AoE attacks that go through walls and apply the knockback function to all mobs in a (radius size) radius.
     */