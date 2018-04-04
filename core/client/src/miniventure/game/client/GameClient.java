package miniventure.game.client;

import javax.swing.JOptionPane;

import java.io.IOException;

import miniventure.game.GameCore;
import miniventure.game.GameProtocol;
import miniventure.game.chat.ChatMessage;
import miniventure.game.screen.ErrorScreen;
import miniventure.game.util.ProgressLogger;
import miniventure.game.world.Chunk;
import miniventure.game.world.Chunk.ChunkData;
import miniventure.game.world.ClientLevel;
import miniventure.game.world.Level;
import miniventure.game.world.Point;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.ClientEntity;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.EntityRenderer;
import miniventure.game.world.entity.EntityRenderer.DirectionalAnimationRenderer;
import miniventure.game.world.entity.mob.ClientPlayer;
import miniventure.game.world.tile.ClientTile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import org.jetbrains.annotations.NotNull;

public class GameClient implements GameProtocol {
	
	private Client client;
	private String username;
	
	public GameClient() {
		client = new Client(writeBufferSize, objectBufferSize);
		
		GameProtocol.registerClasses(client.getKryo());
		
		addListener(/*new LagListener(lagMin, lagMax, */new Listener() {
			@Override
			public void received (Connection connection, Object object) {
				ClientWorld world = ClientCore.getWorld();
				ClientPlayer player = world.getMainPlayer();
				
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
					world.spawnPlayer(data);
					ClientCore.setScreen(null);
				}
				
				if(object instanceof TileUpdate) {
					// individual tile update
					TileUpdate update = (TileUpdate) object;
					ClientLevel level = world.getLevel(update.levelDepth);
					if(level == null) return;
					ClientTile tile = level.getTile(update.x, update.y);
					if(tile != null)
						level.serverUpdate(tile, update.tileData);
				}
				
				if(object instanceof Hurt) {
					//System.out.println("client received object hurt");
					Hurt hurt = (Hurt) object;
					
					WorldObject target = hurt.target.getObject(world);
					WorldObject source = hurt.source.getObject(world);
					
					// TODO later, show health bar
					
					if(target instanceof ClientEntity)
						((ClientEntity)target).hurt(source, hurt.power);
				}
				
				if(object instanceof EntityAddition) {
					//System.out.println("client received entity addition");
					EntityAddition addition = (EntityAddition) object;
					
					if(world.getEntity(addition.eid) != null) return; // entity is already loaded.
					
					if(player != null && addition.eid == player.getId()) return; // shouldn't pay attention to trying to set the client player like this.
					ClientLevel level = world.getLevel(addition.positionUpdate.levelDepth);
					if(level == null || (player != null && !level.equals(player.getLevel()))) return;
					
					ClientEntity e = new ClientEntity(addition);
					PositionUpdate newPos = addition.positionUpdate;
					e.moveTo(level, newPos.x, newPos.y, newPos.z);
				}
				
				if(object instanceof EntityRemoval) {
					//System.out.println("client received entity removal");
					int eid = ((EntityRemoval)object).eid;
					world.deregisterEntity(eid);
				}
				
				if(object instanceof EntityUpdate) {
					EntityUpdate update = (EntityUpdate) object;
					PositionUpdate newPos = update.positionUpdate;
					SpriteUpdate newSprite = update.spriteUpdate;
					
					ClientEntity e = (ClientEntity) update.tag.getObject(world);
					//System.out.println("client received entity update for " + e + ": " + update);
					if(e == null) {
						if(!isPositionLoaded(newPos)) return;
						
						// chunk is loaded, but entity doesn't exist; ask for it from the server
						send(new EntityRequest(update.tag.eid));
						return;
					}
					
					if(newPos != null) {
						ClientLevel level = world.getLevel(newPos.levelDepth);
						if(level != null) {
							e.moveTo(level, newPos.x, newPos.y, newPos.z);
							//System.out.println("moved client entity "+e+", new pos: "+e.getPosition(true));
						}
					}
					if(newSprite != null) {
						e.setRenderer(EntityRenderer.deserialize(newSprite.rendererData));
					}
				}
				
				forPacket(object, MobUpdate.class, update -> {
					Entity e = update.tag.getObject(world);
					if(e == null) return;
					DirectionalAnimationRenderer renderer = (DirectionalAnimationRenderer) e.getMainRenderer();
					renderer.setDirection(update.newDir);
				});
				
				forPacket(object, EntityValidation.class, list -> {
					// TODO
				});
				
				forPacket(object, InventoryUpdate.class, newInv -> {
					if(player == null) return;
					player.getInventory().loadItems(newInv.inventory);
					player.getHands().loadItem(newInv.heldItemStack);
				});
				
				forPacket(object, PositionUpdate.class, newPos -> {
					if(player == null || newPos.levelDepth == null) return;
					//player.updatePos(newPos);
					player.moveTo(world.getLevel(newPos.levelDepth), newPos.x, newPos.y, newPos.z);
				});
				
				forPacket(object, StatUpdate.class, update -> {
					if(player == null) return;
					player.changeStat(update.stat, update.amount);
				});
				
				forPacket(object, Message.class, msg -> ClientCore.addMessage(msg.msg));
				forPacket(object, ChatMessage.class, ClientCore::addMessage);
				
				forPacket(object, LoginFailure.class, failure -> {
					Gdx.app.postRunnable(() -> ClientCore.setScreen(new ErrorScreen(failure.message)));
				});
			}
			
			@Override
			public void disconnected(Connection connection) {
				System.err.println("client disconnected from server.");
				Gdx.app.postRunnable(() -> ClientCore.setScreen(new ErrorScreen("Lost connection with server.")));
			}
		});//);
		
		new Thread(client) {
			public void start() {
				client.start();
				client.getUpdateThread().setUncaughtExceptionHandler((t, e) -> {
					ClientCore.exceptionHandler.uncaughtException(t, e);
					t.getThreadGroup().uncaughtException(t, e);
					client.close();
				});
			}
		}.start();
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
			Gdx.app.postRunnable(() -> ClientCore.setScreen(new ErrorScreen("failed to connect to server.")));
			return false;
		}
		
		logger.editMessage("logging in...");
		
		username = JOptionPane.showInputDialog("Specify username:", username);
		send(new Login(username, GameCore.VERSION));
		
		logger.editMessage("Loading world from server...");
		
		return true;
	}
	
	private boolean isPositionLoaded(PositionUpdate posUpdate) {
		if(posUpdate == null) return false;
		Level level = ClientCore.getWorld().getLevel(posUpdate.levelDepth);
		if(level == null) return false; // entity is on unloaded level
		Vector2 pos = new Vector2(posUpdate.x, posUpdate.y);
		Point cpos = Chunk.getCoords(pos);
		if(!level.isChunkLoaded(cpos))
			return false; // entity is on unloaded chunk
		
		return true;
	}
}
