package miniventure.game.world.entitynew.mod;

import miniventure.game.item.Item;
import miniventure.game.world.ItemDrop;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entitynew.property.AttackListener;
import miniventure.game.world.entitynew.Entity;
import miniventure.game.world.entitynew.property.EntityProperty;
import miniventure.game.world.entitynew.property.MovementListener;
import miniventure.game.world.entitynew.property.RenderSetProperty;
import miniventure.game.world.entitynew.property.UpdateProperty;

import com.badlogic.gdx.math.Vector2;

public class Mob extends RenderSetProperty implements UpdateProperty, MovementListener, AttackListener {
	
	private static final float KNOCKBACK_SPEED = 10; // in tiles / second
	private static final float MIN_KNOCKBACK_TIME = 0.05f;
	private static final float MAX_KNOCKBACK_TIME = 0.25f;
	private static final float DAMAGE_PERCENT_FOR_MAX_PUSH = 0.2f;
	
	private static final float HURT_COOLDOWN = 0.5f; // minimum time between taking damage, in seconds; prevents a mob from getting hurt multiple times in quick succession. 
	
	private final int maxHealth;
	private final MobAnimationController animator;
	private final ItemDrop[] drops;
	
	public Mob(String spriteName, int maxHealth, ItemDrop... deathDrops) {
		super((e) -> {
			e.getType().getProp(Mob.class).getUniquePropertyClass();
		});
		
		this.maxHealth = maxHealth;
		this.drops = deathDrops;
		animator = new MobAnimationController()
	}
	
	@Override
	public boolean attackedBy(WorldObject obj, Item heldItem, int damage, Entity e) {
		return false;
	}
	
	@Override
	public void entityMoved(Entity e, Vector2 delta) {
		
	}
	
	@Override
	public void update(Entity e, float delta) {
		
	}
	
	@Override
	public Class<? extends EntityProperty> getUniquePropertyClass() { return Mob.class; }
	
	/*
		Data:
			- health
			- direction
			- knockback
	 */
	@Override
	public String[] getInitialData() {
		return new String[0];
	}
}
