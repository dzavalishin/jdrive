package com.dzavalishin.struct;

public class GameSettingData {
	public final int min;
	public final int max;
	public final int step;
	public final /*StringID*/ int str;

	public GameSettingData(int i, int j, int k, int s) {
		min = i;
		max = j;
		step = k;
		str = s;
	}
	
}
