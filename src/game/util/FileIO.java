package game.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import game.Global;
import game.Str;
import game.enums.FiosType;
import game.enums.GameModes;
import game.struct.FiosItem;

/*************************************************/
/* FILE IO ROUTINES ******************************/
/*************************************************/

public class FileIO {

	//private static final int FIO_BUFFER_SIZE = 512;

	public static final int SEEK_SET = 0; // TODO check
	public static final int SEEK_CUR = 1;

	//byte *buffer, *buffer_end;
	//static long pos;
	//FILE *cur_fh;
	//FILE *handles[32];
	//byte buffer_start[512];

	//FileInputStream fis = new FileInputStream(raf.getFD());
	//BufferedInputStream bis = new BufferedInputStream(fis);

	static BufferedRandomAccessFile cur_fh;
	static final BufferedRandomAccessFile[] handles = new BufferedRandomAccessFile[32];


	//static FileIO _fio = this;

	// Get current position in file
	public static long FioGetPos()
	{
		//return _fio.pos + (_fio.buffer - _fio.buffer_start) - FIO_BUFFER_SIZE;
		//return pos;// + (_fio.buffer - _fio.buffer_start) - FIO_BUFFER_SIZE;
		return cur_fh.getFilePointer();

	}

	public static void FioSeekTo(long ppos, int mode)
	{
		if (mode == SEEK_CUR) ppos += FioGetPos();
		//_fio.buffer = _fio.buffer_end = _fio.buffer_start + FIO_BUFFER_SIZE;
		//fseek(_fio.cur_fh, (_fio.pos=pos), SEEK_SET);
		try {
			//cur_fh.seek(pos=ppos);
			cur_fh.seek(ppos);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(33);
		}
	}

	// Seek to a file and a position
	public static void FioSeekToFile(long ppos)
	{
		//assert pos > 0;
		BufferedRandomAccessFile f = handles[(int) (ppos >> 24)];
		assert(f != null);
		cur_fh = f;
		FioSeekTo(ppos & 0xFFFFFF, SEEK_SET);
	}

	// Use int to prevent signed conversion to int in caller
	//public static byte FioReadByte()
	public static int FioReadByte()
	{
		/*
		if (_fio.buffer == _fio.buffer_end) {
			_fio.pos += FIO_BUFFER_SIZE;
			fread(_fio.buffer = _fio.buffer_start, 1, FIO_BUFFER_SIZE, _fio.cur_fh);
		}
		return *_fio.buffer++;
		 */

		int d = 0;
		try {
			d = cur_fh.read();
			//pos++;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(33);
		}
		assert( d >= 0 );

		return d & 0xFF;
	}

	public static void FioSkipBytes(int n)
	{
		cur_fh.skip(n);
		//pos += n;
		/*
		for(;;) {
			//int m = min(_fio.buffer_end - _fio.buffer, n);
			//_fio.buffer += m;
			//n -= m;
			if (n <= 0) break;
			FioReadByte(); // TODO faster
			n--;
		}*/
	}


	public static int FioReadWord()
	{
		int b = FioReadByte() & 0xFF;
		return 0xFFFF & ((FioReadByte() << 8) | b);
	}

	public static int FioReadSignedWord()
	{
		int b = FioReadByte() & 0xFF;
		int r = 0xFFFF & ((FioReadByte() << 8) | b);
		if( 0 != (r & 0x8000) ) r |= 0xFFFF0000; // sign extend
		return r;
	}

	public static int FioReadDword()
	{
		int b = 0xFFFF & FioReadWord();
		return (FioReadWord() << 16) | b;
	}

	public static byte[] FioReadBlock(int size)
	{
		FioSeekTo(FioGetPos(), SEEK_SET);
		//pos += size;
		//fread(ptr, 1, size, _fio.cur_fh);
		//return cur_fh.readNBytes(size);

		byte[] buf = new byte[size];
		try {
			cur_fh.read(buf, 0, size);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(33);
		}
		return buf;
	}

