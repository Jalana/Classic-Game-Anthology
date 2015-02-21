package tru.kyle.databases;

/*
This file (DBInterface) is a part of the Classic Game Anthology application.
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

import android.content.ContentValues;
import android.content.Context;
import tru.kyle.classicgameanthology.FileSaver.Game;

public class DBInterface 
{
	/* WARNING: Cursors may be case-sensitive. Keys cannot have upper-case letters. */
	
	private final static String LOGTAG = "DBInterface";
	//This is the base key to use for constructing key names.
	private final static String PACKAGE_NAME = "tru.kyle.databases.";
	private final static String ANTHOLOGY_PACKAGE_NAME = "tru.kyle.classicgameanthology.";
	
	//These are keys to be used in storing the data for save games.
	//Any keys ending in an underscore are base keys, requiring number suffixes to indicate player number, etc.
	public static final String GAME_NAME_KEY = "_game_name";
	public static final String GRID_HEIGHT_KEY = "grid_height";
	public static final String GRID_WIDTH_KEY = "grid_width";
	public static final String GRID_VALUES_KEY = "grid_values";
	public static final String MARKER_VALUES_KEY = "marker_values";
	public static final String CODE_VALUES_KEY = "code_values";
	public static final String COLOR_COUNT_KEY = "color_count";
	public static final String PLAYER_BASE_KEY = "player_";
	public static final String CAPTURES_BASE_KEY = "captures_";
	public static final String TURN_COUNT_KEY = "turn_count";
	public static final String REMAINING_TURNS_KEY = "remaining_turns";
	public static final String CURRENT_PLAYER_KEY = "current_player";
	public static final String EXTRA_STRING_BASE_KEY = "extra_string_";
	public static final String EXTRA_BOOL_BASE_KEY = "extra_bool_";
	/*
	protected static final String GAME_NAME_KEY = PACKAGE_NAME + "_gameName";
	protected static final String GRID_HEIGHT_KEY = PACKAGE_NAME + "grid_height";
	protected static final String GRID_WIDTH_KEY = PACKAGE_NAME + "grid_width";
	protected static final String GRID_VALUES_KEY = PACKAGE_NAME + "grid_values";
	protected static final String PLAYER_BASE_KEY = PACKAGE_NAME + "player_";
	protected static final String CAPTURES_BASE_KEY = PACKAGE_NAME + "captures_";
	protected static final String TURN_COUNT_KEY = PACKAGE_NAME + "turn_count";
	protected static final String CURRENT_PLAYER_KEY = PACKAGE_NAME + "current_player";
	*/
	
	//These keys are for use in managing the table of players and retrieving specific player data.
	//Do not use them for game data, which requires number suffixes.
	public static final String PLAYER_NAME_KEY = "_player_name";
	public static final String PLAYER_WINS_KEY = "player_wins";
	public static final String PLAYER_MATCHES_KEY = "player_matches";
	/*
	protected static final String PLAYER_NAME_KEY = PACKAGE_NAME + "player_name";
	protected static final String PLAYER_WINS_KEY = PACKAGE_NAME + "player_wins";
	protected static final String PLAYER_MATCHES_KEY = PACKAGE_NAME + "player_matches";
	*/
	
	
	
	
	
	
	
	public static String[] getNames(Context context, Game game)
	{
		return DBManager.getNames(context, game);
	}
	
	public static boolean updatePlayerScores(Context context, String[] players, Game game, String[] winners)
	{
		return DBManager.updatePlayerScores(context, players, game, winners);
	}
	
	public static boolean insertSave(Context context, ContentValues values, Game game, boolean overwrite)
	{
		return DBManager.insertSave(context, values, game, overwrite);
	}
	
	public static ContentValues retrieveSave(Context context, String gameName, Game game)
	{
		return DBManager.retrieveSave(context, gameName, game);
	}
	
	public static boolean deleteSave(Context context, String gameName, Game game)
	{
		return DBManager.deleteSave(context, gameName, game);
	}
	
	public static String gridToString(int[][] grid)
	{
		StringBuilder result = new StringBuilder(500);
		for (int countRow = 0; countRow < grid.length; countRow++)
		{
			for (int countColumn = 0; countColumn < grid[countRow].length; countColumn++)
			{
				result.append(grid[countRow][countColumn]);
				result.append(DBManager.GRID_ITEM_SEPARATOR);
			}
			result.append(DBManager.GRID_ROW_SEPARATOR);
		}
		return result.toString();
	}
	
	public static int[][] stringToGrid(int height, int width, String parsedGrid)
	{
		int[][] result = new int[height][width];
		String[] rows = parsedGrid.split(DBManager.GRID_ROW_SEPARATOR);
		String[] column;
		for (int countRow = 0; countRow < height; countRow++)
		{
			column = rows[countRow].split(DBManager.GRID_ITEM_SEPARATOR);
			for (int countColumn = 0; countColumn < width; countColumn++)
			{
				result[countRow][countColumn] = Integer.parseInt(column[countColumn]);
			}
		}
		return result;
	}
	
	public static String arrayToString(int[] array)
	{
		StringBuilder result = new StringBuilder(array.length * 2 + 1);
		for (int countColumn = 0; countColumn < (array.length - 1); countColumn++)
		{
			result.append(array[countColumn]);
			result.append(DBManager.GRID_ITEM_SEPARATOR);
		}
		result.append(array[array.length - 1]);
		return result.toString();
	}
	
	public static int[] stringToArray(String parsedArray, int length)
	{
		String[] columns = parsedArray.split(DBManager.GRID_ITEM_SEPARATOR);
		int[] result = new int[length];
		for (int countColumn = 0; countColumn < result.length; countColumn++)
		{
			result[countColumn] = Integer.parseInt(columns[countColumn]);
		}
		return result;
	}
	
	protected static String convertToFullKey(String partialKey)
	{
		return partialKey;
		//return DBInterface.PACKAGE_NAME + partialKey;
	}
	
	protected static String convertToPartialKey(String fullKey)
	{
		return fullKey;
		//return new String(fullKey.substring(DBInterface.PACKAGE_NAME.length()));
	}
	
	/*
	private static Class<?> getManager(Game game)
	{
		String className = DBInterface.ANTHOLOGY_PACKAGE_NAME;
		className += game.toString();
		className += "Manager";
		try 
		{
			return (Class<?>) Class.forName(className);
		} 
		catch (ClassNotFoundException e) 
		{
			Log.d(LOGTAG, "Unable to find class name.");
			e.printStackTrace();
			return null;
		}
	}
	*/
}
