package miniventure.game.world.tile;

import miniventure.game.item.Item;
import miniventure.game.world.Level;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.Player;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
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
	
	private Rectangle getRect() { return new Rectangle(x*SIZE, y*SIZE, SIZE, SIZE); }
	
	public void resetTile(TileType newType) {
		// check for entities that will not be allowed on the new tile, and move them to the closest adjacent tile they are allowed on.
		Array<Tile> surroundingTiles = getAdjacentTiles(true);
		for(Entity entity: level.getOverlappingEntities(getRect())) {
			// for each entity, check if it can walk on the new tile type. If not, fetch the surrounding tiles, remove those the entity can't walk on, and then fetch the closest tile of the remaining ones.
			if(newType.solidProperty.isPermeableBy(entity)) continue; // no worries for this entity.
			
			Array<Tile> aroundTiles = new Array<>(surroundingTiles);
			for(int i = 0; i < aroundTiles.size; i++) {
				if(!aroundTiles.get(i).type.solidProperty.isPermeableBy(entity)) {
					aroundTiles.removeIndex(i);
					i--;
				}
			}
			
			Tile closest = entity.getClosestTile(aroundTiles);
			// if none remain (returned tile is null), take no action for that entity. If a tile is returned, then move the entity to the center of that tile.
			if(closest != null)
				entity.moveTo(closest);
		}
		
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
	
	private void draw(SpriteBatch batch, TextureRegion texture) {
		batch.draw(texture, x*SIZE, y*SIZE, SIZE, SIZE);
	}
	
	private void drawOverlap(SpriteBatch batch, TileType type, boolean under) {
		Array<AtlasRegion> sprites = type.overlapProperty.getSprites(this, under);
		for(AtlasRegion sprite: sprites)
			draw(batch, sprite);
	}
	
	public void render(SpriteBatch batch, float delta) {
		TileType under = type.animationProperty.renderBehind;
		
		if(under != null) { // draw the tile that ought to be rendered underneath this one
			draw(batch, under.connectionProperty.getSprite(this, true));
			drawOverlap(batch, under, true);
		}
		
		// Due to animation, I'm going to try and draw everything without caching. Hopefully, it won't be slow.
		draw(batch, type.connectionProperty.getSprite(this)); // draws base sprite for this tile
		
		drawOverlap(batch, type, true); // draw the overlap from other under tiles; considers also those without an under tile.
		
		drawOverlap(batch, type, false); // draw overlap from other tiles; only considers those that have an under tile.
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
		type.destructibleProperty.tileAttacked(this, heldItem, player);
	}
	
	public void interactWith(Player player, Item heldItem) { type.interactableProperty.interact(player, heldItem, this); }
	
	public void touchedBy(Entity entity) { type.touchListener.touchedBy(entity, this); }
	
	@Override
	public String toString() { return toTitleCase(type+"") + " Tile"; }
	
	public String toLocString() {
		return x+","+y+" (" + toTitleCase(type+"")+" Tile)";
	}
	
	private static String toTitleCase(String string) {
		String[] words = string.split(" ");
		for(int i = 0; i < words.length; i++) {
			if(words[i].length() == 0) continue;
			words[i] = words[i].substring(0, 1).toUpperCase() + words[i].substring(1).toLowerCase();
		}
		
		return String.join(" ", words);
	}
}
