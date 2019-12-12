package miniventure.game.world.worldgen.noise;

import java.util.Random;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import net.openhft.hashing.LongHashFunction;

public class Coherent2DNoiseFunction implements NoiseGenerator {
	
	/* Options:
		- noiseCoordsPerValue
		- interpolation degree
		- curving of raw value and curve degree and count (count can be zero)
		- curving of final value and curve degree and count (count can be zero)
	 */
	
	private LongHashFunction hashFunction;
	private final float noiseCoordsPerValue;
	private final int numRawCurves;
	private final int numFinalCurves;
	
	public Coherent2DNoiseFunction(int noiseCoordsPerValue) { this(noiseCoordsPerValue, 2); }
	public Coherent2DNoiseFunction(int noiseCoordsPerValue, int numCurves) { this(noiseCoordsPerValue, 0, numCurves); }
	public Coherent2DNoiseFunction(int noiseCoordsPerValue, int numRawCurves, int numFinalCurves) {
		this.noiseCoordsPerValue = noiseCoordsPerValue;
		this.numRawCurves = numRawCurves;
		this.numFinalCurves = numFinalCurves;
	}
	
	@Override
	public float[][] get2DNoise(GenInfo info) {
		hashFunction = LongHashFunction.xx(info.nextSeed());
		
		float[][] smoothNoise = new float[info.width][info.height];
		for(int x = 0; x < info.width; x++)
			for(int y = 0; y < info.height; y++)
				smoothNoise[x][y] = getValue(x, y);
		
		return smoothNoise;
	}
	
	private float getValue(int x, int y) {
		float totalHash = getValueRaw(x, y);
		
		totalHash = (totalHash + 1) / 2;
		
		for(int i = 0; i < numFinalCurves; i++)
			totalHash = curveCubic(totalHash);
		
		return totalHash;
	}
	private float getValueRaw(int x, int y) {
		
		float xVal = x / noiseCoordsPerValue;
		float yVal = y / noiseCoordsPerValue;
		
		int xMin = MathUtils.floor(xVal);
		int yMin = MathUtils.floor(yVal);
		int xMax = xMin+1;
		int yMax = yMin+1;
		
		float downLeftHash = getValueFromRef(xMin, yMin, xVal, yVal);
		float downRightHash = getValueFromRef(xMax, yMin, xVal, yVal);
		float upLeftHash = getValueFromRef(xMin, yMax, xVal, yVal);
		float upRightHash = getValueFromRef(xMax, yMax, xVal, yVal);
		
		float topHash = interpolate(upLeftHash, upRightHash, xVal - xMin);
		float bottomHash = interpolate(downLeftHash, downRightHash, xVal - xMin);
		
		float totalHash = interpolate(bottomHash, topHash, yVal - yMin);
		
		for(int i = 0; i < numRawCurves; i++)
			totalHash = curveCubic(totalHash);
		
		return totalHash;
	}
	
	/*
		given a tile coordinate:
			- find polygon it is in
			- return value of polygon
		
		to find polygon:
			- check all 9 surrounding polygons
			- find closest center
		
		to get value of polygon:
			- center of polygon is at tile coord shifted by random amount based on coordinate
			- value of polygon is noise value at center pos
	 */
	
	private static final Vector2 ref = new Vector2(), diff = new Vector2();
	private static final Vector2 off = new Vector2();
	private final Random rand = new Random();
	
	private float getValueFromRef(int xRef, int yRef, float x, float y) {
		/*
			- get a random unit vector from the ref point
			- get the vector of data point - ref point
			- return dot product
		 */
		
		// alternate way of getting random dir, I don't like it
		// rand.setSeed(hashFunction.hashInts(new int[] {xRef, yRef}));
		// ref.setToRandomDirection();
		
		ref.x = (int) hashFunction.hashInts(new int[] {xRef, yRef, xRef});
		ref.y = (int) hashFunction.hashInts(new int[] {yRef, xRef, xRef});
		ref.nor();
		
		diff.set(x, y);
		diff.sub(xRef, yRef);
		return ref.dot(diff);
		
		// rand.setSeed(hashFunction.hashInts(new int[] {xRef, yRef}));
		// return rand.nextFloat();
	}
	
	private Vector2 getPolygonCenter(int x, int y) {
		off.setLength(1);
		MathUtils.random.setSeed(hashFunction.hashInts(new int[] {x, y}));
		off.setToRandomDirection();
		off.add(x, y);
		// System.out.println("testing polygon at "+x+","+y+"; center="+off);
		return off;
	}
	
	public static float interpolate(float a, float b, float weight) {
		// cubic equation: -2x^3 + 3x^2
		weight = curveCubic(weight);
		return MathUtils.lerp(a, b, weight);
	}
	
	private static float curveCubic(float value) {
		return (float) (-2*Math.pow(value, 3) + 3*Math.pow(value, 2));
	}
}
