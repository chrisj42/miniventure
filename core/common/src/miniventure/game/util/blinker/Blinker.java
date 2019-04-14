package miniventure.game.util.blinker;

import java.util.Arrays;

public interface Blinker {
	
	void update(float delta);
	boolean shouldRender();
	void reset();
	
	String[] serialize();
	
	static Blinker deserialize(String[] allData) {
		String type = allData[0];
		String[] data = Arrays.copyOfRange(allData, 1, allData.length);
		if(type.equals("frame"))
			return new FrameBlinker(data);
		else
			return new TimeBlinker(data);
	}
	
}
