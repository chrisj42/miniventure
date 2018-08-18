package miniventure.game.ui;

import miniventure.game.util.RelPos;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.Nullable;

public class VerticalListLayout implements Layout {
	
	@Nullable
	private RelPos itemAlignment;
	private float spacing;
	
	public VerticalListLayout(@Nullable RelPos forceAlignment, float spacing) {
		this.itemAlignment = forceAlignment;
		this.spacing = spacing;
	}
	
	@Override
	public void layout(Container container) {
		Component[] children = container.getChildren();
		Vector2[] sizes = new Vector2[children.length];
		
		Vector2 max = new Vector2();
		
		for(int i = 0; i < sizes.length; i++) {
			sizes[i] = children[i].getSize();
			max.x = Math.max(max.x, sizes[i].x);
			max.y += sizes[i].y;
			if(i < sizes.length-1)
				max.y += spacing;
		}
		
		float yo = 0;
		for(int i = 0; i < children.length; i++) {
			Rectangle bounds = new Rectangle(0, yo, max.x, sizes[i].y);
			children[i].setPosition((itemAlignment==null?children[i].getRelPos():itemAlignment).positionRect(children[i].getSize(new Vector2(max.x, sizes[i].y)), bounds));
			yo += sizes[i].y + spacing;
		}
	}
	
	@Override
	public Vector2 getSize(Container container) {
		Component[] children = container.getChildren();
		Vector2[] sizes = new Vector2[children.length];
		
		Vector2 max = new Vector2();
		
		for(int i = 0; i < sizes.length; i++) {
			sizes[i] = children[i].getSize();
			max.x = Math.max(max.x, sizes[i].x);
			max.y += sizes[i].y;
			if(i < sizes.length-1)
				max.y += spacing;
		}
		
		return max;
	}
}