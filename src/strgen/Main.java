package strgen;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
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

	
	static final int C_DONTCOUNT = Emitter.C_DONTCOUNT;
	static final int C_CASE = Emitter.C_CASE;
	








	static boolean _masterlang;
	static final String _file = "(unknown file)";
	static int _cur_line;
	static int _errors, _warnings;


	static LangString [] _strings = new LangString[65536];


	static final int HASH_SIZE = 32767;
	static int [] _hash_head = new int[HASH_SIZE];

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

	boolean ParseRelNum(char **buf, int []value)
	{
		char *s = *buf, *end;
		boolean rel = false;
		int v;

		while (*s == ' ' || *s == '\t') s++;
		
		if (*s == '+') { rel = true; s++; }
		
		v = strtol(s, &end, 0);
		
		if (end == s) return false;
		
		if (rel || (v < 0))
			value[0] += v;
		else
			value[0] = v;
		
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


	static CmdStruct FindCmd(String s)
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




	static void HandlePragma(String istr)
	{
		String [] token = istr.split("\\s");
		String str = token[0];
		String param = token[1];
		
		if (str.equals( "id")) {
			_next_string_id = Integer.parseInt(param);
		} else if (str.equals( "name")) {
			_lang_name = param;
		} else if (str.equals( "ownname")) {
			_lang_ownname = param;
		} else if (str.equals( "isocode")) {
			_lang_isocode = param;
		} else if (str.equals( "plural ")) {
			_lang_pluralform = (byte) Integer.parseInt(param); // atoi(str + 7);
			if (_lang_pluralform >= _plural_form_counts.length)
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
				_cases[_numcases] = s;
				_numcases++;
			}
		} else {
			Fatal("unknown pragma '%s'", str);
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
						templ.pairs[i].v.equals(lang.pairs[j].v)) {
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
		for(i = 0; i < templ.cmd.length; i++) {
			if (TranslateCmdForCompare(templ.cmd[i]) != TranslateCmdForCompare(lang.cmd[i])) {
				Warning("%s: Param idx #%d '%s' doesn't match with template command '%s'", name, i,
					null == lang.cmd[i] ? "<empty>" : lang.cmd[i].cmd,
					null == templ.cmd[i] ? "<empty>" : templ.cmd[i].cmd);
				result = false;
			}
		}

		return result;
	}

	static void HandleString(String str, boolean master)
	{
		//char *s,*t;
		LangString ent;
		String casep = null;

		// Ignore blank lines
		if( str.length() == 0)
			return;

		char c0 = str.charAt(0);
		
		if (c0 == '#') {
			if (str.charAt(1) == '#' && str.charAt(2) != '#')
				HandlePragma(str.substring(2));
			return;
		}

		// Ignore comments & blank lines
		if (c0 == ';' || c0 == ' ')
			return;

		//s = strchr(str, ':');
		int colonIndex = str.indexOf(':');
		if (colonIndex < 0 ) {
			Error("Line has no ':' delimiter");
			return;
		}

		// Trim spaces.
		// After this str points to the command name, and s points to the command contents
		//for(t = s; t > str && (t[-1]==' ' || t[-1]=='\t'); t--);
		//*t = 0;
		//s++;
		
		String s = str.substring(colonIndex+1);
		str = str.substring(0,colonIndex).strip();

		// Check if the string has a case..
		// The syntax for cases is IDENTNAME.case
		int icasep = str.indexOf('.');
		if (icasep > 0) 
		{
			casep = str.substring(icasep+1);
			str = str.substring(0, icasep);
		}

		// Check if this string already exists..
		ent = HashFind(str);

		if (master) {
			if (ent != null && casep == null) {
				Error("String name '%s' is used multiple times", str);
				return;
			}

			if (ent == null && casep != null) {
				Error("Base string name '%s' doesn't exist yet. Define it before defining a case.", str);
				return;
			}

			if (ent == null) {
				if (_strings[_next_string_id] != null) {
					Error("String ID 0x%X for '%s' already in use by '%s'", ent, str, _strings[_next_string_id].name);
					return;
				}

				// Allocate a new LangString
				ent = new LangString();
				_strings[_next_string_id] = ent;
				ent.index = _next_string_id++;
				ent.name = str;
				ent.line = _cur_line;

				HashAdd(str, ent);
			}

			if (casep != null) {
				Case c = new Case();
				c.caseidx = ResolveCaseName(casep);
				c.string = s;
				c.next = ent.english_case;
				ent.english_case = c;
			} else {
				ent.english = s;
			}

		} else {
			if (ent == null) {
				Warning("String name '%s' does not exist in master file", str);
				return;
			}

			if (ent.translated != null && null == casep) {
				Error("String name '%s' is used multiple times", str);
				return;
			}

			if (s.charAt(0) == ':' && s.length() == 1 && casep == null) {
				// Special syntax :: means we should just inherit the master string
				ent.translated = ent.english;
			} else {
				// make sure that the commands match
				if (!CheckCommandsMatch(s, ent.english, str))
					return;

				if (casep != null) {
					Case c = new Case();
					c.caseidx = ResolveCaseName(casep);
					c.string = s;
					c.next = ent.translated_case;
					ent.translated_case = c;
				} else {
					ent.translated = s;
				}
			}
		}
	}




	static void ParseFile(final String file, boolean english)
	{
		//Writer out  = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("tmp.xxx")));
		FileReader fis = new FileReader(file);
		if (fis == null) Fatal("Cannot open file");
		
		BufferedReader in = new BufferedReader( fis );
		
		//char buf[2048];

		_file = file;

		// For each new file we parse, reset the genders.
		_numgenders = 0;
		// TODO:!! We can't reset the cases. In case the translated strings
		// derive some strings from english....
		
		_cur_line = 1;
		while(true) // (fgets(buf, sizeof(buf),in) != null) 
		{
			String s = in.readLine();
			if( s == null )
				break;
			
			s = s.strip();
			
			HandleString(s, english);
			_cur_line++;
		}
		in.close();;
	}


	static int MyHashStr(int hash, final String s)
	{
		char[] sc = s.toCharArray();
		
		for(int i = 0; i < sc.length; i++) {
			hash = ((hash << 3) | (hash >> 29)) ^ sc[i];
			if(0 != (hash & 1) ) hash = (hash>>1) ^ 0xDEADBEEF; else hash >>= 1;
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
		char []  buf = new char[256];
		int i;
		int argno;
		int casei;

		for(i = 0; i != 65536; i++) {
			if ((ls=_strings[i]) != null) {
				s = ls.name;
				hash ^= i * 0x717239;
				if(0 != (hash & 1)) hash = (hash>>1) ^ 0xDEADBEEF; else hash >>= 1;
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






	static void WriteLangfile(String filename, int show_todo)
	{
		int [] in_use = new int[32];
		
		LanguagePackHeader hdr = new LanguagePackHeader();
		//int i,j;

		//f = fopen(filename, "wb");
		//if (f == null) Fatal("can't open %s", filename);
		BufferedOutputStream b = new BufferedOutputStream(new FileOutputStream(filename));
		DataOutputStream f = new DataOutputStream(b);
		
		//memset(&hdr, 0, sizeof(hdr));
		for(int i = 0; i != 32; i++) {
			int n = CountInUse(i);
			in_use[i] = n;
			hdr.offsets[i] = n; TO_LE16(n);
		}

		// see line 655: fprintf(..."\tLANGUAGE_PACK_IDENT = 0x474E414C,...)
		hdr.ident = TO_LE32(0x474E414C); // Big Endian value for 'LANG'
		hdr.version = TO_LE32(_hash);
		hdr.plural_form = _lang_pluralform;
		hdr.name = _lang_name;
		hdr.own_name = _lang_ownname;
		hdr.isocode = _lang_isocode;

		//fwrite(&hdr, sizeof(hdr), 1, f);
		hdr.writeTo(f);

		Emitter e = new Emitter(f);
		
		e.writeLangFile(in_use, show_todo);

		//fputc(0, f);
		f.writeByte(0);

		f.close();
	}


	private static int TO_LE32(int i) {
		// TODO Auto-generated method stub
		return i;
	}

	private static int TO_LE16(int n) {
		// TODO Auto-generated method stub
		return n;
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
			if (_errors > 0) System.exit(1);

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
