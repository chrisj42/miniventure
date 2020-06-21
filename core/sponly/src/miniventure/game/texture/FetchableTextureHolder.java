package miniventure.game.texture;

import org.jetbrains.annotations.NotNull;

public class FetchableTextureHolder {
	
	public final ItemTextureSource source;
	public final TextureHolder tex;
	
	public FetchableTextureHolder(@NotNull ItemTextureSource source, @NotNull TextureHolder textureHolder) {
		this.source = source;
		this.tex = textureHolder;
	}
}
