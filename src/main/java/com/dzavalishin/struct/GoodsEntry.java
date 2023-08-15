package com.dzavalishin.struct;

import java.io.Serializable;

public class GoodsEntry implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	public int waiting_acceptance;
	public int days_since_pickup;
	public int rating;
	public int enroute_from;
	public int enroute_time;
	public int last_speed;
	public int last_age;
	public int feeder_profit;

}