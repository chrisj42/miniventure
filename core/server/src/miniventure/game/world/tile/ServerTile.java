package miniventure.game.world.tile;

import java.util.HashSet;

import miniventure.game.item.Item;
import miniventure.game.item.Result;
import miniventure.game.item.ServerItem;
import miniventure.game.network.GameServer;
import miniventure.game.util.MyUtils;
import miniventure.game.world.Boundable;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.player.Player;
import miniventure.game.world.level.Level;
import miniventure.game.world.level.ServerLevel;
import miniventure.game.world.management.ServerWorld;
import miniventure.game.world.tile.ServerTileType.P;
import miniventure.game.world.tile.TileType.TileTypeEnum;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ServerTile extends Tile {
	
	private float lastUpdate;
	
	public ServerTile(@NotNull Level level, int x, int y, @NotNull TileTypeEnum[] types) {
		this(level, x, y, types, null);
	}
	public ServerTile(@NotNull Level level, int x, int y, @NotNull TileTypeEnum[] types, TileStackData dataMaps) {
		this((ServerLevel) level, x, y, types, dataMaps);
	}
	public ServerTile(@NotNull ServerLevel level, int x, int y, @NotNull TileTypeEnum[] types, TileStackData dataMaps) {
		super(level, x, y, types, dataMaps);
	}
	
	/*@Override
	ServerTileStack makeStack(@NotNull TileTypeEnum[] types, @Nullable TileTypeDataMap[] dataMaps) {
		return new ServerTileStack(getWorld(), types, dataMaps);
	}*/
	
	@Override @NotNull
	public ServerWorld getWorld() { return (ServerWorld) super.getWorld(); }
	
	@NotNull
	public GameServer getServer() { return getWorld().getServer(); }
	
	@Override @NotNull
	public ServerLevel getLevel() { return (ServerLevel) super.getLevel(); }
	
	@Override
	public ServerTileType getType() { return (ServerTileType) super.getType(); }
	
	// @Override
	// public ServerTileStack getTypeStack() {
	// 	return (ServerTileStack) super.getTypeStack();
	// }
	
	// public void addTile(@NotNull TileTypeEnum newType) { addTile(new TileTypeInfo(newType)); }
	public void addTile(@NotNull TileTypeEnum newType) { addTile(newType, getType().getTypeEnum()); }
	// not synchronizing this only because it's always called in a synchronized context.
	private void addTile(@NotNull TileTypeEnum newType, @NotNull TileTypeEnum prevType) {
		ServerTileType newTypeInstance = ServerTileType.get(newType);
		
		moveEntities(newTypeInstance);
		
		addLayer(newTypeInstance);
		
		// check for an entrance animation
		if(!newTypeInstance.get(P.TRANS).tryStartAnimation(setMainContext(), prevType))
			getLevel().onTileUpdate(this, newType); // trigger update manually since tile still changed, just without an animation; tryStartAnimation only triggers updates for transition state changes.
	}
	
	// starting point to break a tile
	boolean breakTile() {
		return removeTile(true, null);
	}
	// starting point to replace a tile
	boolean replaceTile(@NotNull TileTypeEnum newType) { return removeTile(true, newType); }
	// boolean replaceTile(@NotNull TileTypeInfo newType) {
	// 	return removeTile(true, newType);
	// }
	// can be called down the line after either method above, after the exit animation plays
	boolean breakTile(@Nullable TileTypeEnum replacementType) {
		return removeTile(false, replacementType);
	}
	private boolean removeTile(boolean checkForExitAnim, @Nullable TileTypeEnum replacementType) {
		ServerTileType type = getType();
		if(checkForExitAnim) {
			boolean addNext = replacementType != null;
			TileTypeEnum nextType = replacementType == null ? getOnBreakType().getTypeEnum() : replacementType;
			if(type.get(P.TRANS).tryStartAnimation(setMainContext(), nextType, addNext)) {
				// transitioning successful, tile will be broken after exit animation
				return true; // don't actually break the tile yet (but line above, still signal for update)
			}
		}
		
		// Action destroyAction = getDataMap(type.getTypeEnum()).get(TileDataTag.DestroyAction);
		ServerTileType prevType = (ServerTileType) removeLayer();
		// if(destroyAction != null)
		// 	destroyAction.act();
		
		if(replacementType != null) {
			// don't worry if a tile type was removed or not, add the next one anyway.
			addTile(replacementType, type.getTypeEnum()); // handles entity movement and tile update
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
		
		HashSet<Tile> surroundingTileSet = getAdjacentTiles(true);
		Tile[] surroundingTiles = surroundingTileSet.toArray(new Tile[0]);
		for(Entity entity: getLevel().getOverlappingEntities(getBounds())) {
			// for each entity, check if it can walk on the new tile type. If not, fetch the surrounding tiles, remove those the entity can't walk on, and then fetch the closest tile of the remaining ones.
			// if(newType.isWalkable()) continue; // no worries for this entity.
			
			Array<Tile> aroundTiles = new Array<>(surroundingTiles);
			for(int i = 0; i < aroundTiles.size; i++) {
				if(!entity.canPermeate(aroundTiles.get(i))) {
					aroundTiles.removeIndex(i);
					i--;
				}
			}
			
			// if none remain (returned tile is null), take no action for that entity.
			if(aroundTiles.size == 0)
				continue;
			
			// from the remaining tiles, find the one that is closest to the entity.
			Boundable.sortByDistance(aroundTiles, entity.getCenter());
			Tile closest = aroundTiles.removeIndex(0);
			// move the entity just barely inside the new tile.
			Rectangle tileBounds = closest.getBounds();
			
			if(aroundTiles.size > 0) {
				// this section here attempts to find a second tile that's touching the first closest, so that movements aren't unnecessarily jarring
				Tile secClosest = null;
				do {
					Tile next = aroundTiles.removeIndex(0);
					if(next.isAdjacent(closest, false))
						secClosest = next;
				} while (aroundTiles.size > 0 && secClosest == null);
				if (secClosest != null)
					// expand the rect that the player can be moved to so it's not so large.
					tileBounds.merge(secClosest.getBounds());
			}
			
			Rectangle entityBounds = entity.getBounds();
			MyUtils.moveRectInside(entityBounds, tileBounds, 0.05f);
			entity.moveTo(entityBounds.x, entityBounds.y);
		}
	}
	
	public float update() {
		float min = 0;
		
		TileContext context = setMainContext();
		
		float now = getWorld().getGameTime();
		float delta = now - lastUpdate;
		
		// if playing an exit animation, then don't update the tile.
		TransitionManager man = ServerTileType.get(context, P.TRANS);
		if(man.playingAnimation(context))
			return man.tryFinishAnimation(context);
		
		for(int i = 0; i < getStackSize(); i++) {
			context = setContext(i);
			float wait = ServerTileType.get(context, P.UPDATE).update(context, delta);
			if(min == 0)
				min = wait;
			else if(wait != 0)
				min = Math.min(wait, min);
		}
		// setMainContext().setData(TileDataTag.LastUpdate, now);
		this.lastUpdate = now;
		return min;
	}
	
	@Override public void render(SpriteBatch batch, float delta, Vector2 posOffset) {}
	
	@Override
	public boolean isPermeable() {
		return getType().isWalkable();
	}
	
	@Override
	public Result attackedBy(WorldObject obj, @Nullable Item item, int damage) {
		if(getType().get(P.TRANS).playingExitAnimation(setMainContext()))
			return Result.NONE;
		return getType().attacked(setMainContext(), obj, (ServerItem) item, damage);
	}
	
	@Override
	public Result interactWith(Player player, @Nullable Item heldItem) {
		if(getType().get(P.TRANS).playingExitAnimation(setMainContext()))
			return Result.NONE;
		return getType().interact(this, player, (ServerItem) heldItem);
	}
	
	@Override
	public boolean touchedBy(Entity entity) {
		if(getType().get(P.TRANS).playingExitAnimation(setMainContext()))
			return false;
		return getType().touched(this, entity, true);
	}
	
	@Override
	public void touching(Entity entity) {
		if(getType().get(P.TRANS).playingExitAnimation(setMainContext()))
			return;
		getType().touched(this, entity, false);
	}
}
