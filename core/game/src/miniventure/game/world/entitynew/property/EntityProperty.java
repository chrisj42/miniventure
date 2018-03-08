package miniventure.game.world.entitynew.property;

import miniventure.game.api.Property;
import miniventure.game.api.PropertyFetcher;
import miniventure.game.world.entitynew.InstanceData;

import com.badlogic.gdx.math.Vector2;

public interface EntityProperty extends Property<EntityProperty> {
	
	/*
		sprite properties:
			- directional mob animations
			- text sprites
			- single textures
			- textures based on something, like an item, aka animation property chooser... such as direction...
		
		update properties:
			- keyboard input for movement / other control
			- ai updates
			- other misc updates
		
		level properties:
			- position and level (however, no behavior, just coords, so this is part of the Entity class)
			- size
		
		... that's about it..?
		
		So, I'm going to have a number of different update properties... Actually, I can just make a static method in the UpdateProperty class to merge different update properties into a single property.
		Oh! I totally forgot about events. Need those too.
		
		Event properties:
			- on attacked
			- on touched
			- on continued touch?
			- on attempted interaction
	 */
	
	static PropertyFetcher<EntityProperty> getDefaultPropertyFetcher() {
		//noinspection Convert2MethodRef, unchecked
		return () -> new EntityProperty[] {
				(SizeProperty) () -> new Vector2(),
				RenderProperty.DEFAULT,
				PermeableProperty.DEFAULT,
				(UpdateProperty) (e, delta) -> {},
				(InteractionListener) (p, i, e) -> false,
				(AttackListener) (obj, i, dmg, e) -> false,
				(TouchListener) (e, toucher, initial) -> false,
				(MovementListener) (e, delta) -> {},
				(LevelListener) (e, lvl) -> {}
		};
	}
	
	// this will be used only to replace types that there are multiple of, for an entry for a certain class; it won't be used to entirely replace properties, unless this type is the base type of the property.
	default EntityProperty combineProperty(EntityProperty other) { return other; }
	
	default InstanceData getInitialDataObject() { return InstanceData.NO_DATA; }
	
	@Override
	default String[] getInitialData() { return new String[0]; }
}
