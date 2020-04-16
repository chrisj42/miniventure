package miniventure.game.network;

import miniventure.game.core.ClientCore;
import miniventure.game.network.PacketPipe.PacketListener;
import miniventure.game.network.PacketPipe.PacketPipeReader;
import miniventure.game.network.PacketPipe.PacketPipeWriter;
import miniventure.game.screen.ErrorScreen;

import com.badlogic.gdx.Gdx;

import org.jetbrains.annotations.NotNull;

public class LocalClient extends GameClient {
	
	private final PacketPipeReader fromServer;
	private final PacketPipeWriter toServer;
	private boolean selfDisconnect = false;
	
	public LocalClient(@NotNull PacketPipeReader fromServer, @NotNull PacketPipeWriter toServer) {
		this.fromServer = fromServer;
		this.toServer = toServer;
		
		fromServer.setListener(new PacketListener() {
			@Override
			public void act(Object obj) {
				handlePacket(toServer, obj);
			}
			
			@Override
			public void onDisconnect() {
				if(!selfDisconnect)
					Gdx.app.postRunnable(() -> ClientCore.setScreen(new ErrorScreen("Internal Error (server thread closed packet pipe)")));
			}
		});
	}
	
	@Override
	public void send(Object obj) { toServer.send(obj); }
	
	@Override
	public void disconnect() {
		selfDisconnect = true;
		toServer.close();
		fromServer.close();
	}
}
