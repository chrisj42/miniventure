package miniventure.game.world.entitynew;

public abstract class InstanceData {
	
	/*
		The idea for this is that each entity has one of these, or more specifically each entity property does, housed in the entity.
		
		This is sort of replacing the String array, though classes which extend this will have to make methods to convert to and from String arrays.
	 */
	
	public abstract String[] serializeData();
	public abstract void parseData(String[] data);
	// note that the data string is going to be as long is the property type specifies.
	
	public static InstanceData NO_DATA = new InstanceData(null) {
		@Override public String[] serializeData() { return new String[0]; }
		@Override public void parseData(String[] data) {}
	};
	
	public static class FloatValue extends InstanceData {
		
		public float value;
		
		public FloatValue() { this(0); }
		public FloatValue(float val) {
			this.value = val;
		}
		
		@Override
		public String[] serializeData() { return new String[] {value+""}; }
		
		@Override
		public void parseData(String[] data) { value = Float.parseFloat(data[0]); }
	}
}
