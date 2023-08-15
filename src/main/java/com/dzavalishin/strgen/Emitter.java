package com.dzavalishin.strgen;

import java.io.DataOutputStream;
import java.io.IOException;

public class Emitter 
{
	private static final String MISSING_CLOSING = "Missing } from command '%s'";

	private final DataOutputStream f;

	int _put_pos;
	int _cur_argidx;
	String _cur_ident = "??";
	// Used when generating some advanced commands.
	ParsedCommandStruct _cur_pcs = new ParsedCommandStruct();
	static boolean _translated;

	
	static final byte [] _put_buf = new byte[4096];
	
	
	
	//enum Mode {
	static final int C_DONTCOUNT = 1;
	static final int C_CASE = 2;
	//};
	
	
	
	
	
	
	
	
	
	public Emitter(DataOutputStream s) {
		f = s;
	}

	void PutByte(int c) {

		if (_put_pos >= _put_buf.length)
			Fatal("Put buffer too small");
		_put_buf[_put_pos++] = (byte) c;

	}

	void PutArgidxCommand()
	{
		PutByte(0x8C);
		PutByte(TranslateArgumentIdx(_cur_argidx));
	}

	void EmitSingleByte(String buf, long value)
	{
		if (buf.length() != 0)
			Warning("Ignoring trailing letters in command");
		PutByte((byte)value);
	}


	void EmitEscapedByte(String buf, long value)
	{
		if (buf.length() != 0)
			Warning("Ignoring trailing letters in command");
		PutByte((byte)0x85);
		PutByte((byte)value);
	}

	void EmitSetX(String buf, long value)
	{
		int x = Integer.parseInt(buf);
				/*char *err;
		int x = strtol(buf, &err, 0);
		if (*err != 0)
			Fatal("SetX param invalid");*/
		PutByte(1);
		PutByte((byte)x);
	}


	void EmitSetXY(String buf, long value)
	{
		/*
		char *err;
		int x,y;

		x = strtol(buf, &err, 0);
		if (*err != ' ') Fatal("SetXY param invalid");
		y = strtol(err+1, &err, 0);
		if (*err != 0) Fatal("SetXY param invalid");
		 */
		String[] words = buf.split("\\s");
		if(words.length != 2)
			Fatal("SetXY param invalid");

		int x = Integer.parseInt(words[0]);
		int y = Integer.parseInt(words[1]);

		PutByte(2);
		PutByte((byte)x);
		PutByte((byte)y);
	}

	void EmitWordList(String [] words, int nw)
	{
		int i,j;

		PutByte(nw);
		for(i=0; i<nw; i++)
		{
			int l = words[i].length();
			assert l <= 255;
			PutByte(l);
		}
		for(i=0; i<nw; i++) 
		{
			int l = words[i].length();
			char[] ch = words[i].toCharArray();
			for(j=0; j < l; j++)
			{
				assert ch[j] < 0xFF;
				PutByte(ch[j]);
			}
		}
	}

	void EmitPlural(String buf, long value)
	{
		int argidx[] = { _cur_argidx };
		String [] words = new String[5];
		int nw = 0;

		buf = buf.trim();

		{
			int [] skip = {-1};
			// Parse out the number, if one exists. Otherwise default to prev arg.
			if (!ParseRelNum(buf, argidx, skip))
				argidx[0]--;
			else
				buf = buf.substring(skip[0]);
		}
		
		// Parse each string
		for (nw = 0; nw < 5; nw++) 
		{
			buf = buf.trim();
			
			int [] skip = {-1};
			words[nw] = ParseWord(buf, skip);
			buf = buf.substring(skip[0]);
					
			if (null == words[nw])
				break;
		}

		if (nw == 0)
			Fatal("%s: No plural words", _cur_ident);

		if (Main._plural_form_counts[Main._lang_pluralform] != nw) {
			if (_translated) {
				Fatal("%s: Invalid number of plural forms. Expecting %d, found %d.", _cur_ident,
						Main._plural_form_counts[Main._lang_pluralform], nw);
			} else {
				Warning("'%s' is untranslated. Tweaking english string to allow compilation for plural forms", _cur_ident);
				if (nw > Main._plural_form_counts[Main._lang_pluralform]) {
					nw = Main._plural_form_counts[Main._lang_pluralform];
				} else {
					for(; nw < Main._plural_form_counts[Main._lang_pluralform]; nw++) {
						words[nw] = words[nw - 1];
					}
				}
			}
		}

		PutByte(0x8D);
		PutByte(TranslateArgumentIdx(argidx[0]));
		EmitWordList(words, nw);
	}


