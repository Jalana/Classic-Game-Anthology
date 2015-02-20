package tru.kyle.databases;

import tru.kyle.classicgameanthology.FileSaver;
import tru.kyle.classicgameanthology.FileSaver.Game;
import tru.kyle.databases.DBManager;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

//A separate database may be needed for each game, with one more for players, due to the fact
//		that different games require different sets of parameters.
//	A separate OpenHelper class may be required for each database, due to the difficulty of managing onUpgrade.
//		-Inherit from this base OpenHelper, and use calls to super() to construct?
//		-Maintain this single OpenHelper, and use construction parameters to specify the table?

//Note that integers must be converted into strings, as SQLite does not accept 2D arrays.
//		-Spaces between integers in a row, with an underscore, colon, or line break for row changes?

//Force the various other activities to put their data into a ContentValues object.
//		Define the string keys on a per-game basis within those activities, or define them in the DBManager class?
//			Define them in the DBOpenHelper instead, and use it as an interface with the appropriate DBManager class?
//Define an interface (wrapper) class for the OpenHelper, and use that to connect with both
//		the DBOpenHelper and DBManager classes.
//		Use the interface class to hold any important constants.
//	Use the game name to assemble the (Game)Manager class name, similar to what the MainMenuActivity does.
//

public class DBOpenHelper extends SQLiteOpenHelper
{
	protected static final int DATABASE_VERSION = 1;
	private static final String LOGTAG = "DBOpenHelper";
	
	protected static final String gridItemSeparator = " ";
	protected static final String gridRowSeparator = "\n";
	protected String createTableStatement = "CREATE TABLE IF NOT EXISTS ";
	protected String dropTableStatement = "DROP TABLE IF EXISTS ";
	//If gameInUse == null, the OpenHelper object works with the Players table.
	//Otherwise, it works with the table for the appropriate game.
	private Game gameInUse;
	private String dbFilename = "Database.db";
    
	/*
	public DBOpenHelper(Context Context, String dbName, int dbVersion) 
	{
		super(Context, dbName, null, dbVersion);
	}
	*/
	
    //Note that "game" may be null, which makes the OpenHelper use the Players table instead of a game-specific table.
	protected DBOpenHelper(Context Context, Game game) 
	{
		super(Context, 
				(game == null ? "PlayersDatabase.db" : game.toString() + "Database.db"), 
				null, DATABASE_VERSION);
		gameInUse = game;
		if (gameInUse == null)
		{
			createTableStatement = DBManager.CREATE_PLAYERS_TABLE;
			dbFilename = "PlayersDatabase.db";
			dropTableStatement += "Players";
		}
		else
		{
			createTableStatement = DBManager.CREATE_GAME_TABLE_STATEMENTS[gameInUse.ordinal()];
			dbFilename = game.toString() + "Database.db";
			dropTableStatement += game.toString();
		}
	}
	
	protected Game getCurrentGame()
	{
		return gameInUse;
	}
	
	protected String getDBName()
	{
		return dbFilename;
	}

	@Override
	public void onCreate(SQLiteDatabase db) 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
		//Note that this code destroys the existing database upon an upgrade.
		try 
		{
			//db.execSQL(dropTableStatement);
			//db.execSQL(createTableStatement);
			Log.i(LOGTAG, "onUpgrade: Table has been updated");
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
			Log.i(LOGTAG, "ERROR # 2");
		}
	}
}
