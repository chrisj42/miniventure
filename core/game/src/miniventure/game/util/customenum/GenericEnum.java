package miniventure.game.util.customenum;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unchecked")
public abstract class GenericEnum<EC extends GenericEnum<EC>> implements Comparable<GenericEnum<EC>> {
	
	private static final HashMap<Class<? extends GenericEnum<? extends GenericEnum>>, EnumData<? extends GenericEnum>> enumClasses = new HashMap<>();
	
	protected static final <EC extends GenericEnum<EC>> void registerEnum(Class<EC> clazz, int constants) {
		enumClasses.put(clazz, new EnumData<>(clazz, constants));
	}
	
	private static final class EnumData<EC extends GenericEnum<EC>> {
		private final Class<EC> enumClass;
		private final String[] names;
		private final EC[] values;
		private final HashMap<String, EC> nameToValue;
		
		private int counter = 0;
		private boolean initialized = false;
		
		private EnumData(Class<EC> enumClass, int constants) {
			this.enumClass = enumClass;
			names = new String[constants];
			values = (EC[])Array.newInstance(enumClass, constants);
			nameToValue = new HashMap<>(constants);
		}
		
		void checkInit() {
			if(!initialized)
				initialize();
		}
		
		private void initialize() {
			initialized = true;
			ArrayList<Field> enumTypes = new ArrayList<>(values.length);
			
			// WARNING: getDeclaredFields makes no guarantee that the fields are returned in order of declaration. So don't rely on that.
			for(Field field: enumClass.getDeclaredFields()) {
				if(Modifier.isStatic(field.getModifiers()) && enumClass.isAssignableFrom(field.getType()))
					enumTypes.add(field);
			}
			
			try {
				for(Field type: enumTypes) {
					EC instance = enumClass.cast(type.get(null));
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
	/*private <C extends GenericEnum<C, T>> ConstantData<C, T> getConstantData(String name) {
		return new ConstantData<>((EnumData<C>) enumData, ordinal, name, (C)this);
	}
	private static class ConstantData<EC extends GenericEnum<EC, T>, T> {
		private final EnumData<EC> enumData;
		private final int ordinal;
		private final String name;
		private final EC constant;
		
		private ConstantData(EnumData<EC> enumData, int ordinal, String name, EC constant) {
			this.enumData = enumData;
			this.ordinal = ordinal;
			this.name = name;
			this.constant = constant;
		}
	}*/
	
	/*protected static final <C extends GenericEnum<?>> C[] getConstants(Class<C> clazz) {
		
	}*/
	
	public static final <EC extends GenericEnum<EC>> EC valueOf(Class<EC> clazz, String name) {
		if(!enumClasses.containsKey(clazz))
			throw new EnumClassNotRegisteredException(clazz);
		
		EnumData<EC> enumData = (EnumData<EC>) enumClasses.get(clazz);
		enumData.checkInit();
		EC constant = enumData.nameToValue.get(name);
		if(constant == null)
			throw new EnumConstantNotFoundException(clazz, name);
		
		return constant;
	}
	
	
	private final EnumData<EC> enumData;
	private final int ordinal;
	// private final Class<T> typeClass;
	
	/** @noinspection SuspiciousMethodCalls*/
	protected GenericEnum(/*Class<T> typeClass*/) {
		// this.typeClass = typeClass;
		Class<? extends GenericEnum> clazz = getClass();
		if(clazz.isAnonymousClass())
			clazz = (Class<? extends GenericEnum>) clazz.getSuperclass();
		if(!enumClasses.containsKey(clazz))
			throw new EnumClassNotRegisteredException(clazz);
		
		try {
			enumData = (EnumData<EC>) enumClasses.get(clazz);
			ordinal = enumData.counter++;
			enumData.values[ordinal] = (EC) this;
		} catch(ClassCastException e) {
			throw new EnumGenericTypeMismatchException(this, e);
		}
	}
	
	public final int ordinal() { return ordinal; }
	public final String name() {
		enumData.checkInit();
		return enumData.names[ordinal];
	}
	
	@Override
	public final int compareTo(@NotNull GenericEnum<EC> o) {
		return Integer.compare(ordinal, o.ordinal);
	}
	
	@Override
	public final boolean equals(Object obj) {
		return enumData.enumClass.isAssignableFrom(obj.getClass()) // same enum class
			&& ((GenericEnum)obj).ordinal == ordinal; // same enum value
	}
	
	@Override
	public final int hashCode() {
		return ordinal;
	}
	
	@Override
	public final String toString() { return getClass().getSimpleName()+'-'+(enumData.initialized?name():String.valueOf(ordinal)); }
}
