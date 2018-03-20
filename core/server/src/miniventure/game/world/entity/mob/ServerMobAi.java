package miniventure.game.world.entity.mob;

import miniventure.game.world.ItemDrop;
import miniventure.game.world.WorldManager;
import miniventure.game.world.entity.Entity;

import org.jetbrains.annotations.NotNull;

public class ServerMobAi extends MobAi {
	
	public ServerMobAi(@NotNull MobAi model) {
		super(model);
	}
	
	@Override
	protected Class<? extends Entity> getSerialClass() { return MobAi.class; }
	
	@Override
	public void remove() {
		ServerLevel level = getServerLevel();
		if(level != null)
			for (ItemDrop drop: itemDrops)
				drop.dropItems(level, this, null);
		
		super.remove();
	}
}
