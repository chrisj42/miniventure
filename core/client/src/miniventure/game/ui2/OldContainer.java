package miniventure.game.ui2;

import miniventure.game.util.RelPos;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

class OldContainer extends OldComponent {
	
	private boolean valid; // for layout.
	
	// only RelPos will be used for positioning.
	private boolean layoutVertical; // default true; if false, children are laid out horizontally.
	@NotNull private RelPos listRelPos = RelPos.CENTER;
	
	private final float spacing;
	// private boolean childFocused = false;
	private Array<OldComponent> components = new Array<>(OldComponent.class);
	// private int highlightIndex = -1; // nothing will be highlighted.
	
	public OldContainer(OldComponent... children) { this(0, children); }
	public OldContainer(float spacing, OldComponent... children) {
		this.spacing = spacing;
		for(OldComponent c: children)
			addComponent(c);
	}
	
	// this should only end up being called during updates; things would break if it were called during render.
	protected void invalidate() {
		valid = false;
		if(getParent() != null)
			getParent().invalidate();
	}
	
	@Override
	protected void render(Batch batch, float alpha, boolean focused) {
		if(!valid)
			layoutChildren();
		
		// TODO don't pass focused to super if any of the children are focused and show a highlight.
		// TODO if not in focus, then multiply the alpha by 0.8 or something, here.
		
		super.render(batch, alpha, focused);
		
		for(OldComponent c: components)
			c.render(batch, alpha, focused);
	}
	
	/*public boolean hasFocus(Component c) {
		if(highlightIndex < 0) return false;
		return highlightIndex == components.indexOf(c, true);
	}*/
	
	public void addComponent(OldComponent c) {
		if(c.getParent() != null)
			c.getParent().removeComponent(c);
		components.add(c);
		invalidate();
	}
	
	public void removeComponent(OldComponent c) {
		components.removeValue(c, true);
		invalidate();
	}
	
	@Override
	public Vector2 getPreferredSize(Vector2 v) {
		// determine from position and size of children. Do not check min and max sizes. That will be determined elsewhere and could cause recursion.
		v.set(0, 0);
		Vector2 cur = new Vector2();
		
		for(OldComponent c: components) {
			c.getPreferredSize(cur);
			
			if(layoutVertical)
				v.set(Math.max(v.x, cur.x), v.y+cur.y); // stack y
			else	
				v.set(v.x+cur.x, Math.max(v.y, cur.y)); // stack x
		}
		
		float space = components.size == 0 ? 0 : spacing * (components.size-1);
		v.add(layoutVertical?0:space, layoutVertical?space:0);
		
		return v;
	}
	
	/*@Override
	public void focus() { childFocused = false; }
	
	@Override
	public void handleInput() {
		super.handleInput();
		if(!hasFocus()) return;
		if(childFocused)
			components.get(highlightIndex).handleInput();
		else if(ClientCore.input.pressingKey(Keys.ENTER))
			childFocused = true;
		else {
			// check for up/down, left/right.
		}
	}*/
	
	/*
		Laying out a container lays out its *children*. It does not position itself according to the size of the parent because their will likely be other components in the parent as well.
		Unless the parent is null, in which case, it will set its own layout.
		
		so sizing is fairly simple. The container should already have a set size and position from when it was laid out by its parent, so we just have to align the children correctly.
		
		Let's just assume for now that the container is *just* big enough to hold all the children. Because it should be. In that case, you'll start at the edge of the axis you're laying out on. The other axis will have to be modified per component. remember to check if it fills; if so, then make it as large as this container on the off-axis.
		
		An idea was component-specific padding, but I'm just going to forget about that one (for now at least).
		
		---
		Below system is withheld for now because it's more complicated than I thought, so I only want to do it if necessary. And with context and actual examples and use cases and such.
		---
		Nvm we can't assume that because of the fill behavior.
		A little more complicated, then; it makes me feel better anyway.
		
		so, instead, fetch the preferredSize and use it as the list area.
		then go through each component, recording the fillParent axes for each one.
		distribute the extra space, from the difference between the pref container size and actual size, evenly among all components which fill along the layout axis.
			- maybe have bool to distribute equally or maintain size ratios when spacing..?
		
		for the off-axis, if all components 
	 */
	
	/*
		This is failing miserably, and I'm only barely starting!
		
		so the relpos idea kinda relies on having an anchor. which would be a little silly... sometimes. Maybe anchors could just be 0-1 points?
	 */
	
	public void layoutChildren() {
		OldContainer parent = getParent();
		
		if(parent == null) {
			Rectangle sc = new Rectangle(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			
		}
		else if(!parent.valid)
				parent.layoutChildren();
		
		float offX = 0, offY = 0;
		Vector2 curSize = new Vector2();
		Rectangle curArea = new Rectangle();
		for(OldComponent c: components) {
			layoutChild(c, curArea, curSize);
			if(layoutVertical) offY += c.getSize(curSize).y + spacing;
			else offX += c.getSize(curSize).x + spacing;
			// curArea.set(offX, offY, )
		}
		valid = true;
	}
	
	protected static void layoutChild(OldComponent child, Rectangle area, Vector2 sizeDummy) {
		// layout with relpos
		Vector2 size = child.getPreferredSize(sizeDummy);
		area = child.getRelPos().positionRect(size, area, area);
		child.setBounds(area);
	}
}
