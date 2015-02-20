package tru.kyle.classicgameanthology;

import tru.kyle.databases.DBInterface;
import android.content.ContentValues;

public class Player 
{
	String playerName;
	int[] winCount;
	int[] matchCount;
	//Use ordinals to find the correct game for each index.
	
	//For file purposes, all standings are written on a single line in this form:
	//		wins:matches wins:matches
	//		etc., where the ordinals determine how they are placed.
	
	public Player(String newName)
	{
		playerName = newName;
		winCount = new int[FileSaver.Game.values().length];
		matchCount = new int[FileSaver.Game.values().length];
		for (int count = 0; count < winCount.length; count++)
		{
			winCount[count] = 0;
			matchCount[count] = 0;
		}
	}
	
	public Player(String newName, int[] wins, int[] matches)
	{
		playerName = newName;
		winCount = wins;
		matchCount = matches;
	}
	
	public Player(ContentValues values)
	{
		playerName = values.getAsString(DBInterface.PLAYER_NAME_KEY);
		winCount = DBInterface.stringToArray(values.getAsString(DBInterface.PLAYER_WINS_KEY), 
				FileSaver.Game.values().length);
		matchCount = DBInterface.stringToArray(values.getAsString(DBInterface.PLAYER_MATCHES_KEY), 
				FileSaver.Game.values().length);
	}
	
	public String getName()
	{
		return playerName;
	}
	
	public int[] getWins()
	{
		return winCount;
	}
	
	public int[] getMatches()
	{
		return matchCount;
	}
	
	public void addWin(int index)
	{
		winCount[index]++;
	}
	
	public void addMatch(int index)
	{
		matchCount[index]++;
	}
	
	public int getGameWins(int index)
	{
		return winCount[index];
	}
	
	public int getGameMatches(int index)
	{
		return matchCount[index];
	}
	
	public int getGlobalWins()
	{
		int result = 0;
		int limit = FileSaver.Game.values().length;
		for (int count = 0; count < limit; count++)
		{
			result += winCount[count];
		}
		return result;
	}
	
	public int getGlobalMatches()
	{
		int result = 0;
		int limit = FileSaver.Game.values().length;
		for (int count = 0; count < limit; count++)
		{
			result += matchCount[count];
		}
		return result;
	}
	
	public String getGameStandings(int index)
	{
		String result = "";
		result = FileSaver.Game.values()[index].toString() + "\n";
		result += "Wins: " + winCount[index] + "\n";
		result += "Matches: " + matchCount[index] + "\n";
		return result;
	}
	
	public String getGameFileStandings(int index)
	{
		String result = "";
		//result = FileSaver.Game.values()[index].toString() + "\n";
		result += winCount[index] + ":";
		result += matchCount[index] + " ";
		return result;
	}
	
	public String toFileString()
	{
		String result = "";
		result = getName() + "\n";
		int limit = FileSaver.Game.values().length;
		for (int count = 0; count < limit; count++)
		{
			result += getGameFileStandings(count);
		}
		return result;
	}
}
