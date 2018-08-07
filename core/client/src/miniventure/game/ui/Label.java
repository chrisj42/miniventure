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
	
	public Label(float width, String text) {
		this.width = width;
		this.text = text;
		this.layout = new GlyphLayout(GameCore.getFont(), text, Color.WHITE, width, Align.center, true);
	}
	
	@Override
	protected Vector2 getSize() {
		return new Vector2(layout.width, layout.height);
	}
	
	@Override
	protected void render(Batch batch, Vector2 parentPos) {
		super.render(batch, parentPos);
		Vector2 pos = getPosition();
		GameCore.getFont().draw(batch, layout, pos.x, pos.y);
	}
}
