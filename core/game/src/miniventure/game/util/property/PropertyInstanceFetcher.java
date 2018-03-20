package miniventure.game.util.property;

public interface PropertyInstanceFetcher<PT extends Property> {
	
	PT getPropertyInstance(PT instanceTemplate);
	
}
