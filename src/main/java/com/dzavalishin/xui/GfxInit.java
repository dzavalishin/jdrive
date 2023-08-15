package com.dzavalishin.xui;

import com.dzavalishin.game.Bridge;
import com.dzavalishin.game.Economy;
import com.dzavalishin.game.Engine;
import com.dzavalishin.game.GRFFile;
import com.dzavalishin.game.Global;
import com.dzavalishin.game.SpriteCache;
import com.dzavalishin.tables.EngineTables2;
import com.dzavalishin.util.FileIO;
import com.dzavalishin.util.LandscapeSprites;
import com.dzavalishin.util.Sprites;


public class GfxInit extends LandscapeSprites
{
	static final String[] files_win =
		{ 
				"TRG1R.GRF",
				"TRGIR.GRF",
				"nsignalsw.grf",
				null
		};

	static final String[] files_landscape =
		{ 
			"TRGC.GRF",
			"TRGHR.GRF", // TODO "TRGH.GRF",
			"TRGT.GRF",
				null
		};

	//#include "table/files.h"
	/*
	static FileList files_dos = {
			{
				{ "TRG1.GRF", {0x93,0x11,0x67,0x62,0x80,0xe5,0xb1,0x40,0x77,0xa8,0xee,0x41,0xc1,0xb4,0x21,0x92} },     //    0 - 4792 inclusive
				{ "TRGI.GRF", {0xda,0x6a,0x6c,0x9d,0xcc,0x45,0x1e,0xec,0x88,0xd7,0x92,0x11,0x43,0x7b,0x76,0xa8} },     // 4793 - 4889 inclusive
				{ "dosdummy.grf", {0x07,0x01,0xe6,0xc4,0x07,0x6a,0x5b,0xc3,0xf4,0x9f,0x01,0xad,0x21,0x6c,0xa0,0xc2} }, // 4890 - 4895 inclusive
				{ "nsignalsw.grf", {0x65,0xb9,0xd7,0x30,0x56,0x06,0xcc,0x9e,0x27,0x57,0xc8,0xe4,0x9b,0xb3,0x66,0x81} }, // 4896 - 5381 inclusive
				{ null, { 0 } }
			},
			{	{ "TRGC.GRF", {0xed,0x44,0x66,0x37,0xe0,0x34,0x10,0x4c,0x55,0x59,0xb3,0x2c,0x18,0xaf,0xe7,0x8d} },
				{ "TRGH.GRF", {0xee,0x66,0x16,0xfb,0x0e,0x6e,0xf6,0xb2,0x48,0x92,0xc5,0x8c,0x93,0xd8,0x6f,0xc9} },
				{ "TRGT.GRF", {0xfc,0xde,0x1d,0x7e,0x8a,0x74,0x19,0x7d,0x72,0xa6,0x26,0x95,0x88,0x4b,0x90,0x9e} }
			}
	};
	 */
	/*	
	static FileList files_win = {
			{
				{ "TRG1R.GRF", {0xb0,0x4c,0xe5,0x93,0xd8,0xc5,0x01,0x6e,0x07,0x47,0x3a,0x74,0x3d,0x7d,0x33,0x58} },    //    0 - 4792 inclusive
				{ "TRGIR.GRF", {0x0c,0x24,0x84,0xff,0x6b,0xe4,0x9f,0xc6,0x3a,0x83,0xbe,0x6a,0xb5,0xc3,0x8f,0x32} },    // 4793 - 4895 inclusive
				{ "nsignalsw.grf", {0x65,0xb9,0xd7,0x30,0x56,0x06,0xcc,0x9e,0x27,0x57,0xc8,0xe4,0x9b,0xb3,0x66,0x81} }, // 4896 - 5381 inclusive
				{ null, { 0 } },
				{ null, { 0 } }
			},
			{	{ "TRGCR.GRF", {0x36,0x68,0xf4,0x10,0xc7,0x61,0xa0,0x50,0xb5,0xe7,0x09,0x5a,0x2b,0x14,0x87,0x9b} },
				{ "TRGHR.GRF", {0x06,0xbf,0x2b,0x7a,0x31,0x76,0x6f,0x04,0x8b,0xaa,0xc2,0xeb,0xe4,0x34,0x57,0xb1} },
				{ "TRGTR.GRF", {0xde,0x53,0x65,0x05,0x17,0xfe,0x66,0x1c,0xea,0xa3,0x13,0x8c,0x6e,0xdb,0x0e,0xb8} }
			}
	};

	static MD5File sample_cat_win = { "SAMPLE.CAT", {0x92,0x12,0xe8,0x1e,0x72,0xba,0xdd,0x4b,0xbe,0x1e,0xae,0xae,0x66,0x45,0x8e,0x10} };
	static MD5File sample_cat_dos = { "SAMPLE.CAT", {0x42,0x2e,0xa3,0xdd,0x07,0x4d,0x28,0x59,0xbb,0x51,0x63,0x9a,0x6e,0x0e,0x85,0xda} };
	 */

