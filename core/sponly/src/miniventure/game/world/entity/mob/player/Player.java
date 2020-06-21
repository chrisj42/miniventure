package miniventure.game.world.entity.mob.player;

import java.util.EnumMap;
import java.util.HashMap;

import miniventure.game.core.AudioCore.SoundEffect;
import miniventure.game.core.GameCore;
import miniventure.game.core.GameScreen;
import miniventure.game.core.GdxCore;
import miniventure.game.core.InputHandler.Control;
import miniventure.game.core.InputHandler.Modifier;
import miniventure.game.item.*;
import miniventure.game.item.ToolItem.ToolType;
import miniventure.game.item.inventory.CraftingScreen;
import miniventure.game.item.inventory.InventoryOverlay;
import miniventure.game.item.recipe.ItemRecipeSet;
import miniventure.game.screen.RespawnScreen;
import miniventure.game.texture.TextureHolder;
import miniventure.game.util.MyUtils;
import miniventure.game.util.SerialHashMap;
import miniventure.game.util.Version;
import miniventure.game.util.function.ValueAction;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.Direction;
import miniventure.game.world.entity.EntityDataSet;
import miniventure.game.world.entity.EntitySpawn;
import miniventure.game.world.entity.mob.Mob;
import miniventure.game.world.entity.particle.ActionParticle.ActionType;
import miniventure.game.world.entity.particle.TextParticle;
import miniventure.game.world.management.Level;
import miniventure.game.world.tile.Tile;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Player extends Mob {
	
	// these are all measured in tiles
	public static final float MOVE_SPEED = 5;
	public static final float INTERACT_RECT_SIZE = 1;
	
	static {
		// a quick one-time assertion that checks the stat icon sizes. Just make sure that initGdx is called before Player is referenced.
		for(Stat stat: Stat.values) {
			if(stat.icon.length() == 0) continue; // unimplemented stat
			TextureHolder fullIcon = GdxCore.icons.get(stat.icon);
			TextureHolder emptyIcon = GdxCore.icons.get(stat.outlineIcon);
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
	
	// private Point spawnLoc;
	// private int spawnLevel = 0;
	private SpawnLocation savedSpawn;
	
	
	@NotNull private final PlayerInventory inv;
	// @NotNull private final Inventory inventory;
	
	private float moveSpeed = Player.MOVE_SPEED;
	// @NotNull private Direction dir;
	
	// private MobAnimationController<Player> animator;
	// private KnockbackController knockbackController;
	
	// when a player interacts with a hammer, that hammer is saved and then the recipes are given.
	// if a recipe selection request is then received, then the saved hammer is modified and the inventory is updated.
	// this is reset when the normal crafting screen is opened, so that if a selection request is made when this is null, then we'll search for a hammer that provides the given recipe
	private HammerItem lastQueried = null;
	
	public Player(@NotNull EntitySpawn info) {
		super(info, "player");
		
		inv = new PlayerInventory(this);
		reset();
	}
	
	// use this to transition the player to a new level
	public Player(@NotNull EntitySpawn info, Player model) {
		super(info, "player");
		lastQueried = model.lastQueried;
		moveSpeed = model.moveSpeed;
		inv = model.inv;
		inv.setPlayer(this);
		savedSpawn = model.savedSpawn;
		stats.putAll(model.stats);
	}
	
	protected Player(@NotNull Level level, EntityDataSet allData, final Version version, ValueAction<EntityDataSet> modifier) {
		super(level, allData, version, modifier);
		SerialHashMap data = allData.get("player");
		
		inv = new PlayerInventory(this);
		
		stats.put(Stat.Health, data.get("health", Integer::parseInt));
		stats.put(Stat.Hunger, data.get("hunger", Integer::parseInt));
		stats.put(Stat.Stamina, data.get("stamina", Integer::parseInt));
		// stats.put(Stat.Armor, Integer.parseInt(data.get(3)));
		
		String spawnInfo = data.get("spawn");
		if(spawnInfo != null)
			savedSpawn = new SpawnLocation(spawnInfo);
		
		inv.load(data.get("inv"), version);
	}
	
	public void updateGameScreen(GameScreen gameScreen) {
		gameScreen.setInventoryGui(inv);
	}
	
	@Override
	public EntityDataSet save() {
		EntityDataSet allData = super.save();
		allData.get("mob").remove("sprite");
		
		SerialHashMap data = new SerialHashMap();
		// data.add("name", name);
		data.add("health", getStat(Stat.Health));
		data.add("hunger", getStat(Stat.Hunger));
		data.add("stamina", getStat(Stat.Stamina));
		
		if(savedSpawn != null)
			data.add("spawn", savedSpawn.serialize());
		
		data.add("inv", inv.save());
		
		allData.put("player", data);
		return allData;
	}
	
	// use this instead of creating a new player.
	@Override
	public void reset() {
		for(Stat stat: Stat.values)
			stats.put(stat, stat.initial);
		// savedSpawn = null; // we want to keep this actually
		
		super.reset();
		
		inv.reset();
		
		if(GameCore.debug) {
			System.out.println("adding debug items to player inventory");
			inv.addItem(new ToolItem(ToolType.Shovel, MaterialQuality.Superior));
			inv.addItem(new ToolItem(ToolType.Axe, MaterialQuality.Superior));
			inv.addItem(new ToolItem(ToolType.Pickaxe, MaterialQuality.Superior));
			for(int i = 0; i < 7; i++)
				inv.addItem(PlaceableItemType.Torch.get());
			for(int i = 0; i < 7; i++)
				inv.addItem(ResourceType.Log.get());
			for(int i = 0; i < 7; i++)
				inv.addItem(FoodType.Cooked_Meat.get());
		}
	}
	
	@Override
	public void update(float delta) {
		super.update(delta);
		
		// update things like hunger, stamina, etc.
		for(StatEvolver evo: statEvoMap.values())
			evo.update(delta);
	}
	
	public InventoryOverlay makeInvOverlay(Viewport viewport) {
		return new InventoryOverlay(inv, viewport);
	}
	
	@NotNull
	PlayerInventory getInv() { return inv; }
	
	@Nullable
	public TextureHolder getCursorTexture() {
		Item cur = inv.getSelectedItem();
		return cur == null ? null : cur.getCursorTexture();
	}
	
	public CursorHighlight getCurrentHighlightMode() {
		Item cur = inv.getSelectedItem();
		return cur == null ? CursorHighlight.FRONT_AREA : cur.getHighlightMode();
	}
	
	public float getSpeed() { return moveSpeed; }
	public void setSpeed(float speed) { moveSpeed = speed; }
	
	// public Integer[] saveStats() { return Stat.save(stats); }
	
	int getStat(@NotNull Stat stat) { return stats.get(stat); }
	
	int changeStat(@NotNull Stat stat, int amt) {
		int prevVal = stats.get(stat);
		stats.put(stat, Math.max(0, Math.min(stat.max, stats.get(stat) + amt)));
		
		int change = stats.get(stat) - prevVal;
		
		if(stat == Stat.Hunger && change > 0)
			new TextParticle(level.getSpawn(getCenter()), String.valueOf(change), Color.CORAL);
		
		return change;
	}
	
	public int restoreHunger(int amount) {
		return changeStat(Stat.Hunger, amount);
	}
	
	@Override
	protected int getMaxHealth() { return Stat.Health.max; }
	@Override
	protected int getHealth() { return getStat(Stat.Health); }
	@Override
	protected int changeHealth(int amount) { return changeStat(Stat.Health, amount); }
	
	public void handleInput(@Nullable Vector2 cursorPos) {
		
		Vector2 moveDist = MyUtils.getV2(0, 0);
		if(GdxCore.input.holdingControl(Control.MOVE_LEFT)) moveDist.x--;
		if(GdxCore.input.holdingControl(Control.MOVE_RIGHT)) moveDist.x++;
		if(GdxCore.input.holdingControl(Control.MOVE_UP)) moveDist.y++;
		if(GdxCore.input.holdingControl(Control.MOVE_DOWN)) moveDist.y--;
		
		moveDist.nor().scl(moveSpeed * GdxCore.getDeltaTime());
		
		Tile closest = getClosestTile();
		moveDist.scl(closest.getType().getSpeedRatio());
		
		//float elapTime = GameCore.getElapsedProgramTime();
		if(!moveDist.isZero()) {
			move(moveDist);
			
			getStatEvo(HungerSystem.class).addHunger(GdxCore.getDeltaTime() * 0.35f);
		}
		
		getStatEvo(StaminaSystem.class).isMoving = !moveDist.isZero();
		
		if(!GdxCore.hasMenu()) {
			
			if(!isKnockedBack() && cursorPos != null) {
				boolean attack = GdxCore.input.pressingControl(Control.ATTACK);
				boolean interact = attack || GdxCore.input.pressingControl(Control.INTERACT);
				
				if(interact)
					doInteract(cursorPos, attack);
			}
			
			/*if(ClientCore.input.pressingControl(Control.INVENTORY_TOGGLE)) {
				ClientCore.setScreen(new InventoryScreen(inventory));
			} else */if(GdxCore.input.pressingControl(Control.CRAFTING_TOGGLE))
				GdxCore.setScreen(new CraftingScreen(ItemRecipeSet.HAND, inv));
			else if(GdxCore.input.pressingControl(Control.DROP_ITEM)) {
				inv.dropItems(inv.getSelection(), Modifier.SHIFT.isPressed());
			}
		}
		
		if(GameCore.debug && Modifier.SHIFT.isPressed() && GdxCore.input.pressingKey(Keys.H))
			changeStat(Stat.Health, -1);
		
		MyUtils.freeV2(moveDist);
	}
	
	// this method gets called by GameServer, so in order to ensure it doesn't mix badly with server world updates, we'll post it as a runnable to the server world update thread.
	private void doInteract(Vector2 actionPos, boolean attack) {
		// setDirection(dir);
		final int selectedIndex = inv.getSelection();
		Item heldItem = inv.getHeldItem(selectedIndex);
		
		if(getStat(Stat.Stamina) < heldItem.getStaminaUsage())
			return;
		
		// Level level = getLevel();
		
		Result result = Result.NONE;
		for(WorldObject obj: getInteractionQueue(actionPos)) {
			if(attack)
				result = heldItem.attack(obj, this);
			else
				result = heldItem.interact(obj, this);
			
			if(result.success)
				break;
		}
		
		if(!attack && !result.success)
			// none of the above interactions were successful, do the reflexive use.
			result = heldItem.interact(this);
		
		if(!result.success && heldItem instanceof EquipmentItem) {
			if(inv.equipItem(((EquipmentItem)heldItem).getEquipmentType(), selectedIndex)) {
				result = Result.USED;
				// getServer().sendToPlayer(this, invManager.getUpdate(true));
			}
		}
		else if (attack) {
			ActionType actionType = result.success ? ActionType.SLASH : ActionType.PUNCH;
			
			// TextureHolder tex = GameCore.entityAtlas.getRegion(actionType.getSpriteName(dir));
			Vector2 offset = getSize();
			offset.y = Mob.unshortenSprite(offset.y);
			offset.scl(0.5f);
			// if(dir == Direction.LEFT)
			// 	offset.x += tex.width / (float) Tile.SIZE;
			// if(dir == Direction.DOWN)
			// 	offset.y += tex.height / (float) Tile.SIZE;
			
			final Direction dir = getDirection();
			
			Vector2 pos = getCenter().add(dir.getVector().scl(offset));
			
			// getServer().broadcastParticle(new ActionParticleData(actionType, dir), level, pos);
			actionType.makeParticle(EntitySpawn.get(level, pos), dir);
		}
		
		if(result == Result.USED)
			changeStat(Stat.Stamina, -heldItem.getStaminaUsage());
		else
			changeStat(Stat.Stamina, -1); // for trying...
		
		if(!result.success)
			// this sound is of an empty swing; successful interaction sounds are taken care of elsewhere.
			SoundEffect.PLAYER_SWING.play(this);
		
		if(result == Result.USED && !GameCore.debug)
			inv.resetItemUsage(heldItem, selectedIndex);
	}
	
	@NotNull
	private Array<WorldObject> getInteractionQueue(Vector2 center) {
		Array<WorldObject> objects = new Array<>();
		
		// get level, and don't interact if level is not found
		Level level = getLevel();
		// if(level == null) return objects;
		
		Rectangle interactionBounds = getInteractionRect(center);
		
		objects.addAll(level.getOverlappingEntities(interactionBounds, this));
		WorldObject.sortByDistance(objects, getCenter());
		
		Tile tile = level.getTile(center);
		if(tile != null)
			objects.add(tile);
		
		return objects;
	}
	
	public Rectangle getInteractionRect(Vector2 center) {
		if(center == null)
			return new Rectangle(-1, -1, 0 ,0);
		
		Rectangle bounds = new Rectangle();
		bounds.setSize(INTERACT_RECT_SIZE);
		bounds.setCenter(center);
		return bounds;
	}
	
	private void resetItemUsage(@NotNull Item item, int index) {
		Item newItem = item.getUsedItem();
		
		// this is already done above
		// if(!GameCore.debug)
		// 	changeStat(Stat.Stamina, -item.getStaminaUsage());
		
		// While I could consider this asking for trouble, I'm already stacking items, so any unspecified "individual" data is lost already.
		if(item.equals(newItem)) // this is true for the hand item.
			return; // there is literally zero difference in what the item is now, and what it was before.
		
		// the item has changed (possibly into nothing)
		
		// remove the current item.
		inv.removeItem(item);
		if(newItem != null) {
			// the item has changed either in metadata or into an entirely separate item.
			// add the new item to the inventory, and then determine what should become the held item: previous or new item.
			// if the new item doesn't fit, then drop it on the ground instead.
			
			if(!inv.addItem(index, newItem)) {
				// inventory is full, try to drop it on the ground
				Level level = getLevel();
				level.dropItem(newItem, getCenter(), getCenter().add(getDirection().getVector()));
			}
			/*else if(findItem(newItem) < 0) { // new item not found in hotbar
				// item was successfully added to the inventory; now figure out what to do with the hotbar.
				// this block is only needed to figure out where to put the new item; if it's already in the hotbar then we don't need to do anything. ;)
				
				// check if the original item has run out, in which case the new item should replace it in the hotbar.
				if(!getInv().hasItem(item))
					hotbarItems[index] = newItem;
				else {
					// original item still exists; decide if new item should replace it or not
					if(newItem.getName().equals(item.getName())) {
						// metadata changed, same item, replace stack
						setSlot(index, newItem);
						addItem(item, index); // try and keep the stack in the hotbar
					}
					else // name changed, different item, keep stack
						addItem(newItem); // try-add new item to hotbar like when you normally pick items up
				}
			}*/
		}
		
		// remove old item from hotbar if it's no longer in the inventory
		// if(!inventory.hasItem(item))
		// 	removeItem(item);
		
		// we are never going to be in inventory mode here, because the client has just used an item; items can't be used with a menu open.
		// getServer().sendToPlayer(this, invManager.getUpdate(false));
	}
	
	public boolean takeItem(@NotNull Item item) {
		if(inv.addItem(item)) {
			SoundEffect.PLAYER_PICKUP.play(this);
			return true;
		}
		return false;
	}
	
	@Override
	public void die() {
		super.die();
		GdxCore.setScreen(new RespawnScreen(this));
	}
	
	// void handlePlayerPackets(@NotNull Object packet, @NotNull PacketPipe.PacketPipeWriter packetSender);
	
	private static final float padding = 3;
	private void drawStat(Stat stat, float x, float y, SpriteBatch batch, Vector2 hold) {
		// if(GameCore.debug) System.out.println("drawing stat "+stat+" at "+x+","+y);
		Vector2 size = renderBar(stat, x, y, batch);
		hold.y += size.y+padding;
		hold.x = Math.max(hold.x, size.x);
	}
	public void drawStatGui(Rectangle canvas, SpriteBatch batch) {
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
		TextureRegion fullIcon = GdxCore.icons.get(stat.icon).texture;
		TextureRegion emptyIcon = GdxCore.icons.get(stat.outlineIcon).texture;
		
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
					attackedBy(Player.this, null, 1);
				hunger -= hungerLost * HUNGER_RATE;
			}
		}
	}
}
