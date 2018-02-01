package miniventure.game.world.tile;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Stack;
import java.util.TreeMap;

import miniventure.game.item.Item;
import miniventure.game.util.MyUtils;
import miniventure.game.world.Level;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.Mob;
import miniventure.game.world.entity.mob.Player;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public class Tile implements WorldObject {
	
	/*
		So, tiles can have any of the following properties/features:
			- walkable or solid
			- replacement tile (tile that appears underneath when this one is broken; can vary?; defaults to a Hole).
			- special rendering
				- animation
				- lighting
				- different colors? maybe a Recolorable interface, and those can have their hues messed with.
				- over-entity animation/rendering? for liquids, certainly
			- health, or one-hit, or impermeable, or conditionally permeable
			- preferred tool
			- event handling
			- knockback (preset touch event)
			- item drops (preset destroy event)
			- experience drops? (preset destroy event)
			- achievement trigger? (custom)
		 
		Tile data:
			- sprite
			- animation state
		
		
		Events:
			- destroy
			- touch
			- attacked (has default to do damage, can override to check a condition before dealing damage)
			- touch per second
		
		
		Now, how to implement such a system...
		
		
	 */
	
	// attack behavior
	// health property - invincible, normal health, conditionally invincible (will generate attack particle of 0 damage if can't hurt yet)
	
	/* NOTE: for tiles that drop something, they will drop them progressively; the last hit will drop the last one. Though, you can bias it so that the last drops all the items, or the last drops half the items, etc.
		lastDropBias:
			1 = all items are dropped when the tile is destroyed; none before.
			0 = items are dropped at equal intervals so that the last hit drops the last item.
			0.5 = half the items are dropped when the tile is destroyed; the other half is equally distributed.
			i.e. lastDropBias = part of items that are dropped when the tile is destroyed. The rest are equally distributed.
	 */
	
	
	/*
		Perhaps I can manage to only object-ify the nearby tiles. The way it will 
	 */
	
	public static final int SIZE = 32;
	private static final TileType baseType = TileType.values[0];
	
	private Stack<TileType> tileTypes = new Stack<>(); // using a for each loop for iteration will go from the bottom of the stack to the top.
	
	@NotNull private Level level;
	protected final int x, y;
	private String[] data;
	
	private Tile(@NotNull Level level, int x, int y) {
		this.level = level;
		this.x = x;
		this.y = y;
	}
	
	// the TileType array is expected in order of top to bottom. If it does not end with the a HOLE, then methods will be called in an attempt to get there.
	// used most when creating new tiles for new levels
	public Tile(@NotNull Level level, int x, int y, @NotNull TileType... types) {
		this(level, x, y);
		
		Stack<TileType> typeStack = new Stack<>(); // top is first to go in.
		for(TileType type: types)
			typeStack.push(type);
		
		if(typeStack.empty())
			typeStack.push(baseType);
		
		while(typeStack.peek() != baseType) {
			TileType[] next = typeStack.peek().getProp(CoveredTileProperty.class).getCoverableTiles();
			typeStack.push(next == null || next.length == 0 || next[0] == null ? baseType : next[0]);
		}
		
		// now, pop all the tile types out of the temp stack and into the real stack.
		while(!typeStack.empty())
			tileTypes.push(typeStack.pop());
		
		/// now it's time to initialize the tile data.
		
		int len = 0;
		Stack<String[]> eachData = new Stack<>();
		// I need to make sure the data for the top tile is first, and the bottom tile data is last. So, I need to reverse the order. 
		
		for(TileType type: tileTypes) { // goes through starting from bottom of stack, at bottom tile.
			eachData.push(type.getInitialData());
			len += type.getDataLength();
		}
		
		data = new String[len];
		int offset = 0;
		while(!eachData.empty()) {
			String[] typeData = eachData.pop();
			System.arraycopy(typeData, 0, data, offset, typeData.length);
			offset += typeData.length;
		}
	}
	
	// will be used most often when loading saved tiles.
	public Tile(@NotNull Level level, int x, int y, @NotNull TileType[] types, @NotNull String[] data) {
		this(level, x, y);
		this.data = data;
		// because this is the first type, we need to establish all the tiles under it.
		for(TileType type: types)
			tileTypes.push(type);
	}
	
	public TileType getType() { return tileTypes.peek(); }
	private TileType[] getTypes() { return tileTypes.toArray(new TileType[tileTypes.size()]); }
	boolean hasType(TileType type) { return tileTypes.contains(type); }
	
	@NotNull @Override public Level getLevel() { return level; }
	
	public int getX() { return x*SIZE; }
	public int getY() { return y*SIZE; }
	
	public int getCenterX() { return x*SIZE + SIZE/2; }
	public int getCenterY() { return y*SIZE + SIZE/2; }
	
	@Override
	public Rectangle getBounds() { return new Rectangle(x*SIZE, y*SIZE, SIZE, SIZE); }
	
	public boolean addTile(@NotNull TileType newType) {
		// first, check to see if the newType can validly be placed on the current type.
		if(newType == getType()
			|| !newType.getProp(CoveredTileProperty.class).canCover(getType())
			|| newType.compareTo(getType()) <= 0)
			return false;
		
		moveEntities(newType);
		
		String[] newData = newType.getInitialData();
		String[] fullData = new String[data.length + newData.length];
		System.arraycopy(data, 0, fullData, newData.length, data.length); // copy ground tile data, first
		System.arraycopy(newData, 0, fullData, 0, newData.length); // copy surface tile data
		data = fullData;
		
		return true;
	}
	
	void breakTile() {
		TileType prevType = tileTypes.pop();
		
		String[] newData = new String[data.length - prevType.getDataLength()];
		System.arraycopy(data, prevType.getDataLength(), newData, 0, newData.length);
		data = newData;
		
		if(tileTypes.size() == 0)
			addTile(baseType);
		else
			moveEntities(getType());
	}
	
	private void moveEntities(TileType newType) {
		// check for entities that will not be allowed on the new tile, and move them to the closest adjacent tile they are allowed on.
		Array<Tile> surroundingTiles = getAdjacentTiles(true);
		for(Entity entity: level.getOverlappingEntities(getBounds())) {
			// for each entity, check if it can walk on the new tile type. If not, fetch the surrounding tiles, remove those the entity can't walk on, and then fetch the closest tile of the remaining ones.
			if(newType.getProp(SolidProperty.class).isPermeableBy(entity)) continue; // no worries for this entity.
			
			Array<Tile> aroundTiles = new Array<>(surroundingTiles);
			for(int i = 0; i < aroundTiles.size; i++) {
				if(!aroundTiles.get(i).getType().getProp(SolidProperty.class).isPermeableBy(entity)) {
					aroundTiles.removeIndex(i);
					i--;
				}
			}
			
			// from the remaining tiles, find the one that is closest to the entity.
			Tile closest = entity.getClosestTile(aroundTiles);
			// if none remain (returned tile is null), take no action for that entity. If a tile is returned, then move the entity just barely inside the new tile.
			if(closest != null) {
				Rectangle entityBounds = entity.getBounds();
				MyUtils.moveRectInside(entityBounds, closest.getBounds(), 1);
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
			return tiles;
		}
	}
	
	/** @noinspection UnusedReturnValue*/
	public static Array<Tile> sortByDistance(Array<Tile> tiles, final Vector2 position) {
		tiles.sort((t1, t2) -> {
			float t1diff = position.dst(t1.getBounds().getCenter(new Vector2()));
			float t2diff = position.dst(t2.getBounds().getCenter(new Vector2()));
			return Float.compare(t1diff, t2diff);
		});
		
		return tiles;
	}
	
	@Override
	public void render(SpriteBatch batch, float delta) {
		
		/*
			- Get the surrounding tile types for a tile
			- draw an overlap only after all the centers under it have been drawn
				So, before drawing an overlap, check that the current center is supposed to be drawn under it.
		 */
		
		TileType[][] aroundTypes = new TileType[9][];
		int idx = 0;
		for(int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {
				Tile oTile = level.getTile(this.x + x, this.y + y);
				if(x == 0 && y == 0) {if(oTile != this) throw new IllegalStateException("Level reference or position of Tile " + this + " is faulty; Level "+level+" returns Tile " + oTile + " at position "+this.x+","+this.y+"."); aroundTypes[idx] = new TileType[0]; }
				else aroundTypes[idx] = oTile != null ? oTile.getTypes() : new TileType[0];
				idx++;
			}
		}
		
		Array<AtlasRegion> sprites = new Array<>();
		
		TileType[] mainTypes = getTypes();
		int firstIdx = 0;
		for(int i = mainTypes.length-1; i >= 0; i--) {
			if(mainTypes[i].getProp(AnimationProperty.class).isOpaque()) {
				firstIdx = i;
				break;
			}
		}
		
		// To find overlap sprites, it's easier if the tiles are sorted by TileType first, and then position.
		TreeMap<TileType, Boolean[]> overlappingTypes = new TreeMap<>();
		Boolean[] model = new Boolean[9];
		Arrays.fill(model, Boolean.FALSE);
		for(int i = 0; i < aroundTypes.length; i++) {
			for (TileType oType : aroundTypes[i]) { // doesn't matter the order.
				if(!oType.getProp(OverlapProperty.class).canOverlap()) continue; // the type can't even overlap anyway.
				//if(TileType.tileSorter.compare(mainTypes[firstIdx], oType) >= 0) continue; // the type is lower than the lowest *visible* main type.
				overlappingTypes.putIfAbsent(oType, Arrays.copyOf(model, model.length));
				overlappingTypes.get(oType)[i] = true;
			}
		}
		
		Iterator<TileType> overlapTypeIter = overlappingTypes.tailMap(mainTypes[firstIdx], false).keySet().iterator();
		TileType overlapType = overlapTypeIter.hasNext() ? overlapTypeIter.next() : null; // this type will always be just above mainTypes[firstIdx].
		
		for(int i = firstIdx; i < mainTypes.length; i++) {
			sprites.add(mainTypes[i].getProp(ConnectionProperty.class).getSprite(this, aroundTypes));
			
			while(overlapType != null && (i >= mainTypes.length-1 || mainTypes[i+1].compareTo(overlapType) > 0)) {
				sprites.addAll(mainTypes[i].getProp(OverlapProperty.class).getSprites(this, overlapType, overlappingTypes.get(overlapType)));
				overlapType = overlapTypeIter.hasNext() ? overlapTypeIter.next() : null;
			}
		}
		
		
		for(AtlasRegion texture: sprites)
			batch.draw(texture, x*SIZE, y*SIZE, SIZE, SIZE);
	}
	
	
	@Override
	public void update(float delta) {
		for(TileType type: tileTypes) // goes from bottom to top
			type.getProp(UpdateProperty.class).update(delta, this);
	}
	
	private int getIndex(TileType type, Class<? extends TileProperty> property, int propDataIndex) {
		if(!tileTypes.contains(type))
			throw new IllegalArgumentException("Tile " + this + " does not have a " + type + " type, cannot fetch the data index.");
		
		type.checkDataAccess(property, propDataIndex);
		
		int offset = 0;
		
		
		return type.getPropDataIndex(property) + propDataIndex + offset;
	}
	
	String getData(Class<? extends TileProperty> property, TileType type, int propDataIndex) {
		return this.data[getIndex(type, property, propDataIndex)];
	}
	
	void setData(Class<? extends TileProperty> property, TileType type, int propDataIndex, String data) {
		this.data[getIndex(type, property, propDataIndex)] = data;
	}
	
	
	
	
	@Override
	public boolean isPermeableBy(Entity entity) {
		return getType().getProp(SolidProperty.class).isPermeableBy(entity);
	}
	
	@Override
	public boolean attackedBy(Mob mob, Item attackItem) {
		return getType().getProp(DestructibleProperty.class).tileAttacked(this, mob, attackItem);
	}
	
	@Override
	public boolean hurtBy(WorldObject obj, int damage) {
		return getType().getProp(DestructibleProperty.class).tileAttacked(this, obj, damage);
	}
	
	@Override
	public boolean interactWith(Player player, Item heldItem) { return getType().getProp(InteractableProperty.class).interact(player, heldItem, this); }
	
	@Override
	public boolean touchedBy(Entity entity) { getType().getProp(TouchListener.class).touchedBy(entity, this); return true; }
	
	@Override
	public void touching(Entity entity) {}
	
	@Override
	public String toString() { return getType().getName() + " Tile (all:"+tileTypes+")"; }
	
	public String toLocString() { return x+","+y+" ("+toString()+")"; }
	
	// I can use the string encoder and string parser in MyUtils to encode the tile data in a way so that I can always re-parse the encoded array. I can use this internally to, with other things, whenever I need to encode a list of objects and don't want to worry about finding the delimiter symbol in string somewhere I don't expect.
}
