package strgen;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/** 
 * Compiles a list of strings into a compiled string list 
**/

public class Main {

	
	

	//enum Mode {
	static final int C_DONTCOUNT = 1;
	static final int C_CASE = 2;
	//};







	static boolean _masterlang;
	static boolean _translated;
	static final String _file = "(unknown file)";
	static int _cur_line;
	static int _errors, _warnings;


	static LangString [] _strings = new LangString[65536];


	static final int HASH_SIZE = 32767;
	static int [] _hash_head = new int[HASH_SIZE];

	//static byte _put_buf[4096];
	static int _put_pos;
	static int _next_string_id;

	static int _hash;
	static String _lang_name, _lang_ownname, _lang_isocode;
	static byte _lang_pluralform;
	static final int MAX_NUM_GENDER = 8;
	static String [] _genders = new String [MAX_NUM_GENDER];
	static int _numgenders;

	// contains the name of all cases.
	static final int MAX_NUM_CASES = 50;
	static String [] _cases = new String [MAX_NUM_CASES];
	static int _numcases;

	// for each plural value, this is the number of plural forms.
	static final byte _plural_form_counts[] = { 2,1,2,3,3,3,3,3,4 };

	static final String _cur_ident;
	// Used when generating some advanced commands.
	static ParsedCommandStruct _cur_pcs;
	static int _cur_argidx;

	class CmdPair {
		final CmdStruct a;
		String v;
	} 

	class ParsedCommandStruct {
		int np;
		CmdPair [] pairs = new CmdPair[32];
		CmdStruct [] cmd = new CmdStruct[32]; // ordered by param #
	}


	static int HashStr(String s)
	{
		int hash = 0;
		for(char c : s.toCharArray())
			hash = ((hash << 3) | (hash >>> 29)) ^ c;
		return hash % HASH_SIZE;
	}

	static void HashAdd(String s, LangString ls)
	{
		int hash = HashStr(s);
		ls.hash_next = _hash_head[hash];
		_hash_head[hash] = ls.index + 1;
	}

	static LangString HashFind(final String s)
	{
		int idx = _hash_head[HashStr(s)];
		while (--idx >= 0) {
			LangString ls = _strings[idx];
			if (ls.name.equals(s)) return ls;
			idx = ls.hash_next;
		}
		return null;
	}


	static void  Warning(String s, Object ... args)
	{
		String buf = String.format(s, args);
		String b1 = String.format( "%s:%d: Warning: %s\n", _file, _cur_line, buf);
		_warnings++;
		System.err.print(b1);
	}


	static void  Error(String s, Object ... args)
	{
		String buf = String.format(s, args);
		String b1 = String.format( "%s:%d: Error: %s\n", _file, _cur_line, buf);
		System.err.print(b1);
		_errors++;
	}


	static void  Fatal(String s, Object ... args)
	{
		String buf = String.format(s, args);
		String b1 = String.format( "%s:%d: FATAL: %s\n", _file, _cur_line, buf);
		System.err.print(b1);
		System.exit(1);
	}


	/*static void ttd_strlcpy(char *dst, final char *src, size_t len)
	{
		assert(len > 0);
		while (--len && *src)
			*dst++=*src++;
		*dst = 0;
	}*/



	// The plural specifier looks like
	// {NUM} {PLURAL -1 passenger passengers} then it picks either passenger/passengers depending on the count in NUM

	// This is encoded like
	//  CommandByte <ARG#> <NUM> {Length of each string} {each string}

	boolean ParseRelNum(char **buf, int *value)
	{
		char *s = *buf, *end;
		boolean rel = false;
		int v;

		while (*s == ' ' || *s == '\t') s++;
		if (*s == '+') { rel = true; s++; }
		v = strtol(s, &end, 0);
		if (end == s) return false;
		if (rel || (v < 0))
			*value += v;
		else
			*value = v;
		*buf = end;
		return true;
	}

	// Parse out the next word, or null
	char *ParseWord(char **buf)
	{
		char *s = *buf, *r;
		while (*s == ' ' || *s == '\t') s++;
		if (*s == 0)
			return null;

		if (*s == '"') {
			r = ++s;
			// parse until next " or NUL
			for(;;) {
				if (*s == 0)
					break;
				if (*s == '"') {
					*s++ = 0;
					break;
				}
				s++;
			}
		} else {
			// proceed until whitespace or NUL
			r = s;
			for(;;) {
				if (*s == 0)
					break;
				if (*s == ' ' || *s == '\t') {
					*s++ = 0;
					break;
				}
				s++;
			}
		}
		*buf = s;
		return r;
	}




