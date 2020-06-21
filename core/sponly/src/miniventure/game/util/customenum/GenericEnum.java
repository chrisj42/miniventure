package miniventure.game.util.customenum;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedList;

import org.jetbrains.annotations.NotNull;

/** @noinspection rawtypes*/
@SuppressWarnings("unchecked")
public abstract class GenericEnum<T, ET extends GenericEnum<T, ET>> implements Comparable<ET> {
	
	private static final HashMap<Class<? extends GenericEnum>, EnumData<? extends GenericEnum>> enumClassData = new HashMap<>();
	
	/*protected static final <ET extends GenericEnum> void registerEnum(Class<ET> clazz, int constants) {
		enumClassData.put(clazz, new EnumData<>(clazz, constants));
	}*/
	
	// initializes all generic enum implementations
	/*public static void init() {
		TileDataTag.init();
		TileDataTag.init();
	}*/
	
	private static final class EnumData<ET extends GenericEnum> {
		private final Class<ET> enumClass;
		private final String[] names;
		private final ET[] values;
		private final HashMap<String, ET> nameToValue;
		
		private int counter = 0;
		private boolean initialized = false;
		
		private final Field[] constantFields;
		
		private EnumData(Class<ET> enumClass) {
			this.enumClass = enumClass;
			
			LinkedList<Field> fields = new LinkedList<>();
			// WARNING: getDeclaredFields makes no guarantee that the fields are returned in order of declaration. So don't rely on that.
			for(Field field: enumClass.getDeclaredFields()) {
				if(Modifier.isStatic(field.getModifiers()) && enumClass.isAssignableFrom(field.getType()))
					fields.add(field);
			}
			constantFields = fields.toArray(new Field[0]);
			
			names = new String[constantFields.length];
			values = (ET[])Array.newInstance(enumClass, constantFields.length);
			nameToValue = new HashMap<>(constantFields.length);
		}
		
		void checkInit() {
			if(!initialized)
				initialize();
		}
		
		private void initialize() {
			initialized = true;
			
			try {
				for(Field type: constantFields) {
					ET instance = enumClass.cast(type.get(null));
					String name = type.getName();
					nameToValue.put(name, instance);
					names[((GenericEnum)instance).ordinal] = name;
				}
			} catch(IllegalAccessException e) {
				throw new EnumInitializationException(enumClass, e);
			}
			
			// nameToData.forEach((name, value) -> names[value.ordinal] = name);
		}
	}
	
	
	private static <ET extends GenericEnum> EnumData<ET> getData(Class<ET> clazz) {
		EnumData<ET> enumData = (EnumData<ET>) enumClassData.get(clazz);
		
		if(enumData == null) {
			try {
				Class.forName(clazz.getName()); // ensure the class is initialized
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e); // shouldn't happen of course
			}
			
			// should be initialized now, assuming there was at least one constant
			enumData = (EnumData<ET>) enumClassData.get(clazz);
			if(enumData == null) // the only reason I can see this happening is if a class has no constants.
				throw new MissingEnumDataException(clazz);
		}
		
		enumData.checkInit();
		return enumData;
	}
	
	public static final <ET extends GenericEnum> ET valueOf(Class<ET> clazz, String name) {
		EnumData<ET> enumData = getData(clazz);
		ET constant = enumData.nameToValue.get(name);
		if(constant == null)
			throw new EnumConstantNotFoundException(clazz, name);
		
		return constant;
	}
	
	public static final <ET extends GenericEnum> ET valueOf(Class<ET> clazz, int ordinal) {
		return getData(clazz).values[ordinal];
	}
	
	public static final <ET extends GenericEnum> ET[] values(Class<ET> clazz) {
		return getData(clazz).values;
	}
	
	
	private final EnumData<ET> enumData;
	private final int ordinal;
	// private final Class<T> typeClass;
	
	protected GenericEnum(/*Class<T> typeClass*/) {
		// this.typeClass = typeClass;
		Class<? extends GenericEnum> clazz = getClass();
		if(clazz.isAnonymousClass())
			clazz = (Class<? extends GenericEnum>) clazz.getSuperclass();
		
		if(!enumClassData.containsKey(clazz)) {
			// this is the first enum constant for this class; initialize stuff
			enumClassData.put(clazz, new EnumData<>(clazz));
		}
		
		try {
			enumData = (EnumData<ET>) enumClassData.get(clazz);
			ordinal = enumData.counter++;
			enumData.values[ordinal] = (ET) this;
		} catch(ClassCastException e) {
			throw new EnumGenericTypeMismatchException(this, e);
		}
	}
	
	public final Class<ET> getEnumClass() {
		return enumData.enumClass;
	}
	
	public final int ordinal() { return ordinal; }
	public final String name() {
		enumData.checkInit();
		return enumData.names[ordinal];
	}
	
	@Override
	public final int compareTo(@NotNull ET o) {
		return ordinal - o.ordinal();
	}
	
	@Override
	public final boolean equals(Object obj) {
		return this == obj;
		// return enumData.enumClass.isAssignableFrom(obj.getClass()) // same enum class
		// 	&& ((ET)obj).ordinal() == ordinal; // same enum value
	}
	
	@Override
	public final int hashCode() {
		return super.hashCode();
	}
	
	@Override
	public String toString() { return getClass().getSimpleName()+'-'+(enumData.initialized?name():String.valueOf(ordinal)); }
	
	// the below methods provide support for GEnumMap
	
	public DataEntry<T, ET> entry(T value) {
		enumData.checkInit();
		return new DataEntry<>(enumData.values[ordinal], value);
	}
}
