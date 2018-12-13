package miniventure.game.world.levelgen;

import org.jetbrains.annotations.NotNull;

public class NamedNoiseFunction {
	
	@NotNull private String name;
	private long seed;
	private int coordsPerValue;
	private int curveCount;
	
	private Coherent2DNoiseFunction noiseFunction;
	
	public NamedNoiseFunction(@NotNull NamedNoiseFunction model) {
		this(model.name, model.coordsPerValue, model.curveCount);
		this.seed = model.seed * 13;
		noiseFunction = new Coherent2DNoiseFunction(seed, coordsPerValue, curveCount);
	}
	
	public NamedNoiseFunction(@NotNull String name) { this(name, 12); }
	public NamedNoiseFunction(@NotNull String name, int coordsPerValue) { this(name, coordsPerValue, 2); }
	public NamedNoiseFunction(@NotNull String name, int coordsPerValue, int curveCount) {
		this.name = name;
		this.coordsPerValue = coordsPerValue;
		this.curveCount = curveCount;
	}
	
	public void resetFunction() { noiseFunction = null; }
	
	public Coherent2DNoiseFunction getNoiseFunction() {
		if(noiseFunction == null)
			noiseFunction = new Coherent2DNoiseFunction(seed, coordsPerValue, curveCount);
		return noiseFunction;
	}
	
	public long getSeed() { return seed; }
	public void setSeed(long seed) { this.seed = seed; }
	
	public void setName(@NotNull String name) { this.name = name; }
	@NotNull
	public String getName() { return name; }
	
	public void setCoordsPerValue(int coordsPerValue) { this.coordsPerValue = coordsPerValue; }
	
	public void setCurveCount(int curveCount) { this.curveCount = curveCount; }
	
	@Override
	public String toString() { return getName(); }
	
	public int getCoordsPerValue() {
		return coordsPerValue;
	}
	
	public int getCurveCount() {
		return curveCount;
	}
}
