package miniventure.game.client;

import miniventure.game.world.Level;
import miniventure.game.world.tile.ClientTile;
import miniventure.game.world.tile.Tile;
import miniventure.game.world.tile.TileType;
import miniventure.game.world.tile.TileType.TileTypeEnum;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;

public class DisplayTile extends Tile {
	
	protected DisplayTile(@NotNull DisplayLevel level, int x, int y, @NotNull TileTypeEnum[] types) {
		super(level, x, y, types, null);
	}
	
	@Override
	public void render(SpriteBatch batch, float delta, Vector2 posOffset) {
		ClientTile.render(this, batch, delta, posOffset);
	}
	
}
