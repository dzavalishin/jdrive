package com.dzavalishin.game;

import java.io.Serializable;
import java.util.Arrays;

// in original code it is an array
// typedef uint AcceptedCargo[NUM_CARGO];

public class AcceptedCargo implements Serializable 
{
	private static final long serialVersionUID = -8588718305311725293L;
	
	public final int[] ct = new int[NUM_CARGO];

	public void clear()
	{
		Arrays.fill(ct, 0);
	}

	public AcceptedCargo() {
		clear();
	}
	
	// Temperate
	public static final int CT_PASSENGERS = 0;
	public static final int CT_COAL = 1;
	public static final int CT_MAIL = 2;
	public static final int CT_OIL = 3;
	public static final int CT_LIVESTOCK = 4;
	public static final int CT_GOODS = 5;
	public static final int CT_GRAIN = 6;
	public static final int CT_WOOD = 7;
	public static final int CT_IRON_ORE = 8;
	public static final int CT_STEEL = 9;
	public static final int CT_VALUABLES = 10;
	public static final int CT_FOOD = 11;

	// Arctic
	public static final int CT_WHEAT = 6;
	public static final int CT_HILLY_UNUSED = 8;
	public static final int CT_PAPER = 9;
	public static final int CT_GOLD = 10;

	// Tropic
	public static final int CT_RUBBER = 1;
	public static final int CT_FRUIT = 4;
	public static final int CT_MAIZE = 6;
	public static final int CT_COPPER_ORE = 8;
	public static final int CT_WATER = 9;
	public static final int CT_DIAMONDS = 10;

	// Toyland
	public static final int CT_SUGAR = 1;
	public static final int CT_TOYS = 3;
	public static final int CT_BATTERIES = 4;
	public static final int CT_CANDY = 5;
	public static final int CT_TOFFEE = 6;
	public static final int CT_COLA = 7;
	public static final int CT_COTTON_CANDY = 8;
	public static final int CT_BUBBLES = 9;
	public static final int CT_PLASTIC = 10;
	public static final int CT_FIZZY_DRINKS = 11;

	public static final int NUM_CARGO = 12;

	public static final int CT_INVALID = -1;


}
