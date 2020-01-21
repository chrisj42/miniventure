package miniventure.game.world.entity.mob.player;

import java.util.EnumMap;
import java.util.HashMap;

import miniventure.game.GameCore;
import miniventure.game.client.InputHandler.Control;
import miniventure.game.client.InputHandler.Modifier;
import miniventure.game.item.*;
import miniventure.game.item.CraftingScreen.ClientObjectRecipe;
import miniventure.game.network.GameProtocol.*;
import miniventure.game.client.ClientCore;
import miniventure.game.network.PacketPipe.PacketPipeWriter;
import miniventure.game.texture.TextureHolder;
import miniventure.game.util.MyUtils;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.ClientEntity;
import miniventure.game.world.entity.Direction;
import miniventure.game.world.entity.KnockbackController;
import miniventure.game.world.entity.mob.Mob;
import miniventure.game.world.entity.mob.MobAnimationController;
import miniventure.game.world.entity.mob.MobAnimationController.AnimationState;
import miniventure.game.world.level.Level;
import miniventure.game.world.tile.ClientTile;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static miniventure.game.network.GameProtocol.forPacket;

public class ClientPlayer extends ClientEntity implements Player {
	
	static {
		// a quick one-time assertion that checks the stat icon sizes. Just make sure that initGdx is called before ClientPlayer is referenced.
		for(Stat stat: Stat.values) {
			if(stat.icon.length() == 0) continue; // unimplemented stat
			TextureHolder fullIcon = GameCore.icons.get(stat.icon);
			TextureHolder emptyIcon = GameCore.icons.get(stat.outlineIcon);
			if(fullIcon.width != emptyIcon.width || fullIcon.height != emptyIcon.height)
				throw new IconSizeMismatchException("full/empty icons for player stat "+stat+" have different dimensions; full="+fullIcon.width+'x'+fullIcon.height+", empty="+emptyIcon.width+'x'+emptyIcon.height);
		}
	}
	
	// TODO move the stat handling to the server, because it is important info that is saved, and all such info ought to be tracked and handled by the server.
	
	@FunctionalInterface
	private interface StatEvolver {
		void update(float delta);
	}
	
	private final HashMap<Class<? extends StatEvolver>, StatEvolver> statEvoMap;
	private <T extends StatEvolver> void addStatEvo(T evolver) {
		statEvoMap.put(evolver.getClass(), evolver);
	}
	@SuppressWarnings("unchecked")
	private <T extends StatEvolver> T getStatEvo(Class<T> clazz) {
		return (T) statEvoMap.get(clazz);
	}
	{
		statEvoMap = new HashMap<>(3);
		addStatEvo(new StaminaSystem());
		addStatEvo(new HealthSystem());
		addStatEvo(new HungerSystem());
	}
	
	@NotNull private final EnumMap<Stat, Integer> stats = new EnumMap<>(Stat.class);
	
	private ClientPlayerInventory inventory;
	@Nullable private ClientObjectRecipe objectRecipe; // currently choosing a location to craft an object
	
	private float moveSpeed = Player.MOVE_SPEED;
	@NotNull private Direction dir;
	
	private MobAnimationController<ClientPlayer> animator;
	private KnockbackController knockbackController;
	
	public ClientPlayer(SpawnData data, InventoryOverlay invScreen) {
		super(data.playerData);
		
		dir = Direction.DOWN;
		
		inventory = new ClientPlayerInventory();
		
		inventory.updateItems(data.inv.inventory, data.inv.equipment);
		invScreen.setInventory(inventory);
		
		Stat.load(data.stats, this.stats);
		
		animator = new MobAnimationController<>(this, "player");
		knockbackController = new KnockbackController(this);
	}
	
	public float getSpeed() { return moveSpeed; }
	public void setSpeed(float speed) { moveSpeed = speed; }
	
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
	// public ClientInventory getInventory() { return inventory; }
	
	public void setObjectRecipe(@Nullable ClientObjectRecipe recipe) {
		this.objectRecipe = recipe;
	}
	
	@Nullable
	public ClientObjectRecipe getObjectRecipe() { return objectRecipe; }
	
	@Override
	public boolean isKnockedBack() { return knockbackController.hasKnockback(); }
	
	public CursorHighlight getCurrentHighlightMode() {
		if(objectRecipe != null)
			return CursorHighlight.TILE_IN_RADIUS;
		ItemStack cur = inventory.getSelectedItem();
		return cur == null ? CursorHighlight.TILE_ADJACENT : cur.item.getHighlightMode();
	}
	
