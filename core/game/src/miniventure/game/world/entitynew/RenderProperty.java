package miniventure.game.world.entitynew;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

@FunctionalInterface
public interface RenderProperty extends EntityProperty {
	
	/*/
		sprite properties:
			- animations
			- text sprites (uhhh...
			- single textures
	 */
	
	RenderProperty textSprite = (e, batch, x, y) -> new RenderProperty() {
		@Override
		public String[] getInitialData() {
			return new String[1];
		}
		
		@Override
		public void render(Entity e, SpriteBatch batch, float x, float y) {
			e.getData(RenderProperty.class, e.getType(), 1);
		}
	};
	
	
	
	void render(Entity e, SpriteBatch batch, float x, float y);
	
	@Override
	default Class<? extends EntityProperty> getUniquePropertyClass() { return RenderProperty.class; }
}
