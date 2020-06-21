package miniventure.game.network;

import java.util.HashMap;

import miniventure.game.chat.InfoMessage;
import miniventure.game.core.AudioCore;
import miniventure.game.core.AudioCore.AudioException;
import miniventure.game.core.GdxCore;
import miniventure.game.core.GameCore;
import miniventure.game.network.PacketPipe.PacketPipeWriter;
import miniventure.game.screen.ChatScreen;
import miniventure.game.screen.ErrorScreen;
import miniventure.game.screen.MapScreen;
import miniventure.game.screen.MenuScreen;
import miniventure.game.util.MyUtils;
import miniventure.game.util.function.Action;
import miniventure.game.util.function.ValueAction;
import miniventure.game.world.WorldObject;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.particle.Particle;
import miniventure.game.world.management.Level;
import miniventure.game.world.management.WorldManager;
import miniventure.game.world.tile.Tile;

import com.badlogic.gdx.Gdx;

import static miniventure.game.network.GameProtocol.forPacket;

/// this is a superclass for clients; the concrete class will determine if actual networking is used or not.
public abstract class GameClient implements GameProtocol, PacketHandler<Object> {
	
	// private boolean suspendPackets = false;
	
	private PacketDispatcher dispatcher = new PacketDispatcher();
	
	GameClient() {
		PacketHandler<Object> playerPackets = (connection, packet) -> {
			Player player = GameCore.getWorld().getMainPlayer();
			if(player != null)
				player.handlePlayerPackets(packet, connection);
		};
		
		dispatcher.registerHandler(InventoryUpdate.class, playerPackets);
		dispatcher.registerHandler(SerialItem.class, playerPackets);
		dispatcher.registerHandler(PositionUpdate.class, playerPackets);
		dispatcher.registerHandler(StatUpdate.class, playerPackets);
		
		final WorldManager world = GameCore.getWorld();
		
		addHandler(Ping.class, this::send);
		addHandler(WorldData.class, world::init);
		addHandler(LevelInfo.class, world::setupLevel);
		addHandler(LevelChunk.class, chunk -> {
			Level level = world.getLevel();
			if(level == null) return;
			level.setTiles(chunk);
		});
		
		addHandler(DatalessRequest.Level_Loading, () -> world.setupLevel(null));
		
		// addHandler(DatalessRequest.Death, () -> GdxCore.setScreen(world.getRespawnScreen()));
		
		addHandler(MapRequest.class, req -> {
			MenuScreen screen = GdxCore.getScreen();
			if(screen instanceof MapScreen)
				((MapScreen)screen).mapUpdate(req);
			// else
			// 	ClientCore.setScreen(new MapScreen(req));
		});
		
		addHandler(SpawnData.class, data -> {
			MyUtils.debug("client received player");
			// ClientCore.ensureLoadingScreen("spawning player"); // I don't think this will ever last long enough to be seen
			world.spawnPlayer(data);
			GdxCore.setScreen(null);
		});
		
		addHandler(TileUpdate.class, update -> {
			Level level = world.getLevel(update.levelId);
			if(level == null) return;
			Tile tile = level.getTile(update.x, update.y);
			if(tile != null)
				tile.apply(update.tileData, update.updatedType);
			else
				MyUtils.error(world, "Tile found null during update; level "+level+", tile "+update.x+','+update.y);
		});
		
		addHandler(Hurt.class, hurt -> {
			WorldObject target = hurt.target.getObject(world);
			WorldObject source = hurt.source.getObject(world);
			
			// TODO later, show health bar
			
			if(target instanceof Entity)
				((Entity)target).hurt(source, hurt.power);
		});
		
		addHandler(ParticleAddition.class, addition -> world.registerEntity(Particle.get(addition)));
		
		addHandler(EntityAddition.class, addition -> {
			if(world.getEntity(addition.eid) != null) return; // entity is already loaded.
			
			Player player = world.getMainPlayer();
			if(player != null && addition.eid == player.getId()) return; // shouldn't pay attention to trying to set the client player like this.
			// Level level = world.getLevel(addition.positionUpdate.levelId);
			// if(level == null || (player != null && !level.equals(player.getLevel()))) return;
			
			Entity e = new Entity(addition);
			world.registerEntity(e);
		});
		
		addHandler(EntityRemoval.class, removal -> world.deregisterEntity(removal.eid));
		
		addHandler(EntityUpdate.class, update -> {
			PositionUpdate newPos = update.positionUpdate;
			SpriteUpdate newSprite = update.spriteUpdate;
			
			Entity e = (Entity) update.tag.getObject(world);
			//System.out.println("client received entity update for " + e + ": " + update);
			if(e == null) {
				// if(!isPositionLoaded(newPos)) return;
				
				// chunk is loaded, but entity doesn't exist; ask for it from the server
				send(new EntityRequest(update.tag.eid));
				return;
			}
			
			if(newPos != null) {
				// e.moveTo(newPos.x, newPos.y, newPos.z);
			}
			if(newSprite != null) {
				// e.setRenderer(EntityRenderer.deserialize(newSprite.rendererData));
			}
		});
		
		/*addHandler(MobUpdate.class, update -> {
			Entity e = update.tag.getObject(world);
			if(e == null) return;
			DirectionalAnimationRenderer renderer = (DirectionalAnimationRenderer) e.getMainRenderer();
			renderer.setDirection(update.newDir);
		});*/
		
		addHandler(EntityValidation.class, list -> {
			// System.out.println("Client received entity validation ("+list.ids.length+" entities)");
			// first, make a list of all the entities that the client has loaded. Then, go through those given here, removing them from the list just made as you go. For each one, check the position; if it is not in a loaded chunk, then unload it. Else, if a local version doesn't exist, request it from the server. Don't use this data to actually change the positions of the entities, as there is already a system that will take care of that.
			
			Player player = world.getMainPlayer();
			if(player == null) {
				System.err.println("Client received entity validation without loaded player; ignoring packet.");
				return;
			}
			Level level = world.getLevel();
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
				if(e != player && !(e instanceof Particle))
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
		
		/*addHandler(RecipeUpdate.class, req -> {
			MenuScreen screen = GdxCore.getScreen();
			if(screen instanceof CraftingScreen)
				((CraftingScreen)screen).recipeUpdate(req);
			else // this part is needed so the hammer works
				GdxCore.setScreen(new CraftingScreen(req));
		});*/
		
		/*addHandler(RecipeStockUpdate.class, stockUpdate -> {
			MenuScreen screen = GdxCore.getScreen();
			if(screen instanceof CraftingScreen)
				((CraftingScreen)screen).refreshCraftability();
		});*/
		
		addHandler(TabResponse.class, response -> {
			MenuScreen screen = GdxCore.getScreen();
			if(!(screen instanceof ChatScreen)) return; // ignore
			ChatScreen chat = (ChatScreen) screen;
			chat.autocomplete(response);
		});
		
		addHandler(LoginFailure.class, failure -> GdxCore.setScreen(new ErrorScreen(failure.message)));
		addHandler(SoundRequest.class, sound -> AudioCore.playSound(sound.sound));
		
		addHandler(Message.class, GdxCore::manageChatPackets);
		addHandler(InfoMessage.class, GdxCore::manageChatPackets);
		addHandler(DatalessRequest.Clear_Console, () -> GdxCore.manageChatPackets(DatalessRequest.Clear_Console));
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
		Gdx.app.postRunnable(() -> dispatcher.handle(GameCore.getWorld(), connection, packet));
	}
	
	public abstract void disconnect();
}
