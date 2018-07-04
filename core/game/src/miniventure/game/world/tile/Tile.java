package miniventure.game.world.tile;

import java.util.*;
import java.util.Map.Entry;

import miniventure.game.item.Item;
import miniventure.game.texture.TextureHolder;
import miniventure.game.util.MyUtils;
import miniventure.game.util.RelPos;
import miniventure.game.world.Level;
import miniventure.game.world.Point;
import miniventure.game.world.WorldManager;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.tile.TileType.TileTypeEnum;
import miniventure.game.world.tile.data.DataMap;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Tile implements WorldObject {
	
	/*
		So. The client, in terms of properties, doesn't have to worry about tile interaction properties. It always sends a request to the server when interactions should occur.
		The server, in turn, doesn't have to worry about animation or rendering properties.
		
		So, it seems like we have a situation where there are ServerProperties and ClientProperties, and some property types are both.
		
		This could mean that I should just take the client and server properties out of the main game module and into their own respective modules. But that would mean they can never be referenced in the main package, and I like how the property types are all given in the same place, the TileType class. So maybe I can leave the shell there, and just separate the stuff that actually does the heavy lifting..?
		
		 Hey, that actually gives me an idea: What if the property types/classes I specify in the TileType class weren't actually the objects that I used for the tile behaviors, in the end? What if they were just markers, and the actual property instances were instantiated later? For entities, they would obviously be instantiated on entity creation, but since there are so many tiles loaded at one time, we have to do tiles different...
		 Hey, I know: how about we have a tile property instance fetcher, that creates all the actual tile property instances within the respective client/server modules, with the right classes, based on the given main property class? That could work! Then, whenever a tile property was asked for, it would fetch it from the fetcher, given the TileType and property class/type. With entities, each would simply have their own list, their own fetcher.
		 The fetchers would be created in ClientCore and ServerCore, more or less. or maybe the Worlds, since the WorldManager class would have to have a way to fetch a property instance given a TileType and and Property class/type. For entities, the fetcher would be given the entity instance too. Or maybe each entity would just already have its properties. Yeah that'll probably be the case.
		 
		 So! End result. Actual property instances are created in Client/Server individually, not in the TileType enum. That is only where the basic templates go. The is a fetcher that can take a property type instance, and return a completed property instance of that type.
		 Note, we might end up having a property type enum as well as a tile type enum...
	 */
	
	public static final int SIZE = 32;
	
	private TileStack tileStack;
	
	@NotNull private Level level;
	protected final int x, y;
	private EnumMap<TileTypeEnum, DataMap> dataMaps = new EnumMap<>(TileTypeEnum.class);
	
	// the TileType array is ALWAYS expected in order of bottom to top.
	protected Tile(@NotNull Level level, int x, int y, @NotNull TileTypeEnum[] types, @Nullable DataMap[] dataMaps) {
		this.level = level;
		this.x = x;
		this.y = y;
		
		tileStack = new TileStack(getWorld(), types);
		for(int i = 0; i < types.length; i++)
			this.dataMaps.put(types[i], dataMaps == null ? types[i].getTileType(level.getWorld()).getInitialData() : dataMaps[i]);
	}
	
	@NotNull @Override
	public WorldManager getWorld() { return level.getWorld(); }
	
	@NotNull @Override public Level getLevel() { return level; }
	
	@NotNull
	@Override public Rectangle getBounds() { return new Rectangle(x, y, 1, 1); }
	@Override public Vector2 getCenter() { return new Vector2(x+0.5f, y+0.5f); }
	
	public Point getLocation() { return new Point(x, y); }
	
	
	public TileType getType() { return tileStack.getTopLayer(); }
	public TileStack getTypeStack() { return tileStack; }
	
	public DataMap getDataMap() { return getDataMap(getType().getEnumType()); }
	public DataMap getDataMap(TileType tileType) { return getDataMap(tileType.getEnumType()); }
	public DataMap getDataMap(TileTypeEnum tileType) { return dataMaps.get(tileType); }
	
	
	public boolean addTile(@NotNull TileType newType) { return addTile(newType, getType()); }
	private boolean addTile(@NotNull TileType newType, @NotNull TileType prevType) {
		
		moveEntities(newType);
		
		tileStack.addLayer(newType);
		dataMaps.put(newType.getEnumType(), newType.getInitialData());
		
		// check for an entrance animation
		newType.getRenderer().transitionManager.tryStartAnimation(this, prevType);
		// we don't use the return value because transition or not, there's nothing we need to do. :P
		
		return true;
	}
	
	boolean breakTile() { return breakTile(true); }
	boolean breakTile(boolean checkForExitAnim) {
		if(checkForExitAnim) {
			TileType type = getType();
			if(type.getRenderer().transitionManager.tryStartAnimation(this, tileStack.getLayerFromBottom(1, true), false))
				// transitioning successful
				return true; // don't actually break the tile yet (but still signal for update)
		}
		
		TileType prevType = tileStack.removeLayer();
		
		if(prevType != null) {
			dataMaps.remove(prevType.getEnumType());
			moveEntities(getType());
			return true;
		}
		
		return false; // cannot break this tile any further.
	}
	
	boolean replaceTile(@NotNull TileType newType) {
		// for doors, the animations will be attached to the open door type; an entrance when coming from a closed door, and an exit when going to a closed door.
		/*
			when adding a type, check only for an entrance animation on the new type, and do it after adding it to the stack. when the animation finishes, do nothing except finish the animation.
			when removing a type, check for an exit anim on the type, before removing. When animation finishes, remove the type for real.
			
			when replacing a type, the old type is checked for an exit anim. but the new type is also
		 */
		
		TileType type = getType();
		TileType underType = tileStack.size() == 1 ? type : tileStack.getLayerFromBottom(1);
		
		if(newType.equals(type)) {
			// just reset the data
			dataMaps.put(type.getEnumType(), type.getInitialData());
			return true;
		}
		
		// check that the new type can be placed on the type that was under the previous type
		if(newType.getEnumType().compareTo(underType.getEnumType()) <= 0)
			return false; // cannot replace tile
		
		if(type.getRenderer().transitionManager.tryStartAnimation(this, newType, true))
			// there is an exit animation; it needs to be played. So let that happen, the tile will be replaced later
			return true; // can replace (but will do it in a second)
		
		// no exit animation, so remove the current tile (without doing the exit anim obviously, since we just checked) and add the new one
		breakTile(false);
		return addTile(newType, type); // already checks for entrance animation, so we don't need to worry about that; but we do need to pass the previous type, otherwise it will compare with the under type.
		// the above should always return true, btw, because we already checked with the same conditional a few lines up.
	}
	
	private void moveEntities(TileType newType) {
		// check for entities that will not be allowed on the new tile, and move them to the closest adjacent tile they are allowed on.
		HashSet<Tile> surroundingTileSet = getAdjacentTiles(true);
		Tile[] surroundingTiles = surroundingTileSet.toArray(new Tile[surroundingTileSet.size()]);
		for(Entity entity: level.getOverlappingEntities(getBounds())) {
			// for each entity, check if it can walk on the new tile type. If not, fetch the surrounding tiles, remove those the entity can't walk on, and then fetch the closest tile of the remaining ones.
			if(newType.isPermeableBy(entity)) continue; // no worries for this entity.
			
			Array<Tile> aroundTiles = new Array<>(surroundingTiles);
			for(int i = 0; i < aroundTiles.size; i++) {
				if(!aroundTiles.get(i).getType().isPermeableBy(entity)) {
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
				entity.moveTo(closest.level, entityBounds.x, entityBounds.y);
			}
		}
	}
	
	public HashSet<Tile> getAdjacentTiles(boolean includeCorners) {
		if(includeCorners)
			return level.getAreaTiles(x, y, 1, false);
		else {
			HashSet<Tile> tiles = new HashSet<>();
			if(x > 0) tiles.add(level.getTile(x-1, y));
			if(y < level.getHeight()-1) tiles.add(level.getTile(x, y+1));
			if(x < level.getWidth()-1) tiles.add(level.getTile(x+1, y));
			if(y > 0) tiles.add(level.getTile(x, y-1));
			tiles.remove(null);
			return tiles;
		}
	}
	
	@Override
	public void render(SpriteBatch batch, float delta, Vector2 posOffset) {}
	
	public void updateSprites() {}
	
	public float update() {
		if(getType().getRenderer().transitionManager.playingExitAnimation(this))
			// playing exit anim; no more updates
			return 0;
		
		return getType().update(this);
	}
	
	@Override
	public float getLightRadius() {
		float maxRadius = 0;
		for(TileType type: tileStack.getTypes())
			maxRadius = Math.max(maxRadius, type.getLightRadius());
		
		return maxRadius;
	}
	
	@Override
	public boolean isPermeableBy(Entity e) {
		return getType().isPermeableBy(e);
	}
	
	@Override
	public boolean attackedBy(WorldObject obj, @Nullable Item item, int damage) {
		if(getType().getRenderer().transitionManager.playingExitAnimation(this))
			return false;
		return getType().attacked(this, obj, item, damage);
	}
	
	@Override
	public boolean interactWith(Player player, @Nullable Item heldItem) {
		if(getType().getRenderer().transitionManager.playingExitAnimation(this))
			return false;
		return getType().interact(this, player, heldItem);
	}
	
	@Override
	public boolean touchedBy(Entity entity) {
		if(getType().getRenderer().transitionManager.playingExitAnimation(this))
			return false;
		return getType().touched(this, entity, true);
	}
	
	@Override
	public void touching(Entity entity) {
		if(getType().getRenderer().transitionManager.playingExitAnimation(this))
			return;
		getType().touched(this, entity, false);
	}
	
	@Override
	public String toString() { return getType()+" Tile"; }
	
	public String toLocString() { return (x-level.getWidth()/2)+","+(y-level.getHeight()/2)+" ("+toString()+")"; }
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof Tile)) return false;
		Tile o = (Tile) other;
		return level.equals(o.level) && x == o.x && y == o.y;
	}
	
	@Override
	public int hashCode() { return Point.javaPointHashCode(x, y) + level.getDepth() * 17; }
	
	// I can use the string encoder and string parser in MyUtils to encode the tile data in a way so that I can always re-parse the encoded array. I can use this internally to, with other things, whenever I need to encode a list of objects and don't want to worry about finding the delimiter symbol in string somewhere I don't expect.
	
	public static class TileData {
		public final int[] typeOrdinals;
		public final String[] data;
		
		private TileData() { this(null, null); }
		private TileData(int[] typeOrdinals, String[] data) {
			this.typeOrdinals = typeOrdinals;
			this.data = data;
		}
		public TileData(Tile tile) {
			TileTypeEnum[] tileTypes = tile.getTypeStack().getEnumTypes(true);
			typeOrdinals = new int[tileTypes.length];
			for(int i = 0; i < tileTypes.length; i++) {
				TileTypeEnum type = tileTypes[i];
				typeOrdinals[i] = type.ordinal();
			}
			
			this.data = new String[tileTypes.length];
			for(int i = 0; i < data.length; i++)
				data[i] = tile.dataMaps.get(tileTypes[i]).serialize();
		}
		
		public TileTypeEnum[] getTypes() { return getTypes(typeOrdinals); }
		public static TileTypeEnum[] getTypes(int[] typeOrdinals) {
			TileTypeEnum[] types = new TileTypeEnum[typeOrdinals.length];
			for(int i = 0; i < types.length; i++) {
				types[i] = TileTypeEnum.values(typeOrdinals[i]);
			}
			return types;
		}
		
		public DataMap[] getDataMaps() { return getDataMaps(data); }
		public static DataMap[] getDataMaps(String[] data) {
			DataMap[] maps = new DataMap[data.length];
			for(int i = 0; i < data.length; i++)
				maps[i] = DataMap.deserialize(data[i]);
			return maps;
		}
		
		public void apply(Tile tile) {
			TileTypeEnum[] types = getTypes();
			
			tile.tileStack = new TileStack(tile.getWorld(), types);
			tile.dataMaps.clear();
			for(int i = 0; i < data.length; i++)
				tile.dataMaps.put(types[i], DataMap.deserialize(data[i]));
			
			tile.updateSprites();
		}
	}
	
	public static class TileTag implements Tag<Tile> {
		public final int x;
		public final int y;
		public final int levelDepth;
		
		private TileTag() { this(0, 0, 0); }
		public TileTag(Tile tile) { this(tile.x, tile.y, tile.getLevel().getDepth()); }
		public TileTag(int x, int y, int levelDepth) {
			this.x = x;
			this.y = y;
			this.levelDepth = levelDepth;
		}
		
		@Override
		public Tile getObject(WorldManager world) {
			Level level = world.getLevel(levelDepth);
			if(level != null)
				return level.getTile(x, y);
			return null;
		}
	}
	
	@Override
	public Tag<Tile> getTag() { return new TileTag(this); }
}
