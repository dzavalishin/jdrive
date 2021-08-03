package game.struct;

public class GameSettingData {
	public int min;
	public int max;
	public int step;
	public /*StringID*/ int str;

	public GameSettingData(int i, int j, int k, int s) {
		min = i;
		max = j;
		step = k;
		str = s;
	}
	
}
