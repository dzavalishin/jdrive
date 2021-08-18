package game;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/** 
 * SaveLoad type struct. 
 * Do NOT use this directly but use the SLE_ 
 * macros defined just below! 
 ** /
public class SaveLoad {
		byte cmd;             /// the action to take with the saved/loaded type, All types need different action
		//VarType type;         /// type of the variable to be saved, int
		int type;         /// type of the variable to be saved, int
		int offset;        /// offset of this variable in the struct (max offset is 65536)
		int length;        /// (conditional) length of the variable (eg. arrays) (max array size is 65536 elements)
		int version_from;  /// save/load the variable starting from this savegame version
		int version_to;    /// save/load the variable until this savegame version

} */


/**
 * 
 * Serialize/deserialize game data
 * 
 * @author dz
 *
 */

public class SaveLoad 
{
	
	
	static void save()
	{
		FileOutputStream fos;
		try {
			fos = new FileOutputStream("temp.sav");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			
			oos.writeObject(Global._m);
			oos.writeObject(Town._town_pool);
			
			oos.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*
		 FileOutputStream fos = new FileOutputStream("settings.xml");
		    XMLEncoder encoder = new XMLEncoder(fos);
		    encoder.setExceptionListener(new ExceptionListener() {
		            public void exceptionThrown(Exception e) {
		                System.out.println("Exception! :"+e.toString());
		            }
		    });
		    encoder.writeObject(settings);
		    encoder.close();		
		*/
		
	}

	
	
	static void load()
	{
		FileInputStream fis;
		try 
		{
			
			fis = new FileInputStream("temp.sav");
			ObjectInputStream oin = new ObjectInputStream(fis);
			Global._m = (Tile[]) oin.readObject();
			
			oin.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*
FileInputStream fis = new FileInputStream("settings.xml");
    XMLDecoder decoder = new XMLDecoder(fis);
    UserSettings decodedSettings = (UserSettings) decoder.readObject();
    decoder.close();
    fis.close();
    return decodedSettings;		 */
		
	}
	
	
	
	
}