package miniventure.game.world.management;

import miniventure.game.world.entity.Entity;
import miniventure.game.world.level.Level;
import miniventure.game.world.level.RenderLevel;
import miniventure.game.world.tile.RenderTile;
import miniventure.game.world.tile.TileTypeEnum;
import miniventure.game.world.worldgen.island.IslandType;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public class DisplayWorld extends LevelManager {
	
	public DisplayWorld() {
		setLevel(new DisplayLevel(this));
	}
	
	@Override
	protected boolean doDaylightCycle() {
		return false;
	}
	
	@Override
	public boolean worldLoaded() {
		return true;
	}
	
	@Override
	public void exitWorld() {}
	
	
	private static class DisplayLevel extends RenderLevel {
		
		DisplayLevel(DisplayWorld world) {
			super(world, 0,
				IslandType.MENU.generateIsland(MathUtils.random.nextLong(), true),
				DisplayTile::new
			);
		}
		
		@Override
		public void render(Rectangle renderSpace, SpriteBatch batch, float delta, Vector2 posOffset) {
			RenderLevel.render(getOverlappingTiles(renderSpace), new Array<>(Entity.class), batch, delta, posOffset);
		}
	}
	
	private static class DisplayTile extends RenderTile {
		
		DisplayTile(@NotNull Level level, int x, int y, @NotNull TileTypeEnum[] types) {
			this((DisplayLevel)level, x, y, types);
		}
		DisplayTile(@NotNull DisplayLevel level, int x, int y, @NotNull TileTypeEnum[] types) {
			super(level, x, y, types, null);
		}
		
	}
}
