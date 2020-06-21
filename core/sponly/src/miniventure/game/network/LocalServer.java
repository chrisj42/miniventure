package miniventure.game.network;

import java.net.InetSocketAddress;

import miniventure.game.network.PacketPipe.PacketListener;
import miniventure.game.network.PacketPipe.PacketPipeReader;
import miniventure.game.network.PacketPipe.PacketPipeWriter;
import miniventure.game.world.entity.mob.player.Player;
import miniventure.game.world.management.WorldManager;

import org.jetbrains.annotations.NotNull;

public class LocalServer extends GameServer {
	
	// there's only one player on a local server, and that player is the host.
	private static final InetSocketAddress PLAYER_ADDR = new InetSocketAddress("localhost", 0);
	
	// these go to the sole client, which is in the same JVM.
	private final PacketPipeReader in;
	private final PacketPipeWriter out;
	
	public LocalServer(@NotNull WorldManager world, PlayerData[] playerData, @NotNull PacketPipeReader serverIn, @NotNull PacketPipeWriter serverOut) {
		super(world, false, playerData);
		this.in = serverIn;
		this.out = serverOut;
		
		in.setListener(new PacketListener() {
			@Override
			public void act(Object obj) {
				world.postRunnable(() -> {
					if(obj instanceof Login)
						login(HOST, true, out);
					else
						handlePacket(out, obj);
				});
			}
			
			@Override
			public void onDisconnect() {
				world.postRunnable(() -> logout(out));
			}
		});
		
		in.start();
	}
	
	@Override
	public boolean isRunning() { return in.isOpen(); }
	
	@Override
	public boolean isHost(@NotNull InetSocketAddress address) { return address == PLAYER_ADDR; }
	
	@Override
	public InetSocketAddress getPlayerAddress(@NotNull Player player) {
		return PLAYER_ADDR;
	}
	
	@Override
	void stopServer() {
		in.close();
		out.close();
	}
}
