package miniventure.game.world.tile;

import java.util.EnumMap;
import java.util.HashSet;

import miniventure.game.item.Item;
import miniventure.game.item.Result;
import miniventure.game.item.ServerItem;
import miniventure.game.network.GameServer;
import miniventure.game.util.MyUtils;
import miniventure.game.world.tile.TileDataTag.TileDataMap;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.player.Player;
import miniventure.game.world.level.Level;
import miniventure.game.world.level.ServerLevel;
import miniventure.game.world.management.ServerWorld;
import miniventure.game.world.tile.ServerTileType.P;
import miniventure.game.world.worldgen.level.ProtoTile;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** @noinspection EqualsAndHashcode*/
public class ServerTile extends Tile {
	
	public ServerTile(@NotNull Level level, ProtoTile tile) {
		super(level, tile);
	}
	
	public ServerTile(@NotNull Level level, int x, int y, @NotNull TileData tileData) {
		this((ServerLevel) level, x, y, tileData);
	}
	public ServerTile(@NotNull ServerLevel level, int x, int y, @NotNull TileData tileData) {
		super(level, x, y, tileData);
	}
	
	@Override @NotNull
	public ServerWorld getWorld() { return (ServerWorld) super.getWorld(); }
	
	@NotNull
	public GameServer getServer() { return getWorld().getServer(); }
	
	@Override @NotNull
	public ServerLevel getLevel() { return (ServerLevel) super.getLevel(); }
	
	@Override
	public ServerTileType getType() { return (ServerTileType) super.getType(); }
	@Override
	public ServerTileType getType(TileLayer layer) {
		return (ServerTileType) super.getType(layer);
	}
	@Override
	@SuppressWarnings("unchecked")
	public EnumMap<TileLayer, ServerTileType> getTypeStack() {
		return (EnumMap<TileLayer, ServerTileType>) super.getTypeStack();
	}
	
	public synchronized void addTile(@NotNull ServerTileType newType) { addTile(newType, getType()); }
	// not synchronizing this only because it's always called in a synchronized context.
	private void addTile(@NotNull ServerTileType newType, @NotNull ServerTileType prevType) {
		// ServerTileType newType = ServerTileType.get(newTypeInfo.tileType);
		
		moveEntities(newType);
		
		setType(newType);
		
		// check for an entrance animation
		if(!newType.get(P.TRANS).tryStartAnimation(this, prevType))
			getLevel().onTileUpdate(this); // trigger update manually since tile still changed, just without an animation; tryStartAnimation only triggers updates for transition state changes.
	}
	
	// starting point to break a tile
	boolean breakTile() {
		return replaceTile(true, null);
	}
	// starting point to replace a tile
	/*boolean replaceTile(@NotNull ServerTileType newType) { return replaceTile(new TileTypeInfo(newType)); }*/
	boolean setTile(@NotNull ServerTileType newType) {
		return replaceTile(true, newType);
	}
	// can be called down the line after either method above, after the exit animation plays
	boolean breakTile(@Nullable ServerTileType replacementType) {
		return replaceTile(false, replacementType);
	}
	private synchronized boolean replaceTile(boolean checkForExitAnim, @Nullable ServerTileType replacementType) {
		ServerTileType type = getType();
		if(checkForExitAnim) {
			boolean addNext = replacementType != null;
			TileType nextType = replacementType == null ? getUnderType() : replacementType;
			if(type.get(P.TRANS).tryStartAnimation(this, nextType, addNext)) {
				// transitioning successful, tile will be broken after exit animation
				return true; // don't actually break the tile yet (but line above, still signal for update)
			}
		}
		
		// Action destroyAction = getCacheMap(type.getTypeEnum()).get(TileCacheTag.DestroyAction);
		// ServerTileType prevType = getTypeStack().removeLayer();
		ServerTileType prevType = getType();
		getTypeStack().remove(prevType.getTypeEnum().layer);
		// if(destroyAction != null)
		// 	destroyAction.act();
		
		getLevel().resetTileData(this);
		
		if(replacementType != null) {
			// don't worry if a tile type was removed or not, add the next one anyway.
			addTile(replacementType, type); // handles entity movement and tile update
			return true;
		}
		else if(prevType != null) {
			// a tile type was removed
			moveEntities(getType());
			getLevel().onTileUpdate(this);
			return true;
		}
		
		return false; // cannot break this tile any further.
	}
	
