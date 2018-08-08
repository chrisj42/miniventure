package miniventure.game.ui;

import miniventure.game.GameCore;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;

public class Label extends Component {
	
	private final float width;
	private final String text;
	private final GlyphLayout layout;
	
	public Label(float width, boolean fillParent, String text) {
		this.width = width;
		this.text = text;
		this.layout = new GlyphLayout(GameCore.getFont(), text, Color.WHITE, width, Align.center, true);
		setFillParent(fillParent, false);
	}
	
	@Override
	protected Vector2 getSize() {
		// layout.setText(GameCore.getFont(), text, Color.WHITE, width, Align.center, true);
		return new Vector2(layout.width, layout.height);
	}
	
	@Override
	protected Vector2 getSize(Vector2 availableSize) {
		layout.setText(GameCore.getFont(), text, Color.WHITE, getFillX()?availableSize.x:width, Align.center, true);
		return super.getSize(availableSize);
	}
	
	@Override
	protected void render(Batch batch, Vector2 parentPos, Vector2 availableSize) {
		Vector2 size = getSize(availableSize);
		// System.out.println(size+" of "+availableSize);
		layout.setText(GameCore.getFont(), text, Color.WHITE, size.x, Align.center, true);
		Vector2 pos = getPosition();
		// MyUtils.drawRect(parentPos.x+pos.x, parentPos.y+pos.y, availableSize.x, availableSize.y, Color.PURPLE, batch);
		super.render(batch, parentPos, availableSize);
		pos.sub(size.x/2, size.y/2);
		pos.add(0, GameCore.getFont().getLineHeight()/2);
		GameCore.getFont().draw(batch, layout, pos.x, pos.y);
	}
}
