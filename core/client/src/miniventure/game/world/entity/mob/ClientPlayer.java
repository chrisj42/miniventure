package miniventure.game.world.entity.mob;

import java.util.Arrays;

import miniventure.game.client.ClientCore;
import miniventure.game.item.CraftingScreen;
import miniventure.game.item.Hands;
import miniventure.game.item.Inventory;
import miniventure.game.item.InventoryScreen;
import miniventure.game.item.Recipes;
import miniventure.game.util.Version;
import miniventure.game.world.entity.Entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public class ClientPlayer extends Player {
	
	
	public ClientPlayer() { super(); }
	
	public ClientPlayer(String[][] allData, Version version) {
		super(allData, version);
	}
	
	@Override
	public Class<? extends Entity> getLoadingClass() { return Player.class; }
	
	public void checkInput(@NotNull Vector2 mouseInput) {
		// checks for keyboard input to move the player.
		// getDeltaTime() returns the time passed between the last and the current frame in seconds.
		Vector2 movement = new Vector2();
		if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) movement.x--;
		if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) movement.x++;
		if(Gdx.input.isKeyPressed(Input.Keys.UP)) movement.y++;
		if(Gdx.input.isKeyPressed(Input.Keys.DOWN)) movement.y--;
		
		movement.nor();
		
		movement.add(mouseInput);
		movement.nor();
		
		movement.scl(MOVE_SPEED * Gdx.graphics.getDeltaTime());
		
		move(movement.x, movement.y);
		
		getStatEvo(StaminaSystem.class).isMoving = !movement.isZero();
		if(!movement.isZero())
			getStatEvo(HungerSystem.class).addHunger(Gdx.graphics.getDeltaTime() * 0.35f);
		
		if(!isKnockedBack()) {
			if (ClientCore.input.pressingKey(Input.Keys.C))
				attack();
			else if (ClientCore.input.pressingKey(Input.Keys.V))
				interact();
			
			//if(Gdx.input.isKeyPressed(Input.Keys.C) || Gdx.input.isKeyPressed(Input.Keys.V))
			//	animator.requestState(AnimationState.ATTACK);
		}
		
		Hands hands = getHands();
		Inventory inventory = getInventory();
		
		hands.resetItemUsage();
		
		if(ClientCore.input.pressingKey(Input.Keys.E)) {
			hands.clearItem(inventory);
			ClientCore.setScreen(new InventoryScreen(inventory, hands));
		}
		else if(ClientCore.input.pressingKey(Input.Keys.Z))
			ClientCore.setScreen(new CraftingScreen(Recipes.recipes, inventory));
	}
	
}
