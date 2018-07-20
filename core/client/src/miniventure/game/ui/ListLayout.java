package miniventure.game.ui;

import miniventure.game.util.RelPos;

import com.badlogic.gdx.math.Vector2;

public class ListLayout implements Layout {
	
	private final boolean verticalList;
	private final float spacing;
	private final RelPos relPos = RelPos.CENTER;
	
	// later, add a RelPos to the constructor for alignment of the items relative to each other. Note that only the list axis will be used.
	
	public ListLayout(boolean verticalList, float spacing) {
		this.verticalList = verticalList;
		this.spacing = spacing;
	}
	
	@Override
	public void layout(final float width, final float height, final Size prefSize, final Component[] components) {
		// float widthRemaining = width;
		// float heightRemaining = height;
		//
		// float bufferSpace = components.length > 1 ? spacing * (components.length-1) : 0;
		//
		// if(verticalList) heightRemaining -= bufferSpace;
		// else widthRemaining -= bufferSpace;
		// System.out.println("laying out components: "+ Arrays.toString(components));
		
		Vector2 pos = relPos.positionRect(new Vector2(prefSize.getWidth(), prefSize.getHeight()), new Vector2(width/2, height/2));
		
		// pos holds the upper left corner of where to start.
		
		// for now, just set everything in line. Don't worry about min/max sizes or anything, just get this working first.
		for(int i = 0; i < components.length; i++) {
			Component c = components[i];
			Size s = c.getPrefSize();
			if(verticalList) {
				c.setPosition(pos.x + (width-s.getWidth())/2, pos.y);
				pos.y += s.getHeight() + spacing;
			}
			else {
				c.setPosition(pos.x, pos.y + (height-s.getHeight())/2);
				pos.x += s.getWidth() + spacing;
			}
			c.setSize(s.getWidth(), s.getHeight());
			// System.out.println("set component "+i+" bounds to "+c.getX()+ ',' +c.getY()+"; dims "+c.getWidth()+ ',' +c.getHeight());
		}
	}
	
	@Override
	public void calcPreferredSize(final Vector2 v, final Component[] components) {
		float mainTotal = 0, offTotal = 0;
		
		for(Component c: components) {
			Size s = c.getPrefSize();
			float width = s.getWidth();
			float height = s.getHeight();
			
			mainTotal += verticalList ? height : width;
			offTotal = Math.max(offTotal, verticalList ? width : height);
		}
		
		if(components.length > 0)
			mainTotal += spacing * (components.length-1);
		
		v.x = verticalList ? offTotal : mainTotal;
		v.y = verticalList ? mainTotal : offTotal;
	}
}
