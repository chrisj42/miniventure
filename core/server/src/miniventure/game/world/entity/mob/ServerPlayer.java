package miniventure.game.world.entity.mob;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;

import miniventure.game.GameProtocol.InventoryUpdate;
import miniventure.game.item.Hands;
import miniventure.game.item.Inventory;
import miniventure.game.item.Item;
import miniventure.game.item.ServerHands;
import miniventure.game.server.ServerCore;
import miniventure.game.util.MyUtils;
import miniventure.game.util.Version;
import miniventure.game.world.Boundable;
import miniventure.game.world.Level;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.particle.ActionParticle.ActionType;
import miniventure.game.world.tile.Tile;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ServerPlayer extends Mob implements Player {
	
	@FunctionalInterface
	public interface StatListener { void statChanged(Stat stat, int amt); }
	
	@Nullable private StatListener statListener = null;
	public void setStatListener(@Nullable StatListener listener) { statListener = listener; }
	
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
	
	@NotNull private final ServerHands hands;
	private Inventory inventory;
	
	public ServerPlayer() {
		super("player", Stat.Health.initial);
		
		hands = new ServerHands(this);
		reset();
	}
	
	protected ServerPlayer(String[][] allData, Version version) {
		super(Arrays.copyOfRange(allData, 0, allData.length-1), version);
		String[] data = allData[allData.length-1];
		
		hands = new ServerHands(this);
		reset();
		
		stats.put(Stat.Health, getHealth());
		stats.put(Stat.Hunger, Integer.parseInt(data[0]));
		stats.put(Stat.Stamina, Integer.parseInt(data[1]));
		//stats.put(Stat.Armor, Integer.parseInt(data[2]));
		
		inventory.loadItems(MyUtils.parseLayeredString(data[2]));
		hands.loadItem(MyUtils.parseLayeredString(data[3]));
	}
	
	@Override
	public Array<String[]> save() {
		Array<String[]> data = super.save();
		
		data.add(new String[] {
			getStat(Stat.Hunger)+"",
			getStat(Stat.Stamina)+"",
			MyUtils.encodeStringArray(inventory.save()),
			MyUtils.encodeStringArray(hands.save())
		});
		
		return data;
	}
	
	// use this instead of creating a new player.
	public void reset() {
		for(Stat stat: Stat.values)
			stats.put(stat, stat.initial);
		
		super.reset();
		
		hands.reset();
		inventory = new Inventory(INV_SIZE, hands);
	}
	
	public int getStat(@NotNull Stat stat) {
		return stats.get(stat);
	}
	public int changeStat(@NotNull Stat stat, int amt) {
		int prevVal = stats.get(stat);
		stats.put(stat, Math.max(0, Math.min(stat.max, stats.get(stat) + amt)));
		if(stat == Stat.Health && amt > 0)
			regenHealth(amt);
		
		int change = stats.get(stat) - prevVal;
		
		if(change != 0 && statListener != null)
			statListener.statChanged(stat, change);
		
		return change;
	}
	
	private boolean moveRequested = false;
	private Vector2 moveDir = null;
	
	public boolean moveInDir(Vector2 direction) {
		if(moveRequested) return false;
		moveDir = direction;
		moveRequested = true;
		return true;
	}
	
	@Override
	public void update(float delta) {
		super.update(delta);
		if(moveRequested) {
			Vector2 movement = moveDir.cpy().scl(MOVE_SPEED * delta);
			
			boolean moved = super.move(movement);
			
			getStatEvo(StaminaSystem.class).isMoving = moved;
			if(moved)
				getStatEvo(HungerSystem.class).addHunger(delta * 0.35f);
			
			moveRequested = false;
		}
		
		updateStats(delta);
	}
	
	@Override
	public Integer[] saveStats() { return Stat.save(stats); }
	
	/// These two methods are ONLY to be accessed by GameScreen, so far.
	@NotNull @Override
	public Hands getHands() { return hands; }
	@Override
	public Inventory getInventory() { return inventory; }
	
	public void updateStats(float delta) {
		// update things like hunger, stamina, etc.
		for(StatEvolver evo: statEvoMap.values())
			evo.update(delta);
	}
	
	public boolean takeItem(@NotNull Item item) {
		boolean success = false;
		if(hands.addItem(item))
			success = true;
		else
			success = inventory.addItem(item, 1) == 1;
		
		if(success)
			ServerCore.getServer().sendToPlayer(this, new InventoryUpdate(this));
		
		return success;
	}
	
	@NotNull
	private Array<WorldObject> getInteractionQueue() {
		Array<WorldObject> objects = new Array<>();
		
		// get level, and don't interact if level is not found
		Level level = getWorld().getEntityLevel(this);
		if(level == null) return objects;
		
		Rectangle interactionBounds = getInteractionRect();
		
		objects.addAll(level.getOverlappingEntities(interactionBounds, this));
		Boundable.sortByDistance(objects, getCenter());
		
		Tile tile = level.getClosestTile(interactionBounds);
		if(tile != null)
			objects.add(tile);
		
		return objects;
	}
	
	public void attack() {
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
	
	public void interact() {
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
