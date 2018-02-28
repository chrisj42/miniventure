package miniventure.game;

import java.io.IOException;

import miniventure.game.GameProtocol.Login;
import miniventure.game.screen.LoadingScreen;
import miniventure.game.world.Level;
import miniventure.game.world.entity.mob.Player;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import org.jetbrains.annotations.NotNull;

public class GameClient {
	
	private static class Bool {
		boolean bool;
		public Bool(boolean val) {bool = val;}
	}
	
	private Client client;
	
	public GameClient(@NotNull LoadingScreen loadingScreen, String host) { this(loadingScreen, host, GameProtocol.PORT); }
	public GameClient(@NotNull LoadingScreen loadingScreen, String host, int port) {
		loadingScreen.pushMessage("starting client...");
		
		client = new Client();
		GameProtocol.registerClasses(client.getKryo());
		client.start();
		
		final Bool loaded = new Bool(false);
		try {
			loadingScreen.editMessage("connecting to server at "+host+"...");
			client.connect(5000, host, port);
			
			client.addListener(new Listener() {
				@Override
				public void received (Connection connection, Object object) {
					if(object instanceof Level) {
						System.out.println("client received level");
						Level.resetLevels((Level)object);
					}
					
					if(object instanceof Player) {
						System.out.println("client received player");
						GameCore.getWorld().setPlayer((Player)object);
						loaded.bool = true;
						GameCore.setScreen(null);
					}
				}
				
				@Override
				public void disconnected(Connection connection) {
					System.out.println("disconnected.");
				}
			});
			
			loadingScreen.editMessage("logging in...");
			
			System.out.println("sending...");
			client.sendTCP(new Login("player", GameCore.VERSION));
			System.out.println("sent");
			
			loadingScreen.editMessage("Loading world from server...");
			
			while(!loaded.bool) {
				try {
					Thread.sleep(5);
				} catch (InterruptedException ignored) {}
				if(!client.isConnected())
					client.reconnect();
			}
			
			System.out.println("loaded!!");
		} catch (IOException e) {
			e.printStackTrace();
			// error screen
		}
		
		loadingScreen.popMessage();
	}
}
