package miniventure.game.world.entity.mob;

import java.util.EnumMap;

import miniventure.game.item.Item;
import miniventure.game.world.Level;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.tile.Tile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Player extends Mob {
	
	public enum Stat {
		Health(20),
		
		Stamina(10),
		
		Hunger(10),
		
		Armor(10, 0);
		
		public final int max, initial;
		
		Stat(int max) {
			this(max, max);
		}
		Stat(int max, int initial) {
			this.max = max;
			this.initial = initial;
		}
		
		public static final Stat[] values = Stat.values();
	}
	
	private final EnumMap<Stat, Integer> stats = new EnumMap<>(Stat.class);
	private Item heldItem;
	
	private Array<Item> inventory = new Array<>();
	
	public Player() {
		super("player");
		heldItem = null;
		for(Stat stat: Stat.values)
			stats.put(stat, stat.initial);
	}
	
	public int getStat(@NotNull Stat stat) {
		return stats.get(stat);
	}
	@Nullable
	public Item getHeldItemClone() {
		if(heldItem == null) return null;
		return heldItem.clone();
	}
	
	public void checkInput(float delta) {
		// checks for keyboard input to move the player.
		// getDeltaTime() returns the time passed between the last and the current frame in seconds.
		int speed = Tile.SIZE * 5; // this is technically in units/second.
		float xd = 0, yd = 0;
		if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) xd -= speed * delta;
		if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) xd += speed * delta;
		if(Gdx.input.isKeyPressed(Input.Keys.UP)) yd += speed * delta;
		if(Gdx.input.isKeyPressed(Input.Keys.DOWN)) yd -= speed * delta;
		
		move(xd, yd);
		
		// Also, see what happens when I go to texturePacker and remove the outer whitespace around the player sprites. If possible, make sure they are all the same size, but see what happens if they aren't anyway.
		//if(pressingKey(Input.Keys.E))
			//GameCore.getGameScreen().setScreen(new InventoryMenu(this, inventory));
		if(pressingKey(Input.Keys.C))
			attack();
		else if(pressingKey(Input.Keys.V))
			interact();
	}
	
	@Override
	public void update(float delta) {
		// update things like hunger, stamina, etc.
	}
	
	public Rectangle getInteractionRect() {
		Rectangle bounds = getBounds();
		Vector2 dirVector = getDirection().getVector();
		//System.out.println("dir vector="+dirVector);
		bounds.setX(bounds.getX()+bounds.getWidth()*dirVector.x);
		bounds.setY(bounds.getY()+bounds.getHeight()*dirVector.y);
		return bounds;
	}
	
	private void attack() {
		// get level, and don't attack if level is not found
		Level level = Level.getEntityLevel(this);
		if(level == null) return;
		
		// get attack rect
		Rectangle attackBounds = getInteractionRect();
		
		// find entities in attack rect, and attack them
		Array<Entity> otherEntities = level.getOverlappingEntities(attackBounds);
		otherEntities.removeValue(this, true); // use ==, not .equals()
		boolean attacked = false;
		for(Entity e: otherEntities)
			attacked = attacked || e.hurtBy(this, heldItem, 1);
		
		if(attacked) return; // don't hurt the tile
		
		// if no entities were successfully attacked, get tile and attack it instead
		Tile tile = level.getClosestTile(attackBounds);
		if(tile != null)
			tile.attackedBy(this, heldItem);
	}
	
	private void interact() {
		if(heldItem != null && heldItem.isReflexive()) {
			useItem(heldItem);
			return;
		}
		
		Level level = Level.getEntityLevel(this);
		if(level == null) return;
		
		Rectangle interactRect = getInteractionRect();
		
		Array<Entity> entities = level.getOverlappingEntities(interactRect);
		boolean used = false;
		for(Entity e: entities) {
			if(e.interactWith(this, heldItem)) {
				used = true;
				break;
			}
		}
		
		if(!used) {
			Tile tile = level.getClosestTile(interactRect);
			if (tile != null) {
				if (!heldItem.interact(this, tile))
					tile.interactWith(this, heldItem);
			}
		}
		
		if(heldItem.isUsed()) heldItem = inventory.size == 0 ? null : inventory.removeIndex(0);
		
		/*TileType newType = null;
		
		if(pressingKey(Input.Keys.G))
			newType = TileType.GRASS;
		if(pressingKey(Input.Keys.D))
			newType = TileType.DIRT;
		if(pressingKey(Input.Keys.H))
			newType = TileType.HOLE;
		if(pressingKey(Input.Keys.S))
			newType = TileType.SAND;
		if(pressingKey(Input.Keys.C))
			newType = TileType.CACTUS;
		if(pressingKey(Input.Keys.T))
			newType = TileType.TREE;
		if(pressingKey(Input.Keys.W))
			newType = TileType.WATER;
		if(pressingKey(Input.Keys.R))
			newType = TileType.ROCK;
		
		if(newType != null)
			tile.resetTile(newType);
		*/
	}
	
	private void useItem(Item item) {
		// this is for reflexive items
	}
	
	public void addToInventory(Item item) { inventory.add(item.clone()); }
	
	@Override
	public boolean hurtBy(Mob mob, Item attackItem, int dmg) { return hurt(dmg); }
	@Override
	public boolean hurtBy(Tile tile, int dmg) { return hurt(dmg); }
	
	private boolean hurt(int dmg) {
		int health = stats.get(Stat.Health);
		if(health == 0) return false;
		stats.put(Stat.Health, Math.max(0, health - dmg));
		return true;
	}
	
	private static boolean pressingKey(int keycode) {
		/*
			The only reason this is necessary is because libGDX doesn't seem to have functionality to make it so if you hold down a key, it fires once immediately, waits a half-second, and then fires a lot more rapidly.
			
			I would like to have that functionality, but it seems I'm going to have to do it myself.
		 */
		
		return Gdx.input.isKeyJustPressed(keycode);
	}
}
