package miniventure.game.world.entity.mob;

import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.mob.player.Player;

@FunctionalInterface
public interface TouchReaction {
	
	// returns whether some arbitrary condition for touching was satisfied, regardless if the interaction ended up doing anything significant.
	void onTouch(MobAi ai, WorldObject touched);
	
	static TouchReaction attackPlayer(final int damage) {
		return (ai, touched) -> {
			if(ai.curMovePattern() instanceof PursuePattern && touched instanceof Player) {
				touched.attackedBy(ai, null, damage);
			}
		};
	};
	
}
