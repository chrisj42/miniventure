package miniventure.game.util;

public enum Interpolate {
	
	LINEAR(weight -> weight),
	
	// QUADRATIC,
	
	CUBIC(weight -> (float) (-2*Math.pow(weight, 3) + 3*Math.pow(weight, 2)));//,
	
	// QUINTIC,
	
	// EXPONENTIAL,
	
	// LOGARITHMIC;
	
	@FunctionalInterface
	interface Interpolator {
		float get(float weight);
		default float get(float a, float b, float weight) { return a + (b - a) * get(weight); }
	}
	
	private final Interpolator interpolator;
	
	Interpolate(Interpolator interpolator) {
		this.interpolator = interpolator;
	}
	
}