	public synchronized boolean removeLayer(TileLayer layer) {
		TileType type = getTypeStack().remove(layer);
		if(type == null)
			return false;
		
		TileTypeEnum under = type.getTypeEnum().getUnderType();
		if(under != null)
			addTile(getWorld().getTileType(under));
		return true;
	}
	
	private void moveEntities(ServerTileType newType) {
		// check for entities that will not be allowed on the new tile, and move them to the closest adjacent tile they are allowed on.
		if(newType.isWalkable()) return; // no worries for this type.
		
		HashSet<Tile> surroundingTileSet = getAdjacentTiles(true);
		Tile[] surroundingTiles = surroundingTileSet.toArray(new Tile[0]);
		for(Entity entity: getLevel().getOverlappingEntities(getBounds())) {
			// for each entity, check if it can walk on the new tile type. If not, fetch the surrounding tiles, remove those the entity can't walk on, and then fetch the closest tile of the remaining ones.
			// if(newType.isWalkable()) continue; // no worries for this entity.
			
			Array<Tile> aroundTiles = new Array<>(surroundingTiles);
			for(int i = 0; i < aroundTiles.size; i++) {
				if(!((ServerTile)aroundTiles.get(i)).getType().isWalkable()) {
					aroundTiles.removeIndex(i);
					i--;
				}
			}
			
			// from the remaining tiles, find the one that is closest to the entity.
			Tile closest = entity.getClosestTile(aroundTiles);
			// if none remain (returned tile is null), take no action for that entity. If a tile is returned, then move the entity just barely inside the new tile.
			if(closest != null) {
				Rectangle tileBounds = closest.getBounds();
				
				Tile secClosest = closest;
				do {
					aroundTiles.removeValue(secClosest, false);
					secClosest = entity.getClosestTile(aroundTiles);
				} while(secClosest != null && secClosest.x != closest.x && secClosest.y != closest.y);
				if(secClosest != null)
					// expand the rect that the player can be moved to so it's not so large.
					tileBounds.merge(secClosest.getBounds());
				
				Rectangle entityBounds = entity.getBounds();
				MyUtils.moveRectInside(entityBounds, tileBounds, 0.05f);
				entity.moveTo(entityBounds.x, entityBounds.y);
			}
		}
	}
	
	public float update() {
		float min = 0;
		for(ServerTileType type: getTypeStack().values()) {
			float wait = type.update(this);
			if(min == 0)
				min = wait;
			else if(wait != 0)
				min = Math.min(wait, min);
		}
		return min;
	}
	
	@Override public void render(SpriteBatch batch, float delta, Vector2 posOffset) {}
	
	@Override
	public boolean isPermeable() {
		return getType().isWalkable();
	}
	
	@Override
	public Result attackedBy(WorldObject obj, @Nullable Item item, int damage) {
		if(getType().get(P.TRANS).playingExitAnimation(this))
			return Result.NONE;
		return getType().attacked(this, obj, (ServerItem) item, damage);
	}
	
	@Override
	public Result interactWith(Player player, @Nullable Item heldItem) {
		if(getType().get(P.TRANS).playingExitAnimation(this))
			return Result.NONE;
		return getType().interact(this, player, (ServerItem) heldItem);
	}
	
	@Override
	public boolean touchedBy(Entity entity) {
		if(getType().get(P.TRANS).playingExitAnimation(this))
			return false;
		return getType().touched(this, entity, true);
	}
	
	@Override
	public void touching(Entity entity) {
		if(getType().get(P.TRANS).playingExitAnimation(this))
			return;
		getType().touched(this, entity, false);
	}
	
	@Override
	public boolean equals(Object other) { return other instanceof ServerTile && super.equals(other); }
}