	//#include "table/landscape_sprite.h"

	static final int [][] _landscape_spriteindexes = {
			_landscape_spriteindexes_1,
			_landscape_spriteindexes_2,
			_landscape_spriteindexes_3,
	};

	static final int [][] _slopes_spriteindexes = {
			_slopes_spriteindexes_0,
			_slopes_spriteindexes_1,
			_slopes_spriteindexes_2,
			_slopes_spriteindexes_3,
	};


	static int LoadGrfFile(final String filename, int load_index, int file_index)
	{
		int load_index_org = load_index;

		FileIO.FioOpenFile(file_index, filename);

		Global.DEBUG_spritecache( 2,"Reading grf-file ``%s'', start index %d", filename, load_index);

		while (SpriteCache.LoadNextSprite(load_index, (byte) file_index)) {
			load_index++;
			if (load_index >= Sprites.MAX_SPRITES) {
				Global.error("Too many sprites. Recompile with higher MAX_SPRITES value or remove some custom GRF files.");
			}
		}
		Global.DEBUG_spritecache( 2,"Currently %d sprites are loaded", load_index);

		return load_index - load_index_org;
	}


	static void LoadGrfIndexed(final String filename, final int[] index_tbl, int file_index)
	{

		FileIO.FioOpenFile(file_index, filename);

		Global.DEBUG_spritecache( 2,"Reading indexed grf-file ``%s''", filename);

		int i = 0;
		//while ((start = *index_tbl++) != END) {
		//	int end = *index_tbl++;

		while(true)
		{
			int start = index_tbl[i++];
			if( start == END ) break;
			int end = index_tbl[i++];

			
			if (start == SKIP) { // skip sprites (amount in second var)
				SpriteCache.SkipSprites(end);
			} else { // load sprites and use indexes from start to end
				do {
					//Global.DEBUG_grf(0, "load spr %d", start );
					boolean b = SpriteCache.LoadNextSprite(start, (byte)file_index);
					assert(b);
				} while (++start <= end);
			}
		}
	}


