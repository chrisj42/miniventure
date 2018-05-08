package miniventure.game.world.tile;

import miniventure.game.item.Item;
import miniventure.game.util.function.ValueTriFunction;
import miniventure.game.world.entity.mob.Player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InteractableProperty extends TileProperty {
	
	private final ValueTriFunction<Player, Item, Tile, Boolean> interaction;
	
	public InteractableProperty(@NotNull TileType type, ValueTriFunction<Player, Item, Tile, Boolean> interaction) {
		super(type);
		this.interaction = interaction;
	}
	
	public boolean interact(Player player, @Nullable Item heldItem, Tile tile) {
		return interaction.get(player, heldItem, tile);
	}
	
}
