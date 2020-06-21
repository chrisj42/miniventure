package miniventure.game.world.management;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import miniventure.game.core.GameScreen;
import miniventure.game.core.GdxCore;
import miniventure.game.screen.ErrorScreen;
import miniventure.game.screen.LoadingScreen;
import miniventure.game.screen.MenuScreen;
import miniventure.game.util.MyUtils;
import miniventure.game.util.ProgressLogger;
import miniventure.game.util.Version;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.file.WorldFileInterface;
import miniventure.game.world.file.WorldFormatException;
import miniventure.game.world.tile.TileType;
import miniventure.game.world.worldgen.island.IslandType;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * this class contains all the levels in the game, and generally manages world-level data.
 */
public class WorldManager {
	
	// this defines the progression order of the islands in the game
	private static final IslandType[] ISLAND_ORDER = {
			IslandType.WOODLAND,
			IslandType.DESERT,
			IslandType.ARCTIC
	};
	
	public static IslandType getIslandType(int levelId) {
		final int idx = Math.abs(levelId) - 1;
		if(idx >= ISLAND_ORDER.length) {
			MyUtils.error("island id "+levelId+" does not correspond to a valid island type, returning woodland.", false, true);
			return IslandType.WOODLAND;
		}
		
		return ISLAND_ORDER[idx];
	}
	
	// world parameters
	private final Path worldPath;
	private final RandomAccessFile lockRef;
	// private final IslandCache[] islandStores;
	private final long worldSeed;
	
	@NotNull
	private final GameScreen gameScreen;
	
	// world state
	private float gameTime, daylightOffset;
	
	// registered entities
	private final Map<Integer, Entity> entityIDMap = new HashMap<>(128);
	// entities that have reserved an id but may not yet be fully initialized; they will be registered as soon as they are added to a level.
	private final Set<Integer> reservedIDs = new HashSet<>(16);
	// sync lock for both the id map and set together.
	private final Object idLock = new Object();
	
	@Nullable
	private Level level; // currently loaded level
	
	// level changing
	private Integer levelRequest; // loads and sets level
	private Thread levelLoadThread;
	
	private String unusedPlayerData; // holds player data when world is initially loaded; in all future loads it's null
	
	private enum SpawnMode {
		INIT, // 
		SPAWN, // spawned in;
		TRAVEL
	}
	
	/*
		how level loading and stuff works: (imagine looking down this list and taking the first route that works)
		
		- when a level is being loaded: all processing is skipped
		- when a level is requested to be loaded:
			- screen set to loading screen
			- the current level (if any) is discarded
			- a thread is dispatched to load the new level
				- this thread ends by setting the level of the world
		- when a level finishes loading: attempt to spawn player into level
			- 
	 */
	
	// private boolean worldLoaded;
	
	// private final List<Runnable> runnables = Collections.synchronizedList(new LinkedList<>());
	// private final Object updateLock = new Object();
	
	
	
	/*
		this is created when a world is created, as was with the original server world.
		I ought to keep in consideration the behavior of switching levels, though
			- better to unload before loading next to prevent having both necessarily in memory at once
			- however, this means that there is going to be time where no level is loaded, instead of the alternative where we can switch them out atomically
				- though, this could technically cause issues too depending on how level fetching works, idk
		- key point, don't change levels while updating the world
			- no entities, items, tiles, etc should be referenced while changing
			- level changes will be requested in libGDx thread, while processing input
			- but switching levels should still be done in a separate thread so the screen doesn't lag
		- create a field in world manager for next level; during update, if there's a value here, the world will unload the current level and begin loading another in a seperate thread
			- this thread will be stored as well, and used for reference...
			- if the thread is running: display loading screen
			- if thread is stopped: remove thread
			- if thread is null: check level; if present, update, else load player level from info
				- loading the player level is what happens when world is launched
				- if for some reason the thread is null and the level is null at some other time, like a blip when changing levels or something, then the player location and level would get reverted to the last time it got saved.
					- so, that should be saved right before switching levels.
	*/
	
	public WorldManager(@NotNull WorldDataSet worldInfo/*, ProgressLogger logger*/) {
		// logger.pushMessage("Parsing world parameters", true);
		
		initWorldTime(worldInfo.gameTime, worldInfo.timeOfDay);
		worldSeed = worldInfo.seed;
		
		worldPath = worldInfo.worldPath;
		lockRef = worldInfo.lockRef;
		
		gameScreen = new GameScreen();
		
		unusedPlayerData = worldInfo.playerData;
		levelRequest = worldInfo.playerLevel;
	}
	
	public void dispose() {
		gameScreen.dispose();
		
		try {
			lockRef.close();
		} catch (IOException ignored) {
		}
	}
	
