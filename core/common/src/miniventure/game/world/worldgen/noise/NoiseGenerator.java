package miniventure.game.world.worldgen.noise;

import java.util.Arrays;

import miniventure.game.util.MyUtils;
import miniventure.game.util.function.MapFunction;

import com.badlogic.gdx.math.Vector2;

// this interface creates noise maps out of nothing but size and seed.
// NoiseGenerator factory methods will consist of patterns that have no prior value.
@FunctionalInterface
public interface NoiseGenerator extends NoiseMapFetcher {
	
	float[][] get2DNoise(GenInfo info);
	
	// seed changes really only matter when you use the same map multiple times. The same seed used on different algorithms cannot be directly said to cause artifacts not seen by any other static association of two seeds. Hence, the need for seed changes is heavily tied to the nature of the generator.
	// Noise instances, that start with a map of white noise and then smooth it, are an example of a generator for which having the same seed causes a noticeable effect. Let's test that...
	default NoiseGenerator modifySeed(MapFunction<Long, Long> seedModifier) {
		return info -> get2DNoise(new GenInfo(seedModifier.get(info.seed), info.width, info.height));
	}
	
	default NoiseGenerator modify(NoiseModifier... modifiers) {
		return info -> {
			float[][] noise = get2DNoise(info);
			// float[][] cache = new float[info.width][info.height];
			for(NoiseModifier mod: modifiers) {
				// for(int i = 0; i < noise.length; i++)
				// 	System.arraycopy(noise[i], 0, cache[i], 0, noise[i].length);
				mod.modify(info, noise);
			}
			return noise;
		};
	}
	
	@Override
	default NoiseValueFetcher get(GenInfo info) {
		float[][] noise = get2DNoise(info);
		return (x, y) -> noise[x][y];
	}
	
	static NoiseGenerator get(NoiseValueFetcher fetcher) {
		return info -> {
			float[][] noise = FULL.get2DNoise(info);
			NoiseModifier.forEach(noise, (val, x, y) -> fetcher.get(x, y));
			return noise;
		};
	}
	
	NoiseGenerator FULL = info -> {
		float[][] values = new float[info.width][info.height];
		for(int x = 0; x < values.length; x++)
			Arrays.fill(values[x], 1f);
		return values;
	};
	
	MapFunction<GenInfo, Float> MIN_RADIUS = info -> Math.min(info.width/2f, info.height/2f); 
	MapFunction<GenInfo, Float> HYPOT = info -> (float) Math.hypot(info.width/2f, info.height/2f); 
	
	// value given to modifier is 1 if at max value, 0 at minimum value.
	static NoiseGenerator islandMask(float dropOffSpeed) {
		return islandMask(dropOffSpeed, MIN_RADIUS);
	}
	static NoiseGenerator islandMask(float dropOffSpeed, MapFunction<GenInfo, Float> maxDistFetcher) {
		return info -> {
			final float maxDist = maxDistFetcher.get(info);
			
			float[][] values = new float[info.width][info.height];
			
			for(int x = 0; x < values.length; x++) {
				for(int y = 0; y < values[x].length; y++) {
					float xd = Math.abs(x-info.width/2f);
					float yd = Math.abs(y-info.height/2f);
					float dist = (float) Math.hypot(xd, yd);
					values[x][y] = 1 - (float) Math.pow(Math.min(1, dist/maxDist), dropOffSpeed);
				}
			}
			
			return values;
		};
	}
	
	static NoiseGenerator islandShape(MapFunction<GenInfo, Float> baseDistFetcher, float disparity, float dropOffSpeed, boolean fillShape, NoiseGenerator borderNoise) {
		return info -> {
			final float baseDist = baseDistFetcher.get(info);
			NoiseValueFetcher border = borderNoise.get(info);
			// first, deduce the angle
			// then, use the noise value for that angle to determine how far off the actual border limit is
			// determine if the current location is within the border, and set noise value accordingly.
			final Vector2 v = new Vector2();
			return get((x, y) -> {
				v.set(x-info.width/2f, y-info.height/2f); // angle ready to fetch
				final float len = v.len();
				float borderOff;
				if(fillShape) {
					v.setAngle((int)v.angle()); // round to 360 possible values
					v.setLength(baseDist); // set length so all with this angle end up in the same place.
					borderOff = border.get(MyUtils.clamp(info.width/2+(int)v.x, 0, info.width-1), MyUtils.clamp(info.height/2+(int)v.y, 0, info.height-1)); // get border noise 
				}
				else
					borderOff = border.get(x, y); // get border noise value for this angle
				// System.out.println("border off for angle "+v.angle()+": "+borderOff);
				borderOff = MyUtils.mapFloat(borderOff, 0, 1, -1, 1); // change range
				borderOff *= disparity * baseDist; // now holds the value to change the border by
				// System.out.println("border delta: "+borderOff);
				final float borderValue = baseDist + borderOff; // final border value; this will be calc'd many times...
				// System.out.println("end border value: "+borderValue);
				return len <= borderValue ? 1 - (float)Math.pow(len/borderValue, dropOffSpeed) : 0;
			}).get2DNoise(info);
		};
	}
}
