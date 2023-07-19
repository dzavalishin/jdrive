package game.struct;

public class RememberData 
{
	public RememberData() {
		cur_length = 0;
		depth = 0;
		pft_var6 = 0;
	}

	public RememberData(RememberData rd) {
		cur_length = rd.cur_length;
		depth = rd.depth;
		pft_var6 = rd.pft_var6;
	}
	
	public int cur_length;
	public int depth;
	public int pft_var6;
}
