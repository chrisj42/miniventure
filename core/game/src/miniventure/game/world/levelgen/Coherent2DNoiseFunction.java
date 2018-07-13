package miniventure.game.world.levelgen;

import java.util.Random;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import net.openhft.hashing.LongHashFunction;

class Coherent2DNoiseFunction {
	
	private final LongHashFunction hashFunction;
	private final float noiseCoordsPerValue;
	private final int numCurves;
	
	Coherent2DNoiseFunction(long seed, int noiseCoordsPerValue) { this(seed, noiseCoordsPerValue, 2); }
	Coherent2DNoiseFunction(long seed, int noiseCoordsPerValue, int numCurves) {
		hashFunction = LongHashFunction.xx(seed);
		this.noiseCoordsPerValue = noiseCoordsPerValue;
		this.numCurves = numCurves;
	}
	
	float getValue(int x, int y) {
		
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
		totalHash = (totalHash + 1) / 2;
		
		for(int i = 0; i < numCurves; i++)
			totalHash = curveCubic(totalHash);
		
		return totalHash;
	}
	
	private static final Vector2 ref = new Vector2(), diff = new Vector2();
	private final Random rand = new Random();
	
	private float getValueFromRef(int xRef, int yRef, float x, float y) {
		/*
			- get a random unit vector from the ref point
			- get the vector of data point - ref point
			- return dot product
		 */
		
		// alternate way of getting random dir, I don't like it
		// MathUtils.random.setSeed(hashFunction.hashInts(new int[] {xRef, yRef}));
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
	
	/*private float dotNor(Vector2 v1, Vector2 v2) {
		if(v1.)
	}*/
	
	private float interpolate(float a, float b, float weight) {
		// cubic equation: -2x^3 + 3x^2
		weight = curveCubic(weight);
		return MathUtils.lerp(a, b, weight);
	}
	
	private float curveCubic(float value) {
		return (float) (-2*Math.pow(value, 3) + 3*Math.pow(value, 2));
	}
}
