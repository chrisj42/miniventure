package miniventure.game.world.levelgen;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import net.openhft.hashing.LongHashFunction;

public class Coherent2DNoiseFunction {
	
	/*
		This will have a 2D integer noise function at its core; it will simply interpolate values in between.
		
		These will simply be the noise, nothing else, so they won't worry about the "scale" of the values they are given. Instead, I will have a wrapper class that will accept world coordinates, and return a value. It will have a scale, so that one world coordinate will equal so many noise coordinates.
		Or maybe that can be here...
	 */
	
	private final LongHashFunction hashFunction;
	private final float noiseCoordsPerValue;
	private final int numCurves;
	
	Coherent2DNoiseFunction(long seed, int noiseCoordsPerValue) { this(seed, noiseCoordsPerValue, 1); }
	Coherent2DNoiseFunction(long seed, int noiseCoordsPerValue, int numCurves) {
		hashFunction = LongHashFunction.xx(seed);
		this.noiseCoordsPerValue = noiseCoordsPerValue;
		this.numCurves = numCurves;
	}
	
	float getValue(int x, int y) {
		
		float xVal = x / noiseCoordsPerValue;
		float yVal = y / noiseCoordsPerValue;
		
		//System.out.println("getting value for "+xVal+","+yVal);
		
		/*
			Take x and y value, convert to local units... then get interpolated random value from surrounding integers.
			start by finding the surrounding integers, and then interpolate on one axis, then the next.
			
			for x and y...
				- check if the value is sitting on an integer already
				- if not, interpolate it between two 
		 */
		
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
		
		//System.out.println("returning " + totalHash);
		return totalHash;
	}
	
	private static final Vector2 ref = new Vector2(), diff = new Vector2();
	
	private float getValueFromRef(int xRef, int yRef, float x, float y) {
		/*
			- get a random unit vector from the ref point
			- get the vector of data point - ref point
			- return dot product
		 */
		// MathUtils.random.setSeed(hashFunction.hashInts(new int[] {xRef, yRef}));
		// ref.setToRandomDirection();
		ref.x = (int) hashFunction.hashInts(new int[] {xRef, yRef});
		ref.y = (int) hashFunction.hashInts(new int[] {yRef, xRef});
		ref.nor();
		
		diff.set(x, y);
		diff.sub(xRef, yRef);
		float result = ref.crs(diff);
		//if(result == 0)
		//	return ref.angle()/180-1;
		//System.out.println("hash value from ref: " + result);
		return result;
	}
	
	/*private float getHashValue(int... ints) {
		long hashSeed = hashFunction.hashInts(ints);
		MathUtils.random.setSeed(hashSeed);
		float ran = MathUtils.random.nextFloat();
		//System.out.println("random hash value: " + ran);
		return ran;
	}*/
	
	private float interpolate(float a, float b, float weight) {
		float oldWeight = weight;
		// cubic equation: -2x^3 + 3x^2
		weight = curveCubic(weight);
		float result = MathUtils.lerp(a, b, weight);
		//System.out.println("interpolated from "+a+" to "+b+", with weight transformed from "+oldWeight+" to "+weight);
		return result;
	}
	
	private float curveCubic(float value) {
		return (float) (-2*Math.pow(value, 3) + 3*Math.pow(value, 2));
	}
}
