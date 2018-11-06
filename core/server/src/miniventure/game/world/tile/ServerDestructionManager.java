package miniventure.game.world.tile;

import miniventure.game.item.Item;
import miniventure.game.server.ServerCore;
import miniventure.game.util.customenum.SerialMap;
import miniventure.game.world.ItemDrop;
import miniventure.game.world.ServerLevel;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.particle.ActionType;
import miniventure.game.world.entity.particle.ParticleData.ActionParticleData;
import miniventure.game.world.entity.particle.ParticleData.TextParticleData;
import miniventure.game.world.tile.data.TileCacheTag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ServerDestructionManager extends DestructionManager {
	
	public ServerDestructionManager(DestructionManager model) {
		super(model);
	}
	
	@Override
	boolean tileAttacked(@NotNull Tile tile, @NotNull WorldObject attacker, @Nullable Item item, int damage) {
		damage = getDamage(item, damage);
		
		if(damage > 0) {
			//if(tile.getServerLevel() != null)
			//	tile.getLevel().getWorld().getSender().sendData(new Hurt(attacker.getTag(), tile.getTag(), damage, Item.save(item)));
			
			// add damage particle
			ServerCore.getServer().broadcastParticle(new ActionParticleData(ActionType.IMPACT), tile);
			
			SerialMap dataMap = tile.getDataMap(tileType);
			
			int health = totalHealth > 1 ? dataMap.getOrDefaultAndPut(TileCacheTag.Health, totalHealth) : 1;
			health -= damage;
			if(totalHealth > 1)
				ServerCore.getServer().broadcastParticle(new TextParticleData(String.valueOf(damage)), tile);
			if(health <= 0) {
				ServerCore.getServer().playTileSound("break", tile, tileType);
				tile.breakTile();
				for(ItemDrop drop: drops)
					((ServerLevel)tile.getLevel()).dropItems(drop, tile, attacker);
			} else {
				dataMap.put(TileCacheTag.Health, health);
				ServerCore.getServer().playTileSound("hit", tile, tileType);
			}
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public String toString() { return "Server"+super.toString(); }
}
