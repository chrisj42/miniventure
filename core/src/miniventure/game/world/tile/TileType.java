package miniventure.game.world.tile;

import java.util.LinkedHashMap;

import miniventure.game.item.Item;
import miniventure.game.world.ItemDrop;
import miniventure.game.world.Level;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.entity.mob.Mob;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum TileType {
	
	HOLE();
	
	
	public final int dataLength; // the number of ints that this tiletype needs to store data.
	
	private final TileType coveredTile; // the tile that is set when this one is destroyed.
	
	private LinkedHashMap<Class<? extends TileProperty>, TileProperty> propertyMap = new LinkedHashMap<>();
	
	TileType(TileProperty... properties) { this(null, properties); } // null = Hole tile.
	TileType(TileType coveredTile, TileProperty... properties) {
		this.coveredTile = coveredTile;
		
		int dataLength = 0;
		for(TileProperty prop: properties) {
			dataLength += prop.getDataCount();
			propertyMap.put(prop.getClass(), prop);
		}
		
		this.dataLength = dataLength;
	}
	
	private <T extends TileProperty> T getProp(Class<T> clazz) {
		//noinspection unchecked
		return (T)propertyMap.get(clazz);
	}
	
	public void render(SpriteBatch batch, float delta, Tile tile) {
		
	}
	
	@NotNull
	public TileType getCoveredTile() {
		if(coveredTile == null) return HOLE;
		else return coveredTile;
	}
	
	
	/// ---- EVENT METHODS ----
	
	public boolean touchedBy(Entity entity) {
		TouchEventProperty touch = getProp(TouchEventProperty.class);
		if(touch != null) return touch.touchedBy(entity);
		else return true;
	}
	
	public void tileAttacked(Mob mob, Item attackItem, int damage, Tile tile) {
		AttackableProperty attackProp = getProp(AttackableProperty.class);
		if(attackProp != null) {
			
		}
	}
	
	private void tileDestroyed(@Nullable Mob cause, Tile tile) {
		Level level = tile.getLevel();
		
		DropItemProperty dProp = getProp(DropItemProperty.class);
		if(dProp != null)
			for(ItemDrop drop: dProp.drops)
				drop.dropItems(level, tile.getCenterX(), tile.getCenterY());
		
		tile.setType(getCoveredTile());
	}
	
}