	/* Check that the supplied MD5 hash matches that stored for the supplied filename * /
	static boolean CheckMD5Digest(final MD5File file, md5_byte_t *digest, boolean warn)
	{
		int i;

		/* Loop through each byte of the file MD5 and the stored MD5... * /
		for (i = 0; i < 16; i++) if (file.hash[i] != digest[i]) break;

			/* If all bytes of the MD5's match (i.e. the MD5's match)... * /
		if (i == 16) {
			return true;
		} else {
			if (warn) fprintf(stderr, "MD5 of %s is ****INCORRECT**** - File Corrupt.\n", file.filename);
			return false;
		};
	}
/*
	//* Calculate and check the MD5 hash of the supplied filename.
	// * returns true if the checksum is correct 
	private static boolean FileMD5(final MD5File file, boolean warn)
	{
		FILE *f;
		char buf[MAX_PATH];

		// open file
		sprintf(buf, "%s%s", _path.data_dir, file.filename);
		f = fopen(buf, "rb");

		/*
	#if !defined(WIN32)
		if (f == null) {
			char *s;
		// make lower case and check again
			for (s = buf + strlen(_path.data_dir) - 1; *s != '\0'; s++)
	 *s = tolower(*s);
			f = fopen(buf, "rb");
		}
	#endif
	 * /
		if (f != null) {
			md5_state_t filemd5state;
			md5_byte_t buffer[1024];
			md5_byte_t digest[16];
			size_t len;

			md5_init(&filemd5state);
			while ((len = fread(buffer, 1, sizeof(buffer), f)) != 0)
				md5_append(&filemd5state, buffer, len);

			if (ferror(f) && warn) fprintf(stderr, "Error Reading from %s \n", buf);
			fclose(f);

			md5_finish(&filemd5state, digest);
			return CheckMD5Digest(file, digest, warn);
		} else { // file not found
			return false;
		}
	}
	 */
	/* Checks, if either the Windows files exist (TRG1R.GRF) or the DOS files (TRG1.GRF)
	 * by comparing the MD5 checksums of the files. _use_dos_palette is set accordingly.
	 * If neither are found, Windows palette is assumed.
	 *
	 * (Note: Also checks sample.cat for corruption) * /
	void CheckExternalFiles()
	{
		int i;
		// count of files from this version
		int dos = 0;
		int win = 0;

		for (i = 0; i < 2; i++) if (FileMD5(files_dos.basic[i], true)) dos++;
		for (i = 0; i < 3; i++) if (FileMD5(files_dos.landscape[i], true)) dos++;

		for (i = 0; i < 2; i++) if (FileMD5(files_win.basic[i], true)) win++;
		for (i = 0; i < 3; i++) if (FileMD5(files_win.landscape[i], true)) win++;

		if (!FileMD5(sample_cat_win, false) && !FileMD5(sample_cat_dos, false))
			fprintf(stderr, "Your sample.cat file is corrupted or missing!\n");

		/*
	 * forced DOS palette via command line . leave it that way
	 * all Windows files present . Windows palette
	 * all DOS files present . DOS palette
	 * no Windows files present and any DOS file present . DOS palette
	 * otherwise . Windows palette
	 * /
		if (_use_dos_palette) {
			return;
		} else if (win == 5) {
			_use_dos_palette = false;
		} else if (dos == 5 || (win == 0 && dos > 0)) {
			_use_dos_palette = true;
		} else {
			_use_dos_palette = false;
		}
	}
	 */

	//static final SpriteID trg1idx[] = {
	static final int trg1idx[] = {
			0,    1, // Mouse cursor, ZZZ
			/* Medium font */
			2,   92, // ' ' till 'z'
			SKIP,   36,
			160,  160, // Move � to the correct position
			98,   98, // Up arrow
			131,  133,
			SKIP,    1, // skip currency sign
			135,  135,
			SKIP,    1,
			137,  137,
			SKIP,    1,
			139,  139,
			140,  140, // TODO Down arrow
			141,  141,
			142,  142, // TODO Check mark
			143,  143, // TODO Cross
			144,  144,
			145,  145, // TODO Right arrow
			146,  149,
			118,  122, // Transport markers
			SKIP,    2,
			157,  157,
			114,  115, // Small up/down arrows
			SKIP,    1,
			161,  225,
			/* Small font */
			226,  316, // ' ' till 'z'
			SKIP,   36,
			384,  384, // Move � to the correct position
			322,  322, // Up arrow
			355,  357,
			SKIP,    1, // skip currency sign
			359,  359,
			SKIP,    1,
			361,  361,
			SKIP,    1,
			363,  363,
			364,  364, // TODO Down arrow
			365,  366,
			SKIP,    1,
			368,  368,
			369,  369, // TODO Right arrow
			370,  373,
			SKIP,    7,
			381,  381,
			SKIP,    3,
			385,  449,
			/* Big font */
			450,  540, // ' ' till 'z'
			SKIP,   36,
			608,  608, // Move � to the correct position
			SKIP,    1,
			579,  581,
			SKIP,    1,
			583,  583,
			SKIP,    5,
			589,  589,
			SKIP,   15,
			605,  605,
			SKIP,    3,
			609,  625,
			SKIP,    1,
			627,  632,
			SKIP,    1,
			634,  639,
			SKIP,    1,
			641,  657,
			SKIP,    1,
			659,  664,
			SKIP,    2,
			667,  671,
			SKIP,    1,
			673,  673,
			/* Graphics */
			674, 4792,
			END
	};

