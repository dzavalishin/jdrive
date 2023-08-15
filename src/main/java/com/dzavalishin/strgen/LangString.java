package com.dzavalishin.strgen;

public class LangString 
{
	
	String	name;				// Name of the string
	String	english;			// English text
	String	translated;			// Translated text
	int		hash_next;			// next hash entry
	int		index;
	int		line;               // line of string in source-file
	Case	english_case;		// cases for english
	Case	translated_case;	// cases for foreign
	
}


/*


	typedef struct LangString {
		char *name;							// Name of the string
		char *english;					// English text
		char *translated;				// Translated text
		uint16 hash_next;				// next hash entry
		uint16 index;
		int line;               // line of string in source-file
		Case *english_case;			// cases for english
		Case *translated_case;	// cases for foreign
	} LangString;


*/