	void EmitGender(String buf, long value)
	{
		int [] argidx = { _cur_argidx };
		String [] words = new String[8];
		int nw;

		buf = buf.trim();

		if (buf.charAt(0) == '=') 
		{
			buf = buf.substring(1);

			// This is a {G=DER} command
			for(nw=0; ;nw++) {
				if (nw >= 8)
					Fatal("G argument '%s' invalid", buf);
				//if (!strcmp(buf, Main._genders[nw]))
				if( buf.equals( Main._genders[nw]) )
					break;
			}
			// now nw contains the gender index
			PutByte(0x87);
			PutByte(nw);

		} else {
			int [] skip = {-1};
			
			buf = buf.trim();
			// This is a {G 0 foo bar two} command.
			// If no relative number exists, default to +0
			//if (!ParseRelNum(buf, argidx, skip)) {}
			ParseRelNum(buf, argidx, skip);

			buf = buf.substring(skip[0]);

			for(nw=0; nw<8; nw++) 
			{
				buf = buf.trim();
				words[nw] = ParseWord(buf, skip);
				buf = buf.substring(skip[0]);
				if (null == words[nw])
					break;
			}
			if (nw != Main._numgenders) Fatal("Bad # of arguments for gender command");
			PutByte(0x85);
			PutByte(13);
			PutByte(TranslateArgumentIdx(argidx[0]));
			EmitWordList(words, nw);
		}
	}



	static void  Warning(String s, Object ... args)
	{
		Main.Warning(s, args);
	}


	static void  Error(String s, Object ... args)
	{
		Main.Error(s, args);
	}


	static void  Fatal(String s, Object ... args)
	{
		Main.Fatal(s, args);
	}

	
	void writeLangFile(int [] in_use, int show_todo) throws IOException
	{
		
		for(int i = 0; i != 32; i++) {
			for(int j = 0; j != in_use[i]; j++) {
				LangString ls = Main._strings[(i<<11)+j];

				Case casep;
				String cmdp;

				// For undefined strings, just set that it's an empty string
				if (ls == null) {
					WriteLength(0);
					continue;
				}

				_cur_ident = ls.name;
				Main._cur_line = ls.line;

				// Produce a message if a string doesn't have a translation.
				if (show_todo != 0 && ls.translated == null) {
					if (show_todo == 2) {
						Warning("'%s' is untranslated", ls.name);
					} else {
						PutString("<TODO> ");
					}
				}

				// Extract the strings and stuff from the english command string
				_cur_pcs = new ParsedCommandStruct();
				ExtractCommandString(_cur_pcs, ls.english, false);

				if (ls.translated_case != null || ls.translated != null) {
					casep = ls.translated_case;
					cmdp = ls.translated;
				} else {
					casep = ls.english_case;
					cmdp = ls.english;
				}

				_translated = Main._masterlang || (cmdp != ls.english);

				if (casep != null) {
					Case c;
					int num;
					// Need to output a case-switch.
					// It has this format
					// <0x9E> <NUM CASES> <CASE1> <LEN1> <STRING1> <CASE2> <LEN2> <STRING2> <CASE3> <LEN3> <STRING3> <STRINGDEFAULT>
					// Each LEN is printed using 2 bytes in big endian order.
					PutByte(0x9E);
					// Count the number of cases
					for(num=0,c=casep; c != null; c=c.next) 
						num++;
					PutByte(num);

					// Write each case
					for(c=casep; c != null; c=c.next) {
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
						_put_buf[pos] = (byte) ((_put_pos - (pos + 2)) >> 8);
						_put_buf[pos+1] = (byte) ((_put_pos - (pos + 2)) & 0xFF);
					}
				}

				if (cmdp != null)
					PutCommandString(cmdp);

				WriteLength(_put_pos);
				//fwrite(_put_buf, 1, _put_pos, f);
				f.write(_put_buf, 0, _put_pos);
				_put_pos = 0;
			}
		}
		
	}

