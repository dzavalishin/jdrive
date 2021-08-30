package game;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import game.xui.Window;

/**
 * 
 * Serialize/deserialize game data
 * 
 * @author dz
 *
 */

public class SaveLoad 
{
	public static final int SL_INVALID = -1;
	public static final int SL_LOAD = 0;
	public static final int SL_SAVE = 1;
	//public static final int SL_OLD_LOAD = 2;

	static enum SaveOrLoadResult
	{
	SL_OK, // completed successfully
	SL_ERROR, // error that was caught before internal structures were modified
	SL_REINIT, // error that was caught in the middle of updating game state, need to clear it. (can only happen during load)
	}
	
	
	/**
	 * Main Save or Load function where the high-level saveload functions are
	 * handled. It opens the savegame, selects format and checks versions
	 * @param filename The name of the savegame being created/loaded
	 * @param mode Save or load. Load can also be a TTD(Patch) game. Use SL_LOAD, SL_OLD_LOAD or SL_SAVE
	 * @return Return the results of the action. SL_OK, SL_ERROR or SL_REINIT ("unload" the game)
	 */
	static SaveOrLoadResult SaveOrLoad(String filename, int mode)
	{

		/* An instance of saving is already active, so don't go saving again * /
		if (_ts.saveinprogress && mode == SL_SAVE) {
			// if not an autosave, but a user action, show error message
			if (!_do_autosave) ShowErrorMessage(INVALID_STRING_ID, STR_SAVE_STILL_IN_PROGRESS, 0, 0);
			return SL_OK;
		}
		WaitTillSaved(); */



		/* TODO: General tactic is to first save the game to memory, then use an available writer
		 * to write it to file, either in threaded mode if possible, or single-threaded */
		if (mode == SL_SAVE) { /* SAVE game */
			save(filename);
		} else { /* LOAD game */
			assert mode == SL_LOAD;

			load(filename);


			/* After loading fix up savegame for any internal changes that
			* might've occured since then. If it fails, load back the old game */
			if (!Main.AfterLoadGame()) return SaveOrLoadResult.SL_REINIT;
		}

		return SaveOrLoadResult.SL_OK;
	}
	
	
	
	
	
	
	
	
	
	
	

	public static void save(String filename)
	{
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(filename); //Main._file_to_saveload.name); //"temp.sav");
			ObjectOutputStream oos = new ObjectOutputStream(fos);

			writeAll(oos);

			oos.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//System.err.println(  );
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






	public static void load(String filename)
	{
		FileInputStream fis;
		ObjectInputStream oin = null;

		try 
		{

			//fis = new FileInputStream("temp.sav");
			fis = new FileInputStream(filename); //Main._file_to_saveload.name);
			oin = new ObjectInputStream(fis);

			Window.DeleteAllNonVitalWindows();

			readAll(oin);

			Hal.MarkWholeScreenDirty();

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
		finally
		{
			if(oin != null )
				try {
					oin.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}

		/*
FileInputStream fis = new FileInputStream("settings.xml");
    XMLDecoder decoder = new XMLDecoder(fis);
    UserSettings decodedSettings = (UserSettings) decoder.readObject();
    decoder.close();
    fis.close();
    return decodedSettings;		 */

	}


	// TODO Misc.java has a lot of stuff to save/load

	private static void writeAll(ObjectOutputStream oos) throws IOException 
	{
		Window.BeforeSave();
		
		oos.writeObject(Global.gs);
		oos.writeObject(Global._m);
		Town.saveGame(oos);
		Engine.saveGame(oos);
		Depot.saveGame(oos);
		Order.saveGame(oos);
		Player.saveGame(oos);
		SignStruct.saveGame(oos);
		TextEffect.saveGame(oos);
		Vehicle.saveGame(oos);
		WayPoint.saveGame(oos);
		RoadStop.saveGame(oos);
		Station.saveGame(oos);
	}


	private static void readAll(ObjectInputStream oin) throws IOException, ClassNotFoundException 
	{
		Global.gs = (GameState) oin.readObject();
		Global._m = (Tile[]) oin.readObject();
		Town.loadGame(oin);
		Engine.loadGame(oin);
		Depot.loadGame(oin);
		Order.loadGame(oin);
		Player.loadGame(oin); // fix after load
		SignStruct.loadGame(oin);
		TextEffect.loadGame(oin);
		Vehicle.loadGame(oin);
		WayPoint.loadGame(oin);
		RoadStop.loadGame(oin);
		Station.loadGame(oin);
		
		Window.afterLoad();
	}




}