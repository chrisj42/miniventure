package miniventure.game.screen;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;

public class Box<T extends Actor> extends Container<T> {
	
	private boolean layoutEnabled = true;
	
	{
		// be the actual pref size of the actor, unless the box has a fillX/Y, in which case it is that.
		
		prefWidth(new Value() {
			@Override
			public float get(Actor context) {
				return getFillX() > 0 ? getParent().getWidth() * getFillX() : context instanceof Layout ? ((Layout)context).getPrefWidth() : context.getWidth();
			}
		});
		prefHeight(new Value() {
			@Override
			public float get(Actor context) {
				return getFillY() > 0 ? getParent().getHeight() * getFillY() : context instanceof Layout ? ((Layout)context).getPrefHeight() : context.getHeight();
			}
		});
	}
	
	public Box() {}
	public Box(T actor) {
		super(actor);
	}
	
	@Override
	public void setLayoutEnabled(boolean enabled) {
		this.layoutEnabled = enabled;
		super.setLayoutEnabled(enabled);
	}
	
	@Override
	protected void sizeChanged() {
		System.out.println("box size for "+getActor()+" changed to "+getWidth()+","+getHeight());
		super.sizeChanged();
	}
	
	@Override
	public void layout() {
		super.layout();
	}
	
	@Override
	public void validate() {
		// System.out.println("validating box for component "+getActor());
		if(!layoutEnabled) return;
		
		/*Group parent = getParent();
		float fillX = getFillX();
		float fillY = getFillY();
		if(parent != null && (fillX > 0 || fillY > 0)) {
			// System.out.println("box for component "+getActor()+" has parent, and fill parameters");
			float targetWidth = fillX > 0 ? parent.getWidth() * fillX : getWidth();
			float targetHeight = fillY > 0 ? parent.getHeight() * fillY : getHeight();
			if(targetWidth != getWidth() || targetHeight != getHeight()) {
				System.out.println("boxed actor "+getActor()+" doesn't meet fill parameters with parent "+parent+"; setting size to "+targetWidth+","+targetHeight);
				setSize(targetWidth, targetHeight); // if either are different, it will trigger an invalidate.
			}
		}*/
		
		
		super.validate();
	}
}
