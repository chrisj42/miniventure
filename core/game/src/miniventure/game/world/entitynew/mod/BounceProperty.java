package miniventure.game.world.entitynew.mod;

import miniventure.game.util.FrameBlinker;
import miniventure.game.world.entitynew.Entity;
import miniventure.game.world.entitynew.InstanceData;
import miniventure.game.world.entitynew.InstanceData.FloatValue;

import com.badlogic.gdx.math.Vector3;

public class BounceProperty extends LifetimeProperty {
	
	// this class takes care of bouncing and fading, with a super that makes it disappear.
	
	private static final float GRAVITY = -50;
	private static final float REBOUND_SPEED_FACTOR = 0.5f;
	
	private static final float BLINK_THRESHOLD = 0.75f; // the minimum percentage of lifetime that time has to be for the entity to start blinking, signaling that it's about to disappear.
	
	
	public BounceProperty(float lifetime) {
		
	}
	
	@Override
	public void update(Entity e, float delta) {
		
	}
	
	@Override
	public InstanceData getInitialDataObject() {
		return new BounceValue();
	}
	
	public class BounceValue extends FloatValue {
		
		private Vector3 velocity; // in tiles / second.
		private FrameBlinker blinker;
		private float lastBounceTime; // used to halt the entity once it starts bouncing a lot really quickly.
		
		public BounceValue() {
			
		}
		
		@Override
		public String[] serializeData() {
			String[] lifeData = super.serializeData();
			String[] bounceData = {};
			String[] data = new String[bounceData.length+lifeData.length];
			System.arraycopy(bounceData, 0, data, 0, bounceData.length);
			System.arraycopy(lifeData, 0, data, bounceData.length, lifeData.length);
			return data;
		}
		
		@Override
		public void parseData(String[] data) {
			int bouceDataLen = 1;
			// deal with bounce data
			
			String[] lifeData = new String[data.length-bouceDataLen];
			System.arraycopy(data, bouceDataLen, lifeData, 0, lifeData.length);
			super.parseData(lifeData);
		}
	}
	
	@Override
	public String[] getInitialData() {
		return new String[] {};
	}
}
