package game.struct;

public class PlayerEconomyEntry {
	
	public int income;
	public int expenses;
	public int delivered_cargo;
	public int performance_history;	// player score (scale 0-1000)
	public long company_value;

	public PlayerEconomyEntry()
	{
		income=expenses=delivered_cargo=performance_history = 0;	// player score (scale 0-1000)		
		company_value = 0;
	}
	
}
