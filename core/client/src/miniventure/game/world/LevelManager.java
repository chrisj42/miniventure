package miniventure.game.world;

import miniventure.game.world.entity.Entity;
import miniventure.game.world.tile.ClientTileType;
import miniventure.game.world.tile.TileTypeEnum;

import org.jetbrains.annotations.NotNull;

// A world manager that specifically loads only one level at a time.
// all registered entities are assumed to be on the current level.
// this works because any change in level wipes the entity list.
public abstract class LevelManager extends WorldManager {
	
	private RenderLevel level;
	
	
	/*  --- WORLD MANAGEMENT --- */
	
	
	@Override
	public boolean worldLoaded() { return level != null; }
	
	
	/*  --- LEVEL MANAGEMENT --- */
	
	
	protected void setLevel(@NotNull RenderLevel level) {
		clearWorld();
		this.level = level;
	}
	
	
	/*  --- ENTITY MANAGEMENT --- */
	
	
	// insert entity stuff here
	
	
	/*  --- GET METHODS --- */
	
	
	public RenderLevel getLevel() { return level; }
	
	@Override
	public RenderLevel getLevel(int levelId) { return level != null && level.getLevelId() == levelId ? level : null; }
	
	@Override
	public RenderLevel getEntityLevel(Entity e) { return isEntityRegistered(e) ? level : null; }
	
	@Override
	public ClientTileType getTileType(TileTypeEnum type) { return ClientTileType.get(type); }
}
