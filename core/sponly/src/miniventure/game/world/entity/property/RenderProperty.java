package miniventure.game.world.entity.property;

import miniventure.game.texture.TextureHolder;
import miniventure.game.util.blinker.Blinker;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.tile.SwimAnimation;
import miniventure.game.world.tile.Tile;
import miniventure.game.world.tile.TileType.Prop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import org.jetbrains.annotations.NotNull;

public interface RenderProperty {
	
	default boolean shouldRender() {
		RenderProperty pipe = getRenderPipe();
		return pipe == null || pipe.shouldRender();
	}
	
	void render(TextureHolder texture, float x, float y, SpriteBatch batch, float drawableHeight);
	
	RenderProperty getRenderPipe();
	
	class RenderModifier implements RenderProperty {
		
		@NotNull
		private RenderProperty pipe;
		
		public RenderModifier(@NotNull Entity entity) {
			this.pipe = entity;
		}
		
		@Override
		public void render(TextureHolder texture, float x, float y, SpriteBatch batch, float drawableHeight) {
			pipe.render(texture, x, y, batch, drawableHeight);
		}
		
		/*protected void render(@NotNull RenderProperty pipe, TextureHolder texture, float x, float y, SpriteBatch batch, float drawableHeight) {
			pipe.render(texture, x, y, batch, drawableHeight);
		}*/
		
		@Override @NotNull
		public RenderProperty getRenderPipe() {
			return pipe;
		}
		
		public void setRenderPipe(@NotNull RenderProperty renderer) {
			this.pipe = renderer;
		}
	}
	
	class ColorProperty extends RenderModifier {
		
		@NotNull
		private final Color color;
		
		public ColorProperty(@NotNull Entity entity, @NotNull Color color) {
			super(entity);
			this.color = color;
		}
		
		@Override
		public void render(TextureHolder texture, float x, float y, SpriteBatch batch, float drawableHeight) {
			Color c = batch.getColor();
			batch.setColor(color);
			super.render(texture, x, y, batch, drawableHeight);
			batch.setColor(c);
		}
	}
	
	class SwimRenderer extends RenderModifier {
		
		@NotNull
		private final Entity entity;
		
		public SwimRenderer(@NotNull Entity entity) {
			super(entity);
			this.entity = entity;
		}
		
		@Override
		public void render(TextureHolder texture, float x, float y, SpriteBatch batch, float drawableHeight) {
			Tile closest = entity.getClosestTile();
			SwimAnimation swimAnimation = closest.getType().get(Prop.RENDER).getSwimAnimation();
			if(swimAnimation != null) {
				// Vector2 pos = e.getCenter().sub(posOffset).sub(0, getSize().y / 2).scl(Tile.SIZE);
				// swimAnimation.drawSwimAnimation(batch, pos, getWorld());
				swimAnimation.drawSwimAnimation(batch, x + entity.getSize().x/2, y, entity.getWorld());
				drawableHeight = swimAnimation.drawableHeight;
			}
			
			super.render(texture, x, y, batch, drawableHeight);
		}
	}
	
	abstract class UpdatableRenderModifier extends RenderModifier {
		
		private long lastRenderFrame = -1;
		
		public UpdatableRenderModifier(@NotNull Entity entity) {
			super(entity);
		}
		
		@Override
		public boolean shouldRender() {
			long frame = Gdx.graphics.getFrameId();
			if(lastRenderFrame < 0 || lastRenderFrame != frame) {
				update();
				lastRenderFrame = frame;
			}
			
			return super.shouldRender();
		}
		
		// next render, return if it should render
		protected abstract void update();
	}
	
	class BlinkProperty extends UpdatableRenderModifier {
		
		@NotNull
		private final Blinker blinker;
		
		public BlinkProperty(@NotNull Entity entity, @NotNull Blinker blinker) {
			super(entity);
			this.blinker = blinker;
		}
		
		@Override
		public boolean shouldRender() {
			return super.shouldRender() && blinker.shouldRender();
		}
		
		@Override
		protected void update() {
			blinker.update();
		}
	}
}
