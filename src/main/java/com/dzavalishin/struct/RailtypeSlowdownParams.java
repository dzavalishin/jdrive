package com.dzavalishin.struct;

public class RailtypeSlowdownParams
{
	public final int small_turn;
	public final int large_turn;
	public final int z_up; // fraction to remove when moving up
	public final int z_down; // fraction to remove when moving down

	public RailtypeSlowdownParams(int i, int j, int k, int l) {
		small_turn = i;
		large_turn = j;
		z_up = k;
		z_down = l;
	}
}