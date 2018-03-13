package miniventure.game.item.typenew;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

@FunctionalInterface
public interface RenderProperty extends ItemProperty {
	
	TextureRegion getSprite(Item item);
	
	@Override
	default Class<? extends ItemProperty> getUniquePropertyClass() { return RenderProperty.class; }
}
