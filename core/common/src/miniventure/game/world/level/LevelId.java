package miniventure.game.world.level;

import java.util.ArrayList;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

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
	
	public static class LevelIdSerializer extends Serializer<LevelId> {
		
		public LevelIdSerializer() {
			super(false, true);
			// note, setting "acceptsNull" to false does not mean null values are not allowed, it means that the framework will handle nulls instead of this custom impl.
		}
		
		@Override
		public void write(Kryo kryo, Output output, LevelId object) {
			output.writeInt(object.uniqueId);
		}
		
		@Override
		public LevelId read(Kryo kryo, Input input, Class<LevelId> type) {
			int id = input.readInt();
			return LevelId.getId(id);
		}
	}
}
