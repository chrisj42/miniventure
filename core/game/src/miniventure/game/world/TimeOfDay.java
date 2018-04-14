package miniventure.game.world;

import miniventure.game.util.MyUtils;

import com.badlogic.gdx.graphics.Color;

import org.jetbrains.annotations.NotNull;

public enum TimeOfDay {
	
	Morning(10.5f, 0.42f, new Color(1, 0.980f, 0, 0.478f)),
	
	Day(12+4, 0.23f, new Color(0, 0, 0, 0)),
	
	Evening(12+7, 0.5f, new Color(0.09f, 0.03f, 0.40f, 0.4f)),
	
	Dusk(12+8, 1f, new Color(0.19f, 0.03f, 0.490f, 0.55f)),
	
	Night(24+6, 0.35f, new Color(0, 0.03f, 0.278f, 0.8f)),
	
	Dawn(24+7, 1f, new Color(0.7f, 0.28f, 0.25f, 0.7f));
	
	public static final TimeOfDay[] values = TimeOfDay.values();
	public static final String[] names = MyUtils.mapArray(values, String.class, TimeOfDay::name);
	
	public static final float SECONDS_IN_DAY = 60 * 5; // in seconds; 5 minutes is like 24 hours in-game. 
	public static final float REL_START_TIME_OFFSET = 7f; // Essentially, this is when the day starts. this determines when dawn is; this is subtracted from the end time of dawn, and the rest.
	private static final float REL_DAY_DURATION = values[values.length-1].endTime;
	
	private final Color mainColor;
	private final float endTime; // relative to nothing but the other times.
	private final float transitionDuration; // relative to the duration of the time of day.
	
	// at the start of the time of day, the screen is the solid color.
	// the transition is at the end; it fades out the main color, and fades in the main color of the next time.
	
	TimeOfDay(float endTime, float transitionDuration, Color mainColor) {
		this.mainColor = mainColor;
		this.endTime = endTime - REL_START_TIME_OFFSET;
		this.transitionDuration = transitionDuration;
	}
	
	public float getStartOffsetSeconds() {
		return (endTime - getRelDuration())/REL_DAY_DURATION * SECONDS_IN_DAY;
	}
	
	private TimeOfDay getNext() { return values[(ordinal()+1) % values.length]; }
	
	private float getRelDuration() { return endTime - (ordinal() == 0 ? 0 : values()[ordinal() - 1].endTime); }
	
	private float getRelProgress(float relTime) { return 1 - ((endTime - relTime) / getRelDuration()); }
	
	private float getTransProgress(float relTime) {
		return (getRelProgress(relTime) - (1-transitionDuration)) / transitionDuration;
	}
	
	
	
	public static float getRelTime(float secondsOffset) {
		return (secondsOffset % SECONDS_IN_DAY) / SECONDS_IN_DAY * REL_DAY_DURATION;
	}
	
	public static Color[] getSkyColors(float secondsOffset) {
		TimeOfDay tod = getTimeOfDay(secondsOffset);
		float time = getRelTime(secondsOffset);
		
		float timeThroughTransition = tod.getTransProgress(time);
		
		if(timeThroughTransition <= 0)
			return new Color[] {tod.mainColor.cpy()};
		
		// there are multiple colors to blend
		Color mainTrans = tod.mainColor.cpy();
		if(timeThroughTransition > 0.5f)
			mainTrans.a *= (1 - timeThroughTransition)*2;
		
		Color secondTrans = values[(tod.ordinal()+1)%values.length].mainColor.cpy();
		secondTrans.a *= timeThroughTransition;
		
		return new Color[] {mainTrans, secondTrans};
	}
	
	
	public static String getTimeString(float secondsOffset) {
		TimeOfDay tod = getTimeOfDay(secondsOffset);
		float time = getRelTime(secondsOffset);
		
		int timePast = Math.round(tod.getRelProgress(time)*100);
		
		String str = getClockString(secondsOffset)+" - "+tod.name()+" ("+timePast+"% past)";
		
		// if transitioning, display progress
		float transProgress = tod.getTransProgress(time);
		if(transProgress > 0)
			str += " ("+tod.getNext()+" trans "+Math.round(transProgress*100)+"%)";
		
		return str;
	}
	
	public static String getClockString(float secondsOffset) {
		float time = ( (getRelTime(secondsOffset)/REL_DAY_DURATION * 24) + REL_START_TIME_OFFSET ) % 24;
		int hour = (int) time;
		int minute = (int) ((time - hour) * 60);
		return (hour<10?"0":"")+hour + ":" + (minute<10?"0":"")+minute;
	}
	
	@NotNull
	public static TimeOfDay getTimeOfDay(float secondsOffset) {
		float timeOfDay = getRelTime(secondsOffset);
		
		TimeOfDay curTime = Dawn;
		for (TimeOfDay overlay : values) {
			if (timeOfDay < overlay.endTime) {
				curTime = overlay;
				break;
			}
		}
		
		return curTime;
	}
}
