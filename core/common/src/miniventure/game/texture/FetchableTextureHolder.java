package miniventure.game.texture;

import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Region;

import org.jetbrains.annotations.NotNull;

public class FetchableTextureHolder {
	
	public final ItemTextureSource source;
	public final TextureHolder tex;
	
	public FetchableTextureHolder(@NotNull ItemTextureSource source, @NotNull TextureHolder textureHolder) {
		this.source = source;
		this.tex = textureHolder;
	}
}