	private static void FioCloseFile(int slot)
	{
		if (handles[slot] != null) {
			//fclose(_fio.handles[slot]);
			try {
				handles[slot].close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			handles[slot] = null;
		}
	}

	public static void FioCloseAll()
	{
		int i;

		for (i = 0; i != handles.length; i++)
			FioCloseFile(i);
	}


	public static boolean FiosCheckFileExists(String filename)
	{
		BufferedRandomAccessFile f = FioFOpenFile(filename);
		if( null != f )
			try {
				f.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		return f != null;
	}

	/*boolean FiosCheckFileExists(String filename)
	{
		FILE *f;
		char buf[MAX_PATH];

		sprintf(buf, "%s%s", _path.data_dir, filename);

		f = fopen(buf, "rb");
		#if !defined(WIN32)
		if (f == NULL) {
			String s;
			// Make lower case and try again
			for(s=buf + strlen(_path.data_dir) - 1; *s != 0; s++)
	 *s = tolower(*s);
			f = fopen(buf, "rb");

			#if defined SECOND_DATA_DIR
			// tries in the 2nd data directory
			if (f == NULL) {
				sprintf(buf, "%s%s", _path.second_data_dir, filename);
				for(s=buf + strlen(_path.second_data_dir) - 1; *s != 0; s++)
	 *s = tolower(*s);
				f = fopen(buf, "rb");
			}
			#endif
		}
		#endif

		if (f == NULL)
			return false;
		else {
			fclose(f);
			return true;
		}
	}*/

	public static BufferedRandomAccessFile FioFOpenFile(String filename)
	{
		BufferedRandomAccessFile f;
		String buf;

		buf = String.format( "%s%s", Global._path.data_dir, filename);

		//FileInputStream fis = new FileInputStream(buf);
		//f = new BufferedInputStream(fis);

		try {
			f = new BufferedRandomAccessFile(buf,"r", 10240 );
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		return f;

		/*
		f = fopen(buf, "rb");
		#if !defined(WIN32)
		if (f == NULL) {
			String s;
			// Make lower case and try again
			for(s=buf + strlen(_path.data_dir) - 1; *s != 0; s++)
		 *s = tolower(*s);
			f = fopen(buf, "rb");

			#if defined SECOND_DATA_DIR
			// tries in the 2nd data directory
			if (f == NULL) {
				sprintf(buf, "%s%s", _path.second_data_dir, filename);
				for(s=buf + strlen(_path.second_data_dir) - 1; *s != 0; s++)
		 *s = tolower(*s);
				f = fopen(buf, "rb");
			}
			#endif
		}
		#endif

		return f;
		 */
	}

	public static void FioOpenFile(int slot, String filename)
	{
		/*
		FILE *f;
		char buf[MAX_PATH];

		sprintf(buf, "%s%s", _path.data_dir, filename);

		f = fopen(buf, "rb");
		#if !defined(WIN32)
		if (f == NULL) {
			String s;
			// Make lower case and try again
			for(s=buf + strlen(_path.data_dir) - 1; *s != 0; s++)
		 *s = tolower(*s);
			f = fopen(buf, "rb");

			#if defined SECOND_DATA_DIR
			// tries in the 2nd data directory
			if (f == NULL) {
				sprintf(buf, "%s%s", _path.second_data_dir, filename);
				for(s=buf + strlen(_path.second_data_dir) - 1; *s != 0; s++)
		 *s = tolower(*s);
				f = fopen(buf, "rb");
			}

			if (f == NULL)
				sprintf(buf, "%s%s", _path.data_dir, filename);	//makes it print the primary datadir path instead of the secundary one

			#endif
		}
		#endif
		 */

		BufferedRandomAccessFile f = FioFOpenFile(filename);

		if (f == null)
		{
			Global.error(String.format("Cannot open file '%s'", filename));
			System.exit(33);
		}
		FioCloseFile(slot); // if file was opened before, close it
		handles[slot] = f;
		FioSeekToFile(slot << 24);
	}
















	static String _fios_path;
	static String _fios_save_path;
	static String _fios_scn_path;
	//static FiosItem [] _fios_items;
	static int _fios_count, _fios_alloc;






	private static void loadDirs(List<FiosItem> items, Path dir) 
	{
		// Parent directory, only if not in root already.
		if (_fios_path.length() != 0) {
			FiosItem fios = new FiosItem();
			fios.type = FiosType.PARENT;
			fios.mtime = 0;
			fios.name = "..";
			fios.title = ".. (Parent directory)";
			items.add(fios);
		}
		
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*")) 
		{
			for (Path entry: stream) {
				System.out.println(entry.getFileName());
				File file = entry.toFile();
				//filename = String.format( "%s/%s", _fios_path, dirent.d_name);
				if(file.isDirectory() && file.getName().charAt(0) != '.') 
				{
					FiosItem fios = new FiosItem();
					fios.type = FiosType.DIR;
					fios.mtime = 0;
					fios.name = file.getName();
					fios.title = String.format(	"%s/ (Directory)", file.getName() );
					items.add(fios);
				}
	
			}
		} catch (IOException x) {
			System.err.println(x); // TODO err print
		}
	}

	
	
	
	// Get a list of savegames
	public static List<FiosItem> FiosGetSavegameList(int mode)
	{
		List<FiosItem> items = new ArrayList<>();
		
		if (_fios_save_path == null)
		_fios_save_path = Global._path.save_dir;
		
		_fios_path = _fios_save_path;

		Path dir = new File(_fios_path).toPath();

		// Show subdirectories first
		loadDirs(items, dir);


		/*{
			// XXX ugly global variables ... 
			byte order = _savegame_sort_order;
			_savegame_sort_order = SORT_BY_NAME | SORT_ASCENDING;
			qsort(_fios_items, _fios_count, sizeof(FiosItem), compare_FiosItems);
			_savegame_sort_order = order;
		}*/

		// this is where to start sorting
		//sort_start = _fios_count;

		/* Show savegame files
		 * .SAV OpenTTD saved game
		 */

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.{sav,scn}")) 
		{
			for (Path entry: stream) {
				System.out.println(entry.getFileName());
				File file= entry.getFileName().toFile();
				//filename = String.format( "%s/%s", _fios_path, dirent.d_name);
				final String name = file.getName();

				if(file.isDirectory() || name.charAt(0) == '.')
					continue;

				FiosItem fios = new FiosItem();
				fios.type = FiosType.FILE;
				fios.mtime = file.lastModified();
				fios.name = name;
				fios.title = String.format(	"%s", name );
				items.add(fios);

				// TODO GetOldSaveGameName(fios.title, filename);

			}
		} catch (IOException x) {
			System.err.println(x); // TODO err print
		}



		//qsort(_fios_items + sort_start, _fios_count - sort_start, sizeof(FiosItem), compare_FiosItems);
		//*num = _fios_count;
		return items;
	}

	// Get a list of scenarios
	public static List<FiosItem> FiosGetScenarioList(int mode)
	{
		List<FiosItem> items = new ArrayList<>();
		
		if (_fios_scn_path == null) 
			_fios_scn_path = Global._path.scenario_dir;		

		_fios_path = _fios_scn_path;

		

		// Show subdirectories first
		Path dir = new File(_fios_path).toPath();

		loadDirs(items, dir);

		/*{
			//* XXX ugly global variables ... 
			byte order = _savegame_sort_order;
			_savegame_sort_order = SORT_BY_NAME | SORT_ASCENDING;
			qsort(_fios_items, _fios_count, sizeof(FiosItem), compare_FiosItems);
			_savegame_sort_order = order;
		}*/

		// this is where to start sorting
		//sort_start = _fios_count;

		/* Show scenario files
		 * .SCN OpenTTD style scenario file
		 */
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.scn")) 
		{
			for (Path entry: stream) {
				System.out.println(entry.getFileName());
				File file= entry.getFileName().toFile();
				//filename = String.format( "%s/%s", _fios_path, dirent.d_name);
				final String name = file.getName();

				if(file.isDirectory() || name.charAt(0) == '.')
					continue;

				FiosItem fios = new FiosItem();
				fios.type = FiosType.FILE;
				fios.mtime = file.lastModified();
				fios.name = name;
				fios.title = String.format(	"%s", name );
				items.add(fios);

				// TODO GetOldSaveGameName(fios.title, filename);

			}
		} catch (IOException x) {
			System.err.println(x); // TODO err print
		}

		//qsort(_fios_items + sort_start, _fios_count - sort_start, sizeof(FiosItem), compare_FiosItems);
		//*num = _fios_count;
		return items;
	}



	// Browse to
	public static String FiosBrowseTo(final FiosItem item)
	{
		String path = _fios_path;

		switch (item.type) {
		case PARENT:
		{
			int pos = path.lastIndexOf(File.separatorChar);
			if( pos < 0 ) break;
			path = path.substring(0, pos);
		}
			break;
			
		case DIR:			
			path = path + File.separator + item.name;			
			break;

		case DIRECT:
			path = item.name;
			while(path.endsWith(File.separator))
				path = path.substring(0, path.length()-1);
			break;

		case FILE:
		case OLDFILE:
		case SCENARIO:
		case OLD_SCENARIO: {
			return String.format("%s%s%s", path, File.separator, item.name);
		}
		case DRIVE:
		default:
			assert false;
			break;
		}

		if(_fios_path == _fios_scn_path)
			_fios_path = _fios_scn_path = path;
		
		if(_fios_path == _fios_save_path)
			_fios_path = _fios_save_path = path;
				
		return null;
	}

	/**
	 * Get descriptive texts. Returns the path and free space
	 * left on the device
	 * @param path string describing the path
	 * @param tfs total free space in megabytes, optional (can be null)
	 * @return StringID describing the path (free space or failure)
	 */
	public static /*StringID*/ int FiosGetDescText(String [] path, long []tot)
	{
		long free = 0;
		path[0] = _fios_path;

		File f = new File(path[0]);
		free = f.getFreeSpace();
		
		/*{
			struct statvfs s;

			if (statvfs(*path, &s) == 0) {
				free = (uint64)s.f_frsize * s.f_bavail >> 20;
			} else
				return STR_4006_UNABLE_TO_READ_DRIVE;
		}*/
		
		if( free == 0 ) return Str.STR_4006_UNABLE_TO_READ_DRIVE;
		
		if (tot != null) tot[0] = free;
		return Str.STR_4005_BYTES_FREE;
	} 

	public static String FiosMakeSavegameName(String name)
	{
		String extension;

		if (Global._game_mode == GameModes.GM_EDITOR)
			extension = ".scn";
		else
			extension = ".sav";

		// Don't append the extension, if it is already there
		//int period = name.lastIndexOf('.');
		//if (period >= 0 && strcasecmp(period, extension) == 0) extension = "";
		if( name.endsWith(extension) ) extension = ""; 

		return String.format("%s/%s%s", _fios_path, name, extension);
	}

	public static boolean FiosDelete(String name)
	{
		File f = new File(String.format("%s/%s", _fios_path, name));		
		return f.delete();
	}

	public static boolean FileExists(String filename)
	{
		File f = new File(filename);		
		return f.canRead();
	}



}

