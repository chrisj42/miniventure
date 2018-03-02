package miniventure.client;

import java.io.IOException;

import miniventure.game.GameCore;
import miniventure.game.GameProtocol;
import miniventure.game.GameProtocol.LevelData;
import miniventure.game.GameProtocol.Login;
import miniventure.game.GameProtocol.SpawnData;
import miniventure.game.screen.LoadingScreen;
import miniventure.game.screen.MainMenu;
import miniventure.game.world.Chunk.ChunkData;
import miniventure.game.world.Level;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import org.jetbrains.annotations.NotNull;

public class GameClient {
	
	private Client client;
	
	public GameClient() {
		client = new Client(16384*2, 16384);
		
		GameProtocol.registerClasses(client.getKryo());
		
		addListener(new Listener() {
			@Override
			public void received (Connection connection, Object object) {
				if(object instanceof LevelData) {
					System.out.println("client received level");
					Level.addLevel(ClientCore.getWorld(), (LevelData)object);
				}
				
				if(object instanceof ChunkData) {
					//System.out.println("client received chunk");
					Level.loadChunk((ChunkData)object);
				}
				
				if(object instanceof SpawnData) {
					System.out.println("client received player");
					SpawnData data = (SpawnData) object; 
					ClientCore.getWorld().spawnPlayer(data.x, data.y);
					GameCore.setScreen(null);
				}
			}
			
			@Override
			public void disconnected(Connection connection) {
				System.err.println("client disconnected from server.");
				// TODO make ErrorScreen, which accepts a string to display and has a "back to title screen" button.
				GameCore.setScreen(new MainMenu(ClientCore.getWorld()));
				//GameCore.setScreen(new ErrorScreen("Lost connection with server."));
			}
		});
		
		client.start();
	}
	
	public void send(Object obj) { client.sendTCP(obj); }
	public void addListener(Listener listener) { client.addListener(listener); }
	
	public boolean connectToServer(@NotNull LoadingScreen loadingScreen, String host) { return connectToServer(loadingScreen, host, GameProtocol.PORT); }
	public boolean connectToServer(@NotNull LoadingScreen loadingScreen, String host, int port) {
		loadingScreen.pushMessage("connecting to server at "+host+":"+port+"...");
		
		try {
			client.connect(5000, host, port);
		} catch(IOException e) {
			e.printStackTrace();
			// error screen
			loadingScreen.editMessage("failed to connect to server.");
			return false;
		}
		
		loadingScreen.editMessage("logging in...");
		
		send(new Login("player", GameCore.VERSION));
		
		loadingScreen.editMessage("Loading world from server...");
		
		return true;
	}
}
