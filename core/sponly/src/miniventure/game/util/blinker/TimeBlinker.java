package miniventure.game.util.blinker;

import miniventure.game.core.GameCore;
import miniventure.game.util.MyUtils;
import miniventure.game.world.management.WorldManager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TimeBlinker implements Blinker {
	
	private final float timeOn;
	private final float timeOff;
	private final boolean startOn;
	@Nullable
	private final WorldManager timeSource;
	
	private float startTime = -1;
	private float elapTime;
	
	private TimeBlinker(float timeOn, float timeOff, boolean startOn, @Nullable WorldManager world) {
		this.timeOn = timeOn;
		this.timeOff = timeOff;
		this.startOn = startOn;
		this.timeSource = world;
	}
	
	public static TimeBlinker fromProgramTime(float timeOn, float timeOff, boolean startOn) {
		return new TimeBlinker(timeOn, timeOff, startOn, null);
	}
	
	public static TimeBlinker fromWorldTime(@NotNull WorldManager world, float timeOn, float timeOff, boolean startOn) {
		return new TimeBlinker(timeOn, timeOff, startOn, world);
	}
	
	@Override
	public void update() {
		float curTime;
		
		if(timeSource != null)
			curTime = timeSource.getGameTime();
		else
			curTime = MyUtils.getDelta(GameCore.PROGRAM_START, System.nanoTime());
		
		if(startTime < 0)
			startTime = curTime;
		
		elapTime = curTime - startTime;
	}
	
	@Override
	public boolean shouldRender() {
		float curTime = elapTime % (timeOn + timeOff);
		
		if(startOn)
			return curTime < timeOn;
		else
			return curTime >= timeOff;
	}
	
	@Override
	public void reset() {
		elapTime = 0;
		startTime = -1;
	}
}
