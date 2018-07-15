package miniventure.gentest;

import miniventure.game.world.levelgen.Coherent2DNoiseFunction;

import org.jetbrains.annotations.NotNull;

public class NamedNoiseFunction implements NamedObject {
	
	@NotNull private String name;
	private long seed;
	private int coordsPerValue;
	private int curveCount;
	
	private Coherent2DNoiseFunction noiseFunction;
	
	public NamedNoiseFunction(@NotNull String name) { this(name, 12); }
	public NamedNoiseFunction(@NotNull String name, int coordsPerValue) { this(name, coordsPerValue, 2); }
	public NamedNoiseFunction(@NotNull String name, int coordsPerValue, int curveCount) {
		this.name = name;
		this.coordsPerValue = coordsPerValue;
		this.curveCount = curveCount;
	}
	
	void resetFunction() { noiseFunction = null; }
	
	public Coherent2DNoiseFunction getNoiseFunction() {
		if(noiseFunction == null)
			noiseFunction = new Coherent2DNoiseFunction(seed, coordsPerValue, curveCount);
		return noiseFunction;
	}
	
	public void setSeed(long seed) { this.seed = seed; }
	
	@Override
	public void setObjectName(@NotNull String name) { this.name = name; }
	@Override @NotNull
	public String getObjectName() { return name; }
	
	public void setCoordsPerValue(int coordsPerValue) { this.coordsPerValue = coordsPerValue; }
	
	public void setCurveCount(int curveCount) { this.curveCount = curveCount; }
	
	@Override
	public String toString() { return getObjectName(); }
	
	public int getCoordsPerValue() {
		return coordsPerValue;
	}
	
	public int getCurveCount() {
		return curveCount;
	}
}
