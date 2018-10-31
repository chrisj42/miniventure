package miniventure.game.item;

import miniventure.game.texture.TextureHolder;

import org.jetbrains.annotations.NotNull;

public class ClientItem extends Item {
	
	ClientItem(@NotNull String name) {
		super(name);
	}
	
	ClientItem(@NotNull String name, @NotNull TextureHolder texture) {
		super(name, texture);
	}
	
	@Override
	public ClientItem copy() {
		return new ClientItem(getName(), getTexture());
	}
	
	/*
		So the deal with items and client/server mixing is a pretty simple one to fix;
		it's just going to take a while.
		I basically have to split up a lot of classes into client/server pairs.
		
		Something else I could do instead, though, is have all the methods in common again;
		but all classes will have client/server implementations, and any methods that are
		only called on one side will be left blank in the middle.
		
		The above way is easier, perhaps, but I can't help but see that it's bad style. There
		shouldn't be methods available that shouldn't be called. If something is only called on
		the server, then it should only exist on the server.
	 */
	
	public static ClientItem load(String[] data) {
		if(data == null) return null;
		return type.load(Arrays.copyOfRange(data, 1, data.length));
	}
}