	/** update the world's game logic. (can also use to render) */
	public void updateAndRender(float delta) {
		
		/*
		// run any runnables that were posted during the above update
		Runnable[] lastRunnables;
		// synchronized extenerally to link the toArray and clear so they are not interrupted.
		synchronized (runnables) {
			lastRunnables = runnables.toArray(new Runnable[0]);
			runnables.clear();
		}

		synchronized (updateLock) {
			for(Runnable r : lastRunnables)
				r.run(); // any runnables added here will be run next update

			// super.update(delta);
		}
		*/
		
		if(levelLoadThread != null) {
			if(levelLoadThread.isAlive())
				return; // currently loading new level, do not update anything
			
			// just finished; can do some initialization here if needed
			levelLoadThread = null;
		}
		
		if(levelRequest != null) {
			// load a level
			
			LoadingScreen loader = new LoadingScreen();
			loader.pushMessage("loading");
			GdxCore.setScreen(loader);
			
			final int req = levelRequest;
			levelLoadThread = new Thread(() -> loadLevel(req, loader));
			levelRequest = null;
			levelLoadThread.start();
			return;
		}
		
		if(level != null) {
			MenuScreen menu = GdxCore.getScreen();
			if(menu == null) {
				gameScreen.handleInput(level.getPlayer());
				level.update(delta);
			}
			
			gameScreen.render(level, getLightingOverlay());
		}
		
		gameTime += delta;
		if(doDaylightCycle())
			daylightOffset = (daylightOffset + delta) % TimeOfDay.SECONDS_IN_DAY;
	}
	
	
	/*  --- WORLD MANAGEMENT --- */
	
	
	protected void initWorldTime(float gameTime, float daylightOffset) {
		this.gameTime = gameTime;
		this.daylightOffset = daylightOffset;
	}
	
	public void setTimeOfDay(float daylightOffset) {
		initWorldTime(getGameTime(), daylightOffset % TimeOfDay.SECONDS_IN_DAY);
	}
	public float changeTimeOfDay(float deltaOffset) {
		float newOff = getDaylightOffset();
		if(deltaOffset < 0)
			newOff += TimeOfDay.SECONDS_IN_DAY;
		newOff = (newOff + deltaOffset) % TimeOfDay.SECONDS_IN_DAY;
		setTimeOfDay(newOff);
		return getDaylightOffset();
	}
	
	protected boolean doDaylightCycle() { return Config.DaylightCycle.get(); }
	
	// TODO add lighting overlays, based on level and/or time of day, depending on the level and perhaps other things.
	private Color getLightingOverlay() {
		//Array<Color> colors = new Array<>(TimeOfDay.getSkyColors(daylightOffset));
		return TimeOfDay.getSkyColor(getDaylightOffset());
	}
	
	/** Saves the world to file */
	public void saveWorld() {
		if(level == null) {
			MyUtils.error("attempted world save without loaded level; not saving.");
			return;
		}
		WorldFileInterface.saveLevel(worldPath, level.save());
		WorldFileInterface.saveWorld(new WorldDataSet(worldPath, lockRef, worldSeed, getGameTime(), getDaylightOffset(), Version.CURRENT, level.getLevelId(), Entity.serialize(level.getPlayer())));
	}
	
	
	
	/*  --- LEVEL MANAGEMENT --- */
	
	
	/*protected void clearEntityIdMap() {
		synchronized (idLock) {
			entityIDMap.clear();
			reservedIDs.clear();
		}
	}*/
	
	long getSeed(int levelId) {
		return worldSeed + levelId;
	}
	
	public void requestLevel(int levelId) {
		levelRequest = levelId;
	}
	
