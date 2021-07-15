package game.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;

import game.Global;

/*************************************************/
/* FILE IO ROUTINES ******************************/
/*************************************************/

public class FileIO {

	private static final int FIO_BUFFER_SIZE = 512;

	public static final int SEEK_SET = 0; // TODO check
	public static final int SEEK_CUR = 1;
	
	//byte *buffer, *buffer_end;
	long pos;
	//FILE *cur_fh;
	//FILE *handles[32];
	//byte buffer_start[512];

	 //FileInputStream fis = new FileInputStream(raf.getFD());
	 //BufferedInputStream bis = new BufferedInputStream(fis);

	BufferedInputStream cur_fh;
	BufferedInputStream handles[] = new BufferedInputStream[32];
	
	
	FileIO _fio = this;

	// Get current position in file
	long FioGetPos()
	{
		//return _fio.pos + (_fio.buffer - _fio.buffer_start) - FIO_BUFFER_SIZE;
		return _fio.pos;// + (_fio.buffer - _fio.buffer_start) - FIO_BUFFER_SIZE;
	}

	void FioSeekTo(long pos, int mode)
	{
		if (mode == SEEK_CUR) pos += FioGetPos();
		//_fio.buffer = _fio.buffer_end = _fio.buffer_start + FIO_BUFFER_SIZE;
		fseek(_fio.cur_fh, (_fio.pos=pos), SEEK_SET);
	}

	// Seek to a file and a position
	void FioSeekToFile(long pos)
	{
		assert pos > 0;
		BufferedInputStream f = _fio.handles[(int) (pos >> 24)];
		assert(f != null);
		_fio.cur_fh = f;
		FioSeekTo(pos & 0xFFFFFF, SEEK_SET);
	}

	byte FioReadByte()
	{
		/*
		if (_fio.buffer == _fio.buffer_end) {
			_fio.pos += FIO_BUFFER_SIZE;
			fread(_fio.buffer = _fio.buffer_start, 1, FIO_BUFFER_SIZE, _fio.cur_fh);
		}
		return *_fio.buffer++;
		*/
		
		int d = cur_fh.read();
		assert( d >= 0 );
		
		return (byte) d;
	}

	void FioSkipBytes(int n)
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


	int FioReadWord()
	{
		byte b = FioReadByte();
		return (FioReadByte() << 8) | b;
	}

	int FioReadDword()
	{
		int b = 0xFFFF & FioReadWord();
		return (FioReadWord() << 16) | b;
	}

	byte[] FioReadBlock(int size)
	{
		FioSeekTo(FioGetPos(), SEEK_SET);
		_fio.pos += size;
		//fread(ptr, 1, size, _fio.cur_fh);
		return cur_fh.readNBytes(size);
	}

	private void FioCloseFile(int slot)
	{
		if (_fio.handles[slot] != null) {
			//fclose(_fio.handles[slot]);
			_fio.handles[slot].close();
			_fio.handles[slot] = null;
		}
	}

	void FioCloseAll()
	{
		int i;

		for (i = 0; i != _fio.handles.length; i++)
			FioCloseFile(i);
	}

	
	boolean FiosCheckFileExists(String filename)
	{
		BufferedInputStream f = FioFOpenFile(filename);
		if( null != f ) f.close();
		
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

	BufferedInputStream FioFOpenFile(String filename)
	{
		BufferedInputStream f;
		String buf;

		buf.format( "%s%s", Global._path.data_dir, filename);

		 FileInputStream fis = new FileInputStream(buf);
		 f = new BufferedInputStream(fis);
		 
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

	void FioOpenFile(int slot, String filename)
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

		BufferedInputStream f = FioFOpenFile(filename);
		
		if (f == null)
			error("Cannot open file '%s'", filename);

		FioCloseFile(slot); // if file was opened before, close it
		_fio.handles[slot] = f;
		FioSeekToFile(slot << 24);
	}

}
