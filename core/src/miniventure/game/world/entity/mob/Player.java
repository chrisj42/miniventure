package miniventure.game.world.entity.mob;

import java.util.EnumMap;
import java.util.HashMap;

import miniventure.game.GameCore;
import miniventure.game.item.CraftingScreen;
import miniventure.game.item.Hands;
import miniventure.game.item.Inventory;
import miniventure.game.item.InventoryScreen;
import miniventure.game.item.Item;
import miniventure.game.item.Recipes;
import miniventure.game.world.Level;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.ActionParticle.ActionType;
import miniventure.game.world.tile.Tile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Player extends Mob {
	
	static final float MOVE_SPEED = 5;
	
	interface StatEvolver { void update(float delta); }
	
	private final HashMap<Class<? extends StatEvolver>, StatEvolver> statEvoMap = new HashMap<>();
	private <T extends StatEvolver> void addStatEvo(T evolver) {
		statEvoMap.put(evolver.getClass(), evolver);
	}
	private <T extends StatEvolver> T getStatEvo(Class<T> clazz) {
		//noinspection unchecked
		return (T) statEvoMap.get(clazz);
	}
	{
		addStatEvo(new StaminaSystem());
		addStatEvo(new HealthSystem());
		addStatEvo(new HungerSystem());
	}
	
	public enum Stat {
		Health("heart", 10, 20),
		
		Stamina("bolt", 12, 100),
		
		Hunger("burger", 10, 10),
		
		Armor("", 10, 10, 0);
		
		private final int max, initial, iconCount;
		private final String icon, outlineIcon;
		private final int iconWidth, iconHeight;
		
		Stat(String icon, int iconCount, int max) { this(icon, iconCount, max, max); }
		Stat(String icon, int iconCount, int max, int initial) {
			this.max = max;
			this.initial = initial;
			this.icon = icon;
			this.outlineIcon = icon+"-outline";
			this.iconCount = iconCount;
			
			if(icon.length() > 0) {
				TextureRegion fullIcon = GameCore.icons.get(icon);
				TextureRegion emptyIcon = GameCore.icons.get(outlineIcon);
				iconWidth = Math.max(fullIcon.getRegionWidth(), emptyIcon.getRegionWidth());
				iconHeight = Math.max(fullIcon.getRegionHeight(), emptyIcon.getRegionHeight());
			} else
				iconWidth = iconHeight = 0;
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
		Vector2 movement = new Vector2();
		if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) movement.x--;
		if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) movement.x++;
		if(Gdx.input.isKeyPressed(Input.Keys.UP)) movement.y++;
		if(Gdx.input.isKeyPressed(Input.Keys.DOWN)) movement.y--;
		
		movement.nor();
		
		movement.add(mouseInput);
		movement.nor();
		
		movement.scl(MOVE_SPEED * Gdx.graphics.getDeltaTime());
		
		move(movement.x, movement.y);
		
		getStatEvo(StaminaSystem.class).curStaminaRate = movement.isZero() ? 1 : 0.75f;
		
		if(!isKnockedBack()) {
			if (GameCore.input.pressingKey(Input.Keys.C))
				attack();
			else if (GameCore.input.pressingKey(Input.Keys.V))
				interact();
			
			//if(Gdx.input.isKeyPressed(Input.Keys.C) || Gdx.input.isKeyPressed(Input.Keys.V))
			//	animator.requestState(AnimationState.ATTACK);
		}
		
		hands.resetItemUsage();
		
		if(GameCore.input.pressingKey(Input.Keys.E)) {
			hands.clearItem(inventory);
			GameCore.setScreen(new InventoryScreen(inventory, hands));
		}
		else if(GameCore.input.pressingKey(Input.Keys.Z))
			GameCore.setScreen(new CraftingScreen(Recipes.recipes, inventory));
	}
	
	public void drawGui(Rectangle canvas, SpriteBatch batch, BitmapFont font) {
		hands.getUsableItem().drawItem(hands.getCount(), batch, font, canvas.width/2, 20);
		float y = canvas.y + 3;
		
		renderBar(Stat.Health, canvas.x, y, batch);
		renderBar(Stat.Stamina, canvas.x, y+Stat.Health.iconHeight+3, batch);
		renderBar(Stat.Hunger, canvas.x + canvas.width, y, batch, 0, false);
	}
	
	private void renderBar(Stat stat, float x, float y, SpriteBatch batch) { renderBar(stat, x, y, batch, 0); }
	/** @noinspection SameParameterValue*/
	private void renderBar(Stat stat, float x, float y, SpriteBatch batch, int spacing) { renderBar(stat, x, y, batch, spacing, true); }
	private void renderBar(Stat stat, float x, float y, SpriteBatch batch, int spacing, boolean rightSide) {
		float pointsPerIcon = stat.max*1f / stat.iconCount;
		TextureRegion fullIcon = GameCore.icons.get(stat.icon);
		TextureRegion emptyIcon = GameCore.icons.get(stat.outlineIcon);
		
		int iconWidth = stat.iconWidth + spacing;
		
		if(!rightSide) x -= stat.iconCount * iconWidth;
		
		// for each icon...
		for(int i = 0; i < stat.iconCount; i++) {
			// gets the amount this icon should be "filled" with the fullIcon
			float iconFillAmount = Math.min(Math.max(0, stats.get(stat) - i * pointsPerIcon) / pointsPerIcon, 1);
			
			// converts it to a pixel width
			int fullWidth = (int) (iconFillAmount * fullIcon.getRegionWidth());
			if(fullWidth > 0)
				batch.draw(fullIcon.getTexture(), x+i*iconWidth, y, fullIcon.getRegionX(), fullIcon.getRegionY(), fullWidth, fullIcon.getRegionHeight());
			
			// now draw the rest of the icon with the empty sprite.
			int emptyWidth = emptyIcon.getRegionWidth()-fullWidth;
			if(emptyWidth > 0)
				batch.draw(emptyIcon.getTexture(), x+i*iconWidth+fullWidth, y, emptyIcon.getRegionX() + fullWidth, emptyIcon.getRegionY(), emptyWidth, emptyIcon.getRegionHeight());
		}
	}
	
	@Override
	public void update(float delta) {
		super.update(delta);
		
		// update things like hunger, stamina, etc.
		for(StatEvolver evo: statEvoMap.values())
			evo.update(delta);
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
		bounds.x += dirVector.x;
		bounds.y += dirVector.y;
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
		WorldObject.sortByDistance(objects, getCenter());
		
		Tile tile = level.getClosestTile(interactionBounds);
		if(tile != null)
			objects.add(tile);
		
		return objects;
	}
	
	private void attack() {
		if(!hands.hasUsableItem()) return;
		
		Level level = getLevel();
		Item heldItem = hands.getUsableItem();
		
		boolean success = false;
		for(WorldObject obj: getInteractionQueue()) {
			if (heldItem.attack(obj, this)) {
				success = true;
				break;
			}
		}
		
		if(!heldItem.isUsed())
			changeStat(Stat.Stamina, -1); // for trying...
		
		if (level != null) {
			if(success)
				level.addEntity(ActionType.SLASH.get(getDirection()), getCenter().add(getDirection().getVector().scl(getSize().scl(0.5f))), true);
			else
				level.addEntity(ActionType.PUNCH.get(getDirection()), getInteractionRect().getCenter(new Vector2()), true);
		}
	}
	
	private void interact() {
		if(!hands.hasUsableItem()) return;
		
		Item heldItem = hands.getUsableItem();
		
		boolean success = false;
		for(WorldObject obj: getInteractionQueue()) {
			if (heldItem.interact(obj, this)) {
				success = true;
				break;
			}
		}
		
		if(!success)
			// none of the above interactions were successful, do the reflexive use.
			heldItem.interact(this);
		
		if(!heldItem.isUsed())
			changeStat(Stat.Stamina, -1); // for trying...
	}
	
	@Override
	public boolean attackedBy(WorldObject source, @Nullable Item item, int dmg) {
		if(super.attackedBy(source, item, dmg)) {
			int health = stats.get(Stat.Health);
			if (health == 0) return false;
			stats.put(Stat.Health, Math.max(0, health - dmg));
			// here is where I'd make a death chest, and show the death screen.
			return true;
		}
		return false;
	}
	
	
	private class StaminaSystem implements StatEvolver {
		
		private static final float STAMINA_REGEN_RATE = 0.25f; // time taken to regen 1 stamina point.
		
		private float curStaminaRate = 1;
		private float regenTime;
		
		public StaminaSystem() {}
		
		@Override
		public void update(float delta) {
			regenTime += delta;
			float regenRate = STAMINA_REGEN_RATE * curStaminaRate;
			int staminaGained = MathUtils.floor(regenTime / regenRate);
			if(staminaGained > 0) {
				regenTime -= staminaGained * regenRate;
				changeStat(Stat.Stamina, staminaGained);
			}
		}
	}
	
	private class HealthSystem implements StatEvolver {
		
		public HealthSystem() {}
		
		@Override
		public void update(float delta) {
			
		}
	}
	
	private class HungerSystem implements StatEvolver {
		
		public HungerSystem() {}
		
		@Override
		public void update(float delta) {
			
		}
	}
}
