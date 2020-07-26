package miniventure.game.util.param;

import miniventure.game.util.function.FetchFunction;
import miniventure.game.util.function.MapFunction;

public class FetchParam<TParam, TReturn> extends Param<MapFunction<TParam, TReturn>> {
	
	public FetchParam(MapFunction<TParam, TReturn> defaultValue) {
		super(defaultValue);
	}
	
	@Override
	public FetchValue<TParam, TReturn> as(MapFunction<TParam, TReturn> val) {
		return new FetchValue<>(this, val);
	}
	
	public FetchValue<TParam, TReturn> as(FetchFunction<TReturn> fetcher) {
		return new FetchValue<>(this, param -> fetcher.get());
	}
	
	public static class FetchValue<TParam, TReturn> extends Value<MapFunction<TParam, TReturn>> {
		
		protected FetchValue(FetchParam<TParam, TReturn> param, MapFunction<TParam, TReturn> value) {
			super(param, value);
		}
		
	}
}
