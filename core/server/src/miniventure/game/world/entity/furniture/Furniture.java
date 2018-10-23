package miniventure.game.world.entity.furniture;

import java.util.ArrayList;
import java.util.Arrays;

import miniventure.game.util.Version;
import miniventure.game.util.function.ValueFunction;
import miniventure.game.world.entity.ClassDataList;
import miniventure.game.world.entity.ServerEntity;

public abstract class Furniture extends ServerEntity {
	
	/*
		Furniture is an odd entity subclass. I don't know if I should even make it.
		Regardless, this is the parent of things like a bed, chest, workbench, etc.
		Very diverse functionality.
		
		However, they do have this common trait:
		- they are things you can place down in the world, and can pickup and move if desired.
		- 
		
	 */
	
	protected Furniture() {
		super();
	}
	
	protected Furniture(ClassDataList allData, Version version, ValueFunction<ClassDataList> modifier) {
		super(allData, version, modifier);
	}
	
	public ClassDataList save() {
		ClassDataList allData = new ClassDataList();
		// ArrayList<String> data = new ArrayList<>(Arrays.asList(
		//	
		// ));
		// allData.add(data);
		return allData;
	}
}
