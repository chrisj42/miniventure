package miniventure.game.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;

import miniventure.game.GameCore;
import miniventure.game.GameProtocol;
import miniventure.game.chat.InfoMessage;
import miniventure.game.item.CraftingScreen;
import miniventure.game.item.InventoryScreen;
import miniventure.game.screen.ChatScreen;
import miniventure.game.screen.ErrorScreen;
import miniventure.game.screen.InputScreen;
import miniventure.game.screen.LoadingScreen;
import miniventure.game.screen.MapScreen;
import miniventure.game.screen.MenuScreen;
import miniventure.game.util.MyUtils;
import miniventure.game.util.function.ValueFunction;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.ClientEntity;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.EntityRenderer;
import miniventure.game.world.entity.EntityRenderer.DirectionalAnimationRenderer;
import miniventure.game.world.entity.mob.player.ClientPlayer;
import miniventure.game.world.entity.particle.ClientParticle;
import miniventure.game.world.level.ClientLevel;
import miniventure.game.world.management.ClientWorld;
import miniventure.game.world.tile.ClientTile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.MathUtils;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.MiniventureClient;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GameClient implements GameProtocol {
	
	private MiniventureClient client;
	private String username;
	
	public GameClient() {
		client = new MiniventureClient(writeBufferSize, objectBufferSize);
		
		GameProtocol.registerClasses(client.getKryo());
		
		addListener(/*new LagListener(lagMin, lagMax, */new Listener() {
			@Override
			public void received (Connection connection, Object object) {
				ClientWorld world = ClientCore.getWorld();
				ClientPlayer player = world.getMainPlayer();
				
				if(object instanceof Ping)
					connection.sendTCP(object);
				
				if(object instanceof WorldData)
					world.init((WorldData)object);
				
				if(object instanceof LevelData) {
					GameCore.debug("client received level");
					world.setLevel((LevelData)object);
				}
				
				if(object == DatalessRequest.Death) {
					Gdx.app.postRunnable(() -> ClientCore.setScreen(world.getRespawnScreen()));
					return;
				}
				
				forPacket(object, MapRequest.class, req -> {
					MenuScreen screen = ClientCore.getScreen();
					if(screen instanceof MapScreen)
						((MapScreen)screen).mapUpdate(req);
					else if(screen == null)
						Gdx.app.postRunnable(() -> ClientCore.setScreen(new MapScreen()));
				});
				
				if(object instanceof SpawnData) {
					GameCore.debug("client received player");
					SpawnData data = (SpawnData) object;
					world.spawnPlayer(data);
					ClientCore.setScreen(null);
					if(ClientCore.PLAY_MUSIC) {
						try {
							Music song = ClientCore.setMusicTrack(Gdx.files.internal("audio/music/game.mp3"));
							song.setOnCompletionListener(music -> {
								music.stop();
								MyUtils.delay(MathUtils.random(30_000, 90_000), () -> MyUtils.tryPlayMusic(music));
							});
							MyUtils.delay(10_000, () -> MyUtils.tryPlayMusic(song));
						} catch(AudioException e) {
							System.err.println("failed to fetch game music.");
							// e.printStackTrace();
						}
					}
				}
				
				if(object instanceof TileUpdate) {
					// individual tile update
					TileUpdate update = (TileUpdate) object;
					ClientLevel level = world.getLevel(update.levelId);
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
				
				forPacket(object, ParticleAddition.class, addition -> {
					// ClientLevel level = world.getLevel(addition.positionUpdate.levelId);
					// if(level == null || (player != null && !level.equals(player.getLevel()))) return;
					
					ClientParticle e = ClientParticle.get(addition);
					world.registerEntity(e);
				});
				
				if(object instanceof EntityAddition) {
					//System.out.println("client received entity addition");
					EntityAddition addition = (EntityAddition) object;
					
					if(world.getEntity(addition.eid) != null) return; // entity is already loaded.
					
					if(player != null && addition.eid == player.getId()) return; // shouldn't pay attention to trying to set the client player like this.
					// ClientLevel level = world.getLevel(addition.positionUpdate.levelId);
					// if(level == null || (player != null && !level.equals(player.getLevel()))) return;
					
					ClientEntity e = new ClientEntity(addition);
					world.registerEntity(e);
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
						// if(!isPositionLoaded(newPos)) return;
						
						// chunk is loaded, but entity doesn't exist; ask for it from the server
						send(new EntityRequest(update.tag.eid));
						return;
					}
					
					if(newPos != null) {
						// ClientLevel level = world.getLevel(newPos.levelId);
						// if(level != null) {
							e.moveTo(newPos.x, newPos.y, newPos.z);
							//System.out.println("moved client entity "+e+", new pos: "+e.getPosition(true));
						// }
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
						System.err.println("Client received entity validation without loaded player; ignoring packet.");
						return;
					}
					ClientLevel level = world.getLevel();
					if(level == null) {
						System.err.println("Client: level is null upon receiving entity validation; ignoring packet.");
						return;
					}
					
					if(level.getLevelId() != list.levelId) {
						System.err.println("Client: level does not match entity validation level; ignoring packet.");
						return;
					}
					
					HashMap<Integer, Entity> loaded = new HashMap<>();
					for(Entity e: level.getEntities())
						if(e != player && !(e instanceof ClientParticle))
							loaded.put(e.getId(), e);
					
					for(int i = 0; i < list.ids.length; i++) {
						boolean entityLoaded = loaded.containsKey(list.ids[i]);
						
						// if the chunk is loaded, but the entity isn't, then request it from the server.
						// if the entity is loaded, but the chunk isn't, then unload it.
						// else, do nothing.
						
						if(!entityLoaded) {
							//System.out.println("client requesting entity due to validation");
							send(new EntityRequest(list.ids[i]));
						}
						
						loaded.remove(list.ids[i]);
					}
					
					// any entities that are left shouldn't still exist
					//if(loaded.size() > 0)
					//	System.out.println("client removing "+loaded.size()+" outdated entities due to validation");
					for(Entity e: loaded.values())
						e.remove();
				});
				
				forPacket(object, HotbarUpdate.class, update -> {
					if(player == null || ClientCore.getScreen() instanceof InventoryScreen)
						return;
					
					player.getHands().updateItems(update.itemStacks, update.fillPercent);
				});
				
				forPacket(object, InventoryUpdate.class, newInv -> {
					MenuScreen screen = ClientCore.getScreen();
					if(screen instanceof InventoryScreen)
						((InventoryScreen)screen).inventoryUpdate(newInv);
				});
				
				forPacket(object, InventoryAddition.class, addition -> {
					MenuScreen screen = ClientCore.getScreen();
					if(screen instanceof InventoryScreen)
						((InventoryScreen)screen).itemAdded(addition);
				});
				
				forPacket(object, RecipeRequest.class, req -> {
					MenuScreen screen = ClientCore.getScreen();
					if(screen instanceof CraftingScreen)
						((CraftingScreen)screen).recipeUpdate(req);
				});
				
				forPacket(object, RecipeStockUpdate.class, stockUpdate -> {
					MenuScreen screen = ClientCore.getScreen();
					if(screen instanceof CraftingScreen)
						((CraftingScreen)screen).refreshCraftability(stockUpdate);
				});
				
				forPacket(object, PositionUpdate.class, newPos -> {
					if(player == null) return;
					//player.updatePos(newPos);
					player.moveTo(newPos.x, newPos.y, newPos.z);
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
				
				if(object == DatalessRequest.Clear_Console)
					ClientCore.clearMessages();
			}
			
			@Override
			public void disconnected(Connection connection) {
				System.err.println("client disconnected from server.");
				Gdx.app.postRunnable(() -> ClientCore.setScreen(new ErrorScreen("Lost connection with server.")));
			}
		});//);
		
		new Thread(client) {
			@Override
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
	
	public InetSocketAddress getClientAddress() {
		return client != null ? client.getLocalAddressTCP() : null;
	}
	
	public void send(Object obj) { client.sendTCP(obj); }
	public void addListener(Listener listener) { client.addListener(listener); }
	
	// public boolean connectToServer(@NotNull LoadingScreen logger, String host, ValueFunction<Boolean> callback) { return connectToServer(logger, host, GameProtocol.PORT, callback); }
	public boolean connectToServer(@NotNull LoadingScreen logger, @Nullable ServerManager personalServer, String host, int port, ValueFunction<Boolean> callback) {
		logger.pushMessage("Connecting to "+(personalServer!=null?"private ":"")+"server at "+host+':'+port+"...");
		
		try {
			client.connect(5000, host, port);
		} catch(IOException e) {
			// e.printStackTrace();
			// error screen
			Gdx.app.postRunnable(() -> {
				ClientCore.setScreen(new ErrorScreen("Failed to connect: "+e.getMessage()));
				callback.act(false);
			});
			return false;
		}
		
		logger.editMessage("Logging in");
		if(personalServer != null) {
			// personalServer.setHost(client.getLocalAddressTCP());
			this.username = HOST;
			send(new Login(username, GameCore.VERSION));
			callback.act(true);
		}
		else {
			Gdx.app.postRunnable(() -> ClientCore.setScreen(new InputScreen("Player name:", username -> {
				this.username = username;
				send(new Login(username, GameCore.VERSION));
				
				logger.editMessage("Logging in as '"+username+"'");
				ClientCore.setScreen(logger);
				callback.act(true);
			}, () -> {
				disconnect();
				ClientCore.backToParentScreen();
				callback.act(false);
			})));
		}
		
		return true;
	}
	
	public void disconnect() { client.stop(); }
	
	/*private boolean isPositionLoaded(PositionUpdate posUpdate) {
		if(posUpdate == null) return false;
		Level level = ClientCore.getWorld().getLevel(posUpdate.levelId);
		if(level == null) return false; // entity is on unloaded level
		// Vector2 pos = new Vector2(posUpdate.x, posUpdate.y);
		// Point cpos = Chunk.getCoords(pos);
		// if(!level.isChunkLoaded(cpos))
		// 	return false; // entity is on unloaded chunk
		
		return true;
	}*/
}