	static final CmdStruct _cmd_structs[] = {
		// Update position
		new CmdStruct("SETX",  Emitter::EmitSetX,  1, 0, 0),
		new CmdStruct("SETXY", Emitter::EmitSetXY, 2, 0, 0),

		// Font size
		new CmdStruct("TINYFONT", Emitter::EmitSingleByte, 8, 0, 0),
		new CmdStruct("BIGFONT",  Emitter::EmitSingleByte, 9, 0, 0),

		// New line
		new CmdStruct("", Emitter::EmitSingleByte, 10, 0, C_DONTCOUNT),

		new CmdStruct("{", Emitter::EmitSingleByte, (int)'{', 0, C_DONTCOUNT),

		// Colors
		new CmdStruct("BLUE",    Emitter::EmitSingleByte, 15, 0, 0),
		new CmdStruct("SILVER",  Emitter::EmitSingleByte, 16, 0, 0),
		new CmdStruct("GOLD",    Emitter::EmitSingleByte, 17, 0, 0),
		new CmdStruct("RED",     Emitter::EmitSingleByte, 18, 0, 0),
		new CmdStruct("PURPLE",  Emitter::EmitSingleByte, 19, 0, 0),
		new CmdStruct("LTBROWN", Emitter::EmitSingleByte, 20, 0, 0),
		new CmdStruct("ORANGE",  Emitter::EmitSingleByte, 21, 0, 0),
		new CmdStruct("GREEN",   Emitter::EmitSingleByte, 22, 0, 0),
		new CmdStruct("YELLOW",  Emitter::EmitSingleByte, 23, 0, 0),
		new CmdStruct("DKGREEN", Emitter::EmitSingleByte, 24, 0, 0),
		new CmdStruct("CREAM",   Emitter::EmitSingleByte, 25, 0, 0),
		new CmdStruct("BROWN",   Emitter::EmitSingleByte, 26, 0, 0),
		new CmdStruct("WHITE",   Emitter::EmitSingleByte, 27, 0, 0),
		new CmdStruct("LTBLUE",  Emitter::EmitSingleByte, 28, 0, 0),
		new CmdStruct("GRAY",    Emitter::EmitSingleByte, 29, 0, 0),
		new CmdStruct("DKBLUE",  Emitter::EmitSingleByte, 30, 0, 0),
		new CmdStruct("BLACK",   Emitter::EmitSingleByte, 31, 0, 0),

		// 0x85
		new CmdStruct("CURRCOMPACT",   Emitter::EmitEscapedByte, 0, 1, 0), // compact currency (32 bits)
		new CmdStruct("REV",           Emitter::EmitEscapedByte, 2, 0, 0), // openttd revision string
		new CmdStruct("SHORTCARGO",    Emitter::EmitEscapedByte, 3, 2, 0), // short cargo description, only ### tons, or ### litres
		new CmdStruct("CURRCOMPACT64", Emitter::EmitEscapedByte, 4, 2, 0), // compact currency 64 bits

		new CmdStruct("COMPANY", Emitter::EmitEscapedByte, 5, 1, 0),				// company string. This is actually a new CmdStruct(STRING1)
																							// The first string includes the second string.

		new CmdStruct("PLAYERNAME", Emitter::EmitEscapedByte, 5, 1, 0),		// playername string. This is actually a new CmdStruct(STRING1)
																							// The first string includes the second string.

		new CmdStruct("VEHICLE", Emitter::EmitEscapedByte, 5, 1, 0),		// playername string. This is actually a new CmdStruct(STRING1)
																							// The first string includes the second string.


		new CmdStruct("STRING1", Emitter::EmitEscapedByte, 5, 1, C_CASE),				// included string that consumes ONE argument
		new CmdStruct("STRING2", Emitter::EmitEscapedByte, 6, 2, C_CASE),				// included string that consumes TWO arguments
		new CmdStruct("STRING3", Emitter::EmitEscapedByte, 7, 3, C_CASE),				// included string that consumes THREE arguments
		new CmdStruct("STRING4", Emitter::EmitEscapedByte, 8, 4, C_CASE),				// included string that consumes FOUR arguments
		new CmdStruct("STRING5", Emitter::EmitEscapedByte, 9, 5, C_CASE),				// included string that consumes FIVE arguments

		new CmdStruct("STATIONFEATURES", Emitter::EmitEscapedByte, 10, 1, 0), // station features string, icons of the features
		new CmdStruct("INDUSTRY",        Emitter::EmitEscapedByte, 11, 1, 0), // industry, takes an industry #
		new CmdStruct("VOLUME",          Emitter::EmitEscapedByte, 12, 1, 0),
		new CmdStruct("DATE_TINY",       Emitter::EmitEscapedByte, 14, 1, 0),
		new CmdStruct("CARGO",           Emitter::EmitEscapedByte, 15, 2, 0),

		new CmdStruct("P", Emitter::EmitPlural, 0, 0, C_DONTCOUNT),					// plural specifier
		new CmdStruct("G", Emitter::EmitGender, 0, 0, C_DONTCOUNT),					// gender specifier

		new CmdStruct("DATE_LONG",  Emitter::EmitSingleByte, 0x82, 1, 0),
		new CmdStruct("DATE_SHORT", Emitter::EmitSingleByte, 0x83, 1, 0),

		new CmdStruct("VELOCITY", Emitter::EmitSingleByte, 0x84, 1, 0),

		new CmdStruct("SKIP", Emitter::EmitSingleByte, 0x86, 1, 0),

		new CmdStruct("STRING", Emitter::EmitSingleByte, 0x88, 1, C_CASE),

		// Numbers
		new CmdStruct("COMMA", Emitter::EmitSingleByte, 0x8B, 1, 0), // Number with comma
		new CmdStruct("NUM",   Emitter::EmitSingleByte, 0x8E, 1, 0), // Signed number

		new CmdStruct("CURRENCY", Emitter::EmitSingleByte, 0x8F, 1, 0),

		new CmdStruct("WAYPOINT",   Emitter::EmitSingleByte, 0x99, 1, 0), // waypoint name
		new CmdStruct("STATION",    Emitter::EmitSingleByte, 0x9A, 1, 0),
		new CmdStruct("TOWN",       Emitter::EmitSingleByte, 0x9B, 1, 0),
		new CmdStruct("CURRENCY64", Emitter::EmitSingleByte, 0x9C, 2, 0),
		// 0x9D is used for the pseudo command SETCASE
		// 0x9E is used for case switching

		// 0x9E=158 is the LAST special character we may use.

		new CmdStruct("UPARROW", Emitter::EmitSingleByte, 0x80, 0, 0),

		new CmdStruct("NBSP",       Emitter::EmitSingleByte, 0xA0, 0, C_DONTCOUNT),
		new CmdStruct("POUNDSIGN",  Emitter::EmitSingleByte, 0xA3, 0, 0),
		new CmdStruct("YENSIGN",    Emitter::EmitSingleByte, 0xA5, 0, 0),
		new CmdStruct("COPYRIGHT",  Emitter::EmitSingleByte, 0xA9, 0, 0),
		new CmdStruct("DOWNARROW",  Emitter::EmitSingleByte, 0xAA, 0, 0),
		new CmdStruct("CHECKMARK",  Emitter::EmitSingleByte, 0xAC, 0, 0),
		new CmdStruct("CROSS",      Emitter::EmitSingleByte, 0xAD, 0, 0),
		new CmdStruct("RIGHTARROW", Emitter::EmitSingleByte, 0xAF, 0, 0),

		new CmdStruct("TRAIN", Emitter::EmitSingleByte, 0x94, 0, 0),
		new CmdStruct("LORRY", Emitter::EmitSingleByte, 0x95, 0, 0),
		new CmdStruct("BUS",   Emitter::EmitSingleByte, 0x96, 0, 0),
		new CmdStruct("PLANE", Emitter::EmitSingleByte, 0x97, 0, 0),
		new CmdStruct("SHIP",  Emitter::EmitSingleByte, 0x98, 0, 0),

		new CmdStruct("SMALLUPARROW",   Emitter::EmitSingleByte, 0x90, 0, 0),
		new CmdStruct("SMALLDOWNARROW", Emitter::EmitSingleByte, 0x91, 0, 0)
	};


