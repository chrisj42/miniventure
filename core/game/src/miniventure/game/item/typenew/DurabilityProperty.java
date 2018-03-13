package miniventure.game.item.typenew;

public class DurabilityProperty implements UsageBehavior {
	
	private final int maxDurability;
	
	public DurabilityProperty(int maxDurability) {
		this.maxDurability = maxDurability;
	}
	
	@Override
	public Item getUsedItem(Item item) {
		int dura = Integer.parseInt(item.getData(DurabilityProperty.class, 0));
		
		dura--;
		if(dura <= 0)
			return null;
		
		Item newItem = item.copy();
		newItem.setData(DurabilityProperty.class, 0, dura+"");
		return newItem;
	}
	
	@Override
	public String[] getInitialData() {
		return new String[] {maxDurability+""};
	}
}
