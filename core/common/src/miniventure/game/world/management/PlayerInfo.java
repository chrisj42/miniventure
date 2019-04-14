package miniventure.game.world.management;

public class PlayerInfo {
	final String name;
	final String passhash;
	final String data;
	final int levelId;
	
	public PlayerInfo(String name, String passhash, String data, int levelId) {
		this.name = name;
		this.passhash = passhash;
		this.data = data;
		this.levelId = levelId;
	}
}
