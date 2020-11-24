package miniventure.game.item;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ServerItemSource extends ServerItemStackSource {
	
	@NotNull
	ServerItem get();
	
	@NotNull
	default ServerItemStack stack(int count) {
		return new ServerItemStack(get(), count);
	}
	
	@Override @NotNull
	default ServerItemStack getStack() {
		return stack(1);
	}
}
