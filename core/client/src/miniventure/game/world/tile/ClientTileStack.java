package miniventure.game.world.tile;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import miniventure.game.client.ClientCore;
import miniventure.game.world.WorldManager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClientTileStack extends TileStack<ClientTileType> {
	
	private LinkedList<ClientTileType> opaqueStack; // opaque tiles only
	
	{
		if(opaqueStack == null)
			opaqueStack = new LinkedList<>();
	}
	
	public ClientTileStack() {
		super(ClientTileType.class, ClientCore.getWorld());
	}
	
	public ClientTileStack(ClientTileType[] types) {
		super(ClientTileType.class, types);
	}
	
	public ClientTileStack(TileTypeEnum[] enumTypes) {
		super(ClientTileType.class, ClientCore.getWorld(), enumTypes);
	}
	
	public ClientTileType getLowestVisibleLayer() { return opaqueStack.peekLast(); }
	
	@Override
	public ClientTileType[] getTypes() { return getTypes(false); }
	public ClientTileType[] getTypes(boolean includeCovered) {
		ClientTileType[] types = super.getTypes();
		if(includeCovered)
			return types;
		else
			return Arrays.copyOfRange(types, Arrays.asList(types).indexOf(opaqueStack.peekLast()), types.length);
	}
	
	@Override
	public TileTypeEnum[] getEnumTypes() { return getEnumTypes(false); }
	public TileTypeEnum[] getEnumTypes(boolean includeCovered) {
		ClientTileType[] tileTypes = getTypes(includeCovered);
		TileTypeEnum[] types = new TileTypeEnum[tileTypes.length];
		for(int i = 0; i < types.length; i++)
			types[i] = tileTypes[i].getTypeEnum();
		return types;
	}
	
	
	@Override
	void addLayer(@NotNull ClientTileType newLayer) {
		super.addLayer(newLayer);
		if(opaqueStack == null)
			opaqueStack = new LinkedList<>();
		if(newLayer.getRenderer().isOpaque())
			opaqueStack.add(newLayer);
	}
	
	@Override
	@Nullable
	ClientTileType removeLayer() {
		ClientTileType layer = super.removeLayer();
		if(layer != null && layer.getRenderer().isOpaque())
			opaqueStack.remove(layer);
		return layer;
	}
}
