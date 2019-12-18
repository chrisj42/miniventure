package miniventure.game.client;

import java.util.HashMap;

import miniventure.game.GameCore;
import miniventure.game.chat.InfoMessage;
import miniventure.game.item.CraftingScreen;
import miniventure.game.network.GameProtocol;
import miniventure.game.network.PacketPipe.PacketPipeWriter;
import miniventure.game.screen.ChatScreen;
import miniventure.game.screen.ErrorScreen;
import miniventure.game.screen.LoadingScreen;
import miniventure.game.screen.MapScreen;
import miniventure.game.screen.MenuScreen;
import miniventure.game.util.MyUtils;
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

import static miniventure.game.network.GameProtocol.forPacket;

/// this is a superclass for clients; the concrete class will determine if actual networking is used or not.
public abstract class GameClient implements GameProtocol {
	
	private boolean suspendPackets = false;
	
	GameClient() {}
	
	public abstract void send(Object obj);
	
	void handlePacket(Object object, PacketPipeWriter connection) {
		// GameCore.debug("Client got packet: "+object.getClass().getSimpleName());
		
		while(suspendPackets)
			MyUtils.sleep(10);
		
		ClientWorld world = ClientCore.getWorld();
		ClientPlayer player = world.getMainPlayer();
		
		if(object instanceof Ping)
			send(object);
		
		if(object instanceof WorldData)
			world.init((WorldData)object);
		
		if(object instanceof LevelData) {
			GameCore.debug("client received level");
			MenuScreen screen = ClientCore.getScreen();
			if(screen instanceof LoadingScreen) {
				LoadingScreen loader = (LoadingScreen) screen;
				world.setLevel((LevelData)object, loader);
			} else {
				suspendPackets = true; // prevents spawn data from being read and screen being set to null before the render thread has a chance to set the screen in the first place; extremely unlikely that it would ever happen given how long it takes to set the level, but I really don't even want the possibility of a race condition. Even though the screen being set to null is in another postRunnable so it's actually safe already.
				Gdx.app.postRunnable(() -> {
					LoadingScreen loader = new LoadingScreen();
					ClientCore.setScreen(loader);
					new Thread(() -> {
						Thread.yield();
						world.setLevel((LevelData)object, loader);
						suspendPackets = false;
					}).start();
				});
				return;
			}
		}
		
		if(object == DatalessRequest.Level_Loading) {
			MenuScreen screen = ClientCore.getScreen();
			if(screen instanceof LoadingScreen) {
				LoadingScreen loader = (LoadingScreen) screen;
				loader.pushMessage("Waiting for level data", true);
			}
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
			MenuScreen screen = ClientCore.getScreen();
			if(screen instanceof LoadingScreen) {
				LoadingScreen loader = (LoadingScreen) screen;
				loader.pushMessage("spawning player", true);
			}
			SpawnData data = (SpawnData) object;
			world.spawnPlayer(data, () -> { // hopefully nothing bad can come of reading packets before this is finished.
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
			});
		}
		
		if(object instanceof TileUpdate) {
			// individual tile update
			TileUpdate update = (TileUpdate) object;
			ClientLevel level = world.getLevel(update.levelId);
			if(level == null) return;
			ClientTile tile = level.getTile(update.x, update.y);
			if(tile != null)
				level.serverUpdate(tile, update.tileData, update.updatedType);
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
			// System.out.println("Client received entity validation ("+list.ids.length+" entities)");
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
				
		/*forPacket(object, HotbarUpdate.class, update -> {
			if(player == null || ClientCore.getScreen() instanceof InventoryScreen)
				return;
			
			player.getInventory().updateItems(update.itemStacks, update.fillPercent);
		});*/
		
		if(player != null)
			player.handlePlayerPackets(object, connection);
		
		forPacket(object, RecipeUpdate.class, req -> {
			MenuScreen screen = ClientCore.getScreen();
			if(screen instanceof CraftingScreen)
				((CraftingScreen)screen).recipeUpdate(req);
		});
		
		forPacket(object, RecipeStockUpdate.class, stockUpdate -> {
			MenuScreen screen = ClientCore.getScreen();
			if(screen instanceof CraftingScreen)
				((CraftingScreen)screen).refreshCraftability(stockUpdate);
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
	
	public abstract void disconnect();
}