	/* NOTE: When adding a normal Sprites, increase OPENTTD_SPRITES_COUNT with the
	 * amount of sprites and add them to the end of the list, with the index of
	 * the old Sprites-count offset from SPR_OPENTTD_BASE. With this there is no
	 * correspondence of any kind with the ID's in the grf file, but results in
	 * a maximum use of Sprites slots. */
	public static final int OPENTTD_SPRITES_COUNT = 95;

	static final int _openttd_grf_indexes[] = {
			Sprites.SPR_IMG_AUTORAIL, Sprites.SPR_CURSOR_WAYPOINT, // icons etc
			134, 134,  // euro symbol medium size
			582, 582,  // euro symbol large size
			358, 358,  // euro symbol tiny
			Sprites.SPR_CURSOR_CANAL, Sprites.SPR_IMG_FASTFORWARD, // more icons
			648, 648, // nordic char: �
			616, 616, // nordic char: �
			666, 666, // nordic char: �
			634, 634, // nordic char: �
			Sprites.SPR_PIN_UP, Sprites.SPR_CURSOR_CLONE, // more icons
			382, 383, // � � tiny
			158, 159, // � � medium
			606, 607, // � � large
			360, 360, // � tiny
			362, 362, // � tiny
			136, 136, // � medium
			138, 138, // � medium
			584, 584, // � large
			586, 586, // � large
			626, 626, // � large
			658, 658, // � large
			374, 374, // � tiny
			378, 378, // � tiny
			150, 150, // � medium
			154, 154, // � medium
			598, 598, // � large
			602, 602, // � large
			640, 640, // � large
			672, 672, // � large
			380, 380, // � tiny
			156, 156, // � medium
			604, 604, // � large
			317, 320, // { | } ~ tiny
			93,  96, // { | } ~ medium
			541, 544, // { | } ~ large
			Sprites.SPR_HOUSE_ICON, Sprites.SPR_HOUSE_ICON,
			END
	};

	// TODO return me private static byte _sprite_page_to_load = (byte) 0xFF;
	private static final byte _sprite_page_to_load = 2; // desert

	//private static boolean _use_dos_palette = false;


