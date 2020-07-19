package miniventure.game.world.level;

import java.util.ArrayList;

public class LevelId {
	
	private static final ArrayList<LevelId> idObjects = new ArrayList<>();
	private static final Object idLock = new Object();
	
	public static LevelId getId(int islandId, boolean isSurface) {
		return getId(islandId * 2 + (isSurface ? 0 : 1));
	}
	public static LevelId getId(int levelId) {
		synchronized (idLock) {
			while(idObjects.size() <= levelId)
				idObjects.add(null);
			LevelId id = idObjects.get(levelId);
			if(id == null) {
				id = new LevelId(levelId);
				idObjects.set(levelId, id);
			}
			return id;
		}
	}
	
	private final int uniqueId;
	
	private LevelId() { this(0); }
	private LevelId(int levelId) {
		uniqueId = levelId;
	}
	
	public int getLevelId() {
		return uniqueId;
	}
	
	public int getIslandId() {
		return getLevelId() / 2;
	}
	
	public boolean isSurface() {
		return getLevelId() % 2 == 0;
	}
	
	@Override
	public int hashCode() {
		return Integer.hashCode(uniqueId);
	}
	
	@Override
	public boolean equals(Object o) {
		return o instanceof LevelId && uniqueId == ((LevelId)o).uniqueId;
	}
	
	@Override
	public String toString() {
		return String.valueOf(uniqueId);
	}
}
