package miniventure.game.world.tile;

import miniventure.game.world.ServerLevel;
import miniventure.game.world.tile.TileType.TileTypeEnum;
import miniventure.game.world.tile.TransitionManager.TransitionMode;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;

/** @noinspection EqualsAndHashcode*/
public class ServerTile extends Tile {
	
	private final ServerLevel level;
	
	public ServerTile(@NotNull ServerLevel level, int x, int y, @NotNull TileTypeEnum[] types) {
		super(level, x, y, types, null);
		this.level = level;
	}
	
	@NotNull @Override
	public ServerLevel getLevel() { return level; }
	
	@Override
	public boolean addTile(@NotNull TileType newType) { return tileUpdate(super.addTile(newType)); }
	
	@Override
	boolean breakTile(boolean checkForExitAnim) { return tileUpdate(super.breakTile(checkForExitAnim)); }
	
	@Override
	boolean replaceTile(@NotNull TileType newType) { return tileUpdate(super.replaceTile(newType)); }
	
	private boolean tileUpdate(boolean success) {
		if(success) level.onTileUpdate(this);
		return success;
	}
	
	@Override public void render(SpriteBatch batch, float delta, Vector2 posOffset) {}
	@Override public void updateSprites() {}
	
	/*@Override
	public float update() {
		float nextUpdate = super.update();
		
		if(getType().getRenderer().transitionManager.playingExitAnimation(this)) {
			float transRemain = getType().getRenderer().transitionManager.tryFinishAnimation(this);
			if(nextUpdate == 0)
				nextUpdate = transRemain;
			else if(transRemain != 0)
				nextUpdate = Math.min(transRemain, nextUpdate);
			
			if(transRemain == 0) // animation finished!
				
		}
	}*/
	
	@Override
	public boolean equals(Object other) { return other instanceof ServerTile && super.equals(other); }
}
