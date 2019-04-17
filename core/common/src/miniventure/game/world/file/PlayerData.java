package miniventure.game.world.file;

import java.util.List;

public class PlayerData {
	public final String name;
	public final String passhash;
	public final String data;
	public final int levelId;
	public final boolean op;
	
	public PlayerData(String name, String passhash, String data, int levelId, boolean op) {
		this.name = name;
		this.passhash = passhash;
		this.data = data;
		this.levelId = levelId;
		this.op = op;
	}
	
	public PlayerData(PlayerData model, String data, int levelId) {
		this(model.name, model.passhash, data, levelId, model.op);
	}
	public PlayerData(PlayerData model, String data) {
		this(model, data, model.levelId);
	}
	
	public void serialize(List<String> list) {
		list.add(name);
		list.add(passhash);
		list.add(String.valueOf(levelId));
		list.add(data);
		list.add(String.valueOf(op));
	}
}
