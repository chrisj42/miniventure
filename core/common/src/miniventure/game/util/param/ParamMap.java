package miniventure.game.util.param;

import java.util.HashMap;

import org.jetbrains.annotations.NotNull;

public class ParamMap {
	
	private HashMap<Param, Value> map;
	
	public ParamMap(Value... values) {
		map = new HashMap<>(values.length);
		for(Value v: values)
			map.put(v.getParam(), v);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(@NotNull Param<T> param) {
		Value<T> val = map.get(param);
		if(val == null) return param.getDefault();
		return val.get();
	}
	
}