	private void PutString(String s) 
	{
		char [] ca = s.toCharArray();
		for( char c : ca )
		{
			assert c <= 0xFF;
			PutByte(c);
		}
		
	}

	
	void PutCommandString(final String str) throws IOException
	{
		
		StringBuilder param = new StringBuilder(); 
		int [] argno = {-1};
		int [] casei = {-1};

		_cur_argidx = 0;

		int sp = 0;
		char [] sc = str.toCharArray(); 
		
		while (sp < sc.length) 
		{
			CmdStruct cs;
			
			// Process characters as they are until we encounter a {
			if (sc[sp] != '{') {
				PutByte(sc[sp++]);
				continue;
			}

			{
			int [] skip = {-1};
			cs = ParseCommandString(str.substring(sp), param, argno, casei, skip);
			assert skip[0] > 0;
			sp += skip[0];
			}
			
			if (cs == null) break;

			if (casei[0] != -1) {
				PutByte(0x9D); // {SETCASE}
				PutByte(casei[0]);
			}

			// For params that consume values, we need to handle the argindex properly
			if (cs.consumes != 0) {
				// Check if we need to output a move-param command
				if (argno[0]!=-1 && argno[0] != _cur_argidx) {
					_cur_argidx = argno[0];
					PutArgidxCommand();
				}

				// Output the one from the master string... it's always accurate.
				cs = _cur_pcs.cmd[_cur_argidx++];
				if (null == cs)
					Fatal("%s: No argument exists at posision %d", _cur_ident, _cur_argidx-1);
			}

			cs.proc.accept(this, param.toString(), cs.value);
		}
	}

	
	// returns null on eof
	// else returns command struct
	static CmdStruct ParseCommandString(final String str, StringBuilder param, int [] argno, int [] casei, int [] skip)
	{
		//final char *s = *str, *start;
		final CmdStruct cmd;
		char c;

		argno[0] = -1;
		casei[0] = -1;
		skip[0] = -1;

		/*
		// Scan to the next command, exit if there's no next command.
		for(; *s != '{'; s++) {
			if (*s == '\0')
				return null;
		}
		*/
		
		
		if(str.length() == 0) return null;
		
		char [] sc = str.toCharArray(); 
		int s = 0; // position in sc

		while( true )
		{
			if( s >= sc.length ) return null;
			
			if( sc[s] == '{' ) break;
			
			s++;
		}
		
		
		assert sc[s] == '{';
		
		s++; // Skip past the {

		if (sc[s] >= '0' && sc[s] <= '9') {
			int end = s;
			while( sc[end] >= '0' && sc[end] <= '9' )
				end++;

			argno[0] = Integer.parseInt(new String(sc,s,end));//strtoul(s, &end, 0);
			
			
			if (sc[end] != ':') {
					Fatal("missing arg #");
				}
			s = end + 1;
		}

		// parse command name
		int start = s;
		c = 0;
		do {
			if( s >= sc.length )
			{
				Error(MISSING_CLOSING, start);
				return null;
			}
			c = sc[s++];
		} while (c != '}' && c != ' ' && c != '=' && c != '.' && c != 0);

		cmd = Main.FindCmd(new String( sc, start, s - start - 1) );
		if (cmd == null) {
			//Error("Undefined command '%.*s'", s - start - 1, start);
			Error("Undefined command '%s'", str.substring(start, s));
			return null;
		}

		if (c == '.') {
			final int casep = s;

			if (0==(cmd.flags & C_CASE))
				Fatal("Command '%s' can't have a case", cmd.cmd);

			do 
			{ 
				if( s >= sc.length )
					break;
				c = sc[s++]; 
			} 
			while (c != '}' && c != ' ' && c != '\0');
			
			casei[0] = Main.ResolveCaseName(new String( sc, casep, s-casep-1) );
		}

		if (c != '}' && s >= sc.length) {
			Error(MISSING_CLOSING, start);
			return null;
		}


		if (c != '}') {
			if (c == '=') s--;
			// copy params
			start = s;
			for(;;) {
				if( s >= sc.length )
					break;
				
				c = sc[s++];
				
				if (c == '}') break;
				if (c == '\0') {
					Error(MISSING_CLOSING, start);
					return null;
				}
				if ( s - start == 250)
					Fatal("param command too long");
				param.append( c );
			}
		}
		//*param = 0;

		//*str = s;
		skip[0] = s;

		return cmd;
	}
	

