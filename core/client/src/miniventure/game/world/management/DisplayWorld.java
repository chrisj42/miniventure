package miniventure.game.world.management;

import miniventure.game.world.entity.Entity;
import miniventure.game.world.level.Level;
import miniventure.game.world.level.LevelId;
import miniventure.game.world.level.RenderLevel;
import miniventure.game.world.tile.RenderTile;
import miniventure.game.world.tile.Tile;
import miniventure.game.world.tile.TileTypeDataMap;
import miniventure.game.world.tile.TileTypeEnum;
import miniventure.game.world.tile.TileTypeInfo;
import miniventure.game.world.worldgen.island.IslandType;
import miniventure.game.world.worldgen.island.ProtoLevel;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public class DisplayWorld extends LevelWorldManager {
	
	public DisplayWorld(boolean createLevel) {
		if(createLevel)
			setLevel(new DisplayLevel(this));
	}
	
	@Override
	public boolean worldLoaded() { return true; }
	
	@Override
	protected boolean doDaylightCycle() { return false; }
	
	@Override
	public void exitWorld() {}
	
	
	private static class DisplayLevel extends RenderLevel {
		
		DisplayLevel(DisplayWorld world) {
			super(world, LevelId.getId(0), IslandType.MENU.width, IslandType.MENU.height);
			setTiles(IslandType.MENU.generateLevel(world, MathUtils.random.nextLong(), true));
		}
		
		@Override
		protected Tile makeTile(int x, int y) {
			return new DisplayTile(this, x, y);
		}
		
		@Override
		public void render(Rectangle renderSpace, SpriteBatch batch, float delta, Vector2 posOffset) {
			RenderLevel.render(getOverlappingTiles(renderSpace), new Array<>(Entity.class), batch, delta, posOffset);
		}
	}
	
	private static class DisplayTile extends RenderTile {
		
		DisplayTile(@NotNull DisplayLevel level, int x, int y) {
			super(level, x, y);
		}
		
	}
}
