package miniventure.game.network;

import java.util.EnumMap;
import java.util.HashMap;

import miniventure.game.core.GameCore;
import miniventure.game.network.GameProtocol.DatalessRequest;
import miniventure.game.network.PacketPipe.PacketPipeWriter;
import miniventure.game.util.function.ValueAction;
import miniventure.game.world.management.WorldManager;

// determines from a list of handlers which handler should parse the packet
public class PacketDispatcher {
	
	private final HashMap<Class<?>, PacketHandler<?>> handlerMap;
	private final EnumMap<DatalessRequest, ValueAction<PacketPipeWriter>> datalessHandlers;
	
	PacketDispatcher() {
		handlerMap = new HashMap<>();
		datalessHandlers = new EnumMap<>(DatalessRequest.class);
	}
	
	public <T> PacketDispatcher registerHandler(Class<T> packetClass, PacketHandler<? super T> action) {
		handlerMap.put(packetClass, action);
		return this;
	}
	public PacketDispatcher registerHandler(DatalessRequest requestType, ValueAction<PacketPipeWriter> action) {
		datalessHandlers.put(requestType, action);
		return this;
	}
	
	public void handle(WorldManager world, PacketPipeWriter connection, Object packet) {
		if(!tryHandlePacket(connection, packet))
			GameCore.error(world, "packet not handled: ("+packet.getClass().getSimpleName()+") "+packet);
	}
	
	private <T> boolean tryHandlePacket(PacketPipeWriter connection, T packet) {
		if(packet instanceof DatalessRequest) {
			ValueAction<PacketPipeWriter> action = datalessHandlers.get(packet);
			if(action == null)
				return false;
			action.act(connection);
			return true;
		}
		
		PacketHandler<T> action = (PacketHandler<T>) handlerMap.get(packet.getClass());
		if(action == null)
			return false;
		
		action.handlePacket(connection, packet);
		return true;
	}
}
