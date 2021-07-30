package game.struct;

public class RailtypeSlowdownParams
{
	public int small_turn; 
	public int large_turn;
	public int z_up; // fraction to remove when moving up
	public int z_down; // fraction to remove when moving down

	public RailtypeSlowdownParams(int i, int j, int k, int l) {
		small_turn = i;
		large_turn = j;
		z_up = k;
		z_down = l;
	}
}