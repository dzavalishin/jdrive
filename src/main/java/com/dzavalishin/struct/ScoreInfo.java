package com.dzavalishin.struct;

public class ScoreInfo 
{
	public final int id;			// Unique ID of the score
	public final int needed;		// How much you need to get the perfect score
	public final int score;			// How much score it will give

	public ScoreInfo(int id, int needed, int score ) {
		this.id = id;
		this.needed = needed;
		this.score = score;
	}
}
