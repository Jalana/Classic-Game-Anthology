package tru.kyle.classicgameanthology;

/*
This file (Player) is a part of the Classic Game Anthology application.
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
		winCount = new int[DBInterface.Game.values().length];
		matchCount = new int[DBInterface.Game.values().length];
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
				DBInterface.Game.values().length);
		matchCount = DBInterface.stringToArray(values.getAsString(DBInterface.PLAYER_MATCHES_KEY), 
				DBInterface.Game.values().length);
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
		int limit = DBInterface.Game.values().length;
		for (int count = 0; count < limit; count++)
		{
			result += winCount[count];
		}
		return result;
	}
	
	public int getGlobalMatches()
	{
		int result = 0;
		int limit = DBInterface.Game.values().length;
		for (int count = 0; count < limit; count++)
		{
			result += matchCount[count];
		}
		return result;
	}
	
	public String getGameStandings(int index)
	{
		String result = "";
		result = DBInterface.Game.values()[index].toString() + "\n";
		result += "Wins: " + winCount[index] + "\n";
		result += "Matches: " + matchCount[index] + "\n";
		return result;
	}
	
	public String getGameFileStandings(int index)
	{
		String result = "";
		result += winCount[index] + ":";
		result += matchCount[index] + " ";
		return result;
	}
	
	public String toFileString()
	{
		String result = "";
		result = getName() + "\n";
		int limit = DBInterface.Game.values().length;
		for (int count = 0; count < limit; count++)
		{
			result += getGameFileStandings(count);
		}
		return result;
	}
	
}
