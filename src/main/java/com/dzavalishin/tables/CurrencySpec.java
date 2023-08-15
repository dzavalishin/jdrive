package com.dzavalishin.tables;

public class CurrencySpec {

	public int rate;
	public  char separator;
	public  long to_euro;
	public  String prefix;
	public  String suffix;

	public static final int CF_NOEURO = 0;
	public static final int CF_ISEURO = 1;


	public CurrencySpec( int rate, char sep, long to_euro, String pref, String suff )
	{
		this.rate = rate;
		this.separator = sep;
		this.to_euro = to_euro;
		this.prefix = pref;
		this.suffix = suff;
	}

	//exchange rate    prefix
	//|  separator        |     postfix
	//|   |    Euro year  |       |
	//|   |    |          |       |
	public static final CurrencySpec[] _currency_specs = {
			//{    1, ',', CF_NOEURO, "\xA3", ""     }, // british pounds
			new CurrencySpec(    1, ',', CF_NOEURO, "", "gbp"     ), // british pounds
			new CurrencySpec(    2, ',', CF_NOEURO, "$",    ""     ), // us dollars
			new CurrencySpec(    2, ',', CF_ISEURO, "�",    ""     ), // Euro
			//{  200, ',', CF_NOEURO, "\xA5", ""     ), // yen
			new CurrencySpec(    200, ',', CF_NOEURO, "", "yen"     ), // yen
			new CurrencySpec(    19, ',', 2002,      "",     " S."  ), // austrian schilling
			new CurrencySpec(    57, ',', 2002,      "BEF ", ""     ), // belgian franc
			new CurrencySpec(    2, ',', CF_NOEURO, "CHF ", ""     ), // swiss franc
			new CurrencySpec(    50, ',', CF_NOEURO, "",     " Kc"  ), // czech koruna // TODO: Should use the "c" with an upside down "^"
			new CurrencySpec(    4, '.', 2002,      "DM ",  ""     ), // deutsche mark
			new CurrencySpec(    10, '.', CF_NOEURO, "",     " kr"  ), // danish krone
			new CurrencySpec(    200, '.', 2002,      "Pts ", ""     ), // spanish pesetas
			new CurrencySpec(    8, ',', 2002,      "",     " mk"  ), // finnish markka
			new CurrencySpec(    10, '.', 2002,      "FF ",  ""     ), // french francs
			new CurrencySpec(    480, ',', 2002,      "",     "Dr."  ), // greek drachma
			new CurrencySpec(    376, ',', 2002,      "",     " Ft"  ), // hungarian forint
			new CurrencySpec(    130, '.', CF_NOEURO, "",     " Kr"  ), // icelandic krona
			new CurrencySpec(    2730, ',', 2002,      "",     " L."  ), // italian lira
			new CurrencySpec(    3, ',', 2002,      "NLG ", ""     ), // dutch gulden
			new CurrencySpec(    11, '.', CF_NOEURO, "",     " Kr"  ), // norwegian krone
			new CurrencySpec(    6, ' ', CF_NOEURO, "",     " zl"  ), // polish zloty
			new CurrencySpec(    6, '.', CF_NOEURO, "",     " Lei" ), // romanian Lei
			new CurrencySpec(    5, ' ', CF_NOEURO, "",     " p"   ), // russian rouble
			new CurrencySpec(    13, '.', CF_NOEURO, "",     " Kr"  ), // swedish krona
			new CurrencySpec(    1, ' ', CF_NOEURO, "",     ""     ), // custom currency
	};


}

