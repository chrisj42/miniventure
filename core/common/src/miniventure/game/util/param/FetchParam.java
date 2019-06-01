package miniventure.game.util.param;

import miniventure.game.util.function.FetchFunction;
import miniventure.game.util.function.MapFunction;

public class FetchParam<TParam, TReturn> extends Param<MapFunction<TParam, TReturn>> {
	
	public FetchParam(MapFunction<TParam, TReturn> defaultValue) {
		super(defaultValue);
	}
	
	public Value<MapFunction<TParam, TReturn>> as(FetchFunction<TReturn> fetcher) {
		return as(param -> fetcher.get());
	}
	
}
