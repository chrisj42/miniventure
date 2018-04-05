package miniventure.game.world;

import com.badlogic.gdx.graphics.Color;

import org.jetbrains.annotations.NotNull;

public enum TimeOfDay {
	
	Dawn(0.08f, 0.035f, new Color(1, 0.980f, 0, 0.478f)),
	
	Day(0.5f, 0.1f, new Color(0, 0, 0, 0)),
	
	Dusk(0.58f, 0.035f, new Color(0.286f, 0, 0.580f, 0.484f)),
	
	Night(1f, 0.1f, new Color(0, 0.03f, 0.278f, 0.75f));
	
	public static final float LENGTH_OF_DAY = 60 * 5; // 5 minutes is like 24 hours in-game. 
	
	private final Color mainColor;
	private final float endTime;
	private final float transitionDuration;
	
	// at the start of the time of day, the screen is the solid color.
	// the transition is at the end; it fades out the main color, and fades in the main color of the next time.
	
	TimeOfDay(float endTime, float transitionDuration, Color mainColor) {
		this.mainColor = mainColor;
		this.endTime = endTime;
		this.transitionDuration = transitionDuration;
		//System.out.println("made time of day with color "+mainColor.r+","+mainColor.g+","+mainColor.b+","+mainColor.a);
		
		//System.out.println(this+" ends at "+endTime);
	}
	
	public float getDuration() {
		return endTime - (ordinal() == 0 ? 0 : values()[ordinal() - 1].endTime);
	}
	
	public float getDaylightOffset() { return (endTime+0.0001f - getDuration()) * LENGTH_OF_DAY; }
	
	public static final TimeOfDay[] values = TimeOfDay.values();
	
	public static float getNormTime(float dayTime) {
		return (dayTime % LENGTH_OF_DAY) / LENGTH_OF_DAY; // now from 0 to 1.
	}
	
	public static Color[] getSkyColors(float dayTime) {
		TimeOfDay tod = getTimeOfDay(dayTime);
		float time = getNormTime(dayTime);
		
		float timeLeft = tod.endTime - time;
		//System.out.println("getting sky colors for time " + timeOfDay + ", time left = " + timeLeft);
		if(timeLeft >= tod.transitionDuration)
			return new Color[] {tod.mainColor.cpy()};
		
		// there are multiple colors to blend
		float timeThroughTransition = (tod.transitionDuration - timeLeft) / tod.transitionDuration;
		//System.out.println("transitioning, time through = " + timeThroughTransition);
		Color mainTrans = tod.mainColor.cpy();
		if(timeThroughTransition > 0.5f)
			mainTrans.a *= (1 - timeThroughTransition)*2;
		
		Color secondTrans = values[(tod.ordinal()+1)%values.length].mainColor.cpy();
		//if(timeThroughTransition < 0.5f)
		secondTrans.a *= timeThroughTransition;// * 2;
		
		return new Color[] {mainTrans, secondTrans};
	}
	
	
	public static String getTimeString(float dayTime) {
		TimeOfDay tod = getTimeOfDay(dayTime);
		float time = getNormTime(dayTime);
		
		String str = tod.name()+", ";
		
		float timePast = time - (tod.endTime - tod.getDuration());
		int percentPast = Math.round(timePast / tod.getDuration() * 100);
		
		str += percentPast+"% past ("+Math.round(time*100)+"% through day)";
		
		return str;
	}
	
	public static String getClockString(float dayTime) {
		float time = getNormTime(dayTime) * 24;
		int hour = (int) time;
		int minute = (int) ((time - hour) * 60);
		return (hour<10?"0":"")+hour + ":" + (minute<10?"0":"")+minute;
	}
	
	@NotNull
	public static TimeOfDay getTimeOfDay(float dayTime) {
		float timeOfDay = getNormTime(dayTime); // now from 0 to 1.
		
		TimeOfDay curTime = Night;
		for (TimeOfDay overlay : values) {
			if (timeOfDay < overlay.endTime) {
				curTime = overlay;
				break;
			}
		}
		
		return curTime;
	}
}
