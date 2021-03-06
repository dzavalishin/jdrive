/* $Id: strings.c 3192 2005-11-16 11:17:52Z tron $ */
#if 0
#include "stdafx.h"
#include "openttd.h"
#include "currency.h"
#include "functions.h"
#include "string.h"
#include "strings.h"
#include "table/strings.h"
#include "namegen.h"
#include "station.h"
#include "town.h"
#include "vehicle.h"
#include "news.h"
#include "screenshot.h"
#include "waypoint.h"
#include "industry.h"
#include "variables.h"

char _userstring[128];

static char *StationGetSpecialString(char *buff, int x);
static char *GetSpecialTownNameString(char *buff, int ind, uint32 seed);
static char *GetSpecialPlayerNameString(char *buff, int ind, const int32 *argv);

static char *FormatString(char *buff, const char *str, const int32 *argv, uint casei);

extern const char _openttd_revision[];

typedef struct LanguagePack {
	uint32 ident;
	uint32 version;			// 32-bits of auto generated version info which is basically a hash of strings.h
	char name[32];			// the international name of this language
	char own_name[32];	// the localized name of this language
	char isocode[16];	// the ISO code for the language (not country code)
	uint16 offsets[32];	// the offsets
	byte plural_form;		// how to compute plural forms
	byte pad[3];				// pad header to be a multiple of 4
	char data[VARARRAY_SIZE];
} LanguagePack;

static char **_langpack_offs;
static LanguagePack *_langpack;
static uint _langtab_num[32]; // Offset into langpack offs
static uint _langtab_start[32]; // Offset into langpack offs

