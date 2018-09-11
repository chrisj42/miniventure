package miniventure.game.ui4;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.Nullable;

public abstract class Container extends Component {
	
	@Nullable private Layout layout;
	
	private ArrayList<Component> children = new ArrayList<>();
	
	public Container() {
		
	}
	
	public void add(Component c) {
		if(c.getParent() != null)
			c.getParent().remove(c);
		children.add(c);
		c.setParent(this);
		invalidate();
	}
	
	public void remove(Component c) {
		if(children.remove(c)) {
			c.setParent(null);
			invalidate();
		}
	}
	
	public int getComponentCount() { return children.size(); }
	public Component getComponentAt(int idx) { return children.get(idx); }
	
	public void setLayout(@Nullable Layout layout) {
		this.layout = layout;
		invalidate();
	}
	
	@Nullable
	public Layout getLayout() { return layout; }
	
	@Override
	public void layout() {
		if(layout != null)
			layout.layoutContainer(this);
		for(Component c: children)
			c.validate();
	}
	
	@Override
	public void render(Batch batch) {
		super.render(batch); // background
		for(Component c: children)
			c.render(batch);
	}
	
	@Override
	protected Vector2 calcMinSize(Vector2 rt) {
		return layout == null || getSizeCache().isMinSizeSet() ? super.calcMinSize(rt) : layout.minLayoutSize(this, rt);
	}
	
	@Override
	protected Vector2 calcPreferredSize(Vector2 rt) {
		return layout == null || getSizeCache().isPreferredSizeSet() ? super.calcPreferredSize(rt) : layout.preferredLayoutSize(this, rt);
	}
	
	@Override
	protected Vector2 calcMaxSize(Vector2 rt) {
		return layout == null || getSizeCache().isMaxSizeSet() ? super.calcMaxSize(rt) : layout.maxLayoutSize(this, rt);
	}
}
