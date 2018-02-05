package miniventure.game.world.entity.mob;

import java.util.EnumMap;

import miniventure.game.GameCore;
import miniventure.game.item.CraftingScreen;
import miniventure.game.item.Hands;
import miniventure.game.item.Inventory;
import miniventure.game.item.InventoryScreen;
import miniventure.game.item.Item;
import miniventure.game.item.Recipes;
import miniventure.game.world.Level;
import miniventure.game.world.WorldObject;
import miniventure.game.world.tile.Tile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public class Player extends Mob {
	
	public static final float MOVE_SPEED = 5;
	
	public enum Stat {
		Health("heart", 10, 20),
		
		Stamina("", 10, 10),
		
		Hunger("burger", 10, 10),
		
		Armor("", 10, 10, 0);
		
		public final int max, initial, iconCount;
		public final String icon, outlineIcon;
		
		Stat(String icon, int iconCount, int max) { this(icon, iconCount, max, max); }
		Stat(String icon, int iconCount, int max, int initial) {
			this.max = max;
			this.initial = initial;
			this.icon = icon;
			this.outlineIcon = icon+"-outline";
			this.iconCount = iconCount;
		}
		
		public static final Stat[] values = Stat.values();
	}
	
	private final EnumMap<Stat, Integer> stats = new EnumMap<>(Stat.class);
	
	@NotNull private final Hands hands;
	private Inventory inventory;
	
	public Player() {
		super("player", Stat.Health.initial);
		for(Stat stat: Stat.values)
			stats.put(stat, stat.initial);
		
		hands = new Hands(this);
		inventory = new Inventory(20, hands);
	}
	
	public int getStat(@NotNull Stat stat) {
		return stats.get(stat);
	}
	public void changeStat(@NotNull Stat stat, int amt) {
		stats.put(stat, Math.max(0, Math.min(stat.max, stats.get(stat) + amt)));
	}
	
	public void checkInput(@NotNull Vector2 mouseInput) {
		// checks for keyboard input to move the player.
		// getDeltaTime() returns the time passed between the last and the current frame in seconds.
		float speed = Tile.SIZE * MOVE_SPEED; // this is technically in units/second.
		Vector2 movement = new Vector2();
		if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) movement.x--;
		if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) movement.x++;
		if(Gdx.input.isKeyPressed(Input.Keys.UP)) movement.y++;
		if(Gdx.input.isKeyPressed(Input.Keys.DOWN)) movement.y--;
		
		movement.nor();
		
		movement.add(mouseInput);
		movement.nor();
		
		movement.scl(speed * Gdx.graphics.getDeltaTime());
		
		move(movement.x, movement.y);
		
		if(pressingKey(Input.Keys.C))
			attack();
		else if(pressingKey(Input.Keys.V))
			interact();
		
		hands.resetItemUsage();
		
		if(pressingKey(Input.Keys.E)) {
			hands.clearItem(inventory);
			GameCore.setScreen(new InventoryScreen(inventory, hands));
		}
		else if(pressingKey(Input.Keys.Z))
			GameCore.setScreen(new CraftingScreen(Recipes.recipes, inventory));
	}
	
	public void drawGui(Rectangle canvas, SpriteBatch batch, BitmapFont font) {
		hands.getUsableItem().drawItem(hands.getCount(), batch, font, canvas.width/2, 20);
		
		renderBar(Stat.Health, canvas.x, canvas.y+3, batch);
		//renderBar(Stat.Hunger, canvas.x + canvas.width - GameCore.icons.get(Stat.Hunger.icon).getRegionWidth()*Stat.Hunger.iconCount, canvas.y+3, batch);
	}
	
	private void renderBar(Stat stat, float x, float y, SpriteBatch batch) {
		float pointsPerIcon = stat.max*1f / stat.iconCount;
		TextureRegion fullIcon = GameCore.icons.get(stat.icon);
		TextureRegion emptyIcon = GameCore.icons.get(stat.outlineIcon);
		
		// for each icon...
		for(int i = 0; i < stat.iconCount; i++) {
			// gets the amount this icon should be "filled" with the fullIcon
			float iconFillAmount = Math.min(Math.max(0, stats.get(stat) - i * pointsPerIcon) / pointsPerIcon, 1);
			
			// converts it to a pixel width
			int fullWidth = (int) (iconFillAmount * fullIcon.getRegionWidth());
			if(fullWidth > 0)
				batch.draw(fullIcon.getTexture(), x+i*fullIcon.getRegionWidth(), y, fullIcon.getRegionX(), fullIcon.getRegionY(), fullWidth, fullIcon.getRegionHeight());
			
			// repeats some of the above, for the empty icon
			int emptyWidth = (int) ((1 - iconFillAmount) * emptyIcon.getRegionWidth());
			int emptyOffset = emptyIcon.getRegionWidth() - emptyWidth;
			if(emptyWidth > 0)
				batch.draw(emptyIcon.getTexture(), x+i*fullIcon.getRegionWidth()+emptyOffset, y, emptyIcon.getRegionX() + emptyOffset, emptyIcon.getRegionY(), emptyWidth, emptyIcon.getRegionHeight());
		}
	}
	
	@Override
	public void update(float delta) {
		super.update(delta);
		// update things like hunger, stamina, etc.
		changeStat(Stat.Stamina, 1);
	}
	
	public boolean takeItem(@NotNull Item item) {
		if(hands.addItem(item))
			return true;
		else
			return inventory.addItem(item, 1) == 1;
	}
	
	public Rectangle getInteractionRect() {
		Rectangle bounds = getBounds();
		Vector2 dirVector = getDirection().getVector();
		bounds.setX(bounds.getX()+Tile.SIZE*dirVector.x);
		bounds.setY(bounds.getY()+Tile.SIZE*dirVector.y);
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
		for(WorldObject obj: getInteractionQueue())
			if(hands.attack(obj))
				return;
	}
	
	private void interact() {
		if(hands.interact()) return;
		
		for(WorldObject obj: getInteractionQueue())
			if(hands.interact(obj))
				return;
	}
	
	public boolean interactWith(Item item) { return false; }
	
	@Override
	public boolean hurtBy(WorldObject source, int dmg) {
		if(super.hurtBy(source, dmg)) {
			int health = stats.get(Stat.Health);
			if (health == 0) return false;
			stats.put(Stat.Health, Math.max(0, health - dmg));
			// here is where I'd make a death chest, and show the death screen.
		}
		return false;
	}
	
	private static boolean pressingKey(int keycode) {
		/*
			The only reason this is necessary is because libGDX doesn't seem to have functionality to make it so if you hold down a key, it fires once immediately, waits a half-second, and then fires a lot more rapidly.
			
			I would like to have that functionality, but it seems I'm going to have to do it myself.
		 */
		
		return Gdx.input.isKeyJustPressed(keycode) && !GameCore.hasMenu();
	}
}
