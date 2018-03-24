package miniventure.game.world.entity.mob;

import java.util.EnumMap;
import java.util.HashMap;

import miniventure.game.GameCore;
import miniventure.game.GameProtocol.InteractRequest;
import miniventure.game.GameProtocol.PositionUpdate;
import miniventure.game.GameProtocol.SpawnData;
import miniventure.game.GameProtocol.SpriteUpdate;
import miniventure.game.GameProtocol.StatUpdate;
import miniventure.game.client.ClientCore;
import miniventure.game.item.ClientHands;
import miniventure.game.item.CraftingScreen;
import miniventure.game.item.Inventory;
import miniventure.game.item.InventoryScreen;
import miniventure.game.item.Recipes;
import miniventure.game.util.MyUtils;
import miniventure.game.world.ClientLevel;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.ClientEntity;
import miniventure.game.world.entity.Direction;
import miniventure.game.world.entity.EntityRenderer;
import miniventure.game.world.entity.KnockbackController;
import miniventure.game.world.entity.mob.MobAnimationController.AnimationState;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;

public class ClientPlayer extends ClientEntity implements Player {
	
	interface StatEvolver { void update(float delta); }
	
	private final HashMap<Class<? extends StatEvolver>, StatEvolver> statEvoMap = new HashMap<>();
	private <T extends StatEvolver> void addStatEvo(T evolver) {
		statEvoMap.put(evolver.getClass(), evolver);
	}
	<T extends StatEvolver> T getStatEvo(Class<T> clazz) {
		//noinspection unchecked
		return (T) statEvoMap.get(clazz);
	}
	{
		addStatEvo(new StaminaSystem());
		addStatEvo(new HealthSystem());
		addStatEvo(new HungerSystem());
	}
	
	private final EnumMap<Stat, Integer> stats = new EnumMap<>(Stat.class);
	
	private Inventory inventory;
	private ClientHands hands;
	
	private float moveSpeed = Player.MOVE_SPEED;
	@NotNull private Direction dir;
	
	private MobAnimationController animator;
	private KnockbackController knockbackController;
	
	public ClientPlayer(SpawnData data) {
		super(data.playerData.eid, data.playerData.permeable, EntityRenderer.deserialize(data.playerData.spriteUpdate.rendererData), data.playerData.descriptor);
		
		dir = Direction.DOWN;
		
		hands = new ClientHands(this);
		inventory = new Inventory(INV_SIZE); // no must-fit because that is handled by the server
		
		hands.loadItem(data.inventory.heldItemStack);
		inventory.loadItems(data.inventory.inventory);
		
		Stat.load(data.stats, this.stats);
		
		animator = new MobAnimationController(this, "player");
		knockbackController = new KnockbackController(this);
	}
	
	@Override
	public Integer[] saveStats() { return Stat.save(stats); }
	
	@Override
	public int getStat(@NotNull Stat stat) { return stats.get(stat); }
	
	@Override
	public int changeStat(@NotNull Stat stat, int amt) {
		int prevVal = stats.get(stat);
		stats.put(stat, Math.max(0, Math.min(stat.max, stats.get(stat) + amt)));
		
		int change = stats.get(stat) - prevVal;
		
		if(change != 0)
			ClientCore.getClient().send(new StatUpdate(stat, stats.get(stat)));
		
		return change;
	}
	
	@Override @NotNull public Direction getDirection() { return dir; }
	@Override public Inventory getInventory() { return inventory; }
	@Override public ClientHands getHands() { return hands; }
	
	@Override
	public void update(float delta) {
		super.update(delta);
		
		knockbackController.update(delta);
		
		// update things like hunger, stamina, etc.
		for(StatEvolver evo: statEvoMap.values())
			evo.update(delta);
	}
	
	@Override
	public void render(SpriteBatch batch, float delta, Vector2 posOffset) {
		animator.progressAnimation(delta);
		SpriteUpdate newSprite = animator.getSpriteUpdate();
		if(newSprite != null)
			setRenderer(EntityRenderer.deserialize(newSprite.rendererData));
		
		super.render(batch, delta, posOffset);
	}
	
	public void handleInput(Vector2 mouseInput) {
		Vector2 movement = new Vector2();
		if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) movement.x--;
		if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) movement.x++;
		if(Gdx.input.isKeyPressed(Input.Keys.UP)) movement.y++;
		if(Gdx.input.isKeyPressed(Input.Keys.DOWN)) movement.y--;
		
		movement.nor();
		
		movement.add(mouseInput); // mouseInput is already normalized
		movement.nor();
		
		Direction newDir = Direction.getDirection(movement.x, movement.y);
		if(newDir != null)
			dir = newDir;
		
		boolean moved = move(movement, Gdx.graphics.getDeltaTime());
		
		if(moved) {
			ClientCore.getClient().send(new PositionUpdate(this));
			animator.requestState(AnimationState.WALK);
		}
		
		getStatEvo(StaminaSystem.class).isMoving = moved;
		if(moved)
			getStatEvo(HungerSystem.class).addHunger(Gdx.graphics.getDeltaTime() * 0.35f);
		
		//if(!player.isKnockedBack()) {
		if (ClientCore.input.pressingKey(Input.Keys.C))
			ClientCore.getClient().send(new InteractRequest(true, new PositionUpdate(this)));
		else if (ClientCore.input.pressingKey(Input.Keys.V))
			ClientCore.getClient().send(new InteractRequest(false, new PositionUpdate(this)));
		
		//if(Gdx.input.isKeyPressed(Input.Keys.C) || Gdx.input.isKeyPressed(Input.Keys.V))
		//	animator.requestState(AnimationState.ATTACK);
		//}
		
		hands.resetItemUsage();
		
		if(ClientCore.input.pressingKey(Input.Keys.E)) {
			// do nothing here; instead, tell the server to set the held item once selected (aka on inventory menu exit). The inventory should be up to date already, generally speaking.
			hands.clearItem(inventory);
			ClientCore.setScreen(new InventoryScreen(inventory, hands));
		}
		else if(ClientCore.input.pressingKey(Input.Keys.Z))
			ClientCore.setScreen(new CraftingScreen(Recipes.recipes, inventory));
		
	}
	
	private boolean move(Vector2 inputDir, float delta) {
		if(inputDir.isZero()) return false;
		Vector2 moveDist = inputDir.cpy().scl(moveSpeed*delta);
		Vector2 newPos = getPosition().add(moveDist);
		
		Rectangle bounds = getBounds();
		bounds.setPosition(newPos);
		
		ClientLevel level = getLevel();
		if(level == null) {
			moveTo(newPos);
			return true;
		}
		
		return move(moveDist, true);
	}
	
	@Override
	public void hurt(WorldObject source, float power) {
		super.hurt(source, power);
		knockbackController.knock(source, KNOCKBACK_SPEED, Mob.getKnockbackDuration(power));
	}
	
	public void drawGui(Rectangle canvas, SpriteBatch batch) {
		hands.getUsableItem().drawItem(hands.getCount(), batch, canvas.width/2, 20);
		float y = canvas.y + 3;
		
		renderBar(Stat.Health, canvas.x, y, batch);
		renderBar(Stat.Stamina, canvas.x, y+ Stat.Health.iconHeight+3, batch);
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
	
	
	protected class StaminaSystem implements StatEvolver {
		
		private static final float STAMINA_REGEN_RATE = 0.35f; // time taken to regen 1 stamina point.
		
		boolean isMoving = false;
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
	
	protected class HealthSystem implements StatEvolver {
		
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
	
	protected class HungerSystem implements StatEvolver {
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
