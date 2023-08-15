package com.dzavalishin.util;

public class YearMonthDay {
	public int year;
	public int month;
	public int day;
	
	public YearMonthDay(int date) {
		ConvertDayToYMD(date);
	}

	public void ConvertDayToYMD(int date)
	{
		int yr = date / (365+365+365+366);
		int rem = date % (365+365+365+366);
		int x;
	
		yr *= 4;
	
		if (rem >= 366) {
			rem--;
			do {
				rem -= 365;
				yr++;
			} while (rem >= 365);
			if (rem >= 31+28) rem++;
		}
	
		year = yr;
	
		x = GameDate._month_date_from_year_day[rem];
		month = x >> 5;
		day = x & 0x1F;
	}
}
