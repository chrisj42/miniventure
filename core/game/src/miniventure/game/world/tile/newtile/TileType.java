package miniventure.game.world.tile.newtile;

import miniventure.game.world.entity.Entity;
import miniventure.game.world.tile.newtile.data.DataMap;

// defines all tiletypes. That is, defines all tiletype *builders*.
public enum TileType {
	
	HOLE() {
		@Override
		public TileLayer get(DataMap dataMap) {
			return new TileLayer(this, dataMap) {
				@Override
				public boolean isPermeableBy(Entity e) {
					return false;
				}
			};
		}
	};
	
	TileType() {
		
	}
	
	public abstract TileLayer get(DataMap dataMap);
	
	// checks if two 
	public boolean checkMatch(DataMap tileData, TileLayer other) {
		return other.getType() == this;
	}
	
	
}
