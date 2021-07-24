package game;

public class PlayerEconomyEntry {
	
	int income;
	int expenses;
	int delivered_cargo;
	int performance_history;	// player score (scale 0-1000)
	long company_value;

	public PlayerEconomyEntry()
	{
		income=expenses=delivered_cargo=performance_history = 0;	// player score (scale 0-1000)		
		company_value = 0;
	}
	
}
