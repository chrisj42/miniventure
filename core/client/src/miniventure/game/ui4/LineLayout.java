package miniventure.game.ui4;

import miniventure.game.ui4.Component.SizeCache;

import com.badlogic.gdx.math.Vector2;

public class LineLayout implements Layout {
	
	private final boolean vertical;
	// private final float spacing;
	private final boolean expandChildrenToFit;
	
	public LineLayout(boolean vertical/*, float spacing*/, boolean expandChildrenToFit) {
		// this.spacing = spacing;
		this.expandChildrenToFit = expandChildrenToFit;
		this.vertical = vertical;
	}
	
	// puts total dims in max var
	private void allocateSizes(Container container, Vector2 bounds, SizeFetcher sizeFetcher, float[] xSizes, float[] ySizes) {
		// relativize x and y to major and minor axes
		float majorLength = 0;
		float minorLength = 0;
		Vector2 compSize = bounds == null ? new Vector2() : bounds;
		for(int i = 0; i < container.getComponentCount(); i++) {
			Component comp = container.getComponentAt(i);
			sizeFetcher.getSize(comp.getSizeCache(), compSize);
			if(xSizes != null) xSizes[i] = compSize.x;
			if(ySizes != null) ySizes[i] = compSize.y;
			float major = vertical ? compSize.y : compSize.x;
			float minor = vertical ? compSize.x : compSize.y;
			majorLength += major;
			minorLength = Math.max(minor, minorLength);
		}
		
		// majorLength += spacing * Math.max(0, container.getComponentCount()-1);
		
		if(bounds == null) return;
		
		if(vertical)
			bounds.set(minorLength, majorLength);
		else
			bounds.set(majorLength, minorLength);
	}
	
	// then use this in both methods below.
	
	@Override
	public Vector2 layoutSize(Container container, Vector2 rt, SizeFetcher sizeFetcher) {
		allocateSizes(container, rt, sizeFetcher, null, null);
		return rt;
	}
	
	@Override
	public void layoutContainer(Container container) {
		float[] xSizes = new float[container.getComponentCount()];
		float[] ySizes = new float[container.getComponentCount()];
		
		Vector2 bounds = new Vector2();
		allocateSizes(container, bounds, SizeCache::getPreferredSize, xSizes, ySizes);
		
		Vector2 limits = container.getSizeCache().getSize();
		Vector2 total = new Vector2();
		
		if(bounds.x > limits.x || expandChildrenToFit && bounds.x < limits.x) {
			float[] xLimits = new float[container.getComponentCount()];
			allocateSizes(container, total, (bounds.x > limits.x) ? SizeCache::getMinSize : SizeCache::getMaxSize, xLimits, null);
			scaleSizes(xSizes, xLimits, bounds.x, limits.x);
		}
		if(bounds.y > limits.y || expandChildrenToFit && bounds.y < limits.y) {
			float[] yLimits = new float[container.getComponentCount()];
			allocateSizes(container, total, (bounds.y > limits.y) ? SizeCache::getMinSize : SizeCache::getMaxSize, null, yLimits);
			scaleSizes(ySizes, yLimits, bounds.y, limits.y);
		}
		
		total.setZero();
		for(int i = 0; i < container.getComponentCount(); i++)
			total.add(xSizes[i], ySizes[i]);
		
		// float space = spacing * (Math.max(0, container.getComponentCount() - 1));
		// total.add(vertical ? 0 : space, vertical ? space : 0);
		
		float majorOffset = vertical ? (limits.y - total.y) / 2 : (limits.x - total.x) / 2;
		for(int i = 0; i < container.getComponentCount(); i++) {
			float xOff, yOff;
			if(vertical) {
				yOff = majorOffset;
				majorOffset += ySizes[i];// + spacing;
				xOff = (limits.x - xSizes[i]) / 2;
			}
			else {
				xOff = majorOffset;
				majorOffset += xSizes[i];// + spacing;
				yOff = (limits.y - ySizes[i]) / 2;
			}
			container.getComponentAt(i).setPosition(xOff, yOff);
			container.getComponentAt(i).getSizeCache().setSize(xSizes[i], ySizes[i]);
		}
	}
	
	private static void scaleSizes(float[] sizes, float[] limits, float total, float target) {
		float totalSpace = 0;
		for(int i = 0; i < sizes.length; i++)
			totalSpace += limits[i] - sizes[i];
		// now we have the total space we can work with; take the change from total to target, and apply it to each size, multiplied by that size's size divided by the total space.
		float change = target - total;
		for(int i = 0; i < sizes.length; i++)
			if(limits[i] != sizes[i])
				sizes[i] += change * (limits[i] - sizes[i]) / totalSpace;
	}
	
	
	private static class Spacer extends Component {
		@Override
		protected Vector2 calcPreferredSize(Vector2 rt) {
			return rt.setZero();
		}
		
		@Override
		protected Vector2 calcMaxSize(Vector2 rt) {
			return super.calcMaxSize(rt).scl(100);
		}
	}
	
	public static Component createSpacer() { return new Spacer(); }
}
