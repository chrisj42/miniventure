package miniventure.game.client;

import miniventure.game.world.Level;
import miniventure.game.world.WorldManager;
import miniventure.game.world.WorldObject;
import miniventure.game.world.tile.ClientTileType;
import miniventure.game.world.tile.TileType;
import miniventure.game.world.tile.TileTypeEnum;

import com.badlogic.gdx.utils.Array;

public class DisplayWorld extends WorldManager {
	
	@Override
	protected boolean doDaylightCycle() {
		return false;
	}
	
	@Override
	public boolean worldLoaded() {
		return true;
	}
	
	@Override
	public void createWorld(int width, int height) {}
	
	@Override
	public void exitWorld(boolean save) {}
	
	@Override
	protected void pruneLoadedLevels() {}
	
	@Override
	public boolean isKeepAlive(WorldObject obj) { return true; }
	
	@Override
	public Array<WorldObject> getKeepAlives(Level level) { return new Array<>(); }
	
	@Override
	public TileType getTileType(TileTypeEnum type) { return ClientTileType.get(type); }
}