static const StringID _cargo_string_list[NUM_LANDSCAPE][NUM_CARGO] = {
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


// Read an int64 from the argv array.
static inline int64 GetInt64(const int32 **argv)
{
	int64 result;

	assert(argv);
	result = (uint32)(*argv)[0] + ((uint64)(uint32)(*argv)[1] << 32);
	(*argv)+=2;
	return result;
}

// Read an int32 from the argv array.
static inline int32 GetInt32(const int32 **argv)
{
	assert(argv);
	return *(*argv)++;
}

// Read an array from the argv array.
static inline const int32 *GetArgvPtr(const int32 **argv, int n)
{
	const int32 *result;
	assert(*argv);
	result = *argv;
	(*argv) += n;
	return result;
}


#define NUM_BOUND_STRINGS 8

// Array to hold the bound strings.
static const char *_bound_strings[NUM_BOUND_STRINGS];

// This index is used to implement a "round-robin" allocating of
// slots for BindCString. NUM_BOUND_STRINGS slots are reserved.
// Which means that after NUM_BOUND_STRINGS calls to BindCString,
// the indices will be reused.
static int _bind_index;

static const char *GetStringPtr(StringID string)
{
	return _langpack_offs[_langtab_start[string >> 11] + (string & 0x7FF)];
}

// The highest 8 bits of string contain the "case index".
// These 8 bits will only be set when FormatString wants to print
// the string in a different case. No one else except FormatString
// should set those bits.
char *GetStringWithArgs(char *buffr, uint string, const int32 *argv)
{
	uint index = GB(string,  0, 11);
	uint tab   = GB(string, 11,  5);

	if (GB(string, 0, 16) == 0) error("!invalid string id 0 in GetString");

	switch (tab) {
		case 4:
			if (index >= 0xC0)
				return GetSpecialTownNameString(buffr, index - 0xC0, GetInt32(&argv));
			break;

		case 14:
			if (index >= 0xE4)
				return GetSpecialPlayerNameString(buffr, index - 0xE4, argv);
			break;

		// User defined name
		case 15:
			return GetName(index, buffr);

		case 31:
			// dynamic strings. These are NOT to be passed through the formatter,
			// but passed through verbatim.
			if (index < (STR_SPEC_USERSTRING & 0x7FF)) {
				return strecpy(buffr, _bound_strings[index], NULL);
			}

			return FormatString(buffr, _userstring, NULL, 0);
	}

	if (index >= _langtab_num[tab]) {
		error(
			"!String 0x%X is invalid. "
			"Probably because an old version of the .lng file.\n", string
		);
	}

	return FormatString(buffr, GetStringPtr(GB(string, 0, 16)), argv, GB(string, 24, 8));
}

char *GetString(char *buffr, StringID string)
{
	return GetStringWithArgs(buffr, string, (int32*)_decode_parameters);
}


// This function takes a C-string and allocates a temporary string ID.
// The duration of the bound string is valid only until the next GetString,
// so be careful.
StringID BindCString(const char *str)
{
	int idx = (++_bind_index) & (NUM_BOUND_STRINGS - 1);
	_bound_strings[idx] = str;
	return idx + STR_SPEC_DYNSTRING;
}

// This function is used to "bind" a C string to a OpenTTD dparam slot.
void SetDParamStr(uint n, const char *str)
{
	SetDParam(n, BindCString(str));
}

void InjectDParam(int amount)
{
	memmove(_decode_parameters + amount, _decode_parameters, sizeof(_decode_parameters) - amount * sizeof(uint32));
}

static const uint32 _divisor_table[] = {
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

static char *FormatCommaNumber(char *buff, int32 number)
{
	uint32 quot,divisor;
	int i;
	uint32 tot;
	uint32 num;

	if (number < 0) {
		*buff++ = '-';
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
		if (tot |= quot || i == 9) {
			*buff++ = '0' + quot;
			if (i == 0 || i == 3 || i == 6) *buff++ = ',';
		}
	}

	*buff = '\0';

	return buff;
}

static char *FormatNoCommaNumber(char *buff, int32 number)
{
	uint32 quot,divisor;
	int i;
	uint32 tot;
	uint32 num;

	if (number < 0) {
		*buff++ = '-';
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
		if (tot |= quot || i == 9) {
			*buff++ = '0' + quot;
		}
	}

	*buff = '\0';

	return buff;
}


static char *FormatYmdString(char *buff, uint16 number)
{
	const char *src;
	YearMonthDay ymd;

	ConvertDayToYMD(&ymd, number);

	for (src = GetStringPtr(ymd.day + STR_01AC_1ST - 1); (*buff++ = *src++) != '\0';) {}

	buff[-1] = ' ';
	memcpy(buff, GetStringPtr(STR_0162_JAN + ymd.month), 4);
	buff[3] = ' ';

	return FormatNoCommaNumber(buff + 4, ymd.year + MAX_YEAR_BEGIN_REAL);
}

static char *FormatMonthAndYear(char *buff, uint16 number)
{
	const char *src;
	YearMonthDay ymd;

	ConvertDayToYMD(&ymd, number);

	for (src = GetStringPtr(STR_MONTH_JAN + ymd.month); (*buff++ = *src++) != '\0';) {}
	buff[-1] = ' ';

	return FormatNoCommaNumber(buff, ymd.year + MAX_YEAR_BEGIN_REAL);
}

static char *FormatTinyDate(char *buff, uint16 number)
{
	YearMonthDay ymd;

	ConvertDayToYMD(&ymd, number);
	buff += sprintf(buff, " %02i-%02i-%04i", ymd.day, ymd.month + 1, ymd.year + MAX_YEAR_BEGIN_REAL);

	return buff;
}

static char *FormatGenericCurrency(char *buff, const CurrencySpec *spec, int64 number, bool compact)
{
	const char *s;
	char c;
	char buf[40], *p;
	int j;

	// multiply by exchange rate
	number *= spec->rate;

	// convert from negative
	if (number < 0) {
		*buff++ = '-';
		number = -number;
	}

	// add prefix part
	s = spec->prefix;
	while (s != spec->prefix + lengthof(spec->prefix) && (c = *s++) != '\0') *buff++ = c;

	// for huge numbers, compact the number into k or M
	if (compact) {
		compact = 0;
		if (number >= 1000000000) {
			number = (number + 500000) / 1000000;
			compact = 'M';
		} else if (number >= 1000000) {
			number = (number + 500) / 1000;
			compact = 'k';
		}
	}

	// convert to ascii number and add commas
	p = buf;
	j = 4;
	do {
		if (--j == 0) {
			*p++ = spec->separator;
			j = 3;
		}
		*p++ = '0' + number % 10;
	} while (number /= 10);
	do *buff++ = *--p; while (p != buf);

	if (compact) *buff++ = compact;

	// add suffix part
	s = spec->suffix;
	while (s != spec->suffix + lengthof(spec->suffix) && (c = *s++) != '\0') *buff++ = c;

	return buff;
}

static int DeterminePluralForm(int32 n)
{
	// The absolute value determines plurality
	if (n < 0) n = -n;

	switch(_langpack->plural_form) {
	// Two forms, singular used for one only
	// Used in:
	//   Danish, Dutch, English, German, Norwegian, Swedish, Estonian, Finnish,
	//   Greek, Hebrew, Italian, Portuguese, Spanish, Esperanto
	case 0:
	default:
		return n != 1;

	// Only one form
	// Used in:
	//   Hungarian, Japanese, Korean, Turkish
	case 1:
		return 0;

	// Two forms, singular used for zero and one
	// Used in:
	//   French, Brazilian Portuguese
	case 2:
		return n > 1;

	// Three forms, special case for zero
	// Used in:
	//   Latvian
	case 3:
		return n%10==1 && n%100!=11 ? 0 : n != 0 ? 1 : 2;

	// Three forms, special case for one and two
	// Used in:
	//   Gaelige (Irish)
	case 4:
		return n==1 ? 0 : n==2 ? 1 : 2;

	// Three forms, special case for numbers ending in 1[2-9]
	// Used in:
	//   Lithuanian
	case 5:
		return n%10==1 && n%100!=11 ? 0 : n%10>=2 && (n%100<10 || n%100>=20) ? 1 : 2;

	// Three forms, special cases for numbers ending in 1 and 2, 3, 4, except those ending in 1[1-4]
	// Used in:
	//   Croatian, Czech, Russian, Slovak, Ukrainian
	case 6:
		return n%10==1 && n%100!=11 ? 0 : n%10>=2 && n%10<=4 && (n%100<10 || n%100>=20) ? 1 : 2;

	// Three forms, special case for one and some numbers ending in 2, 3, or 4
	// Used in:
	//   Polish
	case 7:
		return n==1 ? 0 : n%10>=2 && n%10<=4 && (n%100<10 || n%100>=20) ? 1 : 2;

	// Four forms, special case for one and all numbers ending in 02, 03, or 04
	// Used in:
	//   Slovenian
	case 8:
		return n%100==1 ? 0 : n%100==2 ? 1 : n%100==3 || n%100==4 ? 2 : 3;
	}
}

static const char *ParseStringChoice(const char *b, uint form, char *dst, int *dstlen)
{
	//<NUM> {Length of each string} {each string}
	uint n = (byte)*b++;
	uint pos,i, mylen=0,mypos=0;
	for(i=pos=0; i!=n; i++) {
		uint len = (byte)*b++;
		if (i == form) {
			mypos = pos;
			mylen = len;
		}
		pos += len;
	}
	*dstlen = mylen;
	memcpy(dst, b + mypos, mylen);
	return b + pos;
}


static char *FormatString(char *buff, const char *str, const int32 *argv, uint casei)
{
	byte b;
	const int32 *argv_orig = argv;
	uint modifier = 0;

	while ((b = *str++) != '\0') {
		switch (b) {
		case 0x1: // {SETX}
			*buff++ = b;
			*buff++ = *str++;
			break;
		case 0x2: // {SETXY}
			*buff++ = b;
			*buff++ = *str++;
			*buff++ = *str++;
			break;

		case 0x81: // {STRINL}
			str += 2;
			buff = GetStringWithArgs(buff, READ_LE_UINT16(str-2), argv);
			break;
		case 0x82: // {DATE_LONG}
			buff = FormatYmdString(buff, GetInt32(&argv));
			break;
		case 0x83: // {DATE_SHORT}
			buff = FormatMonthAndYear(buff, GetInt32(&argv));
			break;
		case 0x84: {// {VELOCITY}
			int value = GetInt32(&argv);
			if (_opt_ptr->kilometers) value = value * 1648 >> 10;
			buff = FormatCommaNumber(buff, value);
			if (_opt_ptr->kilometers) {
				memcpy(buff, " km/h", 5);
				buff += 5;
			} else {
				memcpy(buff, " mph", 4);
				buff += 4;
			}
			break;
		}
		// 0x85 is used as escape character..
		case 0x85:
			switch (*str++) {
			case 0: /* {CURRCOMPACT} */
				buff = FormatGenericCurrency(buff, _currency, GetInt32(&argv), true);
				break;
			case 2: /* {REV} */
				buff = strecpy(buff, _openttd_revision, NULL);
				break;
			case 3: { /* {SHORTCARGO} */
				// Short description of cargotypes. Layout:
				// 8-bit = cargo type
				// 16-bit = cargo count
				StringID cargo_str = _cargo_string_list[_opt_ptr->landscape][GetInt32(&argv)];
				uint16 multiplier = (cargo_str == STR_LITERS) ? 1000 : 1;
				// liquid type of cargo is multiplied by 100 to get correct amount
				buff = FormatCommaNumber(buff, GetInt32(&argv) * multiplier);
				buff = strecpy(buff, " ", NULL);
				buff = strecpy(buff, GetStringPtr(cargo_str), NULL);
			} break;
			case 4: {/* {CURRCOMPACT64} */
				// 64 bit compact currency-unit
				buff = FormatGenericCurrency(buff, _currency, GetInt64(&argv), true);
				break;
			}
			case 5: { /* {STRING1} */
				// String that consumes ONE argument
				uint str = modifier + GetInt32(&argv);
				buff = GetStringWithArgs(buff, str, GetArgvPtr(&argv, 1));
				modifier = 0;
				break;
			}
			case 6: { /* {STRING2} */
				// String that consumes TWO arguments
				uint str = modifier + GetInt32(&argv);
				buff = GetStringWithArgs(buff, str, GetArgvPtr(&argv, 2));
				modifier = 0;
				break;
			}
			case 7: { /* {STRING3} */
				// String that consumes THREE arguments
				uint str = modifier + GetInt32(&argv);
				buff = GetStringWithArgs(buff, str, GetArgvPtr(&argv, 3));
				modifier = 0;
				break;
			}
			case 8: { /* {STRING4} */
				// String that consumes FOUR arguments
				uint str = modifier + GetInt32(&argv);
				buff = GetStringWithArgs(buff, str, GetArgvPtr(&argv, 4));
				modifier = 0;
				break;
			}
			case 9: { /* {STRING5} */
				// String that consumes FIVE arguments
				uint str = modifier + GetInt32(&argv);
				buff = GetStringWithArgs(buff, str, GetArgvPtr(&argv, 5));
				modifier = 0;
				break;
			}

			case 10: { /* {STATIONFEATURES} */
				buff = StationGetSpecialString(buff, GetInt32(&argv));
				break;
			}

			case 11: { /* {INDUSTRY} */
				Industry *i = GetIndustry(GetInt32(&argv));
				int32 args[2];

				// industry not valid anymore?
				if (i->xy == 0)
					break;

				// First print the town name and the industry type name
				// The string STR_INDUSTRY_PATTERN controls the formatting
				args[0] = i->town->index;
				args[1] = i->type + STR_4802_COAL_MINE;
				buff = FormatString(buff, GetStringPtr(STR_INDUSTRY_FORMAT), args, modifier >> 24);
				modifier = 0;
				break;
			}

			case 12: { // {VOLUME}
				buff = FormatCommaNumber(buff, GetInt32(&argv) * 1000);
				buff = strecpy(buff, " ", NULL);
				buff = FormatString(buff, GetStringPtr(STR_LITERS), NULL, modifier >> 24);
				modifier = 0;
				break;
			}

			case 13: { // {G 0 Der Die Das}
				const byte* s = (const byte*)GetStringPtr(argv_orig[(byte)*str++]); // contains the string that determines gender.
				int len;
				int gender = 0;
				if (s != NULL && s[0] == 0x87) gender = s[1];
				str = ParseStringChoice(str, gender, buff, &len);
				buff += len;
				break;
			}

			case 14: { // {DATE_TINY}
				buff = FormatTinyDate(buff, GetInt32(&argv));
				break;
			}

			case 15: { // {CARGO}
				// Layout now is:
				//   8bit   - cargo type
				//   16-bit - cargo count
				StringID cargo_str = _cargoc.names_long[GetInt32(&argv)];
				buff = GetStringWithArgs(buff, cargo_str, argv++);
				break;
			}

			default:
				error("!invalid escape sequence in string");
			}
			break;

		case 0x86: // {SKIP}
			argv++;
			break;

		// This sets up the gender for the string.
		// We just ignore this one. It's used in {G 0 Der Die Das} to determine the case.
		case 0x87: // {GENDER 0}
			str++;
			break;

		case 0x88: {// {STRING}
			uint str = modifier + GetInt32(&argv);
			// WARNING. It's prohibited for the included string to consume any arguments.
			// For included strings that consume argument, you should use STRING1, STRING2 etc.
			// To debug stuff you can set argv to NULL and it will tell you
			buff = GetStringWithArgs(buff, str, argv);
			modifier = 0;
			break;
		}

		case 0x8B: // {COMMA}
			buff = FormatCommaNumber(buff, GetInt32(&argv));
			break;

		case 0x8C: // Move argument pointer
			argv = argv_orig + (byte)*str++;
			break;

		case 0x8D: { // {P}
			int32 v = argv_orig[(byte)*str++]; // contains the number that determines plural
			int len;
			str = ParseStringChoice(str, DeterminePluralForm(v), buff, &len);
			buff += len;
			break;
		}

		case 0x8E: // {NUM}
			buff = FormatNoCommaNumber(buff, GetInt32(&argv));
			break;

		case 0x8F: // {CURRENCY}
			buff = FormatGenericCurrency(buff, _currency, GetInt32(&argv), false);
			break;

		case 0x99: { // {WAYPOINT}
			int32 temp[2];
			Waypoint *wp = GetWaypoint(GetInt32(&argv));
			StringID str;
			if (wp->string != STR_NULL) {
				str = wp->string;
			} else {
				temp[0] = wp->town_index;
				temp[1] = wp->town_cn + 1;
				str = wp->town_cn == 0 ? STR_WAYPOINTNAME_CITY : STR_WAYPOINTNAME_CITY_SERIAL;
			}
			buff = GetStringWithArgs(buff, str, temp);
		} break;

		case 0x9A: { // {STATION}
			const Station* st = GetStation(GetInt32(&argv));
			int32 temp[2];

			if (st->xy == 0) { // station doesn't exist anymore
				buff = GetStringWithArgs(buff, STR_UNKNOWN_DESTINATION, NULL);
				break;
			}
			temp[0] = st->town->townnametype;
			temp[1] = st->town->townnameparts;
			buff = GetStringWithArgs(buff, st->string_id, temp);
			break;
		}
		case 0x9B: { // {TOWN}
			const Town* t = GetTown(GetInt32(&argv));
			int32 temp[1];

			assert(t->xy != 0);

			temp[0] = t->townnameparts;
			buff = GetStringWithArgs(buff, t->townnametype, temp);
			break;
		}

		case 0x9C: { // {CURRENCY64}
			buff = FormatGenericCurrency(buff, _currency, GetInt64(&argv), false);
			break;
		}

		case 0x9D: { // {SETCASE}
			// This is a pseudo command, it's outputted when someone does {STRING.ack}
			// The modifier is added to all subsequent GetStringWithArgs that accept the modifier.
			modifier = (byte)*str++ << 24;
			break;
		}

		case 0x9E: { // {Used to implement case switching}
			// <0x9E> <NUM CASES> <CASE1> <LEN1> <STRING1> <CASE2> <LEN2> <STRING2> <CASE3> <LEN3> <STRING3> <STRINGDEFAULT>
			// Each LEN is printed using 2 bytes in big endian order.
			uint num = (byte)*str++;
			while (num) {
				if ((byte)str[0] == casei) {
					// Found the case, adjust str pointer and continue
					str += 3;
					break;
				}
				// Otherwise skip to the next case
				str += 3 + (str[1] << 8) + str[2];
				num--;
			}
			break;
		}

		default:
			*buff++ = b;
		}
	}
	*buff = '\0';
	return buff;
}


static char *StationGetSpecialString(char *buff, int x)
{
	if (x & 0x01) *buff++ = '\x94';
	if (x & 0x02) *buff++ = '\x95';
	if (x & 0x04) *buff++ = '\x96';
	if (x & 0x08) *buff++ = '\x97';
	if (x & 0x10) *buff++ = '\x98';
	*buff = '\0';
	return buff;
}

static char *GetSpecialTownNameString(char *buff, int ind, uint32 seed)
{
	_town_name_generators[ind](buff, seed);

	while (*buff != '\0') buff++;
	return buff;
}

static const char* const _silly_company_names[] = {
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

static const char* const _surname_list[] = {
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

static const char* const _silly_surname_list[] = {
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

static const char _initial_name_letters[] = {
	'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
	'K', 'L', 'M', 'N', 'P', 'R', 'S', 'T', 'W',
};

static char *GenAndCoName(char *buff, uint32 arg)
{
	const char* const* base;
	uint num;

	if (_opt_ptr->landscape == LT_CANDY) {
		base = _silly_surname_list;
		num  = lengthof(_silly_surname_list);
	} else {
		base = _surname_list;
		num  = lengthof(_surname_list);
	}

	buff = strecpy(buff, base[num * GB(arg, 16, 8) >> 8], NULL);
	buff = strecpy(buff, " & Co.", NULL);

	return buff;
}

static char *GenPresidentName(char *buff, uint32 x)
{
	const char* const* base;
	uint num;
	uint i;

	buff[0] = _initial_name_letters[sizeof(_initial_name_letters) * GB(x, 0, 8) >> 8];
	buff[1] = '.';
	buff[2] = ' '; // Insert a space after initial and period "I. Firstname" instead of "I.Firstname"
	buff += 3;

	i = (sizeof(_initial_name_letters) + 35) * GB(x, 8, 8) >> 8;
	if (i < sizeof(_initial_name_letters)) {
		buff[0] = _initial_name_letters[i];
		buff[1] = '.';
		buff[2] = ' '; // Insert a space after initial and period "I. J. Firstname" instead of "I.J.Firstname"
		buff += 3;
	}

	if (_opt_ptr->landscape == LT_CANDY) {
		base = _silly_surname_list;
		num  = lengthof(_silly_surname_list);
	} else {
		base = _surname_list;
		num  = lengthof(_surname_list);
	}

	buff = strecpy(buff, base[num * GB(x, 16, 8) >> 8], NULL);

	return buff;
}

static const char * const _song_names[] = {
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

static char *GetSpecialPlayerNameString(char *buff, int ind, const int32 *argv)
{
	switch (ind) {
		case 1: // not used
			return strecpy(buff, _silly_company_names[GetInt32(&argv) & 0xFFFF], NULL);

		case 2: // used for Foobar & Co company names
			return GenAndCoName(buff, GetInt32(&argv));

		case 3: // President name
			return GenPresidentName(buff, GetInt32(&argv));

		case 4: // song names
			return strecpy(buff, _song_names[GetInt32(&argv) - 1], NULL);
	}

	// town name?
	if (IS_INT_INSIDE(ind - 6, 0, SPECSTR_TOWNNAME_LAST-SPECSTR_TOWNNAME_START + 1)) {
		buff = GetSpecialTownNameString(buff, ind - 6, GetInt32(&argv));
		return strecpy(buff, " Transport", NULL);
	}

	// language name?
	if (IS_INT_INSIDE(ind, (SPECSTR_LANGUAGE_START - 0x70E4), (SPECSTR_LANGUAGE_END - 0x70E4) + 1)) {
		int i = ind - (SPECSTR_LANGUAGE_START - 0x70E4);
		return strecpy(buff,
			i == _dynlang.curr ? _langpack->own_name : _dynlang.ent[i].name, NULL);
	}

	// resolution size?
	if (IS_INT_INSIDE(ind, (SPECSTR_RESOLUTION_START - 0x70E4), (SPECSTR_RESOLUTION_END - 0x70E4) + 1)) {
		int i = ind - (SPECSTR_RESOLUTION_START - 0x70E4);
		return buff + sprintf(buff, "%dx%d", _resolutions[i][0], _resolutions[i][1]);
	}

	// screenshot format name?
	if (IS_INT_INSIDE(ind, (SPECSTR_SCREENSHOT_START - 0x70E4), (SPECSTR_SCREENSHOT_END - 0x70E4) + 1)) {
		int i = ind - (SPECSTR_SCREENSHOT_START - 0x70E4);
		return strecpy(buff, GetScreenshotFormatDesc(i), NULL);
	}

	assert(0);
	return NULL;
}

// remap a string ID from the old format to the new format
StringID RemapOldStringID(StringID s)
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
			if (IS_INT_INSIDE(s, 0x300F, 0x3030))
				return s - 0x300F + STR_SV_STNAME;
			else
				return s;
	}
}

bool ReadLanguagePack(int lang_index)
{
	int tot_count, i;
	LanguagePack *lang_pack;
	size_t len;
	char **langpack_offs;
	char *s;

	{
		char *lang = str_fmt("%s%s", _path.lang_dir, _dynlang.ent[lang_index].file);
		lang_pack = ReadFileToMem(lang, &len, 100000);
		free(lang);
	}
	if (lang_pack == NULL) return false;
	if (len < sizeof(LanguagePack) ||
			lang_pack->ident != TO_LE32(LANGUAGE_PACK_IDENT) ||
			lang_pack->version != TO_LE32(LANGUAGE_PACK_VERSION)) {
		free(lang_pack);
		return false;
	}

#if defined(TTD_BIG_ENDIAN)
	for (i = 0; i != 32; i++) {
		lang_pack->offsets[i] = READ_LE_UINT16(&lang_pack->offsets[i]);
	}
#endif

	tot_count = 0;
	for (i = 0; i != 32; i++) {
		uint num = lang_pack->offsets[i];
		_langtab_start[i] = tot_count;
		_langtab_num[i] = num;
		tot_count += num;
	}

	// Allocate offsets
	langpack_offs = malloc(tot_count * sizeof(*langpack_offs));

	// Fill offsets
	s = lang_pack->data;
	for (i = 0; i != tot_count; i++) {
		len = (byte)*s;
		*s++ = '\0'; // zero terminate the string before.
		if (len >= 0xC0) len = ((len & 0x3F) << 8) + (byte)*s++;
		langpack_offs[i] = s;
		s += len;
	}

	free(_langpack);
	_langpack = lang_pack;

	free(_langpack_offs);
	_langpack_offs = langpack_offs;

	ttd_strlcpy(_dynlang.curr_file, _dynlang.ent[lang_index].file, sizeof(_dynlang.curr_file));


	_dynlang.curr = lang_index;
	return true;
}

// make a list of the available language packs. put the data in _dynlang struct.
void InitializeLanguagePacks(void)
{
	DynamicLanguages *dl = &_dynlang;
	int i;
	int n;
	int m;
	int def;
	int fallback;
	LanguagePack hdr;
	FILE *in;
	char *files[32];
	uint j;

	char lang[] = "en";
	static const char* env[] = {
		"LANGUAGE",
		"LC_ALL",
		"LC_MESSAGES",
		"LANG"
	};

	for (j = 0; j < lengthof(env); j++) {
		const char* envlang = getenv(env[j]);
		if (envlang != NULL) {
			snprintf(lang, lengthof(lang), "%.2s", envlang);
			break;
		}
	}

	n = GetLanguageList(files, lengthof(files));

	def = -1;
	fallback = 0;

	// go through the language files and make sure that they are valid.
	for (i = m = 0; i != n; i++) {
		int j;

		char *s = str_fmt("%s%s", _path.lang_dir, files[i]);
		in = fopen(s, "rb");
		free(s);
		if (in == NULL ||
				(j = fread(&hdr, sizeof(hdr), 1, in), fclose(in), j) != 1 ||
				hdr.ident != TO_LE32(LANGUAGE_PACK_IDENT) ||
				hdr.version != TO_LE32(LANGUAGE_PACK_VERSION)) {
			free(files[i]);
			continue;
		}

		dl->ent[m].file = files[i];
		dl->ent[m].name = strdup(hdr.name);

		if (strcmp(hdr.name, "English") == 0) fallback = m;
		if (strcmp(hdr.isocode, lang) == 0) def = m;

		m++;
	}
	if (def == -1) def = fallback;

	if (m == 0)
		error(n == 0 ? "No available language packs" : "Invalid version of language packs");

	dl->num = m;
	for (i = 0; i != dl->num; i++)
		dl->dropdown[i] = SPECSTR_LANGUAGE_START + i;
	dl->dropdown[i] = INVALID_STRING_ID;

	for (i = 0; i != dl->num; i++)
		if (strcmp(dl->ent[i].file, dl->curr_file) == 0) {
			def = i;
			break;
		}

	if (!ReadLanguagePack(def))
		error("can't read language pack '%s'", dl->ent[def].file);
}
