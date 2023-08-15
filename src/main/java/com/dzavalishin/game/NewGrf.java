package com.dzavalishin.game;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import com.dzavalishin.exceptions.InvalidFileFormat;
import com.dzavalishin.exceptions.InvalidSpriteFormat;
import com.dzavalishin.util.BitOps;

/**
 * New .GRF sprite loader
 *
 * TODO File parsing is ok, but processing of actions with NewGrfActionProcessor crashes on xussr.grf
 * 
 * @author dz
 *
 */
public class NewGrf {
	private final RandomAccessFile f;

	private int spriteOffset;
	private long dataOffset;
	private int dataCompressionFormat;

	private Map<Integer,MultiSprite> map = new HashMap<>();


	/** Signature of a container version 2 GRF. */
	private static final int _grf_cont_v2_sig[] = { 0 , 0, 'G', 'R', 'F', 0x82, 0x0D, 0x0A, 0x1A, 0x0A};

	public NewGrf(File name) throws IOException, InvalidFileFormat {
		f = new RandomAccessFile(name, "r");
		if(!checkFormat())
			throw new InvalidFileFormat(name);
		loadOffsets();
		//loadSprites();
	}

	public NewGrf(String name) throws IOException, InvalidFileFormat {
		this(new File("resources/",name));
	}

	private int readInt() throws IOException {
		return Integer.reverseBytes(f.readInt());
	}

	/** Read UNSIGNED byte */
	private int readByte() throws IOException {
		return f.read();
	}


	private boolean checkFormat() throws IOException {
		for(int c : _grf_cont_v2_sig )
		{
			if( c != f.read() )
				return false;
		}
		return true;
	}

	/**
	 * Parse the sprite section of GRFs.
	 * @throws IOException 
	 */
	private void loadOffsets() throws IOException 
	{
		spriteOffset = readInt() + _grf_cont_v2_sig.length + 4;
		dataCompressionFormat = readByte();		
		dataOffset = f.getFilePointer();
	}


	void loadSprites() throws IOException, InvalidSpriteFormat 
	{
		// reference is grf.cpp:256
		// Seek to sprite section of the GRF. 
		f.seek(spriteOffset);

		// Loop over all sprite section entries and store the file
		// offset for each newly encountered ID. 
		int id;
		while ((id = readInt()) != 0) {
			Global.DEBUG_grf( 7, "NewGrf Sprite id %d", id);
			int len = readInt();
			int type = readByte();

			//byte [] spriteData = new byte[len-1];
			//f.read(spriteData);
			byte [] spriteData = new byte[len];
			f.read(spriteData, 1, len-1);
			spriteData[0] = (byte) type;

			parseSprite( id, type, spriteData );
		}
	}

	private void parseSprite(int id, int type, byte[] spriteData) throws InvalidSpriteFormat {
		MultiSprite ms = map.computeIfAbsent(id, MultiSprite::new );

		ms.load(type, spriteData);
	}

	private void loadData() throws IOException
	{
		for(int stage = 0; stage < 2; stage++) 
		{
			f.seek(dataOffset);

			int size;
			for(int index = 0; (size = readInt()) != 0; index++) {
				int type = readByte();
				Global.DEBUG_grf( 7, "NewGrf data index %d size %d type %x", index, size, type);

				byte [] data = new byte[size];
				f.read(data);

				parseData( index, type, data, stage );
			}
		}
	}


	private void parseData(int index, int type, byte[] data, int stage) 
	{
		switch(type)
		{
		case 0xFD: // reference
			assert data.length == 4;
			int ref = BitOps.READ_LE_UINT32(data, 0);
			Global.DEBUG_grf( 0, "NewGrf sprite reference index %d ref %d", index, ref);
			// TODO and now what?
			break;

		case 0xFF: // ?
			Global.DEBUG_grf( 0, "NewGrf blob index %d size %d", index, data.length);
			BitOps.hexDump(data);

			// TODO and now what? is it correct? No - must implement stages
			NewGrfActionProcessor proc = new NewGrfActionProcessorNew(index); // index == sprite offset?				

			DataLoader bufp = new DataLoader(data, index); // index == sprite offset?
			byte action = bufp.r(0);
			if(action < 0)
			{
				Global.DEBUG_grf( 0, "NewGrf unknown action %d blob index %d size %d", type, index, data.length);
				break;
			}
			proc.processAction(action,bufp,stage);

			break;

		default: // ?
			Global.DEBUG_grf( 0, "NewGrf unknown type %d blob index %d size %d", type, index, data.length);
			break;

		}

	}

	public void load() throws IOException, InvalidSpriteFormat {
		loadSprites();
		loadData();		
	}	

}




















