package miniventure.game.network;

import java.util.HashMap;

import miniventure.game.chat.InfoMessage;
import miniventure.game.core.GameCore;
import miniventure.game.core.AudioException;
import miniventure.game.core.ClientCore;
import miniventure.game.item.CraftingScreen;
import miniventure.game.network.PacketPipe.PacketPipeWriter;
import miniventure.game.screen.ChatScreen;
import miniventure.game.screen.ErrorScreen;
import miniventure.game.screen.LoadingScreen;
import miniventure.game.screen.MapScreen;
import miniventure.game.screen.MenuScreen;
import miniventure.game.util.MyUtils;
import miniventure.game.util.function.Action;
import miniventure.game.util.function.ValueAction;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.ClientEntity;
import miniventure.game.world.entity.ClientEntityRenderer;
import miniventure.game.world.entity.Entity;
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
public abstract class GameClient implements GameProtocol, PacketHandler<Object> {
	
	// private boolean suspendPackets = false;
	
	private PacketDispatcher dispatcher = new PacketDispatcher();
	
	GameClient() {
		PacketHandler<Object> playerPackets = (connection, packet) -> {
			ClientPlayer player = ClientCore.getWorld().getMainPlayer();
			if(player != null)
				player.handlePlayerPackets(packet, connection);
		};
		
		dispatcher.registerHandler(InventoryUpdate.class, playerPackets);
		dispatcher.registerHandler(SerialItem.class, playerPackets);
		dispatcher.registerHandler(PositionUpdate.class, playerPackets);
		dispatcher.registerHandler(StatUpdate.class, playerPackets);
		
		final ClientWorld world = ClientCore.getWorld();
		
		addHandler(Ping.class, this::send);
		addHandler(WorldData.class, world::init);
		addHandler(LevelInfo.class, world::setupLevel);
		addHandler(LevelChunk.class, chunk -> {
			ClientLevel level = world.getLevel();
			if(level == null) return;
			level.setTiles(chunk);
		});
		
		addHandler(DatalessRequest.Level_Loading, () -> world.setupLevel(null));
		
		addHandler(DatalessRequest.Death, () -> ClientCore.setScreen(world.getRespawnScreen()));
		
		addHandler(MapRequest.class, req -> {
			MenuScreen screen = ClientCore.getScreen();
			if(screen instanceof MapScreen)
				((MapScreen)screen).mapUpdate(req);
			// else
			// 	ClientCore.setScreen(new MapScreen(req));
		});
		
		addHandler(SpawnData.class, data -> {
			GameCore.debug("client received player");
			// ClientCore.ensureLoadingScreen("spawning player"); // I don't think this will ever last long enough to be seen
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
		});
		
		addHandler(TileUpdate.class, update -> {
			ClientLevel level = world.getLevel(update.levelId);
			if(level == null) return;
			ClientTile tile = level.getTile(update.x, update.y);
			if(tile != null)
				tile.apply(update.tileData, update.updatedType);
			else
				GameCore.error(world, "Tile found null during update; level "+level+", tile "+update.x+','+update.y);
		});
		
		addHandler(Hurt.class, hurt -> {
			WorldObject target = hurt.target.getObject(world);
			WorldObject source = hurt.source.getObject(world);
			
			// TODO later, show health bar
			
			if(target instanceof ClientEntity)
				((ClientEntity)target).hurt(source, hurt.power);
		});
		
		addHandler(ParticleAddition.class, addition -> world.registerEntity(ClientParticle.get(addition)));
		
		addHandler(EntityAddition.class, addition -> {
			if(world.getEntity(addition.eid) != null) return; // entity is already loaded.
			
			ClientPlayer player = world.getMainPlayer();
			if(player != null && addition.eid == player.getId()) return; // shouldn't pay attention to trying to set the client player like this.
			// ClientLevel level = world.getLevel(addition.positionUpdate.levelId);
			// if(level == null || (player != null && !level.equals(player.getLevel()))) return;
			
			ClientEntity e = new ClientEntity(addition);
			world.registerEntity(e);
		});
		
		addHandler(EntityRemoval.class, removal -> world.deregisterEntity(removal.eid));
		
		addHandler(EntityUpdate.class, update -> {
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
				e.setRenderer(ClientEntityRenderer.deserialize(newSprite.rendererData));
			}
		});
		
		addHandler(MobUpdate.class, update -> {
			Entity e = update.tag.getObject(world);
			if(e == null) return;
			DirectionalAnimationRenderer renderer = (DirectionalAnimationRenderer) e.getMainRenderer();
			renderer.setDirection(update.newDir);
		});
		
		addHandler(EntityValidation.class, list -> {
			// System.out.println("Client received entity validation ("+list.ids.length+" entities)");
			// first, make a list of all the entities that the client has loaded. Then, go through those given here, removing them from the list just made as you go. For each one, check the position; if it is not in a loaded chunk, then unload it. Else, if a local version doesn't exist, request it from the server. Don't use this data to actually change the positions of the entities, as there is already a system that will take care of that.
			
			ClientPlayer player = world.getMainPlayer();
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
		
		addHandler(RecipeUpdate.class, req -> {
			MenuScreen screen = ClientCore.getScreen();
			if(screen instanceof CraftingScreen)
				((CraftingScreen)screen).recipeUpdate(req);
			else // this part is needed so the hammer works
				ClientCore.setScreen(new CraftingScreen(req));
		});
		
		addHandler(RecipeStockUpdate.class, stockUpdate -> {
			MenuScreen screen = ClientCore.getScreen();
			if(screen instanceof CraftingScreen)
				((CraftingScreen)screen).refreshCraftability(stockUpdate);
		});
		
		addHandler(TabResponse.class, response -> {
			MenuScreen screen = ClientCore.getScreen();
			if(!(screen instanceof ChatScreen)) return; // ignore
			ChatScreen chat = (ChatScreen) screen;
			chat.autocomplete(response);
		});
		
		addHandler(LoginFailure.class, failure -> ClientCore.setScreen(new ErrorScreen(failure.message)));
		addHandler(SoundRequest.class, sound -> ClientCore.playSound(sound.sound));
		
		addHandler(Message.class, ClientCore::manageChatPackets);
		addHandler(InfoMessage.class, ClientCore::manageChatPackets);
		addHandler(DatalessRequest.Clear_Console, () -> ClientCore.manageChatPackets(DatalessRequest.Clear_Console));
	}
	
	private <T> void addHandler(Class<T> packetClass, ValueAction<T> action) {
		dispatcher.registerHandler(packetClass, (connection, data) -> action.act(data));
	}
	private void addHandler(DatalessRequest requestType, Action action) {
		dispatcher.registerHandler(requestType, connection -> action.act());
	}
	
	public abstract void send(Object obj);
	
	@Override
	public void handlePacket(PacketPipeWriter connection, Object packet) {
		Gdx.app.postRunnable(() -> dispatcher.handle(ClientCore.getWorld(), connection, packet));
	}
	
	public abstract void disconnect();
}