	static final CmdStruct FindCmd(String s)
	{
		/*
		int i;
		final CmdStruct *cs = _cmd_structs;
		for(i=0; i != lengthof(_cmd_structs); i++, cs++) {
			if (!strncmp(cs.cmd, s, len) && cs.cmd[len] == '\0')
				return cs;
		}
		*/
		for( CmdStruct cs : _cmd_structs)
			if( s.equals(cs.cmd) ) return cs;
		
		return null;
	}

	static int ResolveCaseName(String str)
	{
		for(int i = 0; i < MAX_NUM_CASES; i++)
			if(str.equals(_cases[i]))
				return i + 1;
		
		Fatal("Invalid case-name '%s'", str);
	}


	// returns null on eof
	// else returns command struct
	static final CmdStruct ParseCommandString(final String [] str, String param, int [] argno, int [] casei)
	{
		final char *s = *str, *start;
		final CmdStruct cmd;
		byte c;

		argno[0] = -1;
		casei[0] = -1;

		// Scan to the next command, exit if there's no next command.
		for(; *s != '{'; s++) {
			if (*s == '\0')
				return null;
		}
		s++; // Skip past the {

		if (*s >= '0' && *s <= '9') {
			char *end;
			argno[0] = strtoul(s, &end, 0);
			if (*end != ':') {
					Fatal("missing arg #");
				}
			s = end + 1;
		}

		// parse command name
		start = s;
		do {
			c = *s++;
		} while (c != '}' && c != ' ' && c != '=' && c != '.' && c != 0);

		cmd = FindCmd(start, s - start - 1);
		if (cmd == null) {
			Error("Undefined command '%.*s'", s - start - 1, start);
			return null;
		}

		if (c == '.') {
			final char *casep = s;

			if (0==(cmd.flags & C_CASE))
				Fatal("Command '%s' can't have a case", cmd.cmd);

			do c = *s++; while (c != '}' && c != ' ' && c != '\0');
			casei[0] = ResolveCaseName(casep, s-casep-1);
		}

		if (c == '\0') {
			Error("Missing } from command '%s'", start);
			return null;
		}


		if (c != '}') {
			if (c == '=') s--;
			// copy params
			start = s;
			for(;;) {
				c = *s++;
				if (c == '}') break;
				if (c == '\0') {
					Error("Missing } from command '%s'", start);
					return null;
				}
				if ( s - start == 250)
					Fatal("param command too long");
				*param++ = c;
			}
		}
		*param = 0;

		*str = s;

		return cmd;
	}


