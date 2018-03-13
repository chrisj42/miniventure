package miniventure.game.world.tile;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Stack;
import java.util.TreeMap;

import miniventure.game.item.Item;
import miniventure.game.util.MyUtils;
import miniventure.game.world.Level;
import miniventure.game.world.Point;
import miniventure.game.world.ServerLevel;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.Player;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Tile implements WorldObject {
	
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
	
	// the TileType array is expected in order of bottom to top.
	// used most when creating new tiles for new levels
	public Tile(@NotNull Level level, int x, int y, @NotNull TileType... types) {
		this(level, x, y);
		
		for(TileType type: types)
			tileTypes.push(type);
		
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
	TileType[] getTypes() { return tileTypes.toArray(new TileType[tileTypes.size()]); }
	boolean hasType(TileType type) { return tileTypes.contains(type); }
	
	String getData(Class<? extends TileProperty> property, TileType type, int propDataIndex) {
		return data[getIndex(type, property, propDataIndex)];
	}
	
	void setData(Class<? extends TileProperty> property, TileType type, int propDataIndex, String data) {
		this.data[getIndex(type, property, propDataIndex)] = data;
	}
	
	private int getIndex(TileType type, Class<? extends TileProperty> property, int propDataIndex) {
		if(!tileTypes.contains(type))
			throw new IllegalArgumentException("Tile " + this + " does not have a " + type + " type, cannot fetch the data index.");
		
		type.checkDataAccess(property, propDataIndex);
		
		int offset = 0;
		
		for(int i = tileTypes.size()-1; i >= 0; i--) {
			TileType cur = tileTypes.get(i);
			if(!type.equals(cur))
				offset += cur.getDataLength();
			else
				break;
		}
		
		return type.getPropDataIndex(property) + propDataIndex + offset;
	}
	
	
	@NotNull @Override public Level getLevel() { return level; }
	@Nullable @Override public ServerLevel getServerLevel() {
		if(level instanceof ServerLevel)
			return (ServerLevel) level;
		return null;
	}
	
	@Override public Rectangle getBounds() { return new Rectangle(x, y, 1, 1); }
	@Override public Vector2 getCenter() { return new Vector2(x+0.5f, y+0.5f); }
	
	public boolean addTile(@NotNull TileType newType) { return addTile(newType, getType()); }
	private boolean addTile(@NotNull TileType newType, @NotNull TileType prevType) {
		// first, check to see if the newType can validly be placed on the current type.
		if(newType == getType()
			|| !newType.getProp(CoveredTileProperty.class).canCover(getType())
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
		newType.getProp(TransitionProperty.class).tryStartAnimation(this, prevType);
		// we don't use the return value because transition or not, there's nothing we need to do. :P
		
		return true;
	}
	
	void breakTile() { breakTile(true); }
	void breakTile(boolean checkForExitAnim) {
		if(checkForExitAnim) {
			TileType type = getType();
			if(type.getProp(TransitionProperty.class).tryStartAnimation(this, tileTypes.size() == 1 ? type : tileTypes.elementAt(tileTypes.size()-2), false))
				// transitioning successful
				return; // don't actually break the tile yet
		}
		
		TileType prevType = tileTypes.pop();
		
		String[] newData = new String[data.length - prevType.getDataLength()];
		System.arraycopy(data, prevType.getDataLength(), newData, 0, newData.length);
		data = newData;
		
		if(tileTypes.size() == 0)
			addTile(baseType);
		else
			moveEntities(getType());
	}
	
	/** @noinspection UnusedReturnValue*/
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
		if(!newType.getProp(CoveredTileProperty.class).canCover(underType)
			|| newType.compareTo(underType) <= 0)
			return false; // cannot replace tile
		
		if(type.getProp(TransitionProperty.class).tryStartAnimation(this, newType, true))
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
	public void render(SpriteBatch batch, float delta, Vector2 posOffset) {
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
			// before we use the connection property of the main type, let's check and see if we are transitioning.
			if(i == mainTypes.length - 1 && mainTypes[i].getProp(TransitionProperty.class).playingAnimation(this)) // only the top tile can ever be transitioning.
				sprites.add(mainTypes[i].getProp(TransitionProperty.class).getAnimationFrame(this));
			else // otherwise, use connection sprite.
				sprites.add(mainTypes[i].getProp(ConnectionProperty.class).getSprite(this, aroundTypes));
			
			while(overlapType != null && (i >= mainTypes.length-1 || mainTypes[i+1].compareTo(overlapType) > 0)) {
				sprites.addAll(mainTypes[i].getProp(OverlapProperty.class).getSprites(this, overlapType, overlappingTypes.get(overlapType)));
				overlapType = overlapTypeIter.hasNext() ? overlapTypeIter.next() : null;
			}
		}
		
		
		for(AtlasRegion texture: sprites)
			batch.draw(texture, (x-posOffset.x) * SIZE, (y-posOffset.y) * SIZE);
	}
	
	
	@Override
	public void update(float delta) {
		for(TileType type: tileTypes) {// goes from bottom to top
			if(!(type == getType() && type.getProp(TransitionProperty.class).playingAnimation(this))) // only update main tile if not transitioning.
				type.getProp(UpdateProperty.class).update(delta, this);
		}
	}
	
	@Override
	public float getLightRadius() {
		float maxRadius = 0;
		for(TileType type: tileTypes)
			maxRadius = Math.max(maxRadius, type.getProp(LightProperty.class).getLightRadius());
		
		return maxRadius;
	}
	
	@Override
	public boolean isPermeableBy(Entity entity) {
		return getType().getProp(SolidProperty.class).isPermeableBy(entity);
	}
	
	@Override
	public boolean attackedBy(WorldObject obj, @Nullable Item item, int damage) {
		return getType().getProp(DestructibleProperty.class).tileAttacked(this, obj, item, damage);
	}
	
	@Override
	public boolean interactWith(Player player, @Nullable Item heldItem) { return getType().getProp(InteractableProperty.class).interact(player, heldItem, this); }
	
	@Override
	public boolean touchedBy(Entity entity) { getType().getProp(TouchListener.class).touchedBy(entity, this); return true; }
	
	@Override
	public void touching(Entity entity) {}
	
	@Override
	public String toString() { return getType().getName()/* + " Tile (all:"+tileTypes+")"*/; }
	
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
		public TileData(int[] typeOrdinals, String[] data) {
			this.typeOrdinals = typeOrdinals;
			this.data = data;
		}
		public TileData(Tile tile) {
			this.data = tile.data;
			
			TileType[] tileTypes = tile.getTypes();
			typeOrdinals = new int[tileTypes.length];
			for(int i = 0; i < tileTypes.length; i++)
				typeOrdinals[i] = tileTypes[i].ordinal();
		}
	}
}
