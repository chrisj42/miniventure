package miniventure.game.world.entitynew;

public interface UpdateProperty extends EntityProperty {
	
	void update(Entity e, float delta);
	
	static UpdateProperty all(UpdateProperty... props) {
		return (e, delta) -> {
			for(UpdateProperty prop: props)
				prop.update(e, delta);
		};
	}
	
	@Override
	default Class<? extends EntityProperty> getUniquePropertyClass() { return UpdateProperty.class; }
}