	static void HandlePragma(String istr)
	{
		String [] token = istr.split("\\s");
		String str = token[0];
		String param = token[1];
		
		if (str.equals( "id ")) {
			_next_string_id = Integer.parseInt(param);
		} else if (str.equals( "name ")) {
			_lang_name = param;
		} else if (str.equals( "ownname ")) {
			_lang_ownname = param;
		} else if (str.equals( "isocode ")) {
			_lang_isocode = param;
		} else if (str.equals( "plural ")) {
			_lang_pluralform = atoi(str + 7);
			if (_lang_pluralform >= lengthof(_plural_form_counts))
				Fatal("Invalid pluralform %d", _lang_pluralform);
		} else if (str.equals( "gender ", 7)) {
			char *buf = str + 7, *s;
			for(;;) {
				s = ParseWord(&buf);
				if (!s) break;
				if (_numgenders >= MAX_NUM_GENDER) Fatal("Too many genders, max %d", MAX_NUM_GENDER);
				ttd_strlcpy(_genders[_numgenders], s, sizeof(_genders[_numgenders]));
				_numgenders++;
			}
		} else if (str.equals( "case ", 5)) {
			char *buf = str + 5, *s;
			for(;;) {
				s = ParseWord(&buf);
				if (!s) break;
				if (_numcases >= MAX_NUM_CASES) Fatal("Too many cases, max %d", MAX_NUM_CASES);
				ttd_strlcpy(_cases[_numcases], s, sizeof(_cases[_numcases]));
				_numcases++;
			}
		} else {
			Fatal("unknown pragma '%s'", str);
		}
	}

