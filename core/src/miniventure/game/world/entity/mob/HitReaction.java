package miniventure.game.world.entity.mob;

import miniventure.game.item.Item;

@FunctionalInterface
public interface HitReaction {
	void onHit(MobAi attacked, Mob attacker, Item attackItem);
	
	HitReaction FLEE = (attacked, attacker, item) -> attacked.setMovePattern(new FleePattern((self) -> attacker), 5f);
	HitReaction CHASE = (attacked, attacker, item) -> attacked.setMovePattern(new PursuePattern((self) -> attacker, Player.MOVE_SPEED), 5f);
}
