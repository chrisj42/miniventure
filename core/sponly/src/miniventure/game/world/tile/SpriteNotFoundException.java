package miniventure.game.world.tile;

// TODO this ought to be a checked exception
class SpriteNotFoundException extends RuntimeException {
	SpriteNotFoundException(String msg) {
		super(msg);
	}
}
