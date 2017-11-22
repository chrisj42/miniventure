package miniventure.game.world.tile;

import miniventure.game.world.Level;

import org.jetbrains.annotations.NotNull;

public class Tile {
	
	/*
		So, tiles can have any of the following properties/features:
			- walkable or solid
			- replacement tileold (tileold that appears underneath when this one is broken; can vary?; defaults to a Hole).
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
			1 = all items are dropped when the tileold is destroyed; none before.
			0 = items are dropped at equal intervals so that the last hit drops the last item.
			0.5 = half the items are dropped when the tileold is destroyed; the other half is equally distributed.
			i.e. lastDropBias = part of items that are dropped when the tileold is destroyed. The rest are equally distributed.
	 */
	
	
	/*
		Perhaps I can manage to only object-ify the nearby tiles. The way it will 
	 */
	
	public static final int SIZE = 16;
	
	private TileType type;
	
	private Level level;
	private int x, y;
	private int[] data;
	
	public Tile(TileType type, Level level, int x, int y) {
		this(type, level, x, y, new int[type.dataLength]);
	}
	public Tile(TileType type, Level level, int x, int y, int[] data) {
		this.type = type;
		this.level = level;
		this.x = x;
		this.y = y;
		this.data = data;
	}
	
	public void setType(@NotNull TileType type) { this.type = type; }
	
	public TileType getType() { return type; }
	
	Level getLevel() { return level; }
	int getCenterX() { return x*SIZE + SIZE/2; }
	
	int getCenterY() { return y*SIZE + SIZE/2; }
}