	static void ExtractCommandString(ParsedCommandStruct *p, char *s, boolean warnings)
	{
		final CmdStruct *ar;
		char param[100];
		int argno;
		int argidx = 0;
		int casei;

		memset(p, 0, sizeof(*p));

		for(;;) {
			// read until next command from a.
			ar = ParseCommandString((final char **)&s, param, &argno, &casei);
			if (ar == null)
				break;

			// Sanity checking
			if (argno != -1 && !ar.consumes) Fatal("Non consumer param can't have a paramindex");

			if (ar.consumes) {
				if (argno != -1)
					argidx = argno;
				if (argidx < 0 || argidx >= lengthof(p.cmd)) Fatal("invalid param idx %d", argidx);
				if (p.cmd[argidx] != null && p.cmd[argidx] != ar) Fatal("duplicate param idx %d", argidx);

				p.cmd[argidx++] = ar;
			} else if (!(ar.flags & C_DONTCOUNT)) { // Ignore some of them
				if (p.np >= lengthof(p.pairs)) Fatal("too many commands in string, max %d", lengthof(p.pairs));
				p.pairs[p.np].a = ar;
				p.pairs[p.np].v = param[0]?strdup(param):"";
				p.np++;
			}
		}
	}


	static final CmdStruct TranslateCmdForCompare(final CmdStruct a)
	{
		if (a == null) return null;

		if (a.cmd.equals( "STRING1") ||
				a.cmd.equals( "STRING2") ||
				a.cmd.equals( "STRING3") ||
				a.cmd.equals( "STRING4") ||
				a.cmd.equals( "STRING5"))
			return FindCmd("STRING");

		if (a.cmd.equals( "SKIP"))
			return null;

		return a;
	}


	static boolean CheckCommandsMatch(String a, String b, final String name)
	{
		ParsedCommandStruct templ;
		ParsedCommandStruct lang;
		int i,j;
		boolean result = true;

		ExtractCommandString(&templ, b, true);
		ExtractCommandString(&lang, a, true);

		// For each string in templ, see if we find it in lang
		if (templ.np != lang.np) {
			Warning("%s: template string and language string have a different # of commands", name);
			result = false;
		}

		for(i = 0; i < templ.np; i++) {
			// see if we find it in lang, and zero it out
			boolean found = false;
			for(j = 0; j < lang.np; j++) {
				if (templ.pairs[i].a == lang.pairs[j].a &&
						!strcmp(templ.pairs[i].v, lang.pairs[j].v)) {
					// it was found in both. zero it out from lang so we don't find it again
					lang.pairs[j].a = null;
					found = true;
					break;
				}
			}

			if (!found) {
				Warning("%s: command '%s' exists in template file but not in language file", name, templ.pairs[i].a.cmd);
				result = false;
			}
		}

		// if we reach here, all non consumer commands match up.
		// Check if the non consumer commands match up also.
		for(i = 0; i < lengthof(templ.cmd); i++) {
			if (TranslateCmdForCompare(templ.cmd[i]) != TranslateCmdForCompare(lang.cmd[i])) {
				Warning("%s: Param idx #%d '%s' doesn't match with template command '%s'", name, i,
					!lang.cmd[i] ? "<empty>" : lang.cmd[i].cmd,
					!templ.cmd[i] ? "<empty>" : templ.cmd[i].cmd);
				result = false;
			}
		}

		return result;
	}

	static void HandleString(String str, boolean master)
	{
		char *s,*t;
		LangString *ent;
		char *casep;

		if (*str == '#') {
			if (str[1] == '#' && str[2] != '#')
				HandlePragma(str + 2);
			return;
		}

		// Ignore comments & blank lines
		if (*str == ';' || *str == ' ' || *str == '\0')
			return;

		s = strchr(str, ':');
		if (s == null) {
			Error("Line has no ':' delimiter");
			return;
		}

		// Trim spaces.
		// After this str points to the command name, and s points to the command contents
		for(t = s; t > str && (t[-1]==' ' || t[-1]=='\t'); t--);
		*t = 0;
		s++;

		// Check if the string has a case..
		// The syntax for cases is IDENTNAME.case
		casep = strchr(str, '.');
		if (casep) *casep++ = 0;

		// Check if this string already exists..
		ent = HashFind(str);

		if (master) {
			if (ent != null && !casep) {
				Error("String name '%s' is used multiple times", str);
				return;
			}

			if (ent == null && casep) {
				Error("Base string name '%s' doesn't exist yet. Define it before defining a case.", str);
				return;
			}

			if (ent == null) {
				if (_strings[_next_string_id]) {
					Error("String ID 0x%X for '%s' already in use by '%s'", ent, str, _strings[_next_string_id].name);
					return;
				}

				// Allocate a new LangString
				ent = calloc(sizeof(LangString), 1);
				_strings[_next_string_id] = ent;
				ent.index = _next_string_id++;
				ent.name = strdup(str);
				ent.line = _cur_line;

				HashAdd(str, ent);
			}

			if (casep) {
				Case *c = malloc(sizeof(Case));
				c.caseidx = ResolveCaseName(casep, strlen(casep));
				c.string = strdup(s);
				c.next = ent.english_case;
				ent.english_case = c;
			} else {
				ent.english = strdup(s);
			}

		} else {
			if (ent == null) {
				Warning("String name '%s' does not exist in master file", str);
				return;
			}

			if (ent.translated && !casep) {
				Error("String name '%s' is used multiple times", str);
				return;
			}

			if (s[0] == ':' && s[1] == '\0' && casep == null) {
				// Special syntax :: means we should just inherit the master string
				ent.translated = strdup(ent.english);
			} else {
				// make sure that the commands match
				if (!CheckCommandsMatch(s, ent.english, str))
					return;

				if (casep) {
					Case *c = malloc(sizeof(Case));
					c.caseidx = ResolveCaseName(casep, strlen(casep));
					c.string = strdup(s);
					c.next = ent.translated_case;
					ent.translated_case = c;
				} else {
					ent.translated = strdup(s);
				}
			}
		}
	}


