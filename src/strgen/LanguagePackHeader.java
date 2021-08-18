package strgen;

public class LanguagePackHeader 
{

		int ident;
		int version;					// 32-bits of auto generated version info which is basically a hash of strings.h
		String name;					// the international name of this language
		String own_name;				// the localized name of this language
		String isocode;					// the ISO code for the language (not country code)
		int [] offsets = new int[32];	// the offsets
		int plural_form;				// plural form index
	
	
}



/* Orig C struct for ref

	typedef struct {
		uint32 ident;
		uint32 version;			// 32-bits of auto generated version info which is basically a hash of strings.h
		char name[32];			// the international name of this language
		char own_name[32];	// the localized name of this language
		char isocode[16];	// the ISO code for the language (not country code)
		uint16 offsets[32];	// the offsets
		byte plural_form;		// plural form index
		byte pad[3];				// pad header to be a multiple of 4
	} LanguagePackHeader;


*/