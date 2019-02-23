package miniventure.game.world.worldgen.noise;

public class GenInfo {
	public final long seed;
	public final int width;
	public final int height;
	
	public GenInfo(long seed, int width, int height) {
		this.seed = seed;
		this.width = width;
		this.height = height;
	}
	
	public GenInfo(GenInfo info) {
		this(info.seed, info.width, info.height);
	}
}
