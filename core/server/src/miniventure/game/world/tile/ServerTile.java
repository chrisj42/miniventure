package miniventure.game.world.tile;

import java.util.HashSet;

import miniventure.game.item.Item;
import miniventure.game.item.Result;
import miniventure.game.item.ServerItem;
import miniventure.game.util.MyUtils;
import miniventure.game.util.customenum.SerialMap;
import miniventure.game.world.Level;
import miniventure.game.world.ServerLevel;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.player.Player;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** @noinspection EqualsAndHashcode*/
public class ServerTile extends Tile {
	
	public ServerTile(@NotNull Level level, int x, int y, @NotNull TileTypeEnum[] types) {
		this((ServerLevel)level, x, y, types);
	}
	public ServerTile(@NotNull ServerLevel level, int x, int y, @NotNull TileTypeEnum[] types) {
		super(level, x, y, types);
		
		for(TileTypeEnum type: types)
			this.dataMaps.put(type, ServerTileType.get(type).getInitialData());
	}
	
	public ServerTile(@NotNull Level level, int x, int y, @NotNull TileTypeEnum[] types, SerialMap[] dataMaps) {
		this((ServerLevel) level, x, y, types, dataMaps);
	}
	public ServerTile(@NotNull ServerLevel level, int x, int y, @NotNull TileTypeEnum[] types, SerialMap[] dataMaps) {
		super(level, x, y, types);
		
		for(int i = 0; i < types.length; i++)
			this.dataMaps.put(types[i], dataMaps[i]);
	}
	
	@Override
	TileStack<ServerTileType> makeStack(@NotNull TileTypeEnum[] types) {
		return new ServerTileStack(types);
	}
	
	@Override @NotNull
	public ServerLevel getLevel() { return (ServerLevel) super.getLevel(); }
	
	@Override
	public ServerTileType getType() { return (ServerTileType) super.getType(); }
	
	@Override
	public ServerTileStack getTypeStack() {
		return (ServerTileStack) super.getTypeStack();
	}
	
	public boolean addTile(@NotNull ServerTileType newType) { return addTile(newType, getType()); }
	private boolean addTile(@NotNull ServerTileType newType, @NotNull ServerTileType prevType) {
		
		moveEntities(newType);
		
		getTypeStack().addLayer(newType);
		dataMaps.put(newType.getTypeEnum(), newType.getInitialData());
		
		// check for an entrance animation
		newType.transitionManager.tryStartAnimation(this, prevType);
		// we don't use the return value because transition or not, there's nothing we need to do. :P
		
		getLevel().onTileUpdate(this);
		return true;
	}
	
	boolean breakTile() { return breakTile(true); }
	boolean breakTile(boolean checkForExitAnim) {
		if(checkForExitAnim) {
			ServerTileType type = getType();
			if(type.transitionManager.tryStartAnimation(this, getTypeStack().getLayerFromTop(1, true), false)) {
				// transitioning successful
				getLevel().onTileUpdate(this);
				return true; // don't actually break the tile yet (but line above, still signal for update)
			}
		}
		
		ServerTileType prevType = getTypeStack().removeLayer();
		
		if(prevType != null) {
			dataMaps.remove(prevType.getTypeEnum());
			moveEntities(getType());
			getLevel().onTileUpdate(this);
			return true;
		}
		
		return false; // cannot break this tile any further.
	}
	
	boolean replaceTile(@NotNull ServerTileType newType) {
		// for doors, the animations will be attached to the open door type; an entrance when coming from a closed door, and an exit when going to a closed door.
		/*
			when adding a type, check only for an entrance animation on the new type, and do it after adding it to the stack. when the animation finishes, do nothing except finish the animation.
			when removing a type, check for an exit anim on the type, before removing. When animation finishes, remove the type for real.
			
			when replacing a type, the old type is checked for an exit anim. but the new type is also
		 */
		
		ServerTileType type = getType();
		// ServerTileType underType = getTypeStack().getLayerFromTop(1, true);
		
		if(newType.equals(type)) {
			// just reset the data
			dataMaps.put(type.getTypeEnum(), type.getInitialData());
			getLevel().onTileUpdate(this);
			return true;
		}
		
		// DISABLED because I don't check it in addTile, so checking it here seems inconsistent.
		// check that the new type can be placed on the type that was under the previous type
		/*if(newType.getTypeEnum().compareTo(underType.getTypeEnum()) <= 0) {
			System.err.println("cannot replace; new type "+newType.getTypeEnum()+" does not come after under type "+underType.getTypeEnum());
			return false; // cannot replace tile
		}*/
		
		if(type.transitionManager.tryStartAnimation(this, newType, true)) {
			// there is an exit animation; it needs to be played. So let that happen, the tile will be replaced later
			getLevel().onTileUpdate(this);
			return true; // can replace (but will do it in a second)
		}
		
		// no exit animation, so remove the current tile (without doing the exit anim obviously, since we just checked) and add the new one
		breakTile(false);
		return addTile(newType, type); // already checks for entrance animation, so we don't need to worry about that; but we do need to pass the previous type, otherwise it will compare with the under type.
		// the above should always return true, btw, because we already checked with the same conditional a few lines up.
	}
	
	private void moveEntities(ServerTileType newType) {
		// check for entities that will not be allowed on the new tile, and move them to the closest adjacent tile they are allowed on.
		HashSet<Tile> surroundingTileSet = getAdjacentTiles(true);
		Tile[] surroundingTiles = surroundingTileSet.toArray(new Tile[surroundingTileSet.size()]);
		for(Entity entity: getLevel().getOverlappingEntities(getBounds())) {
			// for each entity, check if it can walk on the new tile type. If not, fetch the surrounding tiles, remove those the entity can't walk on, and then fetch the closest tile of the remaining ones.
			if(newType.isWalkable()) continue; // no worries for this entity.
			
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
		if(getType().transitionManager.playingExitAnimation(this))
			return Result.NONE;
		return getType().attacked(this, obj, (ServerItem) item, damage);
	}
	
	@Override
	public Result interactWith(Player player, @Nullable Item heldItem) {
		if(getType().transitionManager.playingExitAnimation(this))
			return Result.NONE;
		return getType().interact(this, player, (ServerItem) heldItem);
	}
	
	@Override
	public boolean touchedBy(Entity entity) {
		if(getType().transitionManager.playingExitAnimation(this))
			return false;
		return getType().touched(this, entity, true);
	}
	
	@Override
	public void touching(Entity entity) {
		if(getType().transitionManager.playingExitAnimation(this))
			return;
		getType().touched(this, entity, false);
	}
	
	@Override
	public boolean equals(Object other) { return other instanceof ServerTile && super.equals(other); }
}
