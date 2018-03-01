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
import miniventure.game.util.MyUtils;
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
		
		Hunger("burger", 10, 20),
		
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
		
		hands = new Hands(this);
		reset();
	}
	
	// use this instead of creating a new player.
	public void reset() {
		for(Stat stat: Stat.values)
			stats.put(stat, stat.initial);
		
		super.reset();
		
		hands.clearItem(inventory);
		inventory = new Inventory(20, hands);
	}
	
	public int getStat(@NotNull Stat stat) {
		return stats.get(stat);
	}
	public int changeStat(@NotNull Stat stat, int amt) {
		int prevVal = stats.get(stat);
		stats.put(stat, Math.max(0, Math.min(stat.max, stats.get(stat) + amt)));
		if(stat == Stat.Health && amt > 0)
			regenHealth(amt);
		
		return stats.get(stat) - prevVal;
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
		
		getStatEvo(StaminaSystem.class).isMoving = !movement.isZero();
		if(!movement.isZero())
			getStatEvo(HungerSystem.class).addHunger(Gdx.graphics.getDeltaTime() * 0.35f);
		
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
		
		// for each icon...
		for(int i = 0; i < stat.iconCount; i++) {
			// gets the amount this icon should be "filled" with the fullIcon
			float iconFillAmount = Math.min(Math.max(0, stats.get(stat) - i * pointsPerIcon) / pointsPerIcon, 1);
			
			// converts it to a pixel width
			int fullWidth = (int) (iconFillAmount * fullIcon.getRegionWidth());
			float fullX = rightSide ? x+i*iconWidth : x - i*iconWidth - fullWidth;
			if(fullWidth > 0)
				batch.draw(fullIcon.getTexture(), fullX, y, fullIcon.getRegionX() + (rightSide?0:fullIcon.getRegionWidth()-fullWidth), fullIcon.getRegionY(), fullWidth, fullIcon.getRegionHeight());
			
			// now draw the rest of the icon with the empty sprite.
			int emptyWidth = emptyIcon.getRegionWidth()-fullWidth;
			float emptyX = rightSide ? x+i*iconWidth+fullWidth : x - (i+1)*iconWidth;
			if(emptyWidth > 0)
				batch.draw(emptyIcon.getTexture(), emptyX, y, emptyIcon.getRegionX() + (rightSide?fullWidth:0), emptyIcon.getRegionY(), emptyWidth, emptyIcon.getRegionHeight());
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
		
		private static final float STAMINA_REGEN_RATE = 0.35f; // time taken to regen 1 stamina point.
		
		private boolean isMoving = false;
		private float regenTime;
		
		StaminaSystem() {}
		
		@Override
		public void update(float delta) {
			regenTime += delta;
			float regenRate = STAMINA_REGEN_RATE;
			if(isMoving) regenRate *= 0.75f;
			//if(getStat(Stat.Health) != Stat.Health.max)
				//regenRate *= 1 - (0.5f * getStat(Stat.Hunger) / Stat.Hunger.max); // slow the stamina gen based on how fast you're regen-ing health; if you have very little hunger, then you aren't regen-ing much, so your stamina isn't affected as much.
			
			int staminaGained = MathUtils.floor(regenTime / regenRate);
			if(staminaGained > 0) {
				regenTime -= staminaGained * regenRate;
				changeStat(Stat.Stamina, staminaGained);
			}
		}
	}
	
	private class HealthSystem implements StatEvolver {
		
		private static final float REGEN_RATE = 2f; // whenever the regenTime reaches this value, a health point is added.
		private float regenTime;
		
		HealthSystem() {}
		
		@Override
		public void update(float delta) {
			if(getStat(Stat.Health) != Stat.Health.max) {
				float hungerRatio = getStat(Stat.Hunger)*1f / Stat.Hunger.max;
				regenTime += delta * hungerRatio;
				getStatEvo(HungerSystem.class).addHunger(delta);
				if(regenTime >= REGEN_RATE) {
					int healthGained = MathUtils.floor(regenTime / REGEN_RATE);
					changeStat(Stat.Health, healthGained);
					regenTime -= healthGained * REGEN_RATE;
				}
			}
			else regenTime = 0;
		}
	}
	
	private class HungerSystem implements StatEvolver {
		/*
			Hunger... you get it:
				- over time
				- walking
				- doing things (aka when stamina is low)
		 */
		
		private static final float HUNGER_RATE = 60f; // whenever the hunger count reaches this value, a hunger point is taken off.
		private static final float MAX_STAMINA_MULTIPLIER = 6; // you will lose hunger this many times as fast if you have absolutely no stamina.
		
		private float hunger = 0;
		
		HungerSystem() {}
		
		public void addHunger(float amt) {
			float hungerRatio = getStat(Stat.Hunger)*1f / Stat.Hunger.max;
			// make it so a ratio of 1 means x2 addition, and a ratio of 0 makes it 0.5 addition
			float amtMult = MyUtils.map(hungerRatio, 0, 1, 0.5f, 2);
			hunger += amt * amtMult;
		}
		
		@Override
		public void update(float delta) {
			float staminaRatio = 1 + (1 - (getStat(Stat.Stamina)*1f / Stat.Stamina.max)) * MAX_STAMINA_MULTIPLIER;
			addHunger(delta * staminaRatio);
			
			if(hunger >= HUNGER_RATE) {
				int hungerLost = MathUtils.floor(hunger / HUNGER_RATE);
				changeStat(Stat.Hunger, -hungerLost);
				hunger -= hungerLost * HUNGER_RATE;
			}
		}
	}
}
