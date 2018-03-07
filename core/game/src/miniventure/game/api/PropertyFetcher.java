package miniventure.game.api;

public interface PropertyFetcher<P extends Property<P>> {
	P[] getProperties();
}
