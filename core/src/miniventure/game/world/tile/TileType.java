package miniventure.game.world.tile;

import java.util.HashMap;

import miniventure.game.GameCore;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import org.jetbrains.annotations.NotNull;

public enum TileType {
	
	TREE(
		SolidProperty.WALKABLE,
		DestructibleProperty.INDESTRUCTIBLE,
		InteractableProperty.NONE,
		TouchListener.DO_NOTHING
	);
	
	final SolidProperty solidProperty;
	final DestructibleProperty destructibleProperty;
	final InteractableProperty interactableProperty;
	final TouchListener touchListener;
	// TODO add animation field, and with it, a render or getTexture method.
	final TextureRegion tileSprite;
	
	private final HashMap<TileProperty, Integer> propertyDataIndexes = new HashMap<>();
	final int dataLength;
	
	TileType(@NotNull SolidProperty solidProperty, @NotNull DestructibleProperty destructibleProperty, @NotNull InteractableProperty interactableProperty, @NotNull TouchListener touchListener) {
		
		tileSprite = GameCore.tileAtlas.findRegion(name().toLowerCase());
		
		this.solidProperty = solidProperty;
		this.destructibleProperty = destructibleProperty;
		this.interactableProperty = interactableProperty;
		this.touchListener = touchListener;
		
		int curIdx = 0;
		propertyDataIndexes.put(solidProperty, curIdx);
		curIdx += solidProperty.getDataLength();
		
		propertyDataIndexes.put(destructibleProperty, curIdx);
		curIdx += destructibleProperty.getDataLength();
		
		propertyDataIndexes.put(interactableProperty, curIdx);
		curIdx += interactableProperty.getDataLength();
		
		propertyDataIndexes.put(touchListener, curIdx);
		curIdx += touchListener.getDataLength();
		
		dataLength = curIdx;
	}
	
	public int getDataIndex(TileProperty property) { return propertyDataIndexes.get(property); }
	
}
