package miniventure.game.world.management;

import miniventure.game.world.entity.Entity;
import miniventure.game.world.level.Level;
import miniventure.game.world.level.RenderLevel;
import miniventure.game.world.tile.RenderTile;
import miniventure.game.world.tile.Tile;
import miniventure.game.world.worldgen.level.LevelGenerator;
import miniventure.game.world.worldgen.level.ProtoTile;

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
				LevelGenerator.MENU.generateLevel(MathUtils.random.nextLong()),
				DisplayTile::new
			);
		}
		
		@Override
		public void render(Rectangle renderSpace, SpriteBatch batch, float delta, Vector2 posOffset) {
			RenderLevel.render(getOverlappingTiles(renderSpace), new Array<>(Entity.class), batch, delta, posOffset);
		}
		
		@Override
		public void resetTileData(Tile tile) {}
	}
	
	private static class DisplayTile extends RenderTile {
		
		DisplayTile(@NotNull Level level, @NotNull ProtoTile tile) {
			this((DisplayLevel)level, tile);
		}
		DisplayTile(@NotNull DisplayLevel level, @NotNull ProtoTile tile) {
			super(level, tile);
		}
		
	}
}
