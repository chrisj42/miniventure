package miniventure.game.world.file;

import miniventure.game.network.GameProtocol.IslandReference;
import miniventure.game.util.SerialHashMap;
import miniventure.game.util.Version;
import miniventure.game.world.level.LevelId;
import miniventure.game.world.worldgen.island.IslandType;
import miniventure.game.world.worldgen.island.ProtoLevel;

import org.jetbrains.annotations.NotNull;

public class IslandDataManager {
	
	/*
		- clean up the mess of island/level caches
		- figure out how level loading, generating, and saving is going to work in detail
		THE MAIN GOAL OF THIS WORK IS TO PHASE OUT CACHING THE DATA IN LEVELCACHE, THAT'S ALL.
	 */
	
	// holds the LevelCache for the surface and cavern levels
	
	private final IslandType islandType;
	private final int islandId;
	private final long seed;
	
	private final LevelData surface, caverns;
	private boolean unlocked; // show on map?
	private boolean bossDefeated;
	
	private IslandReference refCache;
	
	// world gen
	IslandDataManager(int islandId, IslandType type, long seed, boolean unlocked) {
		this.islandId = islandId;
		this.islandType = type;
		this.seed = seed;
		this.unlocked = unlocked;
		this.bossDefeated = false;
		
		this.surface = new LevelData(false);
		this.caverns = new LevelData(false);
	}
	
	// loading
	IslandDataManager(@NotNull Version dataVersion, String data) {
		SerialHashMap map = new SerialHashMap(data);
		
		this.islandId = map.get("id", Integer::parseInt);
		this.islandType = map.get("type", IslandType::valueOf);
		this.seed = map.get("seed", Long::parseLong);
		
		this.unlocked = map.get("unlocked", Boolean::parseBoolean);
		this.bossDefeated = map.get("boss-defeat", Boolean::parseBoolean);
		
		this.surface = new LevelData(map.get("surface-gen", Boolean::parseBoolean));
		this.caverns = new LevelData(map.get("cavern-gen", Boolean::parseBoolean));
	}
	
	public String serialize() {
		SerialHashMap map = new SerialHashMap();
		map.put("id", String.valueOf(islandId));
		map.put("type", islandType.name());
		map.put("seed", String.valueOf(seed));
		
		map.put("unlocked", String.valueOf(unlocked));
		map.put("boss-defeat", String.valueOf(bossDefeated));
		
		map.put("surface-gen", String.valueOf(surface.generated));
		map.put("cavern-gen", String.valueOf(caverns.generated));
		
		return map.serialize();
	}
	
	/// -- ISLAND DATA EVENTS --
	
	public void onGenerate(boolean surface) {
		getData(surface).generated = true;
	}
	public boolean isGenerated(boolean surface) {
		return getData(surface).generated;
	}
	
	public void onUnlock() { unlocked = true; }
	public boolean isUnlocked() { return unlocked; }
	
	public void onBossDefeat() { bossDefeated = true; }
	public boolean isBossDefeated() { return bossDefeated; }
	
	
	/// -- GETTERS --
	
	public int getIslandId() { return islandId; }
	
	public IslandType getIslandType() { return islandType; }
	
	public long getSeed() { return seed; }
	
	public IslandReference getRef() {
		if(refCache == null)
			refCache = new IslandReference(islandId, LevelId.getId(islandId, true), islandType);
		return refCache;
	}
	
	private LevelData getData(boolean surface) {
		return surface ? this.surface : this.caverns;
	}
	
	/// -- LEVEL DATA CLASS --
	
	private static class LevelData {
		
		private boolean generated;
		
		private LevelData(boolean generated) {
			this.generated = generated;
		}
	}
}
