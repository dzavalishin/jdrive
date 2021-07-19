package game.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import game.Global;

/*************************************************/
/* FILE IO ROUTINES ******************************/
/*************************************************/

public class FileIO {

	//private static final int FIO_BUFFER_SIZE = 512;

	public static final int SEEK_SET = 0; // TODO check
	public static final int SEEK_CUR = 1;

	//byte *buffer, *buffer_end;
	static long pos;
	//FILE *cur_fh;
	//FILE *handles[32];
	//byte buffer_start[512];

	//FileInputStream fis = new FileInputStream(raf.getFD());
	//BufferedInputStream bis = new BufferedInputStream(fis);

	static BufferedRandomAccessFile cur_fh;
	static BufferedRandomAccessFile handles[];// = new BufferedInputStream[32];


	//static FileIO _fio = this;

	// Get current position in file
	public static long FioGetPos()
	{
		//return _fio.pos + (_fio.buffer - _fio.buffer_start) - FIO_BUFFER_SIZE;
		return pos;// + (_fio.buffer - _fio.buffer_start) - FIO_BUFFER_SIZE;
	}

	public static void FioSeekTo(long ppos, int mode)
	{
		if (mode == SEEK_CUR) pos += FioGetPos();
		//_fio.buffer = _fio.buffer_end = _fio.buffer_start + FIO_BUFFER_SIZE;
		//fseek(_fio.cur_fh, (_fio.pos=pos), SEEK_SET);
		try {
			cur_fh.seek(pos=ppos);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(33);
		}
	}

	// Seek to a file and a position
	public static void FioSeekToFile(long ppos)
	{
		assert pos > 0;
		BufferedRandomAccessFile f = handles[(int) (ppos >> 24)];
		assert(f != null);
		cur_fh = f;
		FioSeekTo(pos & 0xFFFFFF, SEEK_SET);
	}

	public static byte FioReadByte()
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
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(33);
		}
		assert( d >= 0 );

		return (byte) d;
	}

	public static void FioSkipBytes(int n)
	{
		for(;;) {
			//int m = min(_fio.buffer_end - _fio.buffer, n);
			//_fio.buffer += m;
			//n -= m;
			if (n <= 0) break;
			FioReadByte(); // TODO faster
			n--;
		}
	}


	public static int FioReadWord()
	{
		byte b = FioReadByte();
		return (FioReadByte() << 8) | b;
	}

	public static int FioReadDword()
	{
		int b = 0xFFFF & FioReadWord();
		return (FioReadWord() << 16) | b;
	}

	public static byte[] FioReadBlock(int size)
	{
		FioSeekTo(FioGetPos(), SEEK_SET);
		pos += size;
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
			char *s;
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
			//e.printStackTrace();
			return null;
		}

		return f;

		/*
		f = fopen(buf, "rb");
		#if !defined(WIN32)
		if (f == NULL) {
			char *s;
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
			char *s;
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
			Global.error(String.format("Cannot open file '%s'", filename));

		FioCloseFile(slot); // if file was opened before, close it
		handles[slot] = f;
		FioSeekToFile(slot << 24);
	}

}
