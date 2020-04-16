package miniventure.game.world.management;

import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.particle.ClientParticle;
import miniventure.game.world.level.RenderLevel;
import miniventure.game.world.tile.ClientTileType;
import miniventure.game.world.tile.TileType.TileTypeEnum;

import org.jetbrains.annotations.Nullable;

// A world manager that specifically loads only one level at a time.
// all registered entities are assumed to be on the current level.
// this works because any change in level wipes the entity list.
public abstract class LevelManager extends WorldManager {
	
	private RenderLevel level;
	
	
	/*  --- WORLD MANAGEMENT --- */
	
	
	public boolean levelLoaded() { return level != null; }
	
	
	/*  --- LEVEL MANAGEMENT --- */
	
	
	protected void setLevel(@Nullable RenderLevel level) {
		clearEntityIdMap();
		this.level = level;
	}
	
	
	/*  --- ENTITY MANAGEMENT --- */
	
	
	@Override
	public void registerEntity(Entity e) {
		registerEntity(e, e instanceof ClientParticle);
	}
	
	
	/*  --- GET METHODS --- */
	
	
	public RenderLevel getLevel() { return level; }
	
	@Override
	public RenderLevel getLevel(int levelId) { return level != null && level.getLevelId() == levelId ? level : null; }
	
	@Override
	public RenderLevel getEntityLevel(Entity e) { return isEntityRegistered(e) ? level : null; }
	
	@Override
	public ClientTileType getTileType(TileTypeEnum type) { return ClientTileType.get(type); }
}
