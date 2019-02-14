package miniventure.game.world;

public class DisplayWorld extends LevelManager {
	
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
	
}
