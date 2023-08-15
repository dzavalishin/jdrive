package com.dzavalishin.game;

import com.dzavalishin.tables.CurrencySpec;

public class Currency {

	public static final int CF_NOEURO = 0;
	public static final int CF_ISEURO = 1;
	
	public final static int _currency_string_list[] = {
		Str.STR_CURR_GBP,
		Str.STR_CURR_USD,
		Str.STR_CURR_EUR,
		Str.STR_CURR_YEN,
		Str.STR_CURR_ATS,
		Str.STR_CURR_BEF,
		Str.STR_CURR_CHF,
		Str.STR_CURR_CZK,
		Str.STR_CURR_DEM,
		Str.STR_CURR_DKK,
		Str.STR_CURR_ESP,
		Str.STR_CURR_FIM,
		Str.STR_CURR_FRF,
		Str.STR_CURR_GRD,
		Str.STR_CURR_HUF,
		Str.STR_CURR_ISK,
		Str.STR_CURR_ITL,
		Str.STR_CURR_NLG,
		Str.STR_CURR_NOK,
		Str.STR_CURR_PLN,
		Str.STR_CURR_ROL,
		Str.STR_CURR_RUR,
		Str.STR_CURR_SEK,
		Str.STR_CURR_CUSTOM,
		Str.INVALID_STRING
	};

	// NOTE: Make sure both lists are in the same order
	// + 1 string list terminator
	//assert_compile(lengthof(_currency_specs) + 1 == lengthof(_currency_string_list));


	// get a mask of the allowed currencies depending on the year
	public static int GetMaskOfAllowedCurrencies()
	{
		int mask = 0;
		int i;

		for (i = 0; i != CurrencySpec._currency_specs.length; i++) {
			long to_euro = CurrencySpec._currency_specs[i].to_euro;

			if (to_euro != CF_NOEURO && to_euro != CF_ISEURO && Global.get_cur_year() >= to_euro - Global.MAX_YEAR_BEGIN_REAL) continue;
			if (to_euro == CF_ISEURO && Global.get_cur_year() < 2000 - Global.MAX_YEAR_BEGIN_REAL) continue;
			mask |= (1 << i);
		}
		mask |= (1 << 23); // always allow custom currency
		return mask;
	}


	public static void CheckSwitchToEuro()
	{
		if (CurrencySpec._currency_specs[GameOptions._opt.currency].to_euro != CF_NOEURO &&
				CurrencySpec._currency_specs[GameOptions._opt.currency].to_euro != CF_ISEURO &&
						Global.MAX_YEAR_BEGIN_REAL + Global.get_cur_year() >= CurrencySpec._currency_specs[GameOptions._opt.currency].to_euro) {
			GameOptions._opt.currency = 2; // this is the index of euro above.
			NewsItem.AddNewsItem(Str.STR_EURO_INTRODUCE, NewsItem.NEWS_FLAGS(NewsItem.NM_NORMAL, 0, NewsItem.NT_ECONOMY, 0), 0, 0);
		}
	}
	
	
}
