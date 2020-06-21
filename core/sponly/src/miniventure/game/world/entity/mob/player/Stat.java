package miniventure.game.world.entity.mob.player;

enum Stat {
	Health("heart", 10, 20),
	
	Stamina("bolt", 10, 50),
	
	Hunger("burger", 10, 20),
	
	Armor("", 10, 10, 0);
	
	public final int max, initial;
	final int iconCount;
	final String icon, outlineIcon;
	
	Stat(String icon, int iconCount, int max) { this(icon, iconCount, max, max); }
	Stat(String icon, int iconCount, int max, int initial) {
		this.max = max;
		this.initial = initial;
		this.icon = icon;
		this.outlineIcon = icon+"-outline";
		this.iconCount = iconCount;
	}
	
	public static final Stat[] values = Stat.values();
	
	/*static Integer[] save(EnumMap<Stat, Integer> stats) {
		Integer[] statValues = new Integer[Stat.values.length];
		for(Stat stat: stats.keySet())
			statValues[stat.ordinal()] = stats.get(stat);
		
		return statValues;
	}
	
	static void load(Integer[] data, EnumMap<Stat, Integer> stats) {
		for(int i = 0; i < data.length; i++) {
			if(data[i] == null) continue;
			Stat stat = Stat.values[i];
			stats.put(stat, data[i]);
		}
	}*/
}
