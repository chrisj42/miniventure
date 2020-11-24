package miniventure.game.item;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ServerItemStackSource {
	
	@NotNull
	ServerItemStack getStack();
	
}
