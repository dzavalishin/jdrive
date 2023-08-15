package com.dzavalishin.util;

import com.dzavalishin.game.GameOptions;
import com.dzavalishin.game.Global;
import com.dzavalishin.game.Industry;
import com.dzavalishin.game.Landscape;
import com.dzavalishin.game.Station;
import com.dzavalishin.game.Str;
import com.dzavalishin.game.Town;
import com.dzavalishin.game.Version;
import com.dzavalishin.game.WayPoint;
import com.dzavalishin.ids.StringID;
import com.dzavalishin.tables.CurrencySpec;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Strings extends StringTable
{

	public static final DynamicLanguages _dynlang = new DynamicLanguages();

	public static BinaryString _userstring;

	public static final String _openttd_revision = Version.NAME;


	// special string constants
	//enum SpecialStrings {

	// special strings for town names. the town name is generated dynamically on request.
	public static final int SPECSTR_TOWNNAME_START = 0x20C0;
	public static final int SPECSTR_TOWNNAME_ENGLISH = SPECSTR_TOWNNAME_START;
	public static final int SPECSTR_TOWNNAME_RUSSIAN = SPECSTR_TOWNNAME_START+1;
	public static final int SPECSTR_TOWNNAME_FRENCH = SPECSTR_TOWNNAME_START+2;
	public static final int SPECSTR_TOWNNAME_GERMAN = SPECSTR_TOWNNAME_START+3;
	public static final int SPECSTR_TOWNNAME_AMERICAN = SPECSTR_TOWNNAME_START+4;
	public static final int SPECSTR_TOWNNAME_LATIN = SPECSTR_TOWNNAME_START+5;
	public static final int SPECSTR_TOWNNAME_SILLY = SPECSTR_TOWNNAME_START+6;
	public static final int SPECSTR_TOWNNAME_SWEDISH = SPECSTR_TOWNNAME_START+7;
	public static final int SPECSTR_TOWNNAME_DUTCH = SPECSTR_TOWNNAME_START+8;
	public static final int SPECSTR_TOWNNAME_FINNISH = SPECSTR_TOWNNAME_START+9;
	public static final int SPECSTR_TOWNNAME_POLISH = SPECSTR_TOWNNAME_START+10;
	public static final int SPECSTR_TOWNNAME_SLOVAKISH = SPECSTR_TOWNNAME_START+11;
	public static final int SPECSTR_TOWNNAME_NORWEGIAN = SPECSTR_TOWNNAME_START+12;
	public static final int SPECSTR_TOWNNAME_HUNGARIAN = SPECSTR_TOWNNAME_START+13;
	public static final int SPECSTR_TOWNNAME_AUSTRIAN = SPECSTR_TOWNNAME_START+14;
	public static final int SPECSTR_TOWNNAME_ROMANIAN = SPECSTR_TOWNNAME_START+15;
		/*
		public static final int SPECSTR_TOWNNAME_CZECH;
		public static final int SPECSTR_TOWNNAME_SWISS;
		public static final int SPECSTR_TOWNNAME_DANISH;
		public static final int SPECSTR_TOWNNAME_LAST = SPECSTR_TOWNNAME_DANISH;
	 */
	//public static final int SPECSTR_TOWNNAME_LAST = SPECSTR_TOWNNAME_RUSSIAN;
	public static final int SPECSTR_TOWNNAME_LAST = SPECSTR_TOWNNAME_ROMANIAN;

	// special strings for player names on the form "TownName transport".
	public static final int SPECSTR_PLAYERNAME_START = 0x70EA;
	public static final int SPECSTR_PLAYERNAME_ENGLISH = SPECSTR_PLAYERNAME_START;
	public static final int SPECSTR_PLAYERNAME_FRENCH = SPECSTR_PLAYERNAME_START+1;
	public static final int SPECSTR_PLAYERNAME_GERMAN = SPECSTR_PLAYERNAME_START+2;
	public static final int SPECSTR_PLAYERNAME_AMERICAN = SPECSTR_PLAYERNAME_START+3;
	public static final int SPECSTR_PLAYERNAME_LATIN = SPECSTR_PLAYERNAME_START+4;
	public static final int SPECSTR_PLAYERNAME_SILLY = SPECSTR_PLAYERNAME_START+5;
	public static final int SPECSTR_PLAYERNAME_LAST = SPECSTR_PLAYERNAME_SILLY;

	public static final int SPECSTR_ANDCO_NAME = 0x70E6;
	public static final int SPECSTR_PRESIDENT_NAME = 0x70E7;
	public static final int SPECSTR_SONGNAME = 0x70E8;

	// reserve 32 strings for the *.lng files
	public static final int SPECSTR_LANGUAGE_START = 0x7100;
	public static final int SPECSTR_LANGUAGE_END = 0x711f;

	// reserve 32 strings for various screen resolutions
	public static final int SPECSTR_RESOLUTION_START = 0x7120;
	public static final int SPECSTR_RESOLUTION_END = 0x713f;

	// reserve 32 strings for screenshot formats
	public static final int SPECSTR_SCREENSHOT_START = 0x7140;
	public static final int SPECSTR_SCREENSHOT_END = 0x715F;

	// Used to implement SetDParamStr
	public static final int STR_SPEC_DYNSTRING = 0xF800;
	public static final int STR_SPEC_USERSTRING = 0xF808;


	private static BinaryString []_langpack_offs;
	private static LanguagePack _langpack;
	private static final int [] _langtab_num = new int [32]; // Offset into langpack offs
	private static final int [] _langtab_start = new int[32]; // Offset into langpack offs

	//private final StringID _cargo_string_list[NUM_LANDSCAPE][NUM_CARGO] = {
	private final static int _cargo_string_list[][] = {
			{ /* LT_NORMAL */
				STR_PASSENGERS,
				STR_TONS,
				STR_BAGS,
				STR_LITERS,
				STR_ITEMS,
				STR_CRATES,
				STR_TONS,
				STR_TONS,
				STR_TONS,
				STR_TONS,
				STR_BAGS,
				STR_RES_OTHER
			},

			{ /* LT_HILLY */
				STR_PASSENGERS,
				STR_TONS,
				STR_BAGS,
				STR_LITERS,
				STR_ITEMS,
				STR_CRATES,
				STR_TONS,
				STR_TONS,
				STR_RES_OTHER,
				STR_TONS,
				STR_BAGS,
				STR_TONS
			},

			{ /* LT_DESERT */
				STR_PASSENGERS,
				STR_LITERS,
				STR_BAGS,
				STR_LITERS,
				STR_TONS,
				STR_CRATES,
				STR_TONS,
				STR_TONS,
				STR_TONS,
				STR_LITERS,
				STR_BAGS,
				STR_TONS
			},

			{ /* LT_CANDY */
				STR_PASSENGERS,
				STR_TONS,
				STR_BAGS,
				STR_NOTHING,
				STR_NOTHING,
				STR_TONS,
				STR_TONS,
				STR_LITERS,
				STR_TONS,
				STR_NOTHING,
				STR_LITERS,
				STR_NOTHING
			}
	};


	//Read an int from the argv array.
	private static int Getint(Object o)
	{
		assert o != null;
		return ((Integer)o).intValue();
	}

	//Read an long from the argv array.
	/*private static long Getlong(Object o)
	{
		assert o != null;
		return ((Long)o).longValue();
	}*/

	//Extract subarray
	private static Object[] GetArgvPtr(Object [] o, int pos, int size )
	{
		assert o != null;

		Object[] ret = new Object[size];
		System.arraycopy(o, pos, ret, 0, size);
		return ret;
	}


	/*
private  long Getlong(final int **argv)
{
	long result;

	assert(argv);
	result = (int)(*argv)[0] + ((ulong)(int)(*argv)[1] << 32);
	(*argv)+=2;
	return result;
}


// Read an array from the argv array.
private  final int *GetArgvPtr(final int **argv, int n)
{
	final int *result;
	assert(*argv);
	result = *argv;
	(*argv) += n;
	return result;
}

	 */
	private static final int NUM_BOUND_STRINGS = 8;

	// Array to hold the bound strings.
	private static final String[] _bound_strings = new String[NUM_BOUND_STRINGS];

	// This index is used to implement a "round-robin" allocating of
	// slots for BindCString. NUM_BOUND_STRINGS slots are reserved.
	// Which means that after NUM_BOUND_STRINGS calls to BindCString,
	// the indices will be reused.
	private static int _bind_index = 0;

	//private static String StringGetStringPtr(StringID string)
	private static BinaryString StringGetStringPtr(int string)
	{
		return _langpack_offs[_langtab_start[string>> 11] + (string & 0x7FF)];
	}


	// The highest 8 bits of string contain the "case index".
	// These 8 bits will only be set when FormatString wants to print
	// the string in a different case. No one else except FormatString
	// should set those bits.
	public static String GetStringWithArgs(int string, Object ... argv )
	{
		int index = BitOps.GB(string,  0, 11);
		int tab   = BitOps.GB(string, 11,  5);

		int argc = 0;

		if (BitOps.GB(string, 0, 16) == 0) 
			Global.error("!invalid string id 0 in GetString");

		switch (tab) {
		case 4:
			if (index >= 0xC0)
				return GetSpecialTownNameString( index - 0xC0, Getint(argv[argc++]));
			break;

		case 14:
			if (index >= 0xE4)
				return GetSpecialPlayerNameString( index - 0xE4, argv);
			break;

			// User defined name
		case 15:
			return Global.GetName(index);

		case 31:
			// dynamic strings. These are NOT to be passed through the formatter,
			// but passed through verbatim.
			if (index < (STR_SPEC_USERSTRING & 0x7FF)) {
				return  _bound_strings[index];
			}

			return FormatString( _userstring, null, 0);
		}

		if (index >= _langtab_num[tab]) {
			Global.error(
					"!String 0x%X is invalid. " +
							"Probably because an old version of the .lng file.\n", string
					);
		}

		return FormatString( StringGetStringPtr(BitOps.GB(string, 0, 16)), argv, BitOps.GB(string, 24, 8));
	}

	public static String GetString(StringID string)
	{
		return GetStringWithArgs(string.id, (Object[])Global._decode_parameters);
	}

	public static String GetString(int string)
	{
		return GetStringWithArgs(string, (Object[])Global._decode_parameters);
	}


	// This function takes a C-string and allocates a temporary string ID.
	// The duration of the bound string is valid only until the next GetString,
	// so be careful.
	//static StringID BindCString(final String str)
	public static int BindCString(final String str)
	{
		int idx = (++_bind_index) & (NUM_BOUND_STRINGS - 1);
		_bound_strings[idx] = str;
		return idx + STR_SPEC_DYNSTRING;
	}

	// This function is used to "bind" a C string to a OpenTTD dparam slot.
	public static void SetDParamStr(int n, final String str)
	{
		Global.SetDParam(n, BindCString(str));
	}


	private static final int _divisor_table[] = {
			1000000000,
			100000000,
			10000000,
			1000000,

			100000,
			10000,
			1000,
			100,
			10,
			1
	};

	private static String FormatCommaNumber(int number)
	{
		int quot,divisor;
		int i;
		int tot;
		int num;
		StringBuilder buff = new StringBuilder();

		if (number < 0) {
			buff.append( '-' );
			number = -number;
		}

		num = number;

		tot = 0;
		for (i = 0; i != 10; i++) {
			divisor = _divisor_table[i];
			quot = 0;
			if (num >= divisor) {
				quot = num / _divisor_table[i];
				num = num % _divisor_table[i];
			}
			if ( 0 != (tot |= quot) || (i == 9)) {
				buff.append( (char)('0' + quot) );
				if (i == 0 || i == 3 || i == 6) buff.append( ',' );
			}
		}

		return buff.toString();
	}

	private static String FormatNoCommaNumber(int number)
	{
		int quot,divisor;
		int i;
		int tot;
		int num;
		StringBuilder buff = new StringBuilder();

		if (number < 0) {
			buff.append( '-' );
			number = -number;
		}

		num = number;

		tot = 0;
		for (i = 0; i != 10; i++) {
			divisor = _divisor_table[i];
			quot = 0;
			if (num >= divisor) {
				quot = num / _divisor_table[i];
				num = num % _divisor_table[i];
			}
			if ( (0 != (tot |= quot)) || (i == 9)) {
				buff.append( (char) ('0' + quot) );
			}
		}

		return buff.toString();
	}


	private static String FormatYmdString(int number)
	{
		StringBuilder buff = new StringBuilder();

		YearMonthDay ymd = new YearMonthDay(number);

		buff.append(StringGetStringPtr(ymd.day + STR_01AC_1ST - 1));

		// TODO buff[-1] = ' ';
		buff.append(StringGetStringPtr(STR_0162_JAN + ymd.month));
		//TODO buff[3] = ' ';

		buff.append( FormatNoCommaNumber( ymd.year + Global.MAX_YEAR_BEGIN_REAL) );
		return buff.toString();
	}

	private static String FormatMonthAndYear(int number)
	{
		StringBuilder buff = new StringBuilder();

		YearMonthDay ymd = new YearMonthDay(number);

		//for (src = GetStringPtr(STR_MONTH_JAN + ymd.month); (*buff++ = *src++) != '\0';) {}
		buff.append( StringGetStringPtr(STR_MONTH_JAN + ymd.month) );
		// TODO buff[-1] = ' ';
		buff.append( "  " );
		buff.append( FormatNoCommaNumber( ymd.year + Global.MAX_YEAR_BEGIN_REAL) );

		return buff.toString();
	}

	private static String FormatTinyDate(int number)
	{
		StringBuilder buff = new StringBuilder();

		YearMonthDay ymd = new YearMonthDay(number);

		buff.append( String.format(" %02d-%02d-%04d", ymd.day, ymd.month + 1, ymd.year + Global.MAX_YEAR_BEGIN_REAL) );

		return buff.toString();
	}

	private static String FormatGenericCurrency(CurrencySpec spec, long number, boolean compact)
	{
		char buf[] = new char[40];
		int bi;
		int j;
		StringBuilder buff = new StringBuilder();

		// multiply by exchange rate
		number *= spec.rate;

		// convert from negative
		if (number < 0) {
			buff.append( '-' );
			number = -number;
		}

		// add prefix part
		//s = spec.prefix;
		//while (s != spec.prefix + lengthof(spec.prefix) && (c = *s++) != '\0') buff.append( c );
		//for( char pc : spec.prefix.toCharArray() )
		buff.append( spec.prefix );

		char compactChar = 0;

		// for huge numbers, compact the number into k or M
		if (compact) {
			compact = false;
			if (number >= 1000000000) {
				number = (number + 500000) / 1000000;
				compactChar = 'M';
			} else if (number >= 1000000) {
				number = (number + 500) / 1000;
				compactChar = 'k';
			}
		}

		// convert to ascii number and add commas
		//p = buf;
		bi = 0;
		j = 4;
		do {
			if (--j == 0) {
				//*p++ = spec.separator;
				buf[bi++] = spec.separator;
				j = 3;
			}
			buf[bi++] = (char) ('0' + number % 10);
		} while( 0 != (number /= 10) );
		do buff.append( buf[--bi] ); while (bi > 0);

		if (compactChar != 0) buff.append( compactChar );

		// add suffix part
		//s = spec.suffix;
		//while (s != spec.suffix + lengthof(spec.suffix) && (c = *s++) != '\0') buff.append( c );
		buff.append( spec.suffix );
		return buff.toString();
	}

	private static int DeterminePluralForm(int n)
	{
		// The absolute value determines plurality
		if (n < 0) n = -n;

		switch(_langpack.plural_form) {
		// Two forms, singular used for one only
		// Used in:
		//   Danish, Dutch, English, German, Norwegian, Swedish, Estonian, Finnish,
		//   Greek, Hebrew, Italian, Portuguese, Spanish, Esperanto
		case 0:
		default:
			return n != 1 ? 1 : 0;

			// Only one form
			// Used in:
			//   Hungarian, Japanese, Korean, Turkish
		case 1:
			return 0;

			// Two forms, singular used for zero and one
			// Used in:
			//   French, Brazilian Portuguese
		case 2:
			return n > 1 ? 1 : 0;

			// Three forms, special case for zero
			// Used in:
			//   Latvian
		case 3:
		{
			final int nn = n != 0 ? 1 : 2;
			return n%10==1 && n%100!=11 ? 0 : nn;
		}
		// Three forms, special case for one and two
		// Used in:
		//   Gaelige (Irish)
		case 4:
		{
			final int nn = n==2 ? 1 : 2;
			return n==1 ? 0 : nn;
		}
		// Three forms, special case for numbers ending in 1[2-9]
		// Used in:
		//   Lithuanian
		case 5:
		{
			final int nn = n%10>=2 && (n%100<10 || n%100>=20) ? 1 : 2;
			return n%10==1 && n%100!=11 ? 0 : nn;
		}
		// Three forms, special cases for numbers ending in 1 and 2, 3, 4, except those ending in 1[1-4]
		// Used in:
		//   Croatian, Czech, Russian, Slovak, Ukrainian
		case 6:
		{
			int nn = n%10>=2 && n%10<=4 && (n%100<10 || n%100>=20) ? 1 : 2;
			return n%10==1 && n%100!=11 ? 0 : nn;
		}
		// Three forms, special case for one and some numbers ending in 2, 3, or 4
		// Used in:
		//   Polish
		case 7:
		{
			int nn = n%10>=2 && n%10<=4 && (n%100<10 || n%100>=20) ? 1 : 2;
			return n==1 ? 0 : nn;
		}
		// Four forms, special case for one and all numbers ending in 02, 03, or 04
		// Used in:
		//   Slovenian
		case 8:
		{
			final int n0 = n%100==3 || n%100==4 ? 2 : 3;
			final int n1 = n%100==2 ? 1 : n0;
			return n%100==1 ? 0 : n1;
		}
		}
	}


	//private static String ParseStringChoice(String src, int form, int [] dstlen)
	private static String ParseStringChoice(String src, int form, int [] skip )
	{
		char [] ca = src.toCharArray();
		int cap = 0;

		//<NUM> {Length of each string} {each string}
		int n = 0xFF & ca[cap++];

		int i, mylen=0,mypos=0;
		int pos = 0;

		for(i=0; i != n; i++) 
		{
			int len = 0xFF & ca[cap++];
			if (i == form) {
				mypos = pos;
				mylen = len;
			}
			pos += len;
		}
		//if( dstlen != null ) dstlen[0] = mylen;
		//memcpy(dst, b + mypos, mylen);
		//return b + pos;
		skip[0] = cap+pos;
		return new String( ca, mypos+cap, mylen );
	}


	private static String ExtractChoice(String b, int[]skip, int form)
	{
		//<NUM> {Length of each string} {each string}
		int pos,i, mylen=0,mypos=0;

		int n = b.charAt(0); // hope it will take one byte
		pos=1;
		for(i=0; i!=n; i++) {
			int len = b.charAt(pos++);
			if (i == form) {
				mypos = pos;
				mylen = len;
			}
			pos += len;
		}
		skip[0] = pos;
		return b.substring(mypos, mypos+mylen-1);
	}

	//private static String FormatString(final String pstr, Object ... arg, int casei)
	private static String FormatString(final BinaryString pstr, Object [] arg, int casei)
	{
		char b;
		//final Object[] arg_orig = arg;
		int modifier = 0;
		StringBuilder buff = new StringBuilder();
		int argc = 0;

		int stri = 0;
		char[] str = pstr.toCharArray();

		while ( stri < str.length && (b = (char) (0xFF & str[stri++])) != '\0') 
		{

			switch (b) {
			case 0x1: // {SETX}
				buff.append( b );
				buff.append( str[stri++] );
				break;
			case 0x2: // {SETXY}
				buff.append(  b );
				buff.append( str[stri++] );
				buff.append( str[stri++] );
				break;

			case 0x81: // {STRINL}
				stri += 2;
				buff.append( GetStringWithArgs(pstr.READ_LE_int(stri-2), arg));
				//buff.append( "??" );
				break;
			case 0x82: // {DATE_LONG}
				buff.append( FormatYmdString( Getint(arg[argc++])) );
				break;
			case 0x83: // {DATE_SHORT}
				buff.append( FormatMonthAndYear(Getint(arg[argc++])) );
				break;
			case 0x84: {// {VELOCITY}
				int value = Getint(arg[argc++]);
				if (GameOptions._opt_ptr.kilometers) value = value * 1648 >> 10;
		buff.append( FormatCommaNumber(value) );
		if (GameOptions._opt_ptr.kilometers) {	buff.append( " km/h" );	} 
		else {	buff.append( " mph" );	}
		break;
			}
			// 0x85 is used as escape character..
			case 0x85:
				switch(str[stri++]) {
				case 0: /* {CURRCOMPACT} */
					// TODO buff.append( FormatGenericCurrency(Global._currency, Getint(arg[argc++]), true) );
					buff.append( "$$$" ); argc++;
					break;
				case 2: /* {REV} */
					buff.append( _openttd_revision );
					break;
				case 3: { /* {SHORTCARGO} */
					// Short description of cargotypes. Layout:
					// 8-bit = cargo type
					// 16-bit = cargo count 
					int cargo_str = _cargo_string_list[GameOptions._opt_ptr.landscape][Getint(arg[argc++])];
					int multiplier = (cargo_str == Str.STR_LITERS) ? 1000 : 1;
					// liquid type of cargo is multiplied by 100 to get correct amount
					buff.append( FormatCommaNumber(Getint(arg[argc++]) * multiplier) );
					buff.append( " " );
					buff.append( StringGetStringPtr(cargo_str) );
				} break;
				case 4: {/* {CURRCOMPACT64} */
					// 64 bit compact currency-unit
					//buff.append( FormatGenericCurrency(_currency, Getlong(arg[argc++]), true) );
					buff.append( "$$$" ); argc+=2;
					break;
				}
				case 5: { /* {STRING1} */
					// String that consumes ONE argument
					int sstr = modifier + Getint(arg[argc++]);
					buff.append( GetStringWithArgs(sstr, GetArgvPtr(arg, argc, 1)));
					argc += 1;
					modifier = 0;
					break;
				}
				case 6: { /* {STRING2} */
					// String that consumes TWO arguments
					int sstr = modifier + Getint(arg[argc++]);
					buff.append( GetStringWithArgs(sstr, GetArgvPtr(arg, argc, 2)));
					argc += 2;
					modifier = 0;
					break;
				}
				case 7: { /* {STRING3} */
					// String that consumes THREE arguments
					int sstr = modifier + Getint(arg[argc++]);
					buff.append( GetStringWithArgs(sstr, GetArgvPtr(arg, argc, 3)));
					argc += 3;
					modifier = 0;
					break;
				}
				case 8: { /* {STRING4} */
					// String that consumes FOUR arguments
					int sstr = modifier + Getint(arg[argc++]);
					buff.append( GetStringWithArgs(sstr, GetArgvPtr(arg, argc, 4)));
					argc += 4;
					modifier = 0;
					break;
				}
				case 9: { /* {STRING5} */
					// String that consumes FIVE arguments
					int sstr = modifier + Getint(arg[argc++]);
					buff.append( GetStringWithArgs(sstr, GetArgvPtr(arg, argc, 5)));
					argc += 5;
					modifier = 0;
					break;
				}

				case 10: { /* {STATIONFEATURES} */
					buff.append( StationGetSpecialString(Getint(arg[argc++])));
					break;
				}

				case 11: { /* {INDUSTRY} */
					Industry i = Industry.GetIndustry(Getint(arg[argc++]));
					Integer args[] = new Integer[2];

					// industry not valid anymore?
					if (i == null || !i.isValid())
						break;

					// First print the town name and the industry type name
					// The string STR_INDUSTRY_PATTERN controls the formatting
					args[0] = i.townId;
					args[1] = i.type + STR_4802_COAL_MINE;
					buff.append( FormatString( StringGetStringPtr(STR_INDUSTRY_FORMAT), args, modifier >> 24) );
					modifier = 0;
					break;
				}

				case 12: { // {VOLUME}
					buff.append( FormatCommaNumber(Getint(arg[argc++]) * 1000) );
					buff.append( " " );
					buff.append( FormatString( StringGetStringPtr(STR_LITERS), null, modifier >> 24) );
					modifier = 0;
					break;
				}

				case 13: { // {G 0 Der Die Das}
					/*
				//final byte* s = (final byte*)GetStringPtr(argv_orig[(byte)str[stri++]]); // contains the string that determines gender.
				final String s = StringGetStringPtr(arg[(byte)str[stri++]]); // contains the string that determines gender.
				int len;
				int gender = 0;
				if (s != null && s[0] == 0x87) gender = s[1];
				str = ParseStringChoice(str, gender, buff, &len);
				buff += len;
					 */


					//final byte* s = (final byte*)GetStringPtr(argv_orig[(byte)str[stri++]]); // contains the string that determines gender.
					//final BinaryString s = StringGetStringPtr((Integer)arg[0xFF & str[stri++]]); // contains the string that determines gender.

					int argindex = 0xFF & str[stri++];
					Object sNo = arg[argindex];
					BinaryString s = StringGetStringPtr((Integer)sNo);

					int gender = 0;
					if (s != null && s.charAt(0) == 0x87) gender = s.charAt(1);

					int[]skip = { 0 };
					buff.append( ExtractChoice( new String( str ).substring(stri), skip, gender) );
					//str = str.substring(skip[0] );
					assert skip[0] >= 0;
					stri += skip[0];

					//stri+=2;
					//buff.append( " !!fixCase13!! " );
					break;
				}

				case 14: { // {DATE_TINY}
					buff.append( FormatTinyDate(Getint(arg[argc++]) ) );
					break;
				}

				case 15: { // {CARGO}
					// Layout now is:
					//   8bit   - cargo type
					//   16-bit - cargo count
					//StringID
					{
						int cargo_str = Global._cargoc.names_long[Getint(arg[argc++])];
						buff.append( GetStringWithArgs(cargo_str, arg[argc++]) );
					}
					break;
				}

				default:
					Global.error("!invalid escape sequence in string");
				}
				break;

			case 0x86: // {SKIP}
				argc++;
				break;

				// This sets up the gender for the string.
				// We just ignore this one. It's used in {G 0 Der Die Das} to determine the case.
			case 0x87: // {GENDER 0}
				stri++;
				break;

			case 0x88: {// {STRING}
				int sstri = modifier + Getint(arg[argc++]);
				// WARNING. It's prohibited for the included string to consume any arguments.
				// For included strings that consume argument, you should use STRING1, STRING2 etc.
				// To debug stuff you can set argv to null and it will tell you
				//int acnt = arg.length - argc;
				//Object [] acopy = new Object[acnt];
				//System.arraycopy(arg, argc, acopy, 0, acnt);
				//argc += acnt; // TO DO wrong, must get used count from GetStringWithArgs 
				//buff.append( GetStringWithArgs(sstri, acopy) );
				buff.append( GetStringWithArgs(sstri) ); // 0 args
				modifier = 0;
				break;
			}

			case 0x8B: // {COMMA}
				buff.append( FormatCommaNumber(Getint(arg[argc++]) ) );
				break;

			case 0x8C: // Move argument pointer
				//argv = argv_orig + (byte)str[stri++];
				argc = str[stri++];
				break;

			case 0x8D: { // {P}
				//int v = argv_orig[(byte)str[stri++]]; // contains the number that determines plural
				int v = (Integer)arg[0xFF & str[stri++]]; // contains the number that determines plural
				int [] skip = { 0 };
				//str = ParseStringChoice(str, DeterminePluralForm(v), buff, len);
				//buff += len[0];
				buff.append( ParseStringChoice( new String( str, stri, str.length-stri ), DeterminePluralForm(v), skip) );
				stri += skip[0];
				break;
			}

			case 0x8E: // {NUM}
				buff.append( FormatNoCommaNumber(Getint(arg[argc++]) ) );
				break;

			case 0x8F: // {CURRENCY}
				// TODO buff.append( FormatGenericCurrency(_currency, Getint(arg[argc++]), false) );
				buff.append(Getint(arg[argc++])).append("$"); //argc++;
				break;

			case 0x99: { // {WAYPOINT}
				WayPoint wp = WayPoint.GetWaypoint(Getint(arg[argc++]));
				int sstr;
				//StringID str;
				if (wp.string.id != STR_NULL) {
					sstr = wp.string.id;
				} else {
					//int [] temp = new int[2];
					//temp[0] = wp.town_index;
					//temp[1] = wp.town_cn + 1;
					sstr = wp.town_cn == 0 ? STR_WAYPOINTNAME_CITY : STR_WAYPOINTNAME_CITY_SERIAL;
				}
				//buff.append( GetStringWithArgs(sstr, temp) );
				buff.append( GetStringWithArgs(sstr, wp.town_index, wp.town_cn + 1 ) );
			} break;

			case 0x9A: { // {STATION}
				final Station st = Station.GetStation(Getint(arg[argc++]));
				Integer [] temp = new Integer[2];

				if (st == null || st.getXy() == null) { // station doesn't exist anymore
					buff.append( GetStringWithArgs(STR_UNKNOWN_DESTINATION ) );
					break;
				}
				temp[0] = st.town.townnametype;
				temp[1] = st.town.townnameparts;
				buff.append( GetStringWithArgs(st.string_id, (Object[])temp) );
				break;
			}
			case 0x9B: { // {TOWN}
				final Town t = Town.GetTown(Getint(arg[argc++]));
				Integer[] temp = new Integer[1];

				assert(t.getXy() != null);

				temp[0] = t.townnameparts;
				buff.append( GetStringWithArgs( t.townnametype, (Object[])temp) );
				break;
			}

			case 0x9C: { // TODO {CURRENCY64}
				//buff.append( FormatGenericCurrency(_currency, Getlong(arg[argc++]), false) );
				buff.append(Getint(arg[argc++])).append("$"); //argc++;
				break;
			}

			case 0x9D: { // {SETCASE}
				// This is a pseudo command, it's outputted when someone does {STRING.ack}
				// The modifier is added to all subsequent GetStringWithArgs that accept the modifier.
				modifier = (byte)str[stri++] << 24;
				break;
			}

			case 0x9E: { // {Used to implement case switching}
				// <0x9E> <NUM CASES> <CASE1> <LEN1> <STRING1> <CASE2> <LEN2> <STRING2> <CASE3> <LEN3> <STRING3> <STRINGDEFAULT>
				// Each LEN is printed using 2 bytes in big endian order.
				int num = (byte)str[stri++];
				while (num != 0) {
					if ((byte)str[0] == casei) {
						// Found the case, adjust str pointer and continue
						stri += 3;
						break;
					}
					// Otherwise skip to the next case
					stri += 3 + (str[1] << 8) + str[2];
					num--;
				}
				break;
			}

			default:
				buff.append( b );
			}
		}

		return buff.toString();
	}


	private static String StationGetSpecialString(int x)
	{
		StringBuilder buff = new StringBuilder();

		if( 0 != (x & 0x01)) buff.append( 0x94 );
		if( 0 !=  (x & 0x02)) buff.append( 0x95 );
		if( 0 !=  (x & 0x04)) buff.append( 0x96 );
		if( 0 !=  (x & 0x08)) buff.append( 0x97 );
		if( 0 !=  (x & 0x10)) buff.append( 0x98 );

		return buff.toString();
	}

	private static String GetSpecialTownNameString(int ind, int seed)
	{
		return TownNameGenerator._town_name_generators[ind].apply(seed);
	}

	private final static String _silly_company_names[] = {
			"Bloggs Brothers",
			"Tiny Transport Ltd.",
			"Express Travel",
			"Comfy-Coach & Co.",
			"Crush & Bump Ltd.",
			"Broken & Late Ltd.",
			"Sam Speedy & Son",
			"Supersonic Travel",
			"Mike's Motors",
			"Lightning International",
			"Pannik & Loozit Ltd.",
			"Inter-City Transport",
			"Getout & Pushit Ltd."
	};

	private final static String _surname_list[] = {
			"Adams",
			"Allan",
			"Baker",
			"Bigwig",
			"Black",
			"Bloggs",
			"Brown",
			"Campbell",
			"Gordon",
			"Hamilton",
			"Hawthorn",
			"Higgins",
			"Green",
			"Gribble",
			"Jones",
			"McAlpine",
			"MacDonald",
			"McIntosh",
			"Muir",
			"Murphy",
			"Nelson",
			"O'Donnell",
			"Parker",
			"Phillips",
			"Pilkington",
			"Quigley",
			"Sharkey",
			"Thomson",
			"Watkins"
	};

	private final static String _silly_surname_list[] = {
			"Grumpy",
			"Dozy",
			"Speedy",
			"Nosey",
			"Dribble",
			"Mushroom",
			"Cabbage",
			"Sniffle",
			"Fishy",
			"Swindle",
			"Sneaky",
			"Nutkins"
	};

	private final static char _initial_name_letters[] = {
			'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
			'K', 'L', 'M', 'N', 'P', 'R', 'S', 'T', 'W',
	};

	private static String GenAndCoName(int arg)
	{
		String[] base;
		int num;
		StringBuilder buff = new StringBuilder();

		if (GameOptions._opt_ptr.landscape == Landscape.LT_CANDY) {
			base = _silly_surname_list;
		} else {
			base = _surname_list;
		}
		num  = base.length;

		buff.append( base[num * BitOps.GB(arg, 16, 8) >> 8] );
		buff.append( " & Co." );

		return buff.toString();
	}

	private static String GenPresidentName(int x)
	{
		final String[] base;
		int num;
		int i;
		StringBuilder buff = new StringBuilder();

		buff.append( _initial_name_letters[_initial_name_letters.length * BitOps.GB(x, 0, 8) >> 8] );
		buff.append( ". " );
		// Insert a space after initial and period "I. Firstname" instead of "I.Firstname"

		i = (_initial_name_letters.length + 35) * BitOps.GB(x, 8, 8) >> 8;
		if (i < _initial_name_letters.length) {
			buff.append( _initial_name_letters[i] );
			buff.append( ". " );
		}

		if (GameOptions._opt_ptr.landscape == Landscape.LT_CANDY) {
			base = _silly_surname_list;
			num  = _silly_surname_list.length;
		} else {
			base = _surname_list;
			num  = _surname_list.length;
		}

		buff.append( base[num * BitOps.GB(x, 16, 8) >> 8] );

		return buff.toString();
	}

	private final static String _song_names[] = {
			"Tycoon DELUXE Theme",
			"Easy Driver",
			"Little Red Diesel",
			"Cruise Control",
			"Don't Walk!",
			"Fell Apart On Me",
			"City Groove",
			"Funk Central",
			"Stoke It",
			"Road Hog",
			"Aliens Ate My Railway",
			"Snarl Up",
			"Stroll On",
			"Can't Get There From Here",
			"Sawyer's Tune",
			"Hold That Train!",
			"Movin' On",
			"Goss Groove",
			"Small Town",
			"Broomer's Oil Rag",
			"Jammit",
			"Hard Drivin'"
	};

	private static String GetSpecialPlayerNameString(int ind, Object ... arg)
	{
		//StringBuilder buff = new StringBuilder();
		int argc = 0;
		switch (ind) {
		case 1: // not used
			return _silly_company_names[Getint(arg[argc++]) & 0xFFFF];

		case 2: // used for Foobar & Co company names
			return GenAndCoName(Getint(arg[argc++]));

		case 3: // President name
			return GenPresidentName( Getint(arg[argc++]));

		case 4: // song names
			return _song_names[Getint(arg[argc++]) - 1];
		}

		// town name?
		if (BitOps.IS_INT_INSIDE(ind - 6, 0, SPECSTR_TOWNNAME_LAST-SPECSTR_TOWNNAME_START + 1)) {
			return GetSpecialTownNameString( ind - 6, Getint(arg[argc++])) + " Transport";
		}

		// language name?
		if (BitOps.IS_INT_INSIDE(ind, (SPECSTR_LANGUAGE_START - 0x70E4), (SPECSTR_LANGUAGE_END - 0x70E4) + 1)) {
			int i = ind - (SPECSTR_LANGUAGE_START - 0x70E4);
			return i == _dynlang.curr ? _langpack.own_name : _dynlang.name[i];
			//return "English";
		}

		// resolution size?
		if (BitOps.IS_INT_INSIDE(ind, (SPECSTR_RESOLUTION_START - 0x70E4), (SPECSTR_RESOLUTION_END - 0x70E4) + 1)) {
			//int i = ind - (SPECSTR_RESOLUTION_START - 0x70E4);
			// TODO return String.format("%dx%d", _resolutions[i][0], _resolutions[i][1]);
			return "1024x768";
		}

		// screenshot format name?
		if (BitOps.IS_INT_INSIDE(ind, (SPECSTR_SCREENSHOT_START - 0x70E4), (SPECSTR_SCREENSHOT_END - 0x70E4) + 1)) {
			int i = ind - (SPECSTR_SCREENSHOT_START - 0x70E4);
			return ScreenShot.GetScreenshotFormatDesc(i);
		}

		assert false;
		return null;
	}




	/* unused / remap a string ID from the old format to the new format
	//static StringID RemapOldStringID(StringID s)
	static int RemapOldStringID(int s)
	{
		switch (s) {
		case 0x0006: return STR_SV_EMPTY;
		case 0x7000: return STR_SV_UNNAMED;
		case 0x70E4: return SPECSTR_PLAYERNAME_ENGLISH;
		case 0x70E9: return SPECSTR_PLAYERNAME_ENGLISH;
		case 0x8864: return STR_SV_TRAIN_NAME;
		case 0x902B: return STR_SV_ROADVEH_NAME;
		case 0x9830: return STR_SV_SHIP_NAME;
		case 0xA02F: return STR_SV_AIRCRAFT_NAME;

		default:
			if (BitOps.IS_INT_INSIDE(s, 0x300F, 0x3030))
				return s - 0x300F + STR_SV_STNAME;
			else
				return s;
		}
	} */

	public static boolean ReadLanguagePack(int lang_index)
	{

		LanguagePack lang_pack = new LanguagePack();
		BinaryString []langpack_offs;

		String lang = String.format("%s%s", Global._path.lang_dir, _dynlang.file[lang_index]);
		byte[] lang_pack_bytes = FileIO.ReadFileToMem(lang, 100000);
		if (lang_pack_bytes == null) return false;

		/*
		lang_pack.name = BitOps.stringFromBytes(lang_pack_bytes, 8, 32 );
		lang_pack.own_name = BitOps.stringFromBytes(lang_pack_bytes, 40, 32 );
		*/
		int tot_count = 0;
		for (int i = 0; i != 32; i++) {
			int off = BitOps.READ_LE_UINT16(lang_pack_bytes, 88+i*2);
			lang_pack.offsets[i] = off;
			int num = lang_pack.offsets[i];
			_langtab_start[i] = tot_count;
			_langtab_num[i] = num;
			tot_count += num;
		}
		
		lang_pack.loadFromBytes(lang_pack_bytes);
		
		// Allocate offsets
		langpack_offs = new BinaryString[tot_count]; 

		// Fill offsets
		byte[] s = BitOps.subArray(lang_pack_bytes, 0x9c); //0x9d);		
		int sp = 0;
		for (int i = 0; i != tot_count; i++) {
			int len = s[sp++];
			len &= 0xFF;
			//*s++ = '\0'; // zero terminate the string before.
			if (len >= 0xC0)
			{ 
				int lo = s[sp++];
				len = ((len & 0x3F) << 8) + (lo & 0xFF);
			}
			//langpack_offs[i] = new String( s, sp, len );
			//langpack_offs[i] = BitOps.stringFromBytes( s, sp, len );
			langpack_offs[i] = new BinaryString( s, sp, len );
			//Global.debug("s = '%s'", langpack_offs[i] );
			sp += len;
		}

		_langpack = lang_pack;
		_langpack_offs = langpack_offs;
		_dynlang.curr_file = _dynlang.file[lang_index];
		_dynlang.curr = lang_index;
		return true;

	}

	private final static String [] env = {
			"LANGUAGE",
			"LC_ALL",
			"LC_MESSAGES",
			"LANG"
	};

	// make a list of the available language packs. put the data in _dynlang struct.
	public static void InitializeLanguagePacks()
	{
		DynamicLanguages dl = _dynlang;
		int m = 0;

		Locale lcl = Locale.getDefault();
		String lang = lcl.getLanguage();

		if(lang == null)
		{
			lang = "en";

			for (String en : env) {
				final String envlang = System.getenv(en);
				if (envlang != null) {
					lang = envlang;
					break;
				}
			}
		}

		List<String> files = FileIO.GetLanguageList();

		int def = -1;
		int fallback = 0;

		// go through the language files and make sure that they are valid.
		for (String file : files) {
			LanguagePack hdr = new LanguagePack();

			String s = String.format("%s%s", Global._path.lang_dir, file);

			try( FileInputStream in = new FileInputStream(s) )
			{
				if( !hdr.readFrom(in) )
					continue;

				if ( !hdr.isValid() )
					continue;
			} catch (FileNotFoundException e) {
				Global.error(e);
				continue;
			} catch (IOException e) {
				Global.error(e);
				continue;
			}

			dl.file[m] = file;
			dl.name[m] = hdr.name;

			if (hdr.name.equals("English")) fallback = m;
			if (hdr.isocode.substring(0,2).equals(lang)) def = m;

			m++;
		}
		
		if (def == -1) def = fallback;

		if (m == 0)
			Global.error(files.size() == 0 ? "No available language packs" : "Invalid version of language packs");

		dl.num = m;
		
		int i;
		
		for (i = 0; i != dl.num; i++)
			dl.dropdown[i] = SPECSTR_LANGUAGE_START + i;
		
		dl.dropdown[i] = Str.INVALID_STRING;

		for (i = 0; i != dl.num; i++)
			if (dl.file[i].equals(dl.curr_file)) {
				def = i;
				break;
			}

		if (!ReadLanguagePack(def))
			Global.error("can't read language pack '%s'", dl.file[def]);

	}

}



