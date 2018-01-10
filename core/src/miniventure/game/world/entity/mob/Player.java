package miniventure.game.world.entity.mob;

import java.util.EnumMap;

import miniventure.game.item.Item;
import miniventure.game.world.Level;
import miniventure.game.world.WorldObject;
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
	@Nullable private Item heldItem;
	
	private Array<Item> inventory = new Array<>();
	
	public Player() {
		super("player", Stat.Health.initial);
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
	
	public void checkInput(float delta, @NotNull Vector2 mouseInput) {
		// checks for keyboard input to move the player.
		// getDeltaTime() returns the time passed between the last and the current frame in seconds.
		int speed = Tile.SIZE * 5; // this is technically in units/second.
		Vector2 movement = new Vector2();
		if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) movement.x--;
		if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) movement.x++;
		if(Gdx.input.isKeyPressed(Input.Keys.UP)) movement.y++;
		if(Gdx.input.isKeyPressed(Input.Keys.DOWN)) movement.y--;
		
		movement.nor();
		
		movement.add(mouseInput);
		movement.nor();
		
		movement.scl(speed * delta);
		
		move(movement.x, movement.y);
		
		// Also, see what happens when I go to texturePacker and remove the outer whitespace around the player sprites. If possible, make sure they are all the same size, but see what happens if they aren't anyway.
		//if(pressingKey(Input.Keys.E))
			//GameCore.getGameScreen().setScreen(new InventoryMenu(this, inventory));
		if(pressingKey(Input.Keys.C))
			attack();
		else if(pressingKey(Input.Keys.V))
			interact();
		
		heldItem = heldItem == null ? null : heldItem.consume();
	}
	
	@Override
	public void update(float delta) {
		super.update(delta);
		// update things like hunger, stamina, etc.
		if(heldItem == null && inventory.size > 0) {
			//System.out.println("updating player's active item");
			heldItem = inventory.removeIndex(0);
		}
	}
	
	public Rectangle getInteractionRect() {
		Rectangle bounds = getBounds();
		Vector2 dirVector = getDirection().getVector();
		//System.out.println("dir vector="+dirVector);
		bounds.setX(bounds.getX()+bounds.getWidth()*dirVector.x);
		bounds.setY(bounds.getY()+bounds.getHeight()*dirVector.y);
		return bounds;
	}
	
	@NotNull
	private Array<WorldObject> getInteractionQueue() {
		Array<WorldObject> objects = new Array<>();
		
		// get level, and don't interact if level is not found
		Level level = Level.getEntityLevel(this);
		if(level == null) return objects;
		
		Rectangle interactionBounds = getInteractionRect();
		
		objects.addAll(level.getOverlappingEntities(interactionBounds, this));
		
		Tile tile = level.getClosestTile(interactionBounds);
		if(tile != null)
			objects.add(tile);
		
		return objects;
	}
	
	private void attack() {
		if(heldItem != null && heldItem.isUsed()) return;
		
		for(WorldObject obj: getInteractionQueue()) {
			if(heldItem == null || !heldItem.isUsed() && !heldItem.attack(obj, this)) {
				if(obj.attackedBy(this, heldItem))
					return;
			} else return;
		}
	}
	
	private void interact() {
		if(heldItem != null && heldItem.isUsed()) return;
		
		if(heldItem != null && heldItem.interact(this)) return;
		
		for(WorldObject obj: getInteractionQueue()) {
			if(heldItem == null || !heldItem.isUsed() && !heldItem.interact(obj, this)) {
				if(obj.interactWith(this, heldItem))
					return;
			} else return;
		}
	}
	
	public void addToInventory(Item item) { inventory.add(item.clone()); }
	
	@Override
	public boolean hurtBy(WorldObject source, int dmg) {
		int health = stats.get(Stat.Health);
		if(health == 0) return false;
		stats.put(Stat.Health, Math.max(0, health - dmg));
		// here is where I'd make a death chest, and show the death screen.
		
		return super.hurtBy(source, dmg);
	}
	
	private static boolean pressingKey(int keycode) {
		/*
			The only reason this is necessary is because libGDX doesn't seem to have functionality to make it so if you hold down a key, it fires once immediately, waits a half-second, and then fires a lot more rapidly.
			
			I would like to have that functionality, but it seems I'm going to have to do it myself.
		 */
		
		return Gdx.input.isKeyJustPressed(keycode);
	}
}
