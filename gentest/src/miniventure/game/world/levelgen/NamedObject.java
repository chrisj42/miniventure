package miniventure.game.world.levelgen;

import org.jetbrains.annotations.NotNull;

public interface NamedObject {
	void setObjectName(@NotNull String name);
	@NotNull String getObjectName();
}
