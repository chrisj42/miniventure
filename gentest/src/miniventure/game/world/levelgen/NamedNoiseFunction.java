package miniventure.game.world.levelgen;

import org.jetbrains.annotations.NotNull;

public class NamedNoiseFunction implements NamedObject {
	
	@NotNull private String name;
	private long seed;
	private int coordsPerValue;
	private int curveCount;
	
	private Coherent2DNoiseFunction noiseFunction;
	
	public NamedNoiseFunction(@NotNull String name) {
		this.name = name;
		curveCount = 2;
		coordsPerValue = 1;
	}
	
	void resetFunction() { noiseFunction = null; }
	
	public Coherent2DNoiseFunction getNoiseFunction() {
		if(noiseFunction == null)
			noiseFunction = new Coherent2DNoiseFunction(seed, coordsPerValue, curveCount);
		return noiseFunction;
	}
	
	public void setSeed(String seed) { setSeed(seed.hashCode()); }
	public void setSeed(long seed) { this.seed = seed; }
	
	@Override
	public void setObjectName(@NotNull String name) { this.name = name; }
	@Override @NotNull
	public String getObjectName() { return name; }
}