	private static void LoadSpriteTables()
	{
		//final FileList files = _use_dos_palette ? files_dos : files_win;
		//final FileList files = files_win;
		final String[] files = files_win;
		int load_index;
		int i;

		//LoadGrfIndexed(files.basic[0].filename, trg1idx, 0);
		LoadGrfIndexed(files[0], trg1idx, 0);
		SpriteCache.DupSprite(  2, 130); // non-breaking space medium
		SpriteCache.DupSprite(226, 354); // non-breaking space tiny
		SpriteCache.DupSprite(450, 578); // non-breaking space large
		load_index = 4793;

		// TODO why start from 1?
		
		//for (i = 1; files.basic[i].filename != null; i++) {
		for (i = 1; files[i] != null; i++) {
			load_index += LoadGrfFile(files[i], load_index, i);
		}

		if (_sprite_page_to_load != 0) {
			LoadGrfIndexed(
					files_landscape[_sprite_page_to_load - 1],
					//files.landscape[_sprite_page_to_load - 1].filename,
					_landscape_spriteindexes[_sprite_page_to_load - 1],
					i++
					);
		}

		assert(load_index == Sprites.SPR_CANALS_BASE);
		load_index += LoadGrfFile("canalsw.grf", load_index, i++);

		assert(load_index == Sprites.SPR_SLOPES_BASE);
		// TODO LoadGrfIndexed("trkfoundw.grf", _slopes_spriteindexes[_opt.landscape], i++);
		LoadGrfIndexed("trkfoundw.grf", _slopes_spriteindexes[_sprite_page_to_load], i++);

		load_index = Sprites.SPR_AUTORAIL_BASE;
		load_index += LoadGrfFile("autorail.grf", load_index, i++);

		assert(load_index == Sprites.SPR_OPENTTD_BASE);
		LoadGrfIndexed("openttd.grf", _openttd_grf_indexes, i++);
		load_index = Sprites.SPR_OPENTTD_BASE + OPENTTD_SPRITES_COUNT;

		// [dz] wrong place, but it was in LoadNewGRF for some reason. 
		
		//memcpy(&_engine_info, &orig_engine_info, sizeof(orig_engine_info));
		//memcpy(&_rail_vehicle_info, &orig_rail_vehicle_info, sizeof(orig_rail_vehicle_info));
		//memcpy(&_ship_vehicle_info, &orig_ship_vehicle_info, sizeof(orig_ship_vehicle_info));
		//memcpy(&_aircraft_vehicle_info, &orig_aircraft_vehicle_info, sizeof(orig_aircraft_vehicle_info));
		//memcpy(&_road_vehicle_info, &orig_road_vehicle_info, sizeof(orig_road_vehicle_info));

		// TODO make deep copy??
		
		//Global._engine_info =  EngineTables2.orig_engine_info;
		
		//for( EngineInfo ei : EngineTables2.orig_engine_info )
		
		System.arraycopy(
				EngineTables2.orig_engine_info, 0,
				Global._engine_info, 0, Global._engine_info.length );

		System.arraycopy(
				EngineTables2.orig_rail_vehicle_info , 0, 
				Global._rail_vehicle_info, 0, Global._rail_vehicle_info.length );

		System.arraycopy(
				EngineTables2.orig_ship_vehicle_info, 0, 
				Global._ship_vehicle_info, 0, Global._ship_vehicle_info.length );

		System.arraycopy(
				EngineTables2.orig_aircraft_vehicle_info, 0, 
				Global._aircraft_vehicle_info, 0, Global._aircraft_vehicle_info.length );

		System.arraycopy(
				EngineTables2.orig_road_vehicle_info, 0, 
				Global._road_vehicle_info, 0, Global._road_vehicle_info.length );

		Bridge.loadOrigBridges();
		
		// Unload sprite group data
		Engine.UnloadWagonOverrides();
		Engine.UnloadCustomEngineSprites();
		Engine.UnloadCustomEngineNames();

		// Reset price base data
		Economy.ResetPriceBaseMultipliers();

		// TODO was called from LoadNewGRF 
		GRFFile.ResetNewGRFData();
		GRFFile.LoadNewGRF(load_index, i);
		
	}


	public static void GfxLoadSprites()
	{
		// Need to reload the sprites only if the landscape changed
		// TODO if (_sprite_page_to_load != _opt.landscape) {
		//	_sprite_page_to_load = _opt.landscape;

			// Sprites cache
			Global.DEBUG_spritecache( 1,"Loading Sprite set %d.", _sprite_page_to_load);

			SpriteCache.GfxInitSpriteMem();
			LoadSpriteTables();
			Gfx.GfxInitPalettes();
		//}
	}

}


/*
class MD5File {
	final String filename;     // filename
	final byte hash[]; // md5 sum of the file

	public MD5File(String fn, byte [] hash ) {
		// TODO Auto-generated constructor stub
	}

}

class FileList {
	final MD5File basic[5];     // grf files that always have to be loaded
	final MD5File landscape[3]; // landscape specific grf files
}

enum {
	SKIP = 0xFFFE,
			END  = 0xFFFF
};
 */
