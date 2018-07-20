package miniventure.game.ui;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.Nullable;

public class Container extends Component {
	
	private final ArrayList<Component> children = new ArrayList<>();
	
	private boolean validated = false;
	@Nullable private Layout layout;
	
	Container() { this(null); }
	Container(Layout layout) {
		setLayout(layout);
	}
	
	public void addComponent(Component c) {
		if(c.getParent() != null)
			c.getParent().removeComponent(c);
		children.add(c);
		c.setParent(this);
		invalidate();
	}
	
	public void removeComponent(Component c) {
		c.setParent(null);
		children.remove(c);
		invalidate();
	}
	
	private Component[] getChildren() { return children.toArray(new Component[children.size()]); }
	
	void setLayout(Layout layout) {
		this.layout = layout;
		invalidate();
	}
	
	@Override
	void invalidate() {
		super.invalidate();
		validated = false;
	}
	
	@Override
	protected void update() {
		if(getParent() != null && !getParent().validated)
			getParent().validate();
		
		if(!validated)
			validate();
		
		// update children
		for(Component c: children)
			c.update();
	}
	
	private void validate() {
		if(validated) return;
		
		if(layout != null)
			layout.layout(getWidth(), getHeight(), getPrefSize(), getChildren());
		
		validated = true;
	}
	
	@Override
	protected void render(Batch batch) {
		super.render(batch);
		// render children
		for(Component c: children)
			c.render(batch);
	}
	
	@Override
	protected void calcPrefSize(Vector2 v) {
		// in order to calculate the preferred size, the container must be laid out... but in order to layout the container, you have to know the preferred size...
		// ....!
		
		// I believe the layout is what can provide the preferred size.
		if(layout != null)
			layout.calcPreferredSize(v, getChildren());
		else {
			// if there is no layout, then we just have to go with the maximum boundaries of the children as they currently are positioned, using the preferred sizes.
			
			if(children.size() == 0) {
				v.setZero();
				return;
			}
			
			Component first = children.get(0);
			
			Vector2 minPos = new Vector2(first.getX(), first.getY());
			Vector2 maxPos = new Vector2(first.getX()+first.getPrefSize().getWidth(), first.getY()+first.getPrefSize().getHeight());
			
			for(int i = 1; i < children.size(); i++) {
				Component c = children.get(i);
				minPos.x = Math.min(minPos.x, c.getX());
				minPos.y = Math.min(minPos.y, c.getY());
				maxPos.x = Math.max(maxPos.x, c.getX()+c.getPrefSize().getWidth());
				maxPos.y = Math.max(maxPos.y, c.getY()+c.getPrefSize().getHeight());
			}
			
			v.set(maxPos.x-minPos.x, maxPos.y-minPos.y);
		}
	}
}
