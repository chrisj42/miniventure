package miniventure.game.world.entitynew;

import java.util.Arrays;
import java.util.HashMap;

import miniventure.game.api.APIObjectType;
import miniventure.game.api.PropertyFetcher;
import miniventure.game.api.TypeLoader;
import miniventure.game.world.entitynew.mod.BounceProperty;
import miniventure.game.world.entitynew.mod.LifetimeProperty;
import miniventure.game.world.entitynew.mod.Mob;
import miniventure.game.world.entitynew.property.EntityProperty;
import miniventure.game.world.entitynew.property.PermeableProperty;
import miniventure.game.world.entitynew.property.RenderProperty;
import miniventure.game.world.entitynew.property.RenderProperty.TextSprite;
import miniventure.game.world.entitynew.property.RenderProperty.TextSprite.TextSpriteData;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;

import org.jetbrains.annotations.NotNull;

public enum EntityType implements APIObjectType<EntityType, EntityProperty> {
	
	/*
		So I've noticed that there are a number of things that are set when an entity is created, but don't change afterward. It would be really useful if I could take advantage of that...
			maybe by having a separate instance of each property (that has these entity-specific fields) for each entity created, on creation. I wonder if I could make a constructor for that...
	 */
	
	TEXT_PARTICLE(() -> new EntityProperty[] {
			new TextSprite(),
			new BounceProperty(5f),
			PermeableProperty.NONSOLID
	}) {
		public Entity getEntity(Color color, String text) {
			Entity e = getEntity();
			TextSpriteData data = e.getDataObject(TextSprite.class, RenderProperty.class);
			data.init(text, color);
			return e;
		}
	},
	
	PLAYER(() -> {
		Array<EntityProperty> props = new Array<>(EntityProperty.class);
		Mob mob = new Mob("player", Player.Stat.Health.max) {
			@Override
			public void update(Entity e, float delta) {
				super.update(e, delta);
				// do stuff
			}
		};
		props.add(mob);
		return props.shrink();
	}) {
		public Entity getEntity() {
			return new Player(this);
		}
	};
	
	private final PropertyFetcher<EntityProperty> propertyFetcher;
	
	EntityType(@NotNull PropertyFetcher<EntityProperty> fetcher) {
		this.propertyFetcher = fetcher;
	}
	
	// all entity types will have a method to create an entity; below is the default one
	public Entity getEntity() {
		return new Entity(this);
	}
	
	
	public HashMap<Class<? extends EntityProperty>, InstanceData> createDataObjectMap() { return createDataObjectMap(getInitialData()); }
	public HashMap<Class<? extends EntityProperty>, InstanceData> createDataObjectMap(String[] data) {
		HashMap<Class<? extends EntityProperty>, InstanceData> map = new HashMap<>();
		
		for(EntityProperty prop: getTypeInstance().getPropertyObjects()) {
			InstanceData dataObj = prop.getInitialDataObject();
			int offset = getPropDataIndex(prop.getUniquePropertyClass());
			int len = getPropDataLength(prop.getUniquePropertyClass());
			dataObj.parseData(Arrays.copyOfRange(data, offset, offset+len));
			map.put(prop.getUniquePropertyClass(), dataObj);
		}
		
		return map;
	}
	
	
	@Override public EntityProperty[] getProperties() { return propertyFetcher.getProperties(); }
	
	@Override public Class<EntityType> getTypeClass() { return EntityType.class; }
	@Override public EntityType getInstance() { return this; }
	
	
	
	public static final EntityType[] values = EntityType.values();
	
	static {
		TypeLoader.loadType(EntityType.class, EntityProperty.getDefaultPropertyFetcher());
	}
}
