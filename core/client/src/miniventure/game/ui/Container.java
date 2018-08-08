package miniventure.game.ui;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;

public final class Container extends Component {
	
	private ArrayList<Component> children;
	private Layout layout;
	
	public Container() { this(null); }
	public Container(Layout layout) { this(0, 0, layout); }
	public Container(float x, float y) { this(x, y, null); }
	public Container(float x, float y, Layout layout) {
		this.layout = layout;
		children = new ArrayList<>();
		setPosition(x, y);
	}
	
	protected Component[] getChildren() { return getChildren(true); }
	protected Component[] getChildren(boolean backwards) {
		if(!backwards) return children.toArray(new Component[0]);
		Component[] backChildren = new Component[children.size()];
		for(int i = 0; i < children.size(); i++)
			backChildren[i] = children.get(children.size()-i-1);
		return backChildren;
	}
	
	public void addComponent(Component c) {
		children.remove(c);
		children.add(c);
	}
	
	public void removeComponent(Component c) {
		children.remove(c);
	}
	
	@Override
	protected void render(Batch batch, Vector2 parentPos, Vector2 availableSize) {
		if(layout != null) {
			layout.layout(this);
			// layout.layout(this);
		}
		super.render(batch, parentPos, availableSize);
		Vector2 pos = getPosition().add(parentPos);
		Vector2 available = getSize(availableSize);
		for(Component child: children)
			child.render(batch, pos, new Vector2(available.x, child.getSize().y));
	}
	
	@Override
	protected Vector2 getSize() {
		if(layout != null)
			return layout.getSize(this);
		
		if(children.size() == 0) {
			return new Vector2();
		}
		
		Component first = children.get(0);
		
		Vector2 minPos = first.getPosition();
		Vector2 maxPos = minPos.cpy().add(first.getSize());
		
		for(int i = 1; i < children.size(); i++) {
			Component c = children.get(i);
			Vector2 pos = c.getPosition();
			Vector2 size = c.getSize();
			minPos.x = Math.min(minPos.x, pos.x);
			minPos.y = Math.min(minPos.y, pos.y);
			maxPos.x = Math.max(maxPos.x, pos.x+size.x);
			maxPos.y = Math.max(maxPos.y, pos.y+size.y);
		}
		
		return new Vector2(maxPos.x-minPos.x, maxPos.y-minPos.y);
	}
	
	public Layout getLayout() {
		return layout;
	}
	
	public void setLayout(Layout layout) {
		this.layout = layout;
	}
}
