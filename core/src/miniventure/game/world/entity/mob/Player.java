package miniventure.game.world.entity.mob;

import miniventure.game.item.Item;
import miniventure.game.world.Level;
import miniventure.game.world.entity.Entity;
import miniventure.game.world.tile.Tile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Player extends Mob {
	
	private Item heldItem;
	
	public Player() {
		super("player");
	}
	
	public void checkInput(float delta) {
		// checks for keyboard input to move the player.
		// getDeltaTime() returns the time passed between the last and the current frame in seconds.
		int speed = 200; // this is technically in units/second.
		int xd = 0, yd = 0;
		if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) xd -= speed * delta;
		if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) xd += speed * delta;
		if(Gdx.input.isKeyPressed(Input.Keys.UP)) yd += speed * delta;
		if(Gdx.input.isKeyPressed(Input.Keys.DOWN)) yd -= speed * delta;
		
		move(xd, yd);
		
		// TODO add attack key input, and behavior.
		// Also, see what happens when I go to texturePacker and remove the outer whitespace around the player sprites. If possible, make sure they are all the same size, but see what happens if they aren't anyway.
		if(pressingKey(Input.Keys.C))
			attack();
		if(pressingKey(Input.Keys.V))
			interact();
	}
	
	@Override
	public void update(float delta) {
		// update things like hunger, stamina, etc.
	}
	
	private Rectangle getInteractionRect() {
		Rectangle bounds = getBounds();
		Vector2 dirVector = getDirection().getVector();
		//System.out.println("dir vector="+dirVector);
		bounds.setX(bounds.getX()+bounds.getWidth()*dirVector.x);
		bounds.setY(bounds.getY()+bounds.getHeight()*dirVector.y);
		return bounds;
	}
	
	private void attack() {
		// get level, and don't attack if level is not found
		Level level = Level.getEntityLevel(this);
		if(level == null) return;
		
		// get attack rect
		Rectangle attackBounds = getInteractionRect();
		
		// find entities in attack rect, and attack them
		Array<Entity> otherEntities = level.getOverlappingEntities(attackBounds);
		otherEntities.removeValue(this, true); // use ==, not .equals()
		boolean attacked = false;
		for(Entity e: otherEntities)
			attacked = attacked || e.hurtBy(this, heldItem);
		
		if(attacked) return; // don't hurt the tile
		
		// if no entities were successfully attacked, get tile and attack it instead
		Tile tile = level.getClosestTile(attackBounds);
		if(tile != null)
			tile.attackedBy(this, heldItem);
	}
	
	private void interact() {
		
	}
	
	private static boolean pressingKey(int keycode) {
		/*
			The only reason this is necessary is because libGDX doesn't seem to have functionality to make it so if you hold down a key, it fires once immediately, waits a half-second, and then fires a lot more rapidly.
			
			I would like to have that functionality, but it seems I'm going to have to do it myself.
		 */
		
		return Gdx.input.isKeyJustPressed(keycode);
	}
}
