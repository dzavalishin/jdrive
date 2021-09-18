package game;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import game.exceptions.InvalidFileFormat;

/**
 * New .GRF sprite loader
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
		loadSprites();
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
		
		/* Seek to sprite section of the GRF. */
		spriteOffset = f.readInt();
		dataCompressionFormat = f.read();		
		dataOffset = f.getFilePointer();


		// Continue processing the data section. 
		//FileIO.FioSeekTo(old_pos, FileIO.SEEK_SET);
		
	}

	private void loadSprites() throws IOException 
	{
		f.seek(spriteOffset);

		// Loop over all sprite section entries and store the file
		// offset for each newly encountered ID. 
		int id;
		while ((id = f.readInt()) != 0) {
			Global.DEBUG_grf( 7, "Sprite id %d", id);
			int len = f.readInt();
			int type = f.read();
			
			byte [] spriteData = new byte[len-1];
			f.read(spriteData);
			
			parseSprite( id, type, spriteData );
		}
	}

	private void parseSprite(int id, int type, byte[] spriteData) {
		MultiSprite ms = map.computeIfAbsent(id, MultiSprite::new );
		
		ms.load(type, spriteData);
	}	
	
}




















