package miniventure.game.network;

import com.esotericsoftware.kryo.Kryo;

public final class GameProtocol {
	
	static final int PORT = 8405;
	
	private GameProtocol() {}
	
	static void registerClasses(Kryo kryo) {
		kryo.register(SendObject.class);
	}
	
}
