package miniventure.game.world.tile;

import java.util.Arrays;
import java.util.Stack;

import miniventure.game.item.Item;
import miniventure.game.util.MyUtils;
import miniventure.game.world.Level;
import miniventure.game.world.Point;
import miniventure.game.world.WorldManager;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.Player;

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
	private static final TileType baseType = TileType.values[0];
	
	private Stack<TileType> tileTypes = new Stack<>(); // using a for each loop for iteration will go from the bottom of the stack to the top.
	
	@NotNull private Level level;
	protected final int x, y;
	private String[] data;
	
	// the TileType array is ALWAYS expected in order of bottom to top.
	protected Tile(@NotNull Level level, int x, int y, @NotNull TileType[] types, @NotNull String[] data) {
		this.level = level;
		this.x = x;
		this.y = y;
		
		this.data = data;
		// because this is the first type, we need to establish all the tiles under it.
		for(TileType type: types)
			tileTypes.push(type);
	}
	
	public TileType getType() { return tileTypes.peek(); }
	TileType[] getTypes() { return tileTypes.toArray(new TileType[tileTypes.size()]); }
	boolean hasType(TileType type) { return tileTypes.contains(type); }
	
	String getData(TilePropertyType propertyType, TileType type, int propDataIndex) {
		return data[getIndex(type, propertyType, propDataIndex)];
	}
	
	void setData(TilePropertyType propertyType, TileType type, int propDataIndex, String data) {
		this.data[getIndex(type, propertyType, propDataIndex)] = data;
	}
	
	private int getIndex(TileType type, TilePropertyType propertyType, int propDataIndex) {
		if(!tileTypes.contains(type))
			throw new IllegalArgumentException("Tile " + this + " does not have a " + type + " type, cannot fetch the data index.");
		
		type.checkDataAccess(propertyType, propDataIndex);
		
		int offset = 0;
		
		for(int i = tileTypes.size()-1; i >= 0; i--) {
			TileType cur = tileTypes.get(i);
			if(!type.equals(cur))
				offset += cur.getDataLength();
			else
				break;
		}
		
		return type.getPropDataIndex(propertyType) + propDataIndex + offset;
	}
	
	<T extends TileProperty> T getProp(TileType tileType, TilePropertyType<T> propertyType) {
		return getWorld().getTilePropertyFetcher().getProp(propertyType, tileType);
	}
	
	<T extends TileProperty> T getProp(TileType tileType, TilePropertyType<? super T> propertyType, Class<T> asClass) {
		return getWorld().getTilePropertyFetcher().getProp(propertyType, tileType, asClass);
	}
	
	
	
	
	@NotNull @Override
	public WorldManager getWorld() { return level.getWorld(); }
	
	@NotNull @Override public Level getLevel() { return level; }
	
	@NotNull
	@Override public Rectangle getBounds() { return new Rectangle(x, y, 1, 1); }
	@Override public Vector2 getCenter() { return new Vector2(x+0.5f, y+0.5f); }
	
	public Point getLocation() { return new Point(x, y); }
	
	public boolean addTile(@NotNull TileType newType) { return addTile(newType, getType()); }
	private boolean addTile(@NotNull TileType newType, @NotNull TileType prevType) {
		// first, check to see if the newType can validly be placed on the current type.
		if(newType == getType()
			|| newType.compareTo(getType()) <= 0)
			return false;
		
		moveEntities(newType);
		
		String[] newData = newType.getInitialData();
		String[] fullData = new String[data.length + newData.length];
		System.arraycopy(data, 0, fullData, newData.length, data.length); // copy old data to end of data
		System.arraycopy(newData, 0, fullData, 0, newData.length); // copy new data to front of data
		data = fullData;
		
		tileTypes.push(newType);
		
		// check for an entrance animation
		getProp(newType, TilePropertyType.Transition).tryStartAnimation(this, prevType);
		// we don't use the return value because transition or not, there's nothing we need to do. :P
		
		return true;
	}
	
	boolean breakTile() { return breakTile(true); }
	boolean breakTile(boolean checkForExitAnim) {
		if(checkForExitAnim) {
			TileType type = getType();
			if(getProp(type, TilePropertyType.Transition).tryStartAnimation(this, tileTypes.size() == 1 ? type : tileTypes.elementAt(tileTypes.size()-2), false))
				// transitioning successful
				return true; // don't actually break the tile yet (but still signal for update)
		}
		
		TileType prevType = tileTypes.pop();
		
		String[] newData = new String[data.length - prevType.getDataLength()];
		System.arraycopy(data, prevType.getDataLength(), newData, 0, newData.length);
		data = newData;
		
		if(tileTypes.size() == 0)
			addTile(baseType);
		else {
			moveEntities(getType());
			return true;
		}
		
		return false; // addTile does the update
	}
	
	boolean replaceTile(@NotNull TileType newType) {
		// for doors, the animations will be attached to the open door type; an entrance when coming from a closed door, and an exit when going to a closed door.
		/*
			when adding a type, check only for an entrance animation on the new type, and do it after adding it to the stack. when the animation finishes, do nothing except finish the animation.
			when removing a type, check for an exit anim on the type, before removing. When animation finishes, remove the type for real.
			
			when replacing a type, the old type is checked for an exit anim. but the new type is also
		 */
		
		TileType type = getType();
		TileType underType = tileTypes.size() == 1 ? type : tileTypes.elementAt(tileTypes.size()-2);
		
		if(newType == type) {
			// just reset the data
			String[] initData = type.getInitialData();
			if(initData.length > 0)
				System.arraycopy(initData, 0, data, data.length-initData.length, initData.length);
			return true;
		}
		
		// check that the new type can be placed on the type that was under the previous type
		if(newType.compareTo(underType) <= 0)
			return false; // cannot replace tile
		
		if(getProp(type, TilePropertyType.Transition).tryStartAnimation(this, newType, true))
			// there is an exit animation; it needs to be played. So let that happen, the tile will be replaced later
			return true; // can replace (but will do it in a second)
		
		// no exit animation, so remove the current tile (without doing the exit anim obviously, since we just checked) and add the new one
		breakTile(false);
		return addTile(newType, type); // already checks for entrance animation, so we don't need to worry about that; but we do need to pass the previous type, otherwise it will compare with the under type.
		// the above should always return true, btw, because we already checked with the same conditional a few lines up.
	}
	
	private void moveEntities(TileType newType) {
		// check for entities that will not be allowed on the new tile, and move them to the closest adjacent tile they are allowed on.
		Array<Tile> surroundingTiles = getAdjacentTiles(true);
		for(Entity entity: level.getOverlappingEntities(getBounds())) {
			// for each entity, check if it can walk on the new tile type. If not, fetch the surrounding tiles, remove those the entity can't walk on, and then fetch the closest tile of the remaining ones.
			if(getProp(newType, TilePropertyType.Solid).isPermeableBy(entity)) continue; // no worries for this entity.
			
			Array<Tile> aroundTiles = new Array<>(surroundingTiles);
			for(int i = 0; i < aroundTiles.size; i++) {
				if(!getProp(aroundTiles.get(i).getType(), TilePropertyType.Solid).isPermeableBy(entity)) {
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
	
	public Array<Tile> getAdjacentTiles(boolean includeCorners) {
		if(includeCorners)
			return level.getAreaTiles(x, y, 1, false);
		else {
			Array<Tile> tiles = new Array<>();
			if(x > 0) tiles.add(level.getTile(x-1, y));
			if(y < level.getHeight()-1) tiles.add(level.getTile(x, y+1));
			if(x < level.getWidth()-1) tiles.add(level.getTile(x+1, y));
			if(y > 0) tiles.add(level.getTile(x, y-1));
			boolean hasNull = true;
			while(hasNull) hasNull = tiles.removeValue(null, true);
			return tiles;
		}
	}
	
	@Override
	public void render(SpriteBatch batch, float delta, Vector2 posOffset) {}
	
	
	public void tick() {}
	
	public boolean update(float delta, boolean initial) {
		TransitionProperty transProp = getProp(getType(), TilePropertyType.Transition);
		if(transProp.playingAnimation(this) && !transProp.isEntranceAnim(this))
			// playing exit anim; no more updates
			return false;
		
		boolean update;
		
		if(initial)
			update = getProp(getType(), TilePropertyType.Update).firstUpdate(this);
		else
			update = getProp(getType(), TilePropertyType.Update).update(delta, this);
		
		return update;
	}
	
	@Override
	public float getLightRadius() {
		float maxRadius = 0;
		for(TileType type: tileTypes)
			maxRadius = Math.max(maxRadius, getProp(type, TilePropertyType.Light).getLightRadius());
		
		return maxRadius;
	}
	
	@Override
	public boolean isPermeableBy(Entity e) {
		return getProp(getType(), TilePropertyType.Solid).isPermeableBy(e);
	}
	
	@Override
	public boolean attackedBy(WorldObject obj, @Nullable Item item, int damage) {
		if(getProp(getType(), TilePropertyType.Transition).playingExitAnimation(this))
			return false;
		return getProp(getType(), TilePropertyType.Attack).tileAttacked(this, obj, item, damage);
	}
	
	@Override
	public boolean interactWith(Player player, @Nullable Item heldItem) {
		if(getProp(getType(), TilePropertyType.Transition).playingExitAnimation(this))
			return false;
		return getProp(getType(), TilePropertyType.Interact).interact(player, heldItem, this);
	}
	
	@Override
	public boolean touchedBy(Entity entity) {
		if(getProp(getType(), TilePropertyType.Transition).playingExitAnimation(this))
			return false;
		return getProp(getType(), TilePropertyType.Touch).touchedBy(entity, this, true);
	}
	
	@Override
	public void touching(Entity entity) {
		if(getProp(getType(), TilePropertyType.Transition).playingExitAnimation(this))
			return;
		getProp(getType(), TilePropertyType.Touch).touchedBy(entity, this, false);
	}
	
	@Override
	public String toString() { return getType().getName()+" Tile"; }
	
	public String toLocString() { return (x-level.getWidth()/2)+","+(y-level.getHeight()/2)+" ("+toString()+")"; }
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof Tile)) return false;
		Tile o = (Tile) other;
		return level.equals(o.level) && x == o.x && y == o.y;
	}
	
	@Override
	public int hashCode() { return new Point(x, y).hashCode() + level.getDepth() * 17; }
	
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
			this.data = Arrays.copyOf(tile.data, tile.data.length);
			
			TileType[] tileTypes = tile.getTypes();
			typeOrdinals = new int[tileTypes.length];
			for(int i = 0; i < tileTypes.length; i++) {
				TileType type = tileTypes[i];
				typeOrdinals[i] = type.ordinal();
				//for(TilePropertyInstance prop: tile.getWorld().getTilePropertyFetcher().getProperties(type))
				//	prop.configureDataForSave(tile);
			}
		}
		
		public void apply(Tile tile) {
			TileType[] types = new TileType[typeOrdinals.length];
			String[] data = Arrays.copyOf(this.data, this.data.length);
			for(int i = 0; i < types.length; i++) {
				types[i] = TileType.values[typeOrdinals[i]];
				//for(TilePropertyInstance prop: tile.getWorld().getTilePropertyFetcher().getProperties(types[i]))
				//	prop.configureDataForLoad(tile);
			}
			
			Stack<TileType> typeStack = new Stack<>();
			for(TileType type: types)
				typeStack.push(type);
			
			tile.tileTypes = typeStack;
			tile.data = data;
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
	public Tag getTag() { return new TileTag(this); }
}
