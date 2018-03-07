package miniventure.game.world.entitynew;

public class RenderSetProperty extends RenderProperty {
	
	@FunctionalInterface
	interface AnimationChooser {
		RenderProperty getAnimation(Entity e);
	}
	
	private final AnimationChooser animationChooser;
	
	public RenderSetProperty(AnimationChooser chooser) {
		animationChooser = chooser;
	}
	
	
	
}
