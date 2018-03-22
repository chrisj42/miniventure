package miniventure.game.world.entity.mob;

import java.util.EnumMap;

import miniventure.game.GameCore;
import miniventure.game.GameProtocol.InteractRequest;
import miniventure.game.GameProtocol.PlayerMovement;
import miniventure.game.GameProtocol.PositionUpdate;
import miniventure.game.GameProtocol.SpawnData;
import miniventure.game.client.ClientCore;
import miniventure.game.item.ClientHands;
import miniventure.game.item.CraftingScreen;
import miniventure.game.item.Inventory;
import miniventure.game.item.InventoryScreen;
import miniventure.game.item.Recipes;
import miniventure.game.world.entity.ClientEntity;
import miniventure.game.world.entity.Direction;
import miniventure.game.world.entity.EntityRenderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class ClientPlayer extends ClientEntity implements Player {
	
	private final EnumMap<Stat, Integer> stats = new EnumMap<>(Stat.class);
	
	private Inventory inventory;
	private ClientHands hands;
	
	private float moveSpeed = Player.MOVE_SPEED;
	private Direction dir;
	
	public ClientPlayer(SpawnData data) {
		super(data.playerData.eid, EntityRenderer.deserialize(data.playerData.spriteUpdate.rendererData));
		
		dir = Direction.DOWN;
		
		hands = new ClientHands(this);
		inventory = new Inventory(INV_SIZE); // no must-fit because that is handled by the server
		
		hands.loadItem(data.inventory.heldItemStack);
		inventory.loadItems(data.inventory.inventory);
		
		Stat.load(data.stats.stats, this.stats);
	}
	
	@Override
	public Integer[] saveStats() { return Stat.save(stats); }
	
	@Override public Direction getDirection() { return dir; }
	@Override public Inventory getInventory() { return inventory; }
	@Override public ClientHands getHands() { return hands; }
	
	public void handleInput(Vector2 mouseInput) {
		Vector2 movement = new Vector2();
		if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) movement.x--;
		if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) movement.x++;
		if(Gdx.input.isKeyPressed(Input.Keys.UP)) movement.y++;
		if(Gdx.input.isKeyPressed(Input.Keys.DOWN)) movement.y--;
		
		movement.nor();
		
		movement.add(mouseInput); // mouseInput is already normalized
		movement.nor();
		
		dir = Direction.getDirection(movement.x, movement.y);
		
		boolean moved = move(movement, Gdx.graphics.getDeltaTime());
		
		if(moved)
			ClientCore.getClient().send(new PlayerMovement(movement, new PositionUpdate(this)));
		
		//if(!player.isKnockedBack()) {
		if (ClientCore.input.pressingKey(Input.Keys.C))
			ClientCore.getClient().send(new InteractRequest(true, new PositionUpdate(this)));
		else if (ClientCore.input.pressingKey(Input.Keys.V))
			ClientCore.getClient().send(new InteractRequest(false, new PositionUpdate(this)));
		
		//if(Gdx.input.isKeyPressed(Input.Keys.C) || Gdx.input.isKeyPressed(Input.Keys.V))
		//	animator.requestState(AnimationState.ATTACK);
		//}
		
		hands.resetItemUsage();
		
		if(ClientCore.input.pressingKey(Input.Keys.E)) {
			// do nothing here; instead, tell the server to set the held item once selected (aka on inventory menu exit). The inventory should be up to date already, generally speaking.
			hands.clearItem(inventory);
			ClientCore.setScreen(new InventoryScreen(inventory, hands));
		}
		else if(ClientCore.input.pressingKey(Input.Keys.Z))
			ClientCore.setScreen(new CraftingScreen(Recipes.recipes, inventory));
		
	}
	
	public boolean move(Vector2 inputDir, float delta) {
		if(inputDir.isZero()) return false;
		
		// TODO add to ClientEntity class boolean for if it is permeable by the player.
		// TODO check permeability of objects that are going to be overlapped.
		
		Vector2 moveDist = inputDir.cpy().scl(moveSpeed*delta);
		return move(getPosition().add(moveDist));
	}
	
	public void drawGui(Rectangle canvas, SpriteBatch batch) {
		hands.getUsableItem().drawItem(hands.getCount(), batch, canvas.width/2, 20);
		float y = canvas.y + 3;
		
		renderBar(Stat.Health, canvas.x, y, batch);
		renderBar(Stat.Stamina, canvas.x, y+ Stat.Health.iconHeight+3, batch);
		renderBar(Stat.Hunger, canvas.x + canvas.width, y, batch, 0, false);
	}
	
	private void renderBar(Stat stat, float x, float y, SpriteBatch batch) { renderBar(stat, x, y, batch, 0); }
	/** @noinspection SameParameterValue*/
	private void renderBar(Stat stat, float x, float y, SpriteBatch batch, int spacing) { renderBar(stat, x, y, batch, spacing, true); }
	private void renderBar(Stat stat, float x, float y, SpriteBatch batch, int spacing, boolean rightSide) {
		float pointsPerIcon = stat.max*1f / stat.iconCount;
		TextureRegion fullIcon = GameCore.icons.get(stat.icon);
		TextureRegion emptyIcon = GameCore.icons.get(stat.outlineIcon);
		
		int iconWidth = stat.iconWidth + spacing;
		
		// for each icon...
		for(int i = 0; i < stat.iconCount; i++) {
			// gets the amount this icon should be "filled" with the fullIcon
			float iconFillAmount = Math.min(Math.max(0, stats.get(stat) - i * pointsPerIcon) / pointsPerIcon, 1);
			
			// converts it to a pixel width
			int fullWidth = (int) (iconFillAmount * fullIcon.getRegionWidth());
			float fullX = rightSide ? x+i*iconWidth : x - i*iconWidth - fullWidth;
			if(fullWidth > 0)
				batch.draw(fullIcon.getTexture(), fullX, y, fullIcon.getRegionX() + (rightSide?0:fullIcon.getRegionWidth()-fullWidth), fullIcon.getRegionY(), fullWidth, fullIcon.getRegionHeight());
			
			// now draw the rest of the icon with the empty sprite.
			int emptyWidth = emptyIcon.getRegionWidth()-fullWidth;
			float emptyX = rightSide ? x+i*iconWidth+fullWidth : x - (i+1)*iconWidth;
			if(emptyWidth > 0)
				batch.draw(emptyIcon.getTexture(), emptyX, y, emptyIcon.getRegionX() + (rightSide?fullWidth:0), emptyIcon.getRegionY(), emptyWidth, emptyIcon.getRegionHeight());
		}
	}
}
