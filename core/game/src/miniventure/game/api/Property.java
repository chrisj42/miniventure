package miniventure.game.api;

public interface Property<T extends Property<T>> {
	String[] getInitialData();
	
	// return the class right below the one that signifies the common class for the properties.
	// essentially, this will be used for sorting and such; property classes that are specific to a certain set should return themselves, while any that extend them should do nothing.
	Class<? extends T> getUniquePropertyClass();
}
