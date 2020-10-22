package miniventure.game.world.tile;

import miniventure.game.core.ClientCore;

import org.jetbrains.annotations.Nullable;

public class ClientTileStack extends TileStack<ClientTileType> {
	
	// private LinkedList<ClientTileType> opaqueStack; // opaque tiles only
	
	// {
		//noinspection ConstantConditions
		// if(opaqueStack == null)
		// 	opaqueStack = new LinkedList<>();
	// }
	
	public ClientTileStack(Tile tile) {
		super(tile);
	}
	
	// was never used.
	// public ClientTileType getLowestVisibleLayer() { return opaqueStack.peekLast(); }
	
	/*@Override
	public List<ClientTileType> getTypes() { return getTypes(false); }
	public List<ClientTileType> getTypes(boolean includeCovered) {
		List<ClientTileType> types = super.getTypes();
		if(includeCovered)
			return types;
		else
			return types.subList(types.indexOf(stack.peekLast()), types.size());
	}
	
	@Override
	public TileTypeEnum[] getEnumTypes() { return getEnumTypes(false); }
	public TileTypeEnum[] getEnumTypes(boolean includeCovered) {
		List<ClientTileType> tileTypes = getTypes(includeCovered);
		TileTypeEnum[] types = new TileTypeEnum[tileTypes.size()];
		for(int i = 0; i < types.length; i++)
			types[i] = tileTypes.get(i).getTypeEnum();
		return types;
	}*/
	
	
	/*@Override
	void addLayer(@NotNull ClientTileType newLayer, @NotNull TileDataTag.TileDataEnumMap dataMap) {
		super.addLayer(newLayer, dataMap);
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
	}*/
}
