package miniventure.game.world.tile;

import miniventure.game.item.Item;
import miniventure.game.world.ItemDrop;
import miniventure.game.world.ServerLevel;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.particle.ActionParticle;
import miniventure.game.world.entity.particle.TextParticle;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ServerDestructibleProperty extends DestructibleProperty {
	
	public ServerDestructibleProperty(DestructibleProperty model) {
		super(model);
	}
	
	@Override
	boolean tileAttacked(@NotNull Tile tile, @NotNull WorldObject attacker, @Nullable Item item, int damage) {
		damage = getDamage(item, damage);
		
		if(damage > 0) {
			//if(tile.getServerLevel() != null)
			//	tile.getLevel().getWorld().getSender().sendData(new Hurt(attacker.getTag(), tile.getTag(), damage, Item.save(item)));
			
			// add damage particle
			tile.getLevel().addEntity(ActionParticle.ActionType.IMPACT.get(null), tile.getCenter(), true);
			
			int health = totalHealth > 1 ? new Integer(tile.getData(TilePropertyType.Attack, tileType, HEALTH_IDX)) : 1;
			health -= damage;
			if(totalHealth > 1)
				tile.getLevel().addEntity(new TextParticle(damage+""), tile.getCenter(), true);
			if(health <= 0) {
				tile.breakTile();
				for(ItemDrop drop: drops)
					if(drop != null)
						((ServerLevel)tile.getLevel()).dropItems(drop, tile, attacker);
			} else
				tile.setData(TilePropertyType.Attack, tileType, HEALTH_IDX, health+"");
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public String toString() { return "Server"+super.toString(); }
}
