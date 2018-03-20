package miniventure.game.world.entity.mob;

import miniventure.game.world.entity.Entity;

import org.jetbrains.annotations.NotNull;

public class ClientMobAi extends MobAi {
	
	public ClientMobAi(@NotNull MobAi mobAi) {
		super(mobAi);
	}
	
	@Override
	protected Class<? extends Entity> getSerialClass() { return MobAi.class; }
	
	
	
}
