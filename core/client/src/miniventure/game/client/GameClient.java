package miniventure.game.client;

import javax.swing.JOptionPane;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.HashMap;

import miniventure.game.GameCore;
import miniventure.game.GameProtocol;
import miniventure.game.chat.InfoMessage;
import miniventure.game.screen.ChatScreen;
import miniventure.game.screen.ErrorScreen;
import miniventure.game.screen.MainMenu;
import miniventure.game.screen.MenuScreen;
import miniventure.game.util.MyUtils;
import miniventure.game.util.ProgressLogger;
import miniventure.game.util.function.MonoVoidFunction;
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
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.MathUtils;
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
				
				if(object instanceof WorldData) {
					world.init((WorldData)object);
				}
				
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
					if(ClientCore.PLAY_MUSIC) {
						Music song = ClientCore.setMusicTrack(Gdx.files.internal("audio/music/game.mp3"));
						song.setOnCompletionListener(music -> {
							music.stop();
							MyUtils.delay(MathUtils.random(30_000, 90_000), () -> MyUtils.tryPlayMusic(music));
						});
						MyUtils.delay(10_000, () -> MyUtils.tryPlayMusic(song));
					}
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
					//System.out.println("Client received entity validation ("+list.ids.length+" entities)");
					// first, make a list of all the entities that the client has loaded. Then, go through those given here, removing them from the list just made as you go. For each one, check the position; if it is not in a loaded chunk, then unload it. Else, if a local version doesn't exist, request it from the server. Don't use this data to actually change the positions of the entities, as there is already a system that will take care of that.
					
					if(player == null) {
						System.err.println("Client received entity validation without loaded world; ignoring packet.");
						return;
					}
					ClientLevel level = player.getLevel();
					if(level == null) {
						System.err.println("Client: player level is null upon receiving entity validation; ignoring packet.");
						return;
					}
					
					if(level.getDepth() != list.levelDepth) {
						System.err.println("Client: player level does not match entity validation level; ignoring packet.");
						return;
					}
					
					HashMap<Integer, Entity> loaded = new HashMap<>();
					for(Entity e: level.getEntities())
						if(e != player)
							loaded.put(e.getId(), e);
					
					for(int i = 0; i < list.ids.length; i++) {
						boolean chunkLoaded = level.isChunkLoaded(list.chunks[i]);
						boolean entityLoaded = loaded.containsKey(list.ids[i]);
						
						// if the chunk is loaded, but the entity isn't, then request it from the server.
						// if the entity is loaded, but the chunk isn't, then unload it.
						// else, do nothing.
						
						if(chunkLoaded && !entityLoaded) {
							//System.out.println("client requesting entity due to validation");
							send(new EntityRequest(list.ids[i]));
						}
						if(entityLoaded && !chunkLoaded) {
							//System.out.println("client unloading entity due to validation");
							loaded.get(list.ids[i]).remove();
						}
						
						loaded.remove(list.ids[i]);
					}
					
					// any entities that are left shouldn't still exist
					//if(loaded.size() > 0)
					//	System.out.println("client removing "+loaded.size()+" outdated entities due to validation");
					for(Entity e: loaded.values())
						e.remove();
				});
				
				forPacket(object, InventoryUpdate.class, newInv -> {
					if(player == null) return;
					if(newInv.inventory != null)
						player.getInventory().loadItems(newInv.inventory);
					if(newInv.hotbar != null)
						player.getHands().loadItemShortcuts(newInv.hotbar);
					// if it's null, then that means the hotbar was valid.
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
				
				forPacket(object, Message.class, ClientCore::addMessage);
				forPacket(object, InfoMessage.class, ClientCore::addMessage);
				
				forPacket(object, TabResponse.class, response -> {
					MenuScreen screen = ClientCore.getScreen();
					if(!(screen instanceof ChatScreen)) return; // ignore
					ChatScreen chat = (ChatScreen) screen;
					chat.autocomplete(response);
				});
				
				forPacket(object, LoginFailure.class, failure -> Gdx.app.postRunnable(() -> ClientCore.setScreen(new ErrorScreen(failure.message))));
				
				forPacket(object, SoundRequest.class, sound -> ClientCore.playSound(sound.sound));
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
	
	public boolean connectToServer(@NotNull ProgressLogger logger, String host, MonoVoidFunction<Boolean> callback) { return connectToServer(logger, host, GameProtocol.PORT, callback); }
	public boolean connectToServer(@NotNull ProgressLogger logger, String host, int port, MonoVoidFunction<Boolean> callback) {
		logger.pushMessage("connecting to server at "+host+":"+port+"...");
		
		try {
			client.connect(5000, host, port);
		} catch(IOException e) {
			System.err.println("(caught IOException:)");
			e.printStackTrace();
			// error screen
			Gdx.app.postRunnable(() -> {
				ClientCore.setScreen(new ErrorScreen("failed to connect to server."));
				callback.act(false);
			});
			return false;
		}
		
		logger.editMessage("logging in...");
		
		EventQueue.invokeLater(() -> {
			username = JOptionPane.showInputDialog("Specify username:", username);
			if(username == null) {
				disconnect();
				MenuScreen parent = ClientCore.getScreen() == null ? null : ClientCore.getScreen().getParent();
				Gdx.app.postRunnable(() -> {
					ClientCore.setScreen(parent == null ? new MainMenu() : parent);
					callback.act(false);
				});
				return;
			}
			
			send(new Login(username, GameCore.VERSION));
			
			logger.editMessage("Loading world from server...");
			callback.act(true);
		});
		
		return true;
	}
	
	public void disconnect() { client.stop(); }
	
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
