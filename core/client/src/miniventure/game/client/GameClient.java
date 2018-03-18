package miniventure.game.client;

import java.io.IOException;

import miniventure.game.GameCore;
import miniventure.game.GameProtocol;
import miniventure.game.item.Item;
import miniventure.game.screen.MainMenu;
import miniventure.game.util.ProgressLogger;
import miniventure.game.world.Chunk.ChunkData;
import miniventure.game.world.Level;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.Player.PlayerUpdate;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import org.jetbrains.annotations.NotNull;

public class GameClient implements GameProtocol {
	
	private Client client;
	
	public GameClient() {
		client = new Client(writeBufferSize, objectBufferSize);
		
		GameProtocol.registerClasses(client.getKryo());
		
		addListener(new Listener() {
			@Override
			public void received (Connection connection, Object object) {
				ClientWorld world = ClientCore.getWorld();
				
				if(object instanceof LevelData) {
					System.out.println("client received level");
					world.addLevel((LevelData)object);
				}
				
				if(object instanceof ChunkData) {
					//System.out.println("client received chunk");
					world.loadChunk((ChunkData)object);
				}
				
				if(object instanceof SpawnData) {
					System.out.println("client received player");
					SpawnData data = (SpawnData) object; 
					world.spawnPlayer(data.x, data.y, data.eid);
					ClientCore.setScreen(null);
				}
				
				if(object instanceof PlayerUpdate) {
					System.out.println("client received update");
					((PlayerUpdate)object).apply(world.getMainPlayer());
				}
				
				if(object instanceof Hurt) {
					System.out.println("client received object hurt");
					Hurt hurt = (Hurt) object;
					
					WorldObject target = hurt.target.getObject(world);
					WorldObject source = hurt.source.getObject(world);
					Item attackItem = Item.load(hurt.attackItem);
					target.attackedBy(source, attackItem, hurt.damage);
				}
				
				if(object instanceof EntityAddition) {
					System.out.println("client received entity addition");
					EntityAddition addition = (EntityAddition) object;
					Entity.deserialize(addition.data, world, addition.eid);
				}
				
				if(object instanceof EntityRemoval) {
					System.out.println("client received entity removal");
					int eid = ((EntityRemoval)object).eid;
					world.deregisterEntity(eid);
				}
				
				if(object instanceof Movement) {
					System.out.println("client received entity movement");
					Movement move = (Movement) object;
					if(move.eid == world.getMainPlayer().getId()) return;
					Entity e = world.getEntity(move.eid);
					Level level = move.levelDepth == null ? null : world.getLevel(move.levelDepth);
					if(e != null && level != null)
						e.moveTo(level, move.x, move.y, move.z);
				}
			}
			
			@Override
			public void disconnected(Connection connection) {
				System.err.println("client disconnected from server.");
				// TODO make ErrorScreen, which accepts a string to display and has a "back to title screen" button.
				ClientCore.setScreen(new MainMenu(ClientCore.getWorld()));
				//GameCore.setScreen(new ErrorScreen("Lost connection with server."));
			}
		});
		
		client.start();
	}
	
	public void send(Object obj) { client.sendTCP(obj); }
	public void addListener(Listener listener) { client.addListener(listener); }
	
	public boolean connectToServer(@NotNull ProgressLogger logger, String host) { return connectToServer(logger, host, GameProtocol.PORT); }
	public boolean connectToServer(@NotNull ProgressLogger logger, String host, int port) {
		logger.pushMessage("connecting to server at "+host+":"+port+"...");
		
		try {
			client.connect(5000, host, port);
		} catch(IOException e) {
			e.printStackTrace();
			// error screen
			logger.editMessage("failed to connect to server.");
			return false;
		}
		
		logger.editMessage("logging in...");
		
		send(new Login("player", GameCore.VERSION));
		
		logger.editMessage("Loading world from server...");
		
		return true;
	}
	
	@Override
	public void sendData(Object obj) { send(obj); }
}
