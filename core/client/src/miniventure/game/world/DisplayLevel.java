package miniventure.game.world;

import miniventure.game.client.DisplayWorld;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.levelgen.LevelGenerator;
import miniventure.game.world.tile.Tile;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class DisplayLevel extends Level {
	
	private final LevelGenerator generator;
	//private final HashMap<Point, Chunk> chunks = new HashMap<>();
	
	public DisplayLevel(LevelGenerator generator) {
		super(new DisplayWorld(), 0, generator.worldWidth, generator.worldHeight);
		
		this.generator = generator;
		int y = 0;
		while(y * Chunk.SIZE < generator.worldHeight) {
			int x = 0;
			while(x * Chunk.SIZE < generator.worldWidth) {
				loadChunk(new Point(x, y));
				x++;
			}
			
			y++;
		}
	}
	
	@Override
	public void render(Rectangle renderSpace, SpriteBatch batch, float delta, Vector2 posOffset) {
		ClientLevel.render(getOverlappingTiles(renderSpace), new Array<>(Entity.class), batch, delta, posOffset);
	}
	
	@Override
	protected void loadChunk(Point chunkCoord) {
		loadChunk(new Chunk(chunkCoord.x, chunkCoord.y, this, generator.generateChunk(chunkCoord.x, chunkCoord.y), (x, y, types) -> new DisplayTile(this, x, y, types)));
	}
	
	@Override
	protected void unloadChunk(Point chunkCoord) {}
}
