package miniventure.game.world.levelgen;

import java.util.Random;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import net.openhft.hashing.LongHashFunction;

public class Coherent2DNoiseFunction {
	
	private final LongHashFunction hashFunction;
	private final float noiseCoordsPerValue;
	private final int numCurves;
	
	public Coherent2DNoiseFunction(long seed, int noiseCoordsPerValue) { this(seed, noiseCoordsPerValue, 2); }
	public Coherent2DNoiseFunction(long seed, int noiseCoordsPerValue, int numCurves) {
		hashFunction = LongHashFunction.xx(seed);
		this.noiseCoordsPerValue = noiseCoordsPerValue;
		this.numCurves = numCurves;
	}
	
	public float getValue(int x, int y) {
		float totalHash = getValueRaw(x, y, false);
		
		totalHash = (totalHash + 1) / 2;
		
		for(int i = 0; i < numCurves; i++)
			totalHash = curveCubic(totalHash);
		
		return totalHash;
	}
	public float getValueRaw(int x, int y) { return getValueRaw(x, y, true); }
	private float getValueRaw(int x, int y, boolean curve) {
		
		float xVal = x / noiseCoordsPerValue;
		float yVal = y / noiseCoordsPerValue;
		/*int xRound = MathUtils.floor(xVal);
		int yRound = MathUtils.floor(yVal);
		
		Vector2 minCenter = new Vector2();
		Vector2 minPos = new Vector2();
		float minDist = -1;
		for(int xo = -1; xo <= 1; xo++) {
			for(int yo = -1; yo <= 1; yo++) {
				Vector2 center = getPolygonCenter(xRound+xo, yRound+yo);
				float dist = center.dst(xVal, yVal);
				if(minDist < 0 || dist < minDist) {
					minDist = dist;
					minPos.set(xRound+xo, yRound+yo);
					minCenter.set(center);
				}
			}
		}
		
		// System.out.println("closest center to "+xVal+","+yVal+" is "+minCenter+" from tile pos "+minPos);
		
		return minDist;
		*/
		// rand.setSeed(hashFunction.hashInts(new int[] {MathUtils.floor(minCenter.x), MathUtils.floor(minCenter.y)}));
		// return rand.nextFloat();
		
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
		
		if(curve)
			for(int i = 0; i < numCurves; i++)
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
	
	private float interpolate(float a, float b, float weight) {
		// cubic equation: -2x^3 + 3x^2
		weight = curveCubic(weight);
		return MathUtils.lerp(a, b, weight);
	}
	
	private float curveCubic(float value) {
		return (float) (-2*Math.pow(value, 3) + 3*Math.pow(value, 2));
	}
}
