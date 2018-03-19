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
import miniventure.game.world.entity.mob.Player;
import miniventure.game.world.entity.mob.Player.PlayerUpdate;
import miniventure.game.world.tile.Tile;

import com.badlogic.gdx.math.Vector3;
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
					world.spawnPlayer((Player)Entity.deserialize(data.playerData, world, data.eid));
					ClientCore.setScreen(null);
				}
				
				if(object instanceof TileUpdate) {
					// individual tile update
					TileUpdate update = (TileUpdate) object;
					Level level = world.getLevel(update.levelDepth);
					if(level == null) return;
					Tile tile = level.getTile(update.x, update.y);
					if(tile != null)
						update.tileData.apply(tile);
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
					if(addition.levelDepth == null) return; // no point to it, really.
					Entity e = Entity.deserialize(addition.data, world, addition.eid);
					Player player = world.getMainPlayer();
					if(player != null && e.getId() == player.getId()) return; // shouldn't pay attention to trying to set the client player like this.
					Level level = world.getLevel(addition.levelDepth);
					if(level != null)
						world.setEntityLevel(e, level);
				}
				
				if(object instanceof EntityRemoval) {
					System.out.println("client received entity removal");
					int eid = ((EntityRemoval)object).eid;
					world.deregisterEntity(eid);
				}
				
				if(object instanceof Movement) {
					//System.out.println("client received entity movement");
					Movement move = (Movement) object;
					if(move.eid == world.getMainPlayer().getId()) return; // no sense to have the server move the client... at least not at this point in development.
					Entity e = world.getEntity(move.eid);
					Level level = move.levelDepth == null ? null : world.getLevel(move.levelDepth);
					if(e != null && level != null) {
						e.move(new Vector3(move.x, move.y, move.z).sub(new Vector3(e.getPosition(), e.getZ())));
						e.moveTo(level, move.x, move.y, move.z);
					}
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
