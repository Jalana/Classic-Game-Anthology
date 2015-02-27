package tru.kyle.databases;

/*
This file (DBManager) is a part of the Classic Game Anthology application.
Copyright (C) <2015>  <Connor Kyle>

The Classic Game Anthology is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

The Classic Game Anthology is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with the Classic Game Anthology.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.util.Arrays;

import tru.kyle.classicgameanthology.FileSaver;
import tru.kyle.classicgameanthology.FileSaver.Game;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

//All methods within this class are static; as such, no DBManager object should ever be instantiated.
//General note: Whenever a function calls for a Game as a parameter, 
//		passing a null value indicates that the function should work with the player data instead of a specific game.
public class DBManager 
{
	private static final String LOGTAG = "DBManager";
	
	private static DBHelper dbHelper;
	private static SQLiteDatabase database;
	//private static String dbFilename = "Database.db";
	
	protected static final String GRID_ITEM_SEPARATOR = ",";
	protected static final String GRID_ROW_SEPARATOR = "\n";
	private static final String DB_SUFFIX = "Database.db";
	private static final String PLAYER_TABLE = "Players";
	protected static String createTableStatement;
	protected static String dropTableStatement;
	protected static final String BASE_CREATE_STATEMENT = "CREATE TABLE IF NOT EXISTS ";
	protected static final String BASE_DROP_STATEMENT = "DROP TABLE IF EXISTS ";
	
	protected static final String[] DATABASE_NAMES = {PLAYER_TABLE + DB_SUFFIX,
		FileSaver.Game.ConnectFour.toString() + DB_SUFFIX,
		FileSaver.Game.Pente.toString() + DB_SUFFIX,
		FileSaver.Game.Reversi.toString() + DB_SUFFIX,
		FileSaver.Game.Mastermind.toString() + DB_SUFFIX
	};
    
	protected static final String CREATE_PLAYERS_TABLE = 
    	BASE_CREATE_STATEMENT + PLAYER_TABLE + "(" +
			//" _id INTEGER primary key autoincrement, " +
			DBInterface.PLAYER_NAME_KEY + " TEXT primary key not null, " +
			DBInterface.PLAYER_WINS_KEY + " TEXT not null, " +
			DBInterface.PLAYER_MATCHES_KEY + " TEXT not null" +
			")";
	
    protected static final String CREATE_CONNECT_FOUR_TABLE = 
    	BASE_CREATE_STATEMENT + FileSaver.Game.ConnectFour.toString() + "(" +
    		//" _id INTEGER primary key autoincrement, " +
    		DBInterface.GAME_NAME_KEY + " TEXT primary key not null, " +
    		DBInterface.GRID_WIDTH_KEY + " INTEGER not null, " +
    		DBInterface.GRID_HEIGHT_KEY + " INTEGER not null, " +
    		DBInterface.CURRENT_PLAYER_KEY + " INTEGER not null, " +
    		DBInterface.TURN_COUNT_KEY + " INTEGER not null, " +
    		DBInterface.PLAYER_BASE_KEY + "1 TEXT not null, " +
    		DBInterface.PLAYER_BASE_KEY + "2 TEXT not null, " +
    		DBInterface.GRID_VALUES_KEY + " TEXT not null, " +
    		DBInterface.EXTRA_BOOL_BASE_KEY + "1 INTEGER not null" +
    		")";
    
    protected static final String CREATE_PENTE_TABLE = 
    	BASE_CREATE_STATEMENT + FileSaver.Game.Pente.toString() + "(" + 
    		//" _id INTEGER primary key autoincrement, " +
    		DBInterface.GAME_NAME_KEY + " TEXT primary key not null, " +
    		DBInterface.GRID_WIDTH_KEY + " INTEGER not null, " +
    		DBInterface.GRID_HEIGHT_KEY + " INTEGER not null, " +
    		DBInterface.CURRENT_PLAYER_KEY + " INTEGER not null, " +
    		DBInterface.TURN_COUNT_KEY + " INTEGER not null, " +
    		DBInterface.CAPTURES_BASE_KEY + "1 INTEGER not null, " + 
    		DBInterface.CAPTURES_BASE_KEY + "2 INTEGER not null, " +
    		DBInterface.PLAYER_BASE_KEY + "1 TEXT not null, " +
    		DBInterface.PLAYER_BASE_KEY + "2 TEXT not null, " +
    		DBInterface.GRID_VALUES_KEY + " TEXT not null, " +
    		DBInterface.EXTRA_STRING_BASE_KEY + "1 TEXT not null, " +
    		DBInterface.EXTRA_BOOL_BASE_KEY + "1 INTEGER not null" +
    		")";
    
    protected static final String CREATE_REVERSI_TABLE = 
    	BASE_CREATE_STATEMENT + FileSaver.Game.Reversi.toString() + "(" + 
    		//" _id INTEGER primary key autoincrement, " +
    		DBInterface.GAME_NAME_KEY + " TEXT primary key not null, " +
    		DBInterface.GRID_WIDTH_KEY + " INTEGER not null, " +
    		DBInterface.GRID_HEIGHT_KEY + " INTEGER not null, " +
    		DBInterface.CURRENT_PLAYER_KEY + " INTEGER not null, " +
    		DBInterface.TURN_COUNT_KEY + " INTEGER not null, " +
    		DBInterface.PLAYER_BASE_KEY + "1 TEXT not null, " +
    		DBInterface.PLAYER_BASE_KEY + "2 TEXT not null, " +
    		DBInterface.GRID_VALUES_KEY + " TEXT not null, " +
    		DBInterface.EXTRA_BOOL_BASE_KEY + "1 INTEGER not null" +
    		")";
    
    protected static final String CREATE_MASTERMIND_TABLE = 
        	BASE_CREATE_STATEMENT + FileSaver.Game.Mastermind.toString() + "(" + 
        		//" _id INTEGER primary key autoincrement, " +
        		DBInterface.GAME_NAME_KEY + " TEXT primary key not null, " +
        		DBInterface.GRID_WIDTH_KEY + " INTEGER not null, " +
        		DBInterface.GRID_HEIGHT_KEY + " INTEGER not null, " +
        		DBInterface.REMAINING_TURNS_KEY + " INTEGER not null, " +
        		DBInterface.PLAYER_BASE_KEY + "1 TEXT not null, " +
        		DBInterface.GRID_VALUES_KEY + " TEXT not null, " +
        		DBInterface.MARKER_VALUES_KEY + " TEXT not null, " +
        		DBInterface.CODE_VALUES_KEY + " TEXT not null, " +
        		DBInterface.COLOR_COUNT_KEY + " INTEGER not null, " +
        		DBInterface.EXTRA_STRING_BASE_KEY + "1 TEXT not null, " +
        		DBInterface.EXTRA_STRING_BASE_KEY + "2 TEXT not null, " +
        		DBInterface.EXTRA_STRING_BASE_KEY + "3 TEXT not null, " +
        		DBInterface.EXTRA_BOOL_BASE_KEY + "1 INTEGER not null, " +
        		DBInterface.EXTRA_BOOL_BASE_KEY + "2 INTEGER not null" +
        		")";
    
    protected static final String[] CREATE_GAME_TABLE_STATEMENTS = {CREATE_PLAYERS_TABLE,
    		CREATE_CONNECT_FOUR_TABLE,
    		CREATE_PENTE_TABLE,
    		CREATE_REVERSI_TABLE,
    		CREATE_MASTERMIND_TABLE
    		};
    
    //DBManager is not a class that should be instantiated, so the constructor is private.
    private DBManager()
    {
    	
    }
	
    //If an SQLException is thrown, it means that another game or player shares the same name.
    //		A false return means that this occurred.
	public static boolean insertSave(Context context, ContentValues values, Game game, boolean overwrite)
	{
		//If game == null, then a new player is being added.
		try
		{
			if (game == null)
			{
				return insertPlayer(context, values.getAsString(DBInterface.PLAYER_NAME_KEY), overwrite);
			}
			else
			{
				String gameName = values.getAsString(DBInterface.GAME_NAME_KEY);
				if (DBManager.checkNameConflicts(context, game, gameName) == true)
				{
					if (overwrite == true)
					{
						deleteSave(context, gameName, game);
					}
					else
					{
						throw new SQLException("Error: Cannot use the same name twice.");
					}
				}
				
				openDB(context, game);
				database.insertOrThrow(game.toString(), null, values);
			}
			closeDB();
			return true;
		}
		catch (SQLException e)
		{
			closeDB();
			return false;
		}
	}
	
	public static boolean insertPlayer(Context context, String newName, boolean overwrite)
	{
		try
		{
			if (DBManager.checkNameConflicts(context, null, newName) == true)
			{
				if (overwrite == true)
				{
					deleteSave(context, newName, null);
				}
				else
				{
					throw new SQLException("Error: Cannot use the same name twice.");
				}
			}
			
			openDB(context, null);
			String temp = "";
			int limit = FileSaver.Game.values().length;
			for (int count = 1; count < limit; count++)
			{
				temp += "0";
				temp += DBManager.GRID_ITEM_SEPARATOR;
			}
			temp += "0";
			
			ContentValues values = new ContentValues();
			values.put(DBInterface.PLAYER_NAME_KEY, newName);
			values.put(DBInterface.PLAYER_WINS_KEY, temp);
			values.put(DBInterface.PLAYER_MATCHES_KEY, temp);
			database.insertOrThrow(PLAYER_TABLE, null, values);
			closeDB();
			return true;
		}
		catch (SQLException e)
		{
			closeDB();
			return false;
		}
	}
	
	public static boolean updatePlayerScores(Context context, String[] players, Game game, String[] winners)
	{
		openDB(context, null);
		int[] wins;
		int[] matches;
		
		String table = PLAYER_TABLE;
		String[] columns = null;
		String selection = DBInterface.PLAYER_NAME_KEY + " IN (?";
		for(int i = 1 ; i < players.length; i++)
		{
		    selection += ", ?";
		}
		selection += ")";
		String[] selectionArgs = players;
		String groupBy = null;
		String having = null;
		String orderBy = null; 
		Cursor cursor = database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
		cursor.moveToFirst();
		int limit = cursor.getCount();
		if (limit != players.length)
		{
			Log.d(DBManager.LOGTAG, "Error: Number of rows returned != number of players provided in updatePlayerScores.");
		}
		
		ContentValues values;
		int nameColumn = cursor.getColumnIndex(DBInterface.PLAYER_NAME_KEY);
		int winsColumn = cursor.getColumnIndex(DBInterface.PLAYER_WINS_KEY);
		int matchesColumn = cursor.getColumnIndex(DBInterface.PLAYER_MATCHES_KEY);
		Log.d(DBManager.LOGTAG, nameColumn + ", " + winsColumn + ", " + matchesColumn);
		for (int count = 0; count < limit; count++)
		{
			values = new ContentValues();
			matches = DBInterface.stringToArray(cursor.getString(matchesColumn), FileSaver.Game.values().length);
			matches[game.ordinal()] += 1;
			if (winners != null && Arrays.asList(winners).contains(cursor.getString(nameColumn)))
			{
				wins = DBInterface.stringToArray(cursor.getString(winsColumn), FileSaver.Game.values().length);
				wins[game.ordinal()] += 1;
				values.put(DBInterface.PLAYER_WINS_KEY, DBInterface.arrayToString(wins));
			}
			values.put(DBInterface.PLAYER_MATCHES_KEY, DBInterface.arrayToString(matches));
			database.update(PLAYER_TABLE, values, DBInterface.PLAYER_NAME_KEY + " = ?", 
					new String[]{cursor.getString(nameColumn)});
			cursor.moveToNext();
		}
		
		closeDB();
		return true;
	}
	
	public static boolean clearPlayerScores(Context context, String player)
	{
		//For simplicity, simply drop the appropriate row and reinsert with this name.
		//		Since a player consists of their name and scores only, no other data is lost with the current implementation.
		deleteSave(context, player, null);
		insertPlayer(context, player, true);
		return true;
	}
	
	public static ContentValues retrieveSave(Context context, String gameName, Game game)
	{
		openDB(context, game);
		String table;
		String[] columns = null;
		String selection;
		String[] selectionArgs = {gameName};
		String groupBy = null;
		String having = null;
		String orderBy = null;
		if (game == null)
		{
			table = PLAYER_TABLE;
			selection = DBInterface.PLAYER_NAME_KEY + " = ?";
		}
		else
		{
			table = game.toString();
			selection = DBInterface.GAME_NAME_KEY + " = ?";
		}
		
		Cursor cursor = database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
		cursor.moveToFirst();
		columns = cursor.getColumnNames();
		ContentValues results = new ContentValues();
		
		for (int count = 0; count < columns.length; count++)
		{
			switch (cursor.getType(count))
			{
				case Cursor.FIELD_TYPE_INTEGER:
				{
					results.put(columns[count], cursor.getInt(count));
					break;
				}
				case Cursor.FIELD_TYPE_FLOAT:
				{
					results.put(columns[count], cursor.getFloat(count));
					break;
				}
				case Cursor.FIELD_TYPE_STRING:
				{
					results.put(columns[count], cursor.getString(count));
					break;
				}
			}
		}
		
		cursor.close();
		closeDB();
		return results;
	}
	
	public static boolean deleteSave(Context context, String gameName, Game game)
	{
		openDB(context, game);
		int rowCount = 0;
		if (game == null)
		{
			rowCount = database.delete(PLAYER_TABLE, DBInterface.PLAYER_NAME_KEY + " = ?", new String[]{gameName});
		}
		else
		{
			//Get the table and drop the appropriate row.
			rowCount = database.delete(game.toString(), DBInterface.GAME_NAME_KEY + " = ?", new String[]{gameName});
		}
		closeDB();
		if (rowCount > 0)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	private static void openDB(Context context, Game game)
	{
		closeDB();
		dbHelper = new DBHelper(context, game);
		database = dbHelper.getWritableDatabase();
	}
	
	private static void closeDB()
	{
		if (dbHelper != null)
		{
			dbHelper.close();
		}
		if (database != null)
		{
			database.close();
		}
	}
	
	protected static String[] getNames(Context context, Game game)
	{
		openDB(context, game);
		String table;
		String[] columns = new String[1];
		String selection = null;
		String[] selectionArgs = null;
		String groupBy = null;
		String having = null;
		String orderBy = null;
		
		if (game == null)
		{
			table = PLAYER_TABLE;
			columns[0] = DBInterface.convertToFullKey(DBInterface.PLAYER_NAME_KEY);
		}
		else
		{
			table = game.toString();
			columns[0] = DBInterface.convertToFullKey(DBInterface.GAME_NAME_KEY);
		}
		Cursor cursor = database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
		cursor.moveToFirst();
		
		String[] results = new String[cursor.getCount()];
		int columnIndex = cursor.getColumnIndex(columns[0]);
		for (int count = 0; count < results.length; count++)
		{
			results[count] = cursor.getString(columnIndex);
			cursor.moveToNext();
			Log.d(LOGTAG, results[count]);
		}
		closeDB();
		return results;
	}
	
	private static boolean checkNameConflicts(Context context, Game game, String name)
	{
		String[] existingNames = DBManager.getNames(context, game);
		for (int count = 0; count < existingNames.length; count++)
		{
			if (existingNames[count].equalsIgnoreCase(name))
			{
				return true;
			}
		}
		return false;
	}
	
	
	
	protected static class DBHelper extends SQLiteOpenHelper
	{
		private static final String _LOGTAG = "DBHelper";
		private static final int _DATABASE_VERSION = 1;
		private static final String[] DEFAULT_PLAYER_NAMES = {"Joe", "Jane", "Bob", "Sarah"};
		private String _createTableStatement;
		private String _dropTableStatement;
		private String _dbName;
		
		protected DBHelper(Context context, Game game) 
		{
			super(context, 
					(game == null ? DATABASE_NAMES[0] : DATABASE_NAMES[game.ordinal() + 1]),
					null, _DATABASE_VERSION);
			if (game == null)
			{
				_createTableStatement = CREATE_PLAYERS_TABLE;
				_dbName = DATABASE_NAMES[0];
				_dropTableStatement = BASE_DROP_STATEMENT + PLAYER_TABLE;
			}
			else
			{
				_createTableStatement = CREATE_GAME_TABLE_STATEMENTS[game.ordinal() + 1];
				_dbName = DATABASE_NAMES[game.ordinal() + 1];
				_dropTableStatement = BASE_DROP_STATEMENT + game.toString();
			}
		}

		@Override
		public void onCreate(SQLiteDatabase _db) 
		{
			// TODO Auto-generated method stub
			_db.execSQL(_createTableStatement);
			if (_createTableStatement == CREATE_PLAYERS_TABLE)
			{
				addDefaultPlayers(_db);
			}
		}
		
		private void addDefaultPlayers(SQLiteDatabase _db)
		{
			ContentValues values = new ContentValues();
			String zeroScores = "";
			int limit = FileSaver.Game.values().length;
			for (int count = 1; count < limit; count++)
			{
				zeroScores += "0";
				zeroScores += DBManager.GRID_ITEM_SEPARATOR;
			}
			zeroScores += "0";
			
			for (int count = 0; count < DBHelper.DEFAULT_PLAYER_NAMES.length; count++)
			{
				values.clear();
				values.put(DBInterface.PLAYER_NAME_KEY, DEFAULT_PLAYER_NAMES[count]);
				values.put(DBInterface.PLAYER_WINS_KEY, zeroScores);
				values.put(DBInterface.PLAYER_MATCHES_KEY, zeroScores);
				_db.insertOrThrow(PLAYER_TABLE, null, values);
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) 
		{
			//Note that this code destroys the existing database upon an upgrade.
			try 
			{
				_db.execSQL(_dropTableStatement);
				onCreate(_db);
				Log.i(_LOGTAG, "onUpgrade: Table has been updated");
			} 
			catch (SQLException e) 
			{
				e.printStackTrace();
				Log.i(_LOGTAG, "ERROR # 2");
			}
		}
	}
	

}