	static void rstrip(char *buf)
	{
		int i = strlen(buf);
		while (i>0 && (buf[i-1]=='\r' || buf[i-1]=='\n' || buf[i-1] == ' ')) i--;
		buf[i] = 0;
	}


	static void ParseFile(final char *file, boolean english)
	{
		FILE *in;
		char buf[2048];

		_file = file;

		// For each new file we parse, reset the genders.
		_numgenders = 0;
		// TODO:!! We can't reset the cases. In case the translated strings
		// derive some strings from english....


		in = fopen(file, "r");
		if (in == null) Fatal("Cannot open file");
		_cur_line = 1;
		while (fgets(buf, sizeof(buf),in) != null) {
			rstrip(buf);
			HandleString(buf, english);
			_cur_line++;
		}
		fclose(in);
	}


	static int MyHashStr(int hash, final char *s)
	{
		for(; *s; s++) {
			hash = ((hash << 3) | (hash >> 29)) ^ *s;
			if (hash & 1) hash = (hash>>1) ^ 0xDEADBEEF; else hash >>= 1;
		}
		return hash;
	}


	// make a hash of the file to get a unique "version number"
	static void MakeHashOfStrings()
	{
		int hash = 0;
		LangString ls;
		String s;
		final CmdStruct cs;
		char buf[256];
		int i;
		int argno;
		int casei;

		for(i = 0; i != 65536; i++) {
			if ((ls=_strings[i]) != null) {
				s = ls.name;
				hash ^= i * 0x717239;
				if (hash & 1) hash = (hash>>1) ^ 0xDEADBEEF; else hash >>= 1;
				hash = MyHashStr(hash, s + 1);

				s = ls.english;
				while ((cs = ParseCommandString((final char **)&s, buf, &argno, &casei)) != null) {
					if (cs.flags & C_DONTCOUNT)
						continue;

					hash ^= (cs - _cmd_structs) * 0x1234567;
					if (hash & 1) hash = (hash>>1) ^ 0xF00BAA4; else hash >>= 1;
				}
			}
		}
		_hash = hash;
	}


	static int CountInUse(int grp)
	{
		int i;

		for(i = 0x800; --i >= 0;) {
			if (_strings[(grp<<11)+i] != null)
				break;
		}
		return i + 1;
	}




	static boolean CompareFiles(final String n1, final String n2)
	{
		//FILE *f1, *f2;
		byte [] b1 = new byte[4096];
		byte [] b2 = new byte[4096];
		int l1, l2;

		//f2 = fopen(n2, "rb");
		BufferedInputStream f2 = new BufferedInputStream( new FileInputStream(n2) );
		if (f2 == null) return false;

		//f1 = fopen(n1, "rb");
		//if (f1 == null) Fatal("can't open %s", n1);
		BufferedInputStream f1 = new BufferedInputStream( new FileInputStream(n1) );
		if (f1 == null) Fatal("can't open %s", n1);

		do {
			//l1 = fread(b1, 1, sizeof(b1), f1);
			//l2 = fread(b2, 1, sizeof(b2), f2);
			l1 = f1.read(b1);
			l2 = f2.read(b2);
			
			boolean diff = Arrays.compare(b1, b2) != 0;
			if (l1 != l2 || 
					//memcmp(b1, b2, l1)
					diff
					) {
				f2.close();
				f1.close();
				return false;
			}
		} while (l1 > 0);

		f2.close();
		f1.close();
		return true;
	}