	/*public Rectangle computeInteractionRect(Vector2 cursorPos) {
		Vector2 clampedPos = Player.clampCursorPos(getCenter(), cursorPos, getCurrentHighlightMode());
		return getInteractionRect(clampedPos);
	}*/
	
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
		super.render(batch, delta, posOffset);
		animator.progressAnimation(delta);
	}
	
	//private float lastWalkTime = 0;
	public void handleInput(Vector2 mouseInput, @Nullable Vector2 cursorPos) {
		
		Vector2 inputDir = new Vector2();
		if(ClientCore.input.holdingControl(Control.MOVE_LEFT)) inputDir.x--;
		if(ClientCore.input.holdingControl(Control.MOVE_RIGHT)) inputDir.x++;
		if(ClientCore.input.holdingControl(Control.MOVE_UP)) inputDir.y++;
		if(ClientCore.input.holdingControl(Control.MOVE_DOWN)) inputDir.y--;
		
		inputDir.nor();
		
		// inputDir.add(mouseInput); // mouseInput is already normalized
		// inputDir.nor();
			
		Direction newDir = Direction.getDirection(inputDir.x, inputDir.y);
		if(newDir != null) {
			if(animator.setDirection(newDir))
				dir = newDir;
		}
		
		Vector2 moveDist = inputDir.cpy().scl(moveSpeed * GameCore.getDeltaTime());
		// FIXME speed needs to be set in server 
		ClientTile closest = (ClientTile) getClosestTile();
		if(closest != null)
			moveDist.scl(closest.getType().getSpeedRatio());
		
		//float elapTime = GameCore.getElapsedProgramTime();
		if(!moveDist.isZero()) {
			move(moveDist, getLevel() != null);
			
			animator.requestState(AnimationState.WALK);
			
			getStatEvo(HungerSystem.class).addHunger(GameCore.getDeltaTime() * 0.35f);
			
			/*if(elapTime - lastWalkTime > 0.25f) {
				ClientCore.playSound("player/walk");
				lastWalkTime = elapTime;
			}*/
		}/*
		else
			lastWalkTime = elapTime-.2f;*/
		
		getStatEvo(StaminaSystem.class).isMoving = !moveDist.isZero();
		
		if(!ClientCore.hasMenu()) {
			
			if(!isKnockedBack() && cursorPos != null) {
				boolean attack = ClientCore.input.pressingControl(Control.ATTACK);
				boolean interact = attack || ClientCore.input.pressingControl(Control.INTERACT);
				
				if(interact) {
					// Level level = getLevel();
					// Tile cursorTile = level != null ? getCursorTile(cursorPos, level, getCurrentHighlightMode()) : null;
					
					// level cursor is already clamped
					// Vector2 clampedPos = Player.clampCursorPos(getCenter(), cursorPos.cpy(), getCurrentHighlightMode());
					
					if(objectRecipe != null) {
						if(attack) {
							// place the object on attack
							// if(cursorTile != null)
							ClientCore.getClient().send(objectRecipe.getBuildRequest(cursorPos));
						} else {
							// cancel the object recipe on interact
							objectRecipe = null;
						}
					} else {
						// if(cursorTile != null)
						ClientCore.getClient().send(new InteractRequest(attack, cursorPos, getDirection(), inventory.getSelection()));
					}
				}
			}
			
			/*if(ClientCore.input.pressingControl(Control.INVENTORY_TOGGLE)) {
				ClientCore.setScreen(new InventoryScreen(inventory));
			} else */if(ClientCore.input.pressingControl(Control.CRAFTING_TOGGLE))
				ClientCore.setScreen(new CraftingScreen());
			else if(ClientCore.input.pressingControl(Control.DROP_ITEM)) {
				inventory.dropInvItems(Modifier.SHIFT.isPressed());
			}
			/*else if(ClientCore.input.pressingControl(Control.DROP_STACK)) {
				inventory.dropInvItems(true);
			}*/
		}
		
		if(GameCore.debug && Modifier.SHIFT.isPressed() && ClientCore.input.pressingKey(Keys.H))
			changeStat(Stat.Health, -1);
	}
	
	@Override
	public boolean move(float xd, float yd, float zd, boolean validate) {
		
		PositionUpdate prevPos = new PositionUpdate(this);
		
		boolean moved = super.move(xd, yd, zd, validate);
		
		ClientCore.getClient().send(new MovementRequest(prevPos, xd, yd, zd, new PositionUpdate(this)));
		
		return moved;
	}
	
	@Override
	public void hurt(WorldObject source, float power) {
		super.hurt(source, power);
		knockbackController.knock(source, KNOCKBACK_SPEED, Mob.getKnockbackDuration(power));
	}
	
	@Override
	public void handlePlayerPackets(@NotNull Object object, @NotNull PacketPipeWriter packetSender) {
		
		forPacket(object, InventoryUpdate.class, newInv -> {
			inventory.updateItems(newInv.inventory, newInv.equipment/*, newInv.hotbarData*/);
		});
		
		forPacket(object, InventoryAddition.class, addition -> {
			inventory.getInv().addItem(ClientItem.deserialize(addition.newItem));
		});
		
		forPacket(object, PositionUpdate.class, newPos -> {
			moveTo(newPos.x, newPos.y, newPos.z);
		});
		
		forPacket(object, StatUpdate.class, update -> {
			changeStat(update.stat, update.amount);
		});
	}
	
	private static final float padding = 3;
	private void drawStat(Stat stat, float x, float y, SpriteBatch batch, Vector2 hold) {
		// if(GameCore.debug) System.out.println("drawing stat "+stat+" at "+x+","+y);
		Vector2 size = renderBar(stat, x, y, batch);
		hold.y += size.y+padding;
		hold.x = Math.max(hold.x, size.x);
	}
	public void drawGui(Rectangle canvas, SpriteBatch batch) {
		Vector2 hold = new Vector2(0, canvas.y + padding);
		
		float x = canvas.x + canvas.width;
		drawStat(Stat.Health, x, hold.y, batch, hold);
		// if(getStat(Stat.Armor) > 0)
		// 	drawStat(Stat.Armor, canvas.x, hold.y, batch, hold);
		drawStat(Stat.Stamina, x, hold.y, batch, hold);
		drawStat(Stat.Hunger, x, hold.y, batch, hold);
	}
	
	private Vector2 renderBar(Stat stat, float x, float y, SpriteBatch batch) { return renderBar(stat, x, y, batch, 0); }
	private Vector2 renderBar(Stat stat, float x, float y, SpriteBatch batch, int spacing) { return renderBar(stat, x, y, batch, spacing, true); }
	private Vector2 renderBar(Stat stat, float x, float y, SpriteBatch batch, int spacing, boolean rightSide) {
		float pointsPerIcon = stat.max*1f / stat.iconCount;
		TextureRegion fullIcon = GameCore.icons.get(stat.icon).texture;
		TextureRegion emptyIcon = GameCore.icons.get(stat.outlineIcon).texture;
		
		final int statWidth = fullIcon.getRegionWidth();
		final int statHeight = fullIcon.getRegionHeight();
		final int iconWidth = statWidth + spacing;
		
		// for each icon...
		for(int i = 0; i < stat.iconCount; i++) {
			// gets the amount this icon should be "filled" with the fullIcon
			float iconFillAmount = Math.min(Math.max(0, stats.get(stat) - i * pointsPerIcon) / pointsPerIcon, 1);
			
			// converts it to a pixel width
			int fullWidth = (int) (iconFillAmount * statWidth);
			float fullX = rightSide ? x - i*iconWidth - fullWidth : x + i*iconWidth;
			if(fullWidth > 0)
				batch.draw(fullIcon.getTexture(), fullX, y, fullIcon.getRegionX() + (rightSide?statWidth-fullWidth:0), fullIcon.getRegionY(), fullWidth, statHeight);
			
			// now draw the rest of the icon with the empty sprite.
			int emptyWidth = statWidth-fullWidth;
			float emptyX = rightSide ? x - (i+1)*iconWidth : x+i*iconWidth+fullWidth;
			if(emptyWidth > 0)
				batch.draw(emptyIcon.getTexture(), emptyX, y, emptyIcon.getRegionX() + (rightSide?0:fullWidth), emptyIcon.getRegionY(), emptyWidth, statHeight);
		}
		
		return new Vector2(iconWidth * stat.iconCount, statHeight);
	}
	
	
	class StaminaSystem implements StatEvolver {
		
		private static final float STAMINA_REGEN_RATE = 0.35f; // time taken to regen 1 stamina point.
		
		boolean isMoving = false;
		private float regenTime;
		
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
	
	class HealthSystem implements StatEvolver {
		
		private static final float REGEN_RATE = 2f; // whenever the regenTime reaches this value, a health point is added.
		private float regenTime;
		
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
	
	class HungerSystem implements StatEvolver {
		/*
			Hunger... you get it:
				- over time
				- walking
				- doing things (aka when stamina is low)
		 */
		
		private static final float HUNGER_RATE = 60f; // whenever the hunger count reaches this value, a hunger point is taken off.
		private static final float MAX_STAMINA_MULTIPLIER = 6; // you will lose hunger this many times as fast if you have absolutely no stamina.
		
		private float hunger = 0;
		
		public void addHunger(float amt) {
			float hungerRatio = getStat(Stat.Hunger)*1f / Stat.Hunger.max;
			// make it so a ratio of 1 means x2 addition, and a ratio of 0 makes it 0.5 addition
			float amtMult = MyUtils.mapFloat(hungerRatio, 0, 1, 0.5f, 2);
			hunger += amt * amtMult;
		}
		
		@Override
		public void update(float delta) {
			float staminaRatio = 1 + (1 - (getStat(Stat.Stamina)*1f / Stat.Stamina.max)) * MAX_STAMINA_MULTIPLIER;
			addHunger(delta * staminaRatio);
			
			if(hunger >= HUNGER_RATE) {
				int hungerLost = MathUtils.floor(hunger / HUNGER_RATE);
				int changed = changeStat(Stat.Hunger, -hungerLost);
				if(Math.abs(changed) < hungerLost)
					ClientCore.getClient().send(new SelfHurt(1));
				hunger -= hungerLost * HUNGER_RATE;
			}
		}
	}
}
