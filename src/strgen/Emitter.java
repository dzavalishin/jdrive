package strgen;

import java.io.BufferedOutputStream;

public class Emitter 
{
	private BufferedOutputStream os;


	void PutByte(int c)
	{
		/*
		if (_put_pos == lengthof(_put_buf))
			Fatal("Put buffer too small");
		_put_buf[_put_pos++] = c;
		 */
		os.write(c);
	}

	void PutArgidxCommand()
	{
		PutByte(0x8C);
		PutByte(Main.TranslateArgumentIdx(Main._cur_argidx));
	}

	void EmitSingleByte(String buf, int value)
	{
		if (buf.length() != 0)
			Warning("Ignoring trailing letters in command");
		PutByte((byte)value);
	}


	void EmitEscapedByte(String buf, int value)
	{
		if (buf.length() != 0)
			Warning("Ignoring trailing letters in command");
		PutByte((byte)0x85);
		PutByte((byte)value);
	}

	void EmitSetX(String buf, int value)
	{
		int x = Integer.parseInt(buf);
				/*char *err;
		int x = strtol(buf, &err, 0);
		if (*err != 0)
			Fatal("SetX param invalid");*/
		PutByte(1);
		PutByte((byte)x);
	}


	void EmitSetXY(String buf, int value)
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

	void EmitPlural(String buf, int value)
	{
		int argidx = Main._cur_argidx;
		String [] words = new String[5];
		int nw = 0;

		// Parse out the number, if one exists. Otherwise default to prev arg.
		if (!ParseRelNum(&buf, &argidx))
			argidx--;

		// Parse each string
		for (nw = 0; nw < 5; nw++) {
			words[nw] = ParseWord(&buf);
			if (!words[nw])
				break;
		}

		if (nw == 0)
			Fatal("%s: No plural words", Main._cur_ident);

		if (Main._plural_form_counts[Main._lang_pluralform] != nw) {
			if (Main._translated) {
				Fatal("%s: Invalid number of plural forms. Expecting %d, found %d.", Main._cur_ident,
						Main._plural_form_counts[Main._lang_pluralform], nw);
			} else {
				Warning("'%s' is untranslated. Tweaking english string to allow compilation for plural forms", Main._cur_ident);
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
		PutByte(TranslateArgumentIdx(argidx));
		EmitWordList(words, nw);
	}


	void EmitGender(String buf, int value)
	{
		int argidx = Main._cur_argidx;
		String [] words = new String[8];
		int nw;

		if (buf[0] == '=') {
			buf++;

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
			// This is a {G 0 foo bar two} command.
			// If no relative number exists, default to +0
			if (!ParseRelNum(&buf, &argidx)) {}

			for(nw=0; nw<8; nw++) {
				words[nw] = ParseWord(&buf);
				if (!words[nw])
					break;
			}
			if (nw != _numgenders) Fatal("Bad # of arguments for gender command");
			PutByte(0x85);
			PutByte(13);
			PutByte(TranslateArgumentIdx(argidx));
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


}