	static void WriteStringsH(final String filename)
	{
		//FILE *out;
		Writer out  = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("tmp.xxx")));
		int i;
		int next = -1;
		int lastgrp;

		//out = fopen("tmp.xxx", "w");
		if (out == null) { Fatal("can't open tmp.xxx"); }

		out.write("enum {");

		lastgrp = 0;

		for(i = 0; i != 65536; i++) {
			if (_strings[i] != null) {
				if (lastgrp != (i >> 11)) {
					lastgrp = (i >> 11);
					out.write("};\n\nenum {");
				}

				String s = String.format( next == i ? "%s,\n" : "\n%s = 0x%X,\n", _strings[i].name, i);
				out.write( s );
				next = i + 1;
			}
		}

		out.write("};\n");

		String suffix = String.format(
			"\nenum {\n" +
			"\tLANGUAGE_PACK_IDENT = 0x474E414C, // Big Endian value for 'LANG' (LE is 0x 4C 41 4E 47)\n" +
			"\tLANGUAGE_PACK_VERSION = 0x%X,\n" +
			"};\n", (int)_hash);

			out.write(suffix);

		out.close();

		if (CompareFiles("tmp.xxx", filename)) {
			// files are equal. tmp.xxx is not needed
			Files.delete(Path.of("tmp.xxx"));
			
		} else {
			// else rename tmp.xxx into filename
	//#if defined(WIN32) || defined(WIN64)
			Files.delete(Path.of(filename));
	//#endif
			Files.move(Path.of("tmp.xxx"), Path.of(filename), null);
			//if (rename("tmp.xxx", filename) == -1) Fatal("rename() failed");
		}
	}

	static int TranslateArgumentIdx(int argidx)
	{
		int i, sum;

		if (argidx < 0 || argidx >= _cur_pcs.cmd.length)
			Fatal("invalid argidx %d", argidx);

		for(i = sum = 0; i < argidx; i++) {
			final CmdStruct cs = _cur_pcs.cmd[i];
			sum += cs != null ? cs.consumes : 1;
		}

		return sum;
	}



	static void PutCommandString(final String str)
	{
		final CmdStruct cs;
		char param[256];
		int argno;
		int casei;

		_cur_argidx = 0;

		while (*str != '\0') {
			// Process characters as they are until we encounter a {
			if (*str != '{') {
				PutByte(*str++);
				continue;
			}
			cs = ParseCommandString(&str, param, &argno, &casei);
			if (cs == null) break;

			if (casei != -1) {
				PutByte(0x9D); // {SETCASE}
				PutByte(casei);
			}

			// For params that consume values, we need to handle the argindex properly
			if (cs.consumes) {
				// Check if we need to output a move-param command
				if (argno!=-1 && argno != _cur_argidx) {
					_cur_argidx = argno;
					PutArgidxCommand();
				}

				// Output the one from the master string... it's always accurate.
				cs = _cur_pcs.cmd[_cur_argidx++];
				if (!cs)
					Fatal("%s: No argument exists at posision %d", _cur_ident, _cur_argidx-1);
			}

			cs.proc(param, cs.value);
		}
	}

	static void WriteLength(BufferedOutputStream f, uint length)
	{
		if (length < 0xC0) {
			//fputc(length, f);
			f.write((byte)length);
		} else if (length < 0x4000) {
			f.write((byte)((length >> 8) | 0xC0));
			f.write((byte)length & 0xFF);
		} else {
			Fatal("string too long");
		}
	}


	static void WriteLangfile(String filename, int show_todo)
	{
		BufferedOutputStream f;
		int in_use[32];
		LanguagePackHeader hdr;
		int i,j;

		//f = fopen(filename, "wb");
		//if (f == null) Fatal("can't open %s", filename);
		f = new BufferedOutputStream(new FileOutputStream(filename));
		
		
		memset(&hdr, 0, sizeof(hdr));
		for(i = 0; i != 32; i++) {
			int n = CountInUse(i);
			in_use[i] = n;
			hdr.offsets[i] = TO_LE16(n);
		}

		// see line 655: fprintf(..."\tLANGUAGE_PACK_IDENT = 0x474E414C,...)
		hdr.ident = TO_LE32(0x474E414C); // Big Endian value for 'LANG'
		hdr.version = TO_LE32(_hash);
		hdr.plural_form = _lang_pluralform;
		strcpy(hdr.name, _lang_name);
		strcpy(hdr.own_name, _lang_ownname);
		strcpy(hdr.isocode, _lang_isocode);

		fwrite(&hdr, sizeof(hdr), 1, f);

		for(i = 0; i != 32; i++) {
			for(j = 0; j != in_use[i]; j++) {
				LangString *ls = _strings[(i<<11)+j];

				Case *casep;
				char *cmdp;

				// For undefined strings, just set that it's an empty string
				if (ls == null) {
					WriteLength(f, 0);
					continue;
				}

				_cur_ident = ls.name;
				_cur_line = ls.line;

				// Produce a message if a string doesn't have a translation.
				if (show_todo && ls.translated == null) {
					if (show_todo == 2) {
						Warning("'%s' is untranslated", ls.name);
					} else {
						final char *s = "<TODO> ";
						while(*s) PutByte(*s++);
					}
				}

				// Extract the strings and stuff from the english command string
				ExtractCommandString(&_cur_pcs, ls.english, false);

				if (ls.translated_case || ls.translated) {
					casep = ls.translated_case;
					cmdp = ls.translated;
				} else {
					casep = ls.english_case;
					cmdp = ls.english;
				}

				_translated = _masterlang || (cmdp != ls.english);

				if (casep) {
					Case *c;
					int num;
					// Need to output a case-switch.
					// It has this format
					// <0x9E> <NUM CASES> <CASE1> <LEN1> <STRING1> <CASE2> <LEN2> <STRING2> <CASE3> <LEN3> <STRING3> <STRINGDEFAULT>
					// Each LEN is printed using 2 bytes in big endian order.
					PutByte(0x9E);
					// Count the number of cases
					for(num=0,c=casep; c; c=c.next) num++;
					PutByte(num);

					// Write each case
					for(c=casep; c; c=c.next) {
						int pos;
						PutByte(c.caseidx);
						// Make some space for the 16-bit length
						pos = _put_pos;
						PutByte(0);
						PutByte(0);
						// Write string
						PutCommandString(c.string);
						PutByte(0); // terminate with a zero
						// Fill in the length
						_put_buf[pos] = (_put_pos - (pos + 2)) >> 8;
						_put_buf[pos+1] = (_put_pos - (pos + 2)) & 0xFF;
					}
				}

				if (cmdp)
					PutCommandString(cmdp);

				WriteLength(f, _put_pos);
				fwrite(_put_buf, 1, _put_pos, f);
				_put_pos = 0;
			}
		}

		fputc(0, f);

		fclose(f);
	}


	public static void main(String[] args) {
		int argc = args.length;
		
	/*
		char *r;
		char buf[256];
		int show_todo = 0;

		if (argc > 1 && (!strcmp(argv[1], "-v") || !strcmp(argv[1], "--version"))) {
			puts("$Revision: 3310 $");
			return 0;
		}

		if (argc > 1 && !strcmp(argv[1], "-t")) {
			show_todo = 1;
			argc--, argv++;
		}

		if (argc > 1 && !strcmp(argv[1], "-w")) {
			show_todo = 2;
			argc--, argv++;
		}


		if (argc == 1) {
*/		
			_masterlang = true;
			// parse master file
			ParseFile("lang/english.txt", true);
			MakeHashOfStrings();
			if (_errors) return 1;

			// write english.lng and strings.h

			WriteLangfile("lang/english.lng", 0);
			WriteStringsH("table/strings.h");
/*
		} else if (argc == 2) {
			_masterlang = false;
			ParseFile("lang/english.txt", true);
			MakeHashOfStrings();
			ParseFile(argv[1], false);

			if (_errors) return 1;

			strcpy(buf, argv[1]);
			r = strrchr(buf, '.');
			if (!r || strcmp(r, ".txt")) r = strchr(buf, 0);
			strcpy(r, ".lng");
			WriteLangfile(buf, show_todo);
		} else {
			fprintf(stderr, "invalid arguments\n");
		}

		return 0;
*/		
	}

	
	
}
