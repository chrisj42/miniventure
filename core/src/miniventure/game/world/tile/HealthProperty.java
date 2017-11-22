package miniventure.game.world.tile;

// FIXME uhh... I have a feeling I did something wrong here... the data is stored in an array, but this is per type, not per tile. I think the getData method maybe shouldn't exist..? But... then, what would this be for..? I think I need to instead have this modify the behavior of a method in AttackableProperty, the attacked() method. Ugh, this is too much... I basically now have to have two methods, with a third accessor, to essentially do one thing... Man, this has totally backfired...

public class HealthProperty implements TileProperty {
	
	private int maxHealth;
	
	HealthProperty(int maxHealth) {
		this.maxHealth = maxHealth;
	}
	
	
	@Override
	public int getDataCount() {
		return 1;
	}
	
	@Override
	public int[] getData() {
		return new int[] {maxHealth};
	}
}
