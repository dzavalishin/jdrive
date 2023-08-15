package com.dzavalishin.util;

import java.io.FileInputStream;
import java.io.IOException;

class LanguagePack {
	int ident;
	int version;			// 32-bits of auto generated version info which is basically a hash of strings.h

	String name;			// the international name of this language
	String own_name;	// the localized name of this language	
	String isocode;	// the ISO code for the language (not country code)

	final int[] offsets = new int[32];	// the offsets
	byte plural_form;		// how to compute plural forms

	byte data[];
	
	/* Orig C struct is
	int32 ident;
	int32 version;				// +4 32-bits of auto generated version info which is basically a hash of strings.h
	char name[32];				// +8 the international name of this language
	char own_name[32];			// +40 the localized name of this language
	char isocode[16];			// +72 the ISO code for the language (not country code)
	int16 offsets[32];			// +88 the offsets
	byte plural_form;			// +152 how to compute plural forms
	byte pad[3];				// +153 pad header to be a multiple of 4
	char data[VARARRAY_SIZE];	// +156
	 */


	public boolean loadFromBytes(byte [] src)
	{
		data = src; 
		/*
		ByteBuffer bb = ByteBuffer.wrap(src);

		ident = bb.getInt();
		version = bb.getInt();

		byte [] nameChars = new byte[32]; 
		bb.get(nameChars);

		byte [] ownNameChars = new byte[32]; 
		bb.get(ownNameChars);

		byte [] isoChars = new byte[16]; 
		bb.get(isoChars);

		for( int i = 0; i < offsets.length; i++ )
			offsets[i] = bb.getInt();

		plural_form = bb.get();
		*/
		
		name = BitOps.stringFromBytes(src, 8, 32 ).strip();
		own_name = BitOps.stringFromBytes(src, 40, 32 ).strip();
		isocode = BitOps.stringFromBytes(src, 72, 16 ).strip();

		
		return isValid();
	}

	public boolean isValid() {
		/* TODO 
		if ( //lang_pack_bytes.length < sizeof(LanguagePack) ||
				lang_pack.ident != BitOps.TO_LE32(LANGUAGE_PACK_IDENT) ||
				lang_pack.version != BitOps.TO_LE32(LANGUAGE_PACK_VERSION)) {
			return false;
		} */

		//return ident == TO_LE32(LANGUAGE_PACK_IDENT) && hdr.version == TO_LE32(LANGUAGE_PACK_VERSION);
		return true;
	}

	public boolean readFrom(FileInputStream in) throws IOException {
		byte[] src = in.readNBytes(144); // a bit less?
		return loadFromBytes(src);
	}
}