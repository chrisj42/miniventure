package miniventure.game.world.entity.particle;

import miniventure.game.world.entity.Direction;

import com.badlogic.gdx.graphics.Color;

public interface ParticleData {
	
	class TextParticleData implements ParticleData {
		public final String text;
		public final String mainColor;
		public final String shadowColor;
		
		private TextParticleData() { this(null, null, (String)null); }
		public TextParticleData(String text) { this(text, Color.RED); }
		public TextParticleData(String text, Color main) { this(text, main, Color.BLACK); }
		public TextParticleData(String text, Color main, Color shadow) {
			this(text, main.toString(), shadow.toString());
		}
		public TextParticleData(String text, String mainColor, String shadowColor) {
			this.text = text;
			this.mainColor = mainColor;
			this.shadowColor = shadowColor;
		}
	}
	
	class ActionParticleData implements ParticleData {
		public final ActionType action;
		public final Direction dir;
		
		private ActionParticleData() { this(null, null); }
		public ActionParticleData(ActionType type) { this(type, null); }
		public ActionParticleData(ActionType type, Direction dir) {
			this.action = type;
			this.dir = dir;
		}
	}
	
}
