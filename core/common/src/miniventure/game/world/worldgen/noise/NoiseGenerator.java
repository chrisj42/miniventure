package miniventure.game.world.worldgen.noise;

import java.util.Arrays;

import miniventure.game.util.MyUtils;
import miniventure.game.util.function.MapFunction;

import com.badlogic.gdx.math.Vector2;
import miniventure.game.world.worldgen.noise.NoiseModifier.NoiseValueMerger;

import static miniventure.game.world.worldgen.noise.NoiseModifier.FILL_VALUE_RANGE;
import static miniventure.game.world.worldgen.noise.NoiseModifier.combine;

// this interface creates noise maps out of nothing but size and seed.
// NoiseGenerator factory methods will consist of patterns that have no prior value.
@FunctionalInterface
public interface NoiseGenerator extends NoiseMapFetcher {
	
	float[][] get2DNoise(GenInfo info);
	
	
	MapFunction<GenInfo, Float> MIN_RADIUS = info -> Math.min(info.width/2f, info.height/2f);
	MapFunction<GenInfo, Float> HYPOT = info -> (float) Math.hypot(info.width/2f, info.height/2f);
	
	NoiseGenerator islandShapeOld = islandShape(MIN_RADIUS, .8f, .1f, 3, false, new Coherent2DNoiseFunction(36, 3));
	NoiseGenerator islandShape600 = /*Testing.removeHoles(Testing.removeHoles(*/NoiseGenerator.rectMask(2, .9f).modify(
		NoiseModifier.perturb(
			new Coherent2DNoiseFunction(128, 1),
			new Coherent2DNoiseFunction(48, 2),
			45
		)
	);//, .1f), .2f);
	NoiseGenerator islandShape = NoiseGenerator.rectMask(2, .8f).modify(
		NoiseModifier.perturb(
			new Coherent2DNoiseFunction(128, 1),
			new Coherent2DNoiseFunction(36, 2),
			30
		)
	);
	
	NoiseGenerator EMPTY = info -> new float[info.width][info.height];
	NoiseGenerator FULL = info -> {
		float[][] values = EMPTY.get2DNoise(info);
		for(float[] row: values)
			Arrays.fill(row, 1f);
		return values;
	};
	
	// seed changes really only matter when you use the same map multiple times. The same seed used on different algorithms cannot be directly said to cause artifacts not seen by any other static association of two seeds. Hence, the need for seed changes is heavily tied to the nature of the generator.
	// Noise instances, that start with a map of white noise and then smooth it, are an example of a generator for which having the same seed causes a noticeable effect. Let's test that...
	/*default NoiseGenerator modifySeed(MapFunction<Long, Long> seedModifier) {
		return info -> get2DNoise(new GenInfo(seedModifier.get(info.seed), info.width, info.height));
	}*/
	
	default NoiseGenerator modify(NoiseModifier... modifiers) { return modify(1, modifiers); }
	default NoiseGenerator modify(int loops, NoiseModifier... modifiers) {
		return info -> {
			float[][] noise = get2DNoise(info);
			for(int i = 0; i < loops; i++) {
				for(NoiseModifier mod : modifiers) {
					mod.modify(info, noise);
				}
			}
			return noise;
		};
	}
	
	@Override
	default NoiseValueFetcher preFetch(GenInfo info) {
		float[][] noise = get2DNoise(info);
		return (x, y) -> noise[x][y];
	}
	
	static NoiseGenerator getFrom(NoiseValueFetcher fetcher) {
		return info -> {
			float[][] noise = EMPTY.get2DNoise(info);
			NoiseModifier.forEach(noise, (val, x, y) -> fetcher.get(x, y));
			return noise;
		};
	}
	
	static NoiseGenerator getFromFetcher(NoiseMapFetcher initializer) {
		return info -> getFrom(initializer.preFetch(info)).get2DNoise(info);
	}
	
	
	// value given to modifier is 1 if at max value, 0 at minimum value.
	static NoiseGenerator circleMask(float dropOffSpeed) {
		return getFromFetcher(info -> {
			final float maxDist = MIN_RADIUS.get(info);
			return (x, y) -> {
				float xCenterDist = Math.abs(x-info.width/2f);
				float yCenterDist = Math.abs(y-info.height/2f);
				float dist = (float) Math.hypot(xCenterDist, yCenterDist);
				return 1 - (float) Math.pow(Math.min(1, dist/maxDist), dropOffSpeed);
			};
		});
	}
	
	static NoiseGenerator rectMask(float dropOffSpeed) { return rectMask(dropOffSpeed, 1); }
	static NoiseGenerator rectMask(float dropOffSpeed, float edgeProportion) {
		return getFromFetcher(info -> (x, y) -> {
			final float xRadius = info.width / 2f;
			final float yRadius = info.height / 2f;
			float xEdgeDist = Math.max(0, edgeProportion * xRadius - Math.abs(x - xRadius));
			float yEdgeDist = Math.max(0, edgeProportion * yRadius - Math.abs(y - yRadius));
			
			float distRatio = (float) xEdgeDist/xRadius * yEdgeDist/yRadius;
			return (float) Math.pow(Math.min(1, distRatio), 1/dropOffSpeed);
		});
	}
	
	static NoiseGenerator islandShape(MapFunction<GenInfo, Float> baseDistFetcher, float scale, float disparity, float dropOffSpeed, boolean fillShape, NoiseGenerator borderNoise) {
		return getFromFetcher(info -> {
			final float baseDist = baseDistFetcher.get(info) * scale;
			NoiseValueFetcher border = borderNoise.preFetch(info);
			// NoiseValueFetcher rectMask = rectMask(2).preFetch(info);
			// first, deduce the angle
			// then, use the noise value for that angle to determine how far off the actual border limit is
			// determine if the current location is within the border, and set noise value accordingly.
			final Vector2 v = new Vector2();
			return (x, y) -> {
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
				// borderOff = borderOff + rectMask.get(x, y);
				borderOff = MyUtils.mapFloat(borderOff, 0, 1, -1, 1); // change range
				borderOff *= disparity * baseDist; // now holds the value to change the border by
				// System.out.println("border delta: "+borderOff);
				final float borderValue = baseDist + borderOff; // final border value; this will be calc'd many times...
				// System.out.println("end border value: "+borderValue);
				return len <= borderValue ? 1 - (float)Math.pow(len/borderValue, dropOffSpeed) : 0;
			};
		});
	}
	
	static NoiseGenerator tunnelPattern(Coherent2DNoiseFunction func1, Coherent2DNoiseFunction func2) {
		NoiseModifier reScaler = NoiseModifier.forEach((n, x, y) -> n * 2 - 1);
		
		return func1.modify(reScaler,
			combine(func2.modify(reScaler), NoiseValueMerger.ABS_DIFF)
			,FILL_VALUE_RANGE
		);
	}
}
