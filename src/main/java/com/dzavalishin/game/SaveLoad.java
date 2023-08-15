package com.dzavalishin.game;

import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;

import com.dzavalishin.enums.SaveOrLoadResult;
import com.dzavalishin.struct.HighScore;
import com.dzavalishin.xui.Window;

/**
 * 
 * Serialize/deserialize game data
 * 
 * @author dz
 *
 */

public class SaveLoad 
{
	private static final String HISCORE_FILE_NAME = "hiscore.xml";
	public static final int SL_INVALID = -1;
	public static final int SL_LOAD = 0;
	public static final int SL_SAVE = 1;
	//public static final int SL_OLD_LOAD = 2;

	/**
	 * Main Save or Load function where the high-level saveload functions are
	 * handled. It opens the savegame, selects format and checks versions
	 * @param filename The name of the savegame being created/loaded
	 * @param mode Save or load. Load can also be a TTD(Patch) game. Use SL_LOAD, SL_OLD_LOAD or SL_SAVE
	 * @return Return the results of the action. SL_OK, SL_ERROR or SL_REINIT ("unload" the game)
	 */
	public static SaveOrLoadResult SaveOrLoad(String filename, int mode)
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
		ObjectOutputStream oos = null;
		try {
			fos = new FileOutputStream(filename); //Main._file_to_saveload.name); //"temp.sav");
			oos = new ObjectOutputStream(fos);

			writeAll(oos);
			
		} catch (FileNotFoundException e) {

			Global.error(e);
		} catch (IOException e) {

			Global.error(e);
			//System.err.println(  );
		} finally {
			if(oos != null)
				try {
					oos.close();
				} catch (IOException e) {

					Global.error(e);
				}
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
		ObjectInputStream oin = null;

		try( FileInputStream fis = new FileInputStream(filename) ) 
		{
			oin = new ObjectInputStream(fis);

			Window.DeleteAllNonVitalWindows();

			readAll(oin);

			Hal.MarkWholeScreenDirty();

		} catch (FileNotFoundException e) {
			Global.error(e);
		} catch (IOException e) {
			Global.error(e);
		} catch (ClassNotFoundException e) {
			Global.error(e);
		} 
		finally
		{
			if(oin != null )
				try {
					oin.close();
				} catch (IOException e) {

					Global.error(e);
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
		//oos.writeObject(Global._m);
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
		//Global._m = (Tile[]) oin.readObject();
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




	static void SaveToHighScore()
	{
		File fn = new File( Global._path.personal_dir, HISCORE_FILE_NAME );

		FileOutputStream fos;
		try {
			fos = new FileOutputStream(fn);
		} catch (FileNotFoundException e1) {
			Global.error("Can't load hiscore: ", e1.toString());
			return;
		}

		XMLEncoder encoder = new XMLEncoder(fos);
		encoder.setExceptionListener(new ExceptionListener() {
			public void exceptionThrown(Exception e) {
				Global.error(e);
			}
		});
		encoder.writeObject(Global._highscore_table);
		encoder.close();		
	}

	static void LoadFromHighScore()
	{
		// If can't load - clear 
		Global._highscore_table = new HighScore[5][5];

		File fn = new File( Global._path.personal_dir, HISCORE_FILE_NAME );

		FileInputStream fis;
		try {
			fis = new FileInputStream(fn);
			XMLDecoder decoder = new XMLDecoder(fis);
			Object read = decoder.readObject();
			if( read != null ) Global._highscore_table = (HighScore [][]) read;
			decoder.close();
			fis.close();
		} catch (IOException e) {
			Global.error("Can't save hiscore: ", e.toString());
			return;
		}

	}


	static Properties prop = new Properties();

	public static void LoadFromConfig()
	{
		//ini file should look like host=localhost
		try {
			prop.load(new FileInputStream(Global._path.config_file));
		} catch (FileNotFoundException e) {
			// Ignore
		} catch (IOException e) {
			Global.error(e);		
		}
		//String host = prop.getProperty("host");

		loadConfigItems();
	}

	static void loadConfigItems()
	{
		Global._debug_ai_level 			= getIntProperty("debug-ai", 0);
		Global._debug_grf_level			= getIntProperty("debug-grf", 0);
		Global._debug_map_level			= getIntProperty("debug-map", 0);
		Global._debug_misc_level		= getIntProperty("debug-misc", 0);
		Global._debug_ms_level 			= getIntProperty("debug-ms", 0);
		Global._debug_npf_level			= getIntProperty("debug-npf", 0);
		Global._debug_ntp_level			= getIntProperty("debug-ntp", 0);
		Global._debug_pbs_level			= getIntProperty("debug-pbs", 0);
	}











	private static int getIntProperty(String name, int def) {
		String v = prop.getProperty(name);

		if(v == null) return def;
		try {
			return Integer.parseInt(v);
		}
		catch (NumberFormatException e) {
			return def;
		}

	}
}

