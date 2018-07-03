package miniventure.game.client;

import miniventure.game.world.Level;
import miniventure.game.world.WorldManager;
import miniventure.game.world.WorldObject;
import miniventure.game.world.tile.TileEnumMapper;

import com.badlogic.gdx.utils.Array;

public class DisplayWorld extends WorldManager {
	
	public DisplayWorld() { super(new TileEnumMapper<>(prop -> prop)); }
	
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
	public boolean isKeepAlive(WorldObject obj) { return true; }
	
	@Override
	public Array<WorldObject> getKeepAlives(Level level) { return new Array<>(); }
}
