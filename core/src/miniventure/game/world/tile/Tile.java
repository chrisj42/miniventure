package miniventure.game.world.tile;

import miniventure.game.item.Item;
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
	
	private TileType groundType, surfaceType;
	
	private Level level;
	protected final int x, y;
	private int[] data;
	
	public Tile(TileType type, Level level, int x, int y) { this(type, level, x, y, type.getInitialData()); }
	public Tile(TileType type, Level level, int x, int y, int[] data) {
		this.level = level;
		this.x = x;
		this.y = y;
		this.data = data;
		
		surfaceType = type.isGroundTile() ? null : type;
		// with the surfaceType, data, and other tile info being set above, the CoveredTileProperty should have no problem fetching the correct ground tile type.
		groundType = type.isGroundTile() ? type : type.getProp(CoveredTileProperty.class).getCoveredTile(this);
	}
	
	public TileType getType() { return surfaceType == null ? groundType : surfaceType; }
	TileType getGroundType() { return groundType; }
	TileType getSurfaceType() { return surfaceType; }
	
	@Override public Level getLevel() { return level; }
	
	public int getCenterX() { return x*SIZE + SIZE/2; }
	public int getCenterY() { return y*SIZE + SIZE/2; }
	
	@Override
	public Rectangle getBounds() { return new Rectangle(x*SIZE, y*SIZE, SIZE, SIZE); }
	
	public void resetTile(@NotNull TileType newType) {
		// check for entities that will not be allowed on the new tile, and move them to the closest adjacent tile they are allowed on.
		Array<Tile> surroundingTiles = getAdjacentTiles(true);
		for(Entity entity: level.getOverlappingEntities(getBounds())) {
			// for each entity, check if it can walk on the new tile type. If not, fetch the surrounding tiles, remove those the entity can't walk on, and then fetch the closest tile of the remaining ones.
			if(newType.getProp(SolidProperty.class).isPermeableBy(entity)) continue; // no worries for this entity.
			
			Array<Tile> aroundTiles = new Array<>(surroundingTiles);
			for(int i = 0; i < aroundTiles.size; i++) {
				if(!aroundTiles.get(i).groundType.getProp(SolidProperty.class).isPermeableBy(entity)) {
					aroundTiles.removeIndex(i);
					i--;
				}
			}
			
			Tile closest = entity.getClosestTile(aroundTiles);
			// if none remain (returned tile is null), take no action for that entity. If a tile is returned, then move the entity to the center of that tile.
			if(closest != null)
				entity.moveTo(closest);
		}
		
		TileType prev = getType();
		
		if(!newType.isGroundTile()) {
			// the new type has no defined under type, so the previous tile type should be conserved.
			// also, since you should never place one surface tile directly on another surface tile, I can technically assume that the current data array only has the ground tile data. But, since creative mode could be a thing, and it isn't too hard, I'm goinf to double check anyway.
			
			int[] newData = newType.getInitialData();
			int groundDataLen = groundType.getDataLength();
			int[] fullData = new int[groundDataLen + newData.length];
			System.arraycopy(data, 0, fullData, 0, groundDataLen); // copy ground tile data, first
			System.arraycopy(newData, 0, fullData, groundDataLen, newData.length); // copy surface tile data
			data = fullData;
			
			surfaceType = newType;
		}
		else { // new type is ground tile, remove surface type if present
			data = newType.getInitialData();
			surfaceType = null;
			
			groundType = newType;
		}
		
		newType.getProp(CoveredTileProperty.class).tilePlaced(this, prev);
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
	
	private void draw(SpriteBatch batch, Array<AtlasRegion> textures) {
		for(AtlasRegion texture: textures)
			batch.draw(texture, x*SIZE, y*SIZE, SIZE, SIZE);
	}
	
	private void renderTileType(@NotNull TileType type, SpriteBatch batch) {
		Array<AtlasRegion> sprites = type.getProp(OverlapProperty.class).getSprites(this);
		sprites.insert(0, type.getProp(ConnectionProperty.class).getSprite(this));
		
		draw(batch, sprites);
	}
	
	@Override
	public void render(SpriteBatch batch, float delta) {
		
		// draw the ground sprite and overlaps
		renderTileType(groundType, batch);
		
		// draw the surface sprite and overlaps
		if(surfaceType != null)
			renderTileType(surfaceType, batch);
	}
	
	
	@Override
	public void update(float delta) {
		groundType.getProp(UpdateProperty.class).update(delta, this);
		if(surfaceType != null)
			surfaceType.getProp(UpdateProperty.class).update(delta, this);
	}
	
	private int getIndex(Class<? extends TileProperty> property, TileType type, int propDataIndex) {
		type.checkDataAccess(property, this, propDataIndex);
		
		int offset = !type.isGroundTile() ? groundType.getPropDataLength(property) : 0;
		
		return type.getPropDataIndex(property) + propDataIndex + offset;
	}
	
	int getData(Class<? extends TileProperty> property, TileType type, int propDataIndex) {
		return this.data[getIndex(property, type, propDataIndex)];
	}
	
	void setData(Class<? extends TileProperty> property, TileType type, int propDataIndex, int data) {
		this.data[getIndex(property, type, propDataIndex)] = data;
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
	public String toString() { return getType().getName() + " Tile" + (surfaceType != null ? " on " + groundType.getName() : ""); }
	
	public String toLocString() { return x+","+y+" ("+toString()+")"; }
}