	// Separate level-loading thread only
	private void loadLevel(int levelId, LoadingScreen loader) {
		/*
			a couple goals:
			- decommission the current level (dispose of the game screen) and save it to file
			- load the new level
		 */
		
		final Level prevLevel = level;
		
		if(level != null) {
			loader.pushMessage("saving current level", true);
			// WorldFileInterface.saveLevel(worldPath, level.save());
			saveWorld();
			level = null;
		}
		
		loader.pushMessage("loading new level", true);
		
		LevelDataSet levelData;
		try {
			levelData = WorldFileInterface.loadLevel(worldPath, levelId);
		} catch (WorldFormatException e) {
			Gdx.app.postRunnable(() -> GdxCore.setScreen(new ErrorScreen(MyUtils.combineThrowableCauses(e, "Error loading level "+levelId))));
			e.printStackTrace();
			return;
		}
		
		if(levelData == null) {
			loader.pushMessage("generating level");
			// TileType[][][] tiles = getIslandType(levelId).generateIsland(worldSeed + levelId, true);
			// new LevelDataSet()
			levelData = new LevelDataSet(levelId);
		}
		
		if(prevLevel == null) {
			level = new Level(this, levelData, unusedPlayerData);
			unusedPlayerData = null;
		} else
			level = new Level(prevLevel, levelData);
		
		Gdx.app.postRunnable(() -> GdxCore.setScreen(null));
	}
	
	
	/*  --- ENTITY MANAGEMENT --- */
	
	
	/*public int getEntityTotal() { synchronized (idLock) { return entityIDMap.size(); } }
	
	boolean isEntityRegistered(Entity e) { return isEntityRegistered(e, false); }
	boolean isEntityRegistered(Entity e, boolean includeReserved) {
		synchronized (idLock) {
			return entityIDMap.containsKey(e.getId()) || (includeReserved && reservedIDs.contains(e.getId()));
		}
	}
	
	public Set<Entity> getRegisteredEntities() { synchronized (idLock) { return new HashSet<>(entityIDMap.values()); } }
	
	// only register entities in the reserved id map; give warning otherwise, or TODO possibly fail (check uses).
	public void registerEntity(Entity e) { registerEntity(e, true); }
	void registerEntity(Entity e, boolean shouldBeReserved) {
		synchronized (idLock) {
			if(!reservedIDs.remove(e.getId()) && shouldBeReserved)
				MyUtils.error(this+" has not reserved entity ID "+e.getId()+"; continuing registration of given entity "+e);
			
			// check for existing registration
			Entity cur;
			if((cur = entityIDMap.put(e.getId(), e)) != null)
				MyUtils.error(this+" recieved redundant registration request for entity "+e+"; existing mapping (should match): "+cur);
		}
	}
	
	*//**
	 * generates a entity id that is unique for this game.
	 * Negative numbers represent entities updated only by the client.
	 *//*
	public int reserveNewEntityId() { return reserveNewEntityId(false); }
	public int reserveNewEntityId(boolean negative) {
		while(true) {
			final int eid = Math.abs(MathUtils.random.nextInt()) * (negative ? -1 : 1);
			if(eid == 0) continue;
			
			synchronized (idLock) {
				// if it is not contained in the id map or the reserved set, then its free
				if(!entityIDMap.containsKey(eid) && !reservedIDs.contains(eid)) {
					reservedIDs.add(eid);
					return eid;
				}
			}
		}
	}*/
	
	/*public void cancelIdReservation(Entity e) {
		synchronized (idLock) {
			if(!reservedIDs.remove(e.getId()))
				MyUtils.error("(in WorldManager.cancelIdReservation): id for entity " + e + " not reserved.");
		}
	}*/
	
	/*public void deregisterEntity(int eid) {
		synchronized (idLock) {
			if(entityIDMap.remove(eid) == null)
				MyUtils.error("(in WorldManager.deregisterEntity): id "+eid+" not registered.");
		}
	}*/
	
	
	// GDX thread only.
	/*public void spawnPlayer(SpawnData data) {
		// this has to come before making the new client player, because it has the same eid and so will overwrite some things.
		if(this.mainPlayer != null) {
			super.deregisterEntity(this.mainPlayer.getId());
		}
		
		InventoryOverlay invScreen = new InventoryOverlay(new OrthographicCamera());
		this.mainPlayer = new Player(data, invScreen);
		registerEntity(mainPlayer);
		gameScreen = GdxCore.newGameScreen(new GameScreen(mainPlayer, gameScreen, invScreen));
	}*/
	
	public void respawnPlayer() {
		LoadingScreen loader = new LoadingScreen();
		GdxCore.setScreen(loader);
		loader.pushMessage("Respawning");
		// client.send(DatalessRequest.Respawn);
		if(level != null && level.getLevelId() == 1)
			level.resetPlayer();
		else
			requestLevel(1);
	}

	// public RespawnScreen getRespawnScreen() { return new RespawnScreen(mainPlayer, getLightingOverlay(), gameScreen); }
	
	
	/*  --- GET METHODS --- */
	
	
	// public abstract TileType getTileType(TileType type);
	
	// public Player getMainPlayer() { return mainPlayer; }
	
	// public Entity getEntity(int eid) { synchronized (idLock) { return entityIDMap.get(eid); } }
	
	@Nullable
	public Level getLevel() { return level; }
	
	// public abstract Level getEntityLevel(Entity e);
	
	@NotNull
	GameScreen getGameScreen() { return gameScreen; }
	
	/** fetches time since the world was originally created (while the world is loaded and running) */
	public float getGameTime() { return gameTime; }
	public float getDaylightOffset() { return daylightOffset; }
	
	public String getTimeString() { return TimeOfDay.getTimeString(daylightOffset); }
}
