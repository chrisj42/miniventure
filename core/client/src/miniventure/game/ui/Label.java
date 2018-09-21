package miniventure.game.ui;

import miniventure.game.GameCore;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;

public class Label extends Component {
	
	// displays a string of text.
	
	private String text;
	private float lastWidth = -1;
	
	public Label(String text) {
		this.text = text;
	}
	
	public void setText(String text) {
		this.text = text;
		invalidate();
	}
	
	public String getText() { return text; }
	
	@Override
	public void layout() {
		// lastWidth = getSizeCache().getSize().x;
	}
	
	@Override
	public void render(Batch batch) {
		super.render(batch);
		float save = lastWidth;
		lastWidth = Math.min(getSizeCache().getSize().x, getParent().getSizeCache().getSize().x);
		GameCore.getFont().draw(batch, text, getScreenX(), getScreenY()+GameCore.getFont().getLineHeight()-GameCore.getFont().getAscent(), lastWidth, Align.center, true);
		if(save != lastWidth)
			invalidate();
	}
	
	@Override
	protected Vector2 calcMinSize(Vector2 rt) {
		return calcPreferredSize(rt);
	}
	
	@Override
	protected Vector2 calcPreferredSize(Vector2 rt) {
		GlyphLayout gl = GameCore.getTextLayout(text);
		gl.setText(GameCore.getFont(), text, Color.BLACK, lastWidth > 0 ? lastWidth : gl.width, Align.center, true);
		return rt.set(gl.width, gl.height);
	}
	
	@Override
	protected Vector2 calcMaxSize(Vector2 rt) {
		return calcPreferredSize(rt);
	}
}
