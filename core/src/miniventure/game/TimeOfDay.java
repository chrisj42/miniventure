package miniventure.game;

import com.badlogic.gdx.graphics.Color;

import org.jetbrains.annotations.NotNull;

enum TimeOfDay {
	
	Dawn(0.08f, 0.035f, new Color(1, 0.980f, 0, 0.478f)),
	
	Day(0.5f, 0.1f, new Color(0, 0, 0, 0)),
	
	Dusk(0.58f, 0.035f, new Color(0.286f, 0, 0.580f, 0.484f)),
	
	Night(1f, 0.1f, new Color(0, 0.03f, 0.278f, 0.75f));
	
	private static final float LENGTH_OF_DAY = 60 * 5; // 5 minutes is like 24 hours in-game. 
	
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
	}
	
	public float getDuration() {
		return endTime - (ordinal() == 0 ? 0 : values()[ordinal() - 1].endTime);
	}
	
	public Color[] getSkyColors(float gameTime) {
		float timeOfDay = (gameTime % LENGTH_OF_DAY) / LENGTH_OF_DAY; // now from 0 to 1.
		
		float timeLeft = endTime - timeOfDay;
		//System.out.println("getting sky colors for time " + timeOfDay + ", time left = " + timeLeft);
		if(timeLeft >= transitionDuration)
			return new Color[] {mainColor.cpy()};
		
		// there are multiple colors to blend
		float timeThroughTransition = (transitionDuration - timeLeft) / transitionDuration;
		//System.out.println("transitioning, time through = " + timeThroughTransition);
		Color mainTrans = mainColor.cpy();
		if(timeThroughTransition > 0.5f)
			mainTrans.a *= (1 - timeThroughTransition)*2;
		
		Color secondTrans = values[(ordinal()+1)%values.length].mainColor.cpy();
		//if(timeThroughTransition < 0.5f)
		secondTrans.a *= timeThroughTransition;// * 2;
		
		return new Color[] {mainTrans, secondTrans};
	}
	
	
	public String getTimeString(float gameTime) {
		float timeOfDay = (gameTime % LENGTH_OF_DAY) / LENGTH_OF_DAY; // now from 0 to 1.
		
		String str = name()+", ";
		
		float timePast = timeOfDay - (endTime - getDuration());
		int percentPast = Math.round(timePast / getDuration() * 100);
		
		str += percentPast+"% past ("+Math.round(timeOfDay*100)+"% through day)";
		
		return str;
	}
	
	
	
	public static final TimeOfDay[] values = TimeOfDay.values();
	
	@NotNull
	public static TimeOfDay getTimeOfDay(float gameTime) {
		float timeOfDay = (gameTime % LENGTH_OF_DAY) / LENGTH_OF_DAY; // now from 0 to 1.
		
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
