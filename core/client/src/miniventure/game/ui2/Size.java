package miniventure.game.ui2;

import miniventure.game.util.function.MonoVoidFunction;

import com.badlogic.gdx.math.Vector2;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Size {
	
	private boolean sizeValid = false;
	private final Vector2 size = new Vector2();
	@Nullable
	private Float userWidth, userHeight;
	
	private final Object setLock = new Object();
	
	@NotNull
	private final MonoVoidFunction<Vector2> valueSetter;
	
	Size(@NotNull MonoVoidFunction<Vector2> valueSetter) {
		this.valueSetter = valueSetter;
	}
	
	public void setWidth(@Nullable Float width) { userWidth = width; }
	public void setHeight(@Nullable Float height) { userHeight = height; }
	public void setSize(@Nullable Float width, @Nullable Float height) {
		setWidth(width);
		setHeight(height);
	}
	
	void invalidate() { synchronized (setLock) {sizeValid = false;} }
	
	public float getWidth() {
		if(userWidth != null) return userWidth;
		synchronized (setLock) {
			if(!sizeValid) {
				valueSetter.act(size);
				sizeValid = true;
			}
			return size.x;
		}
	}
	public float getHeight() {
		if(userHeight != null) return userHeight;
		synchronized (setLock) {
			if(!sizeValid) {
				valueSetter.act(size);
				sizeValid = true;
			}
			return size.y;
		}
	}
}