	void WriteLength(int length) throws IOException
	{
		if (length < 0xC0) {
			//fputc(length, f);
			f.writeByte(length);
		} else if (length < 0x4000) {
			f.writeByte( (length >> 8) | 0xC0 );
			f.writeByte(length & 0xFF);
		} else {
			Fatal("string too long");
		}
	}
	

	static void ExtractCommandString(ParsedCommandStruct p, String s, boolean warnings)
	{
		int argidx = 0;
		int [] argno = {-1};
		int [] casei = {-1};
		int [] skip = {-1};

		//memset(p, 0, sizeof(*p));
		//TODO p.cmd.clear();

		for(;;) 
		{
			StringBuilder param = new StringBuilder();
			// read until next command from a.
			final CmdStruct ar = ParseCommandString(s, param, argno, casei, skip);
			if (ar == null)
				break;

			assert skip[0] > 0;
			s = s.substring(skip[0]);
			
			// Sanity checking
			if (argno[0] != -1 && 0 == ar.consumes) Fatal("Non consumer param can't have a paramindex");

			if (ar.consumes != 0) {
				if (argno[0] != -1)
					argidx = argno[0];
				if (argidx < 0 || argidx >= p.cmd.length) 
					Fatal("invalid param idx %d", argidx);
				if (p.cmd[argidx] != null && p.cmd[argidx] != ar) 
					Fatal("duplicate param idx %d", argidx);

				p.cmd[argidx++] = ar;
			} 
			else if (0==(ar.flags & C_DONTCOUNT)) 
			{ // Ignore some of them
				String ps = param.toString();
				if (p.np >= p.pairs.length) 
					Fatal("too many commands in string, max %d", p.pairs.length);
				p.pairs[p.np] = new CmdPair();
				p.pairs[p.np].a = ar;
				p.pairs[p.np].v = ps.length() != 0 ? ps : "";
				p.np++;
			}
		}
	}


	
	// Parse out the next word, or null
	static String ParseWord(String buf, int [] skip)
	{
		
		char [] cs = buf.toCharArray();
		int s = 0, r = -1;
		
		if( skip != null ) skip[0] = 0;
		
		if (cs.length == 0)
			return null;

		if (cs[s] == '"') {
			r = ++s;
			// parse until next " or NUL
			for(;;) {
				if (s >= cs.length)
					break;
				if (cs[s] == '"') {
					//*s++ = 0;
					//break;
					if(skip != null) skip[0] = s+1;					
					assert r > 0;					
					return new String( cs, r, s-1 );
				}
				s++;
			}
		} else {
			// proceed until whitespace or NUL
			r = s;
			for(;;) {
				if (s >= cs.length)
					break;
				if (cs[s] == ' ' || cs[s] == '\t') {
					//*s++ = 0;
					break;
				}
				s++;
			}
		}
		//*buf = s;
		if(skip != null) skip[0] = s;
		
		assert r > 0;
		
		return new String( cs, r, s );
	}
	

	// The plural specifier looks like
	// {NUM} {PLURAL -1 passenger passengers} then it picks either passenger/passengers depending on the count in NUM

	// This is encoded like
	//  CommandByte <ARG#> <NUM> {Length of each string} {each string}

	boolean ParseRelNum(String buf, int []value, int [] skip)
	{
		int s = 0, end;
		boolean rel = false;
		int v;
		char [] cs = buf.toCharArray();

		// caller must do
		//while (*s == ' ' || *s == '\t') s++;
		
		if (cs[s] == '+') { rel = true; s++; }
		
		for( end = s; end < cs.length; end++ )
			if(cs[end] < '0' || cs[end] > '9')
				break;
		
		try {
			v = Integer.parseInt(new String(cs,s,end)); //strtol(s, &end, 0);
		} catch (NumberFormatException e) {
			return false;
		}
		
		//if (end == s) return false;
		
		if (rel || (v < 0))
			value[0] += v;
		else
			value[0] = v;
		
		//*buf = end;
		skip[0] = end;
		return true;
	}


	int TranslateArgumentIdx(int argidx)
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


	
	
}
