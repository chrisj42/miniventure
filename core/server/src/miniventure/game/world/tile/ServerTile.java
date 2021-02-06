package miniventure.game.world.tile;

import miniventure.game.item.Item;
import miniventure.game.item.Result;
import miniventure.game.item.ServerItem;
import miniventure.game.network.GameServer;
import miniventure.game.util.MyUtils;
import miniventure.game.util.function.Action;
import miniventure.game.util.pool.RectPool;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.player.Player;
import miniventure.game.world.level.ServerLevel;
import miniventure.game.world.management.ServerWorld;
import miniventure.game.world.tile.ServerTileType.P;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** @noinspection EqualsAndHashcode*/
public class ServerTile extends Tile {
	
	public ServerTile(@NotNull ServerLevel level, int x, int y) {
		super(level, x, y);
	}
	
	@Override
	ServerTileStack makeStack() {
		return new ServerTileStack(this);
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
	public ServerTileStack getTypeStack() {
		return (ServerTileStack) super.getTypeStack();
	}
	
	public void addTile(@NotNull ServerTileType newType) { addTile(new TileTypeInfo(newType)); }
	public synchronized void addTile(@NotNull TileTypeInfo newType) { addTile(newType, getType()); }
	// not synchronizing this only because it's always called in a synchronized context.
	private void addTile(@NotNull TileTypeInfo newTypeInfo, @NotNull ServerTileType prevType) {
		ServerTileType newType = ServerTileType.get(newTypeInfo.typeEnum);
		
		moveEntities(newType);
		
		getTypeStack().addLayer(newType, newTypeInfo.getData());
		
		// check for an entrance animation
		if(!newType.get(P.TRANS).tryStartAnimation(this, prevType))
			getLevel().onTileUpdate(this, newTypeInfo.typeEnum); // trigger update manually since tile still changed, just without an animation; tryStartAnimation only triggers updates for transition state changes.
	}
	
	// starting point to break a tile
	boolean breakTile() {
		return removeTile(true, null);
	}
	// starting point to replace a tile
	boolean replaceTile(@NotNull ServerTileType newType) { return replaceTile(new TileTypeInfo(newType)); }
	boolean replaceTile(@NotNull TileTypeInfo newType) {
		return removeTile(true, newType);
	}
	// can be called down the line after either method above, after the exit animation plays
	boolean breakTile(@Nullable TileTypeInfo replacementType) {
		return removeTile(false, replacementType);
	}
	private synchronized boolean removeTile(boolean checkForExitAnim, @Nullable TileTypeInfo replacementType) {
		ServerTileType type = getType();
		if(checkForExitAnim) {
			boolean addNext = replacementType != null;
			TileTypeInfo nextType = replacementType == null ? new TileTypeInfo(getTypeStack().getLayerFromTop(1, true)) : replacementType;
			if(type.get(P.TRANS).tryStartAnimation(this, nextType, addNext)) {
				// transitioning successful, tile will be broken after exit animation
				return true; // don't actually break the tile yet (but line above, still signal for update)
			}
		}
		
		Action destroyAction = getDataMap(type.getTypeEnum()).getIfValid(TileDataTag.DestroyAction);
		ServerTileType prevType = getTypeStack().removeLayer();
		if(destroyAction != null)
			destroyAction.act();
		
		if(replacementType != null) {
			// don't worry if a tile type was removed or not, add the next one anyway.
			addTile(replacementType, type); // handles entity movement and tile update
			return true;
		}
		else if(prevType != null) {
			// a tile type was removed
			moveEntities(getType());
			getLevel().onTileUpdate(this, null);
			return true;
		}
		
		return false; // cannot break this tile any further.
	}
	
	private void moveEntities(ServerTileType newType) {
		// check for entities that will not be allowed on the new tile, and move them to the closest adjacent tile they are allowed on.
		if(newType.isWalkable()) return; // no worries for this type.
		
		Array<Tile> surroundingTiles = new Array<>(Tile.class);
		forAdjacentTiles(true, surroundingTiles::add);
		// Tile[] surroundingTiles = surroundingTileSet.toArray(new Tile[0]);
		getLevel().forOverlappingEntities(getBounds(), true, entity -> {
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
				if(secClosest != null) {
					Rectangle secBounds = secClosest.getBounds();
					// expand the rect that the player can be moved to so it's not so large.
					tileBounds.merge(secBounds);
					RectPool.POOL.free(secBounds);
				}
				
				Rectangle entityBounds = entity.getBounds();
				MyUtils.moveRectInside(entityBounds, tileBounds, 0.05f);
				entity.moveTo(entityBounds.x, entityBounds.y);
				RectPool.POOL.free(entityBounds);
				RectPool.POOL.free(tileBounds);
			}
		});
	}
	
	public float update() {
		float min = 0;
		for(ServerTileType type: getTypeStack().getTypes()) {
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
		ServerTileType type = getType();
		if(type.get(P.TRANS).playingExitAnimation(this))
			return Result.NONE;
		return type.get(P.DESTRUCT).tileAttacked(this, obj, (ServerItem) item, damage);
	}
	
	@Override
	public Result interactWith(Player player, @Nullable Item heldItem) {
		ServerTileType type = getType();
		if(type.get(P.TRANS).playingExitAnimation(this))
			return Result.NONE;
		return type.get(P.INTERACT).onInteract(this, player, (ServerItem) heldItem);
	}
	
	@Override
	public void touchedBy(Entity entity, boolean initial) {
		ServerTileType type = getType();
		if(!type.get(P.TRANS).playingExitAnimation(this))
			type.get(P.TOUCH).onTouched(this, entity, initial);
	}
	
	@Override
	public boolean equals(Object other) { return other instanceof ServerTile && super.equals(other); }
}
