package miniventure.game.network;

import java.io.IOException;
import java.net.InetSocketAddress;

import miniventure.game.core.ClientCore;
import miniventure.game.network.PacketPipe.PacketHandler;
import miniventure.game.network.PacketPipe.PacketPipeReader;
import miniventure.game.screen.ErrorScreen;
import miniventure.game.screen.InputScreen;
import miniventure.game.screen.LoadingScreen;
import miniventure.game.util.Version;
import miniventure.game.util.function.ValueAction;

import com.badlogic.gdx.Gdx;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.FrameworkMessage.KeepAlive;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.MiniventureClient;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NetworkClient extends GameClient {
	
	private String username;
	
	private MiniventureClient client;
	
	private final PacketPipeReader packetSendQueue;
	
	public NetworkClient() {
		PacketPipe sendPipe = new PacketPipe();
		packetSendQueue = sendPipe.getPipeReader();
		packetSendQueue.setListener(new PacketHandler() {
			@Override
			public void act(Object obj) {
				send(obj);
			}
			
			@Override
			public void onDisconnect() {
				client.stop();
			}
		});
		
		client = new MiniventureClient(clientWriteBufferSize, objectBufferSize);
		client.addListener(new Listener() {
			@Override
			public void received(Connection connection, Object object) {
				if(object instanceof KeepAlive)
					return; // we don't care about these.
				
				handlePacket(object, sendPipe.getPipeWriter());
			}
			
			@Override
			public void disconnected(Connection connection) {
				System.err.println("client disconnected from server.");
				Gdx.app.postRunnable(() -> ClientCore.setScreen(new ErrorScreen("Lost connection with server.")));
				packetSendQueue.close(false);
			}
		});
		
		GameProtocol.registerClasses(client.getKryo());
		packetSendQueue.start(); // starts the loop that checks for packets to process
		
		new Thread(client) {
			@Override
			public void run() {
				Thread.currentThread().setUncaughtExceptionHandler((t, e) -> {
					ClientCore.exceptionHandler.uncaughtException(t, e);
					t.getThreadGroup().uncaughtException(t, e);
					client.close();
				});
				client.run();
			}
		}.start();
	}
	
	public InetSocketAddress getClientAddress() {
		return client.getLocalAddressTCP();
	}
	
	@Override
	public void send(Object obj) { client.sendTCP(obj); }
	
	public void connectToServer(@NotNull LoadingScreen logger, @Nullable ServerManager personalServer, String host, int port, ValueAction<Boolean> callback) {
		logger.pushMessage("Connecting to "+(personalServer != null ? "local server" : "server at "+host+':'+port));
		
		try {
			client.connect(5000, host, port);
		} catch(IOException e) {
			// e.printStackTrace();
			Gdx.app.postRunnable(() -> {
				// error screen
				ClientCore.setScreen(new ErrorScreen("Failed to connect: "+e.getMessage()));
				callback.act(false);
			});
			return;
		}
		
		logger.editMessage("Logging in");
		if(personalServer != null) {
			this.username = HOST;
			send(new Login(username, Version.CURRENT));
			callback.act(true);
		}
		else {
			Gdx.app.postRunnable(() -> ClientCore.setScreen(new InputScreen("Player name:", username -> {
				this.username = username;
				send(new Login(username, Version.CURRENT));
				
				logger.editMessage("Logging in as '"+username+'\'');
				ClientCore.setScreen(logger);
				callback.act(true);
			}, () -> {
				disconnect();
				ClientCore.backToParentScreen();
				callback.act(false);
			})));
		}
	}
	
	@Override
	public void disconnect() {
		packetSendQueue.close(true);
		// client.stop();
	}
}
