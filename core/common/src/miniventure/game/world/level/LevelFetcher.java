package miniventure.game.world.level;

public interface LevelFetcher<L extends Level> {
	
	L fetchLevel(LevelId levelId);
	
}
