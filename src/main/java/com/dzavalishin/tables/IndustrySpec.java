package com.dzavalishin.tables;

public class IndustrySpec {
	public final IndustryTileTable [][]table;
	//int num_table;
	
	public final int a,b,c;
	
	public final byte [] produced_cargo;// = new byte[2];
	public final byte [] production_rate;// = new byte[2];
	public final byte [] accepts_cargo;// = new byte[3];
	
	public final int check_proc;

	public IndustrySpec(
			IndustryTileTable [][] tbl, 
			int a, int b, int c, 
			int p1, int p2, 
			int r1, int r2, 
			int a1, int a2, int a3, 
			int proc) 
	{
		table = tbl;
		this.a = a;
		this.b = b;
		this.c = c;

		produced_cargo = new byte[2];
		production_rate = new byte[2];
		accepts_cargo = new byte[3];

		produced_cargo[0] = (byte) p1;
		produced_cargo[1] = (byte) p2;

		production_rate[0] = (byte) r1;
		production_rate[1] = (byte) r2;

		accepts_cargo[0] = (byte) a1;
		accepts_cargo[1] = (byte) a2;
		accepts_cargo[2] = (byte) a3;

		check_proc = proc;
	}
}
