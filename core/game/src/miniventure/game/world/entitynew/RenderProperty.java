package miniventure.game.world.entitynew;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class RenderProperty implements EntityProperty {
	
	/*/
		sprite properties:
			- animations
			- text sprites (uhhh...
			- single textures
	 */
	
	public RenderProperty() {
		
	}
	
	public TextureRegion getSprite() {
		return null;
	}
	
	@Override
	public Class<? extends EntityProperty> getUniquePropertyClass() { return RenderProperty.class; }
}
