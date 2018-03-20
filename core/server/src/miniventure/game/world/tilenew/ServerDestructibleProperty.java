package miniventure.game.world.tilenew;

import miniventure.game.world.ItemDrop;
import miniventure.game.world.ServerLevel;
import miniventure.game.world.WorldObject;

import org.jetbrains.annotations.NotNull;

public class ServerDestructibleProperty extends DestructibleProperty {
	
	public ServerDestructibleProperty(DestructibleProperty model) {
		super(model);
	}
	
	@Override
	void dropItems(ItemDrop[] drops, @NotNull Tile tile, @NotNull WorldObject attacker) {
		for(ItemDrop drop: drops)
			if(drop != null)
				((ServerLevel)tile.getLevel()).dropItems(drop, tile, attacker);
	}
	
	@Override
	public String toString() { return "Server"+super.toString(); }
}
