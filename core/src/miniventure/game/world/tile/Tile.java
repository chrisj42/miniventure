package miniventure.game.world.tile;

import miniventure.game.item.Item;
import miniventure.game.world.Level;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.Player;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.utils.Array;

public class Tile {
	
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
	
	private TileType type;
	
	private Level level;
	protected final int x, y;
	private int[] data;
	
	public Tile(TileType type, Level level, int x, int y) { this(type, level, x, y, type.getInitialData()); }
	public Tile(TileType type, Level level, int x, int y, int[] data) {
		this.type = type;
		this.level = level;
		this.x = x;
		this.y = y;
		this.data = data;
	}
	
	public TileType getType() { return type; }
	
	public Level getLevel() { return level; }
	
	public int getCenterX() { return x*SIZE + SIZE/2; }
	public int getCenterY() { return y*SIZE + SIZE/2; }
	
	public void resetTile(TileType newType) {
		type = newType;
		data = type.getInitialData();
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
	
	public void render(SpriteBatch batch, float delta) {
		TileType under = type.destructibleProperty.getCoveredTile();
		
		if(under != null)
			batch.draw(under.animationProperty.getSprite("00", this, under), x*SIZE, y*SIZE, SIZE, SIZE);
		
		// Due to animation, I'm going to try and draw everything without caching. Hopefully, it won't be slow.
		
		// TODO here, ask the connectionProperty to get the array of sprites to render. the connection property will accept a tile. It will cache a reference to the surrounding tiles, and then it will then loop through the connection checks, passing the cached list of tile types, and add the corresponding sprite for every match. it then returns this array.
		// TODO the array returned is actually a reference to the tile sprites, however, all could be part of an animation. So, the animation property is checked for each one. The animation property for that tile type, then, takes an int (String?) for the overlap sprite index/id. It should have registered a list of animations for each connection index. it applies the program time and given index and returns the correct frame of the correct sprite's animation.
		
		Array<AtlasRegion> sprites = type.connectionProperty.getSprites(this);
		for(AtlasRegion sprite: sprites)
			batch.draw(sprite, x*SIZE, y*SIZE, SIZE, SIZE);
	}
	
	public void update(float delta) {
		type.updateProperty.update(delta, this);
	}
	
	private void checkDataAccess(TileProperty property, int propDataIndex) {
		if(!type.propertyDataIndexes.containsKey(property))
			throw new IllegalArgumentException("specified property " + property + " is not from this tile's type, "+type+".");
		
		if(propDataIndex >= type.propertyDataLengths.get(property))
			throw new IndexOutOfBoundsException("tile property " + property + " tried to access index past stated length; length="+type.propertyDataLengths.get(property)+", index="+propDataIndex);
	}
	
	int getData(TileProperty property, int propDataIndex) {
		checkDataAccess(property, propDataIndex);
		return this.data[type.propertyDataIndexes.get(property)+propDataIndex];
	}
	
	void setData(TileProperty property, int propDataIndex, int data) {
		checkDataAccess(property, propDataIndex);
		this.data[type.propertyDataIndexes.get(property)+propDataIndex] = data;
	}
	
	public boolean isPermeableBy(Entity entity) {
		return type.solidProperty.isPermeableBy(entity);
	}
	
	public void attackedBy(Player player, Item heldItem) {
		type.destructibleProperty.tileAttacked(this, heldItem);
	}
	
	public void interactWith(Player player, Item heldItem) { type.interactableProperty.interact(player, heldItem, this); }
	
	public void touchedBy(Entity entity) { type.touchListener.touchedBy(entity, this); }
	
	@Override
	public String toString() { return type + " Tile"; }
}
