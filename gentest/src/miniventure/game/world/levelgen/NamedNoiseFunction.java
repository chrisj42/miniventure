package miniventure.game.world.levelgen;

import java.util.Random;

import org.jetbrains.annotations.NotNull;

public class NamedNoiseFunction implements NamedObject {
	
	@NotNull private String name;
	private boolean randomSeed = true;
	private long seed;
	private int coordsPerValue;
	private int curveCount;
	
	private Coherent2DNoiseFunction noiseFunction;
	
	public NamedNoiseFunction(@NotNull String name) {
		this.name = name;
		curveCount = 2;
		coordsPerValue = 12;
	}
	
	void resetFunction() { noiseFunction = null; }
	
	public Coherent2DNoiseFunction getNoiseFunction() {
		if(noiseFunction == null) {
			System.out.println("caching noise function "+this);
			noiseFunction = new Coherent2DNoiseFunction(randomSeed ? new Random().nextLong() : seed, coordsPerValue, curveCount);
		}
		return noiseFunction;
	}
	
	public void setSeed(String seed) {
		if(seed == null || seed.length() == 0)
			randomSeed = true;
		else
			setSeed(seed.hashCode());
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
