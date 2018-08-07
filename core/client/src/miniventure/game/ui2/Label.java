package miniventure.game.ui2;

import miniventure.game.GameCore;

import com.badlogic.gdx.math.Vector2;

public class Label extends OldComponent {
	
	private String text;
	
	public Label(String text) {
		this.text = text;
	}
	
	public String getText() { return text; }
	public void setText(String text) { this.text = text; }
	
	@Override
	public Vector2 getPreferredSize(Vector2 v) {
		return v.set(GameCore.getTextLayout(text).width, GameCore.getFont().getLineHeight());
	}
}
