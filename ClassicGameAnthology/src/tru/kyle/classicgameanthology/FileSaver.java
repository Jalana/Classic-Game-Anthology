package tru.kyle.classicgameanthology;

import tru.kyle.mylists.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import android.content.Context;

public class FileSaver 
{
	public static enum SaveType {PLAYERS, GAMES, DATA};
	public static enum Game {ConnectFour, Pente, Reversi, Mastermind};
	//When filling a spinner with game options, these must be stored in order.
	public static enum GameByLayout {connect_four, pente, reversi, mastermind};
	//Use these for grabbing game filenames.
	public static enum CaptureType {NONE, REMOVE, CHANGE};
	public final static String LAYOUT_PREFIX = "activity_";
	public final static String ACTIVITY_SUFFIX = "Activity";
	
	public final static String SPECIFIC_SAVE_PREFIX = "_$";
	public final static String GLOBAL_SUFFIX = ".txt";
	//This is appended to the end of all filenames if not already present.
	public final static String PLAYER_FILE_STORAGE = "global_players";
	//This one holds the filenames of all players.
	//Append the save_prefix to the end for finding specific players.
	//Use the first line to count the number of players.
	public final static String SAVE_GAME_FILE_STORAGE = "_savedgames";
	//This one holds the filenames of all saved games.
	//Store full filenames, or only the part that the player decides?
	//		Save only the partials for ease of displaying later.
	public final static String AUTOSAVE_NAME = "autosave";
	
	public static final int MIN_PLAYERS = 4;
	
	
	SaveType type;
	String filename;
	//File file;
	Game game;
	
	public FileSaver(Game currentGame, String coreFilename)
	{
		filename = coreFilename;
		game = currentGame;
		type = SaveType.GAMES;
	}
	
	public FileSaver(String corePlayerName)
	{
		filename = corePlayerName;
		type = SaveType.PLAYERS;
	}
	
	//public FileSaver(Game currentGame, SaveType newType)
	//{
	//	filename = "";
	//	type = newType;
	//	game = currentGame;
	//}
	
	public void writeToFileLineBased(Context c, int[][] values, int currentPlayer, 
			int turnCount, String[] players, Game g)
	{
		int[] captures = new int[players.length];
		for (int count = 0; count < captures.length; count++)
		{
			captures[count] = 0;
		}
		writeToFileLineBased(c, values, currentPlayer, turnCount, players, captures, g);
	}
	
	//Use this function for ConnectFour, Pente, and Reversi.
	//When reading data, make sure to check if the game needs to count captures (if it is Pente, in other words).
	//		If not, then skip the captures line.
	//Format:
	//		Line 0 is player count, the current player, and the turn count, in that order.
	//			Split the string by spaces.
	//		Line 1 is the captures. Skip this line when reading, unless the game is Pente.
	//		Lines 2-n are for player names, based on the player count.
	//		Subsequent lines are for the 2D int array.
	public void writeToFileLineBased(Context c, int[][] values, int currentPlayer,
			int turnCount, String[] players, int[] captures, Game g)
	{
		File file = new File(c.getApplicationContext().getFilesDir(), assembleFileName(filename, type, game));
		String temp;
		BufferedWriter writer;
		int localClick;
		
		try 
		{
			writer = new BufferedWriter(new FileWriter(file));
			
			temp = players.length + ":";
			temp += currentPlayer + ":";
			temp += turnCount + "";
			writer.append(temp);
			writer.newLine();
			
			if (captures.length > 0)
			{
				for (int count = 0; count < captures.length; count++)
				{
					writer.append(captures[count] + "");
					if (count != captures.length - 1)
					{
						writer.append(":");
					}
				}
				writer.newLine();
			}
			else
			{
				for (int count = 0; count < players.length; count++)
				{
					writer.append("0");
					if (count != players.length - 1)
					{
						writer.append(":");
					}
				}
				writer.newLine();
			}
			
			for (int count = 0; count < players.length; count++)
			{
				temp = players[count] + "";
				writer.append(temp);
				writer.newLine();
			}
			
			for (int count = 0; count < values.length; count++)
			{
				for (int count2 = 0; count2 < values[count].length; count2++)
				{
					//if (buttons[count][count2].isClickable() == false)
					if (values[count][count2] != 0)
					{
						localClick = 0;
					}
					else
					{
						localClick = 1;
					}
					temp = localClick + ":";
					writer.append(temp);
					temp = values[count][count2] + " ";
					writer.append(temp);
				}
				writer.newLine();
			}
			
			writer.flush();
			writer.close();
			addFile(c, g, filename);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	public DataWrapper readDataLineBased(Context c) throws FileNotFoundException
	{
		String middleInput = "";
		String inputString;
		BufferedReader inputReader;
		StringBuffer stringBuffer;
		DataWrapper result = null;
		try 
		{
			inputReader = new BufferedReader(new InputStreamReader(c.openFileInput(assembleFileName(filename, type, game))));
			stringBuffer = new StringBuffer();
			inputString = inputReader.readLine();
			while (inputString != null)
			{
				stringBuffer.append(inputString + "\n");
				inputString = inputReader.readLine();
			}
			middleInput = stringBuffer.toString();
			inputReader.close();
			result = createWrapperLineBased(middleInput.split("\\n"));
		}
		catch (FileNotFoundException f)
		{
			throw new FileNotFoundException();
			//Catch this in the activity itself, which can call its own resetData() method.
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		if (result == null)
		{
			throw new FileNotFoundException();
		}
		
		return result;
	}
	
	public DataWrapper createWrapperLineBased(String[] input)
	{
		int countMain = 0;
		int tempValue;
		int currentPlayer;
		int turnCount;
		int[][] pointsPlaced = null;
		String[] names;
		int[] captures;
		
		String[] temp;
		String[] temp2;
		
		temp = input[countMain].split(":");
		names = new String[Integer.parseInt(temp[0])];
		currentPlayer = Integer.parseInt(temp[1]);
		turnCount = Integer.parseInt(temp[2]);
		
		countMain++; //count == 1
		temp = input[countMain].split(":");
		captures = new int[names.length];
		for (int count = 0; count < captures.length; count++)
		{
			captures[count] = Integer.parseInt(temp[count]);
		}
		countMain++; //count == 2
		
		for (int tempCount = 0; tempCount < names.length; tempCount++)
		{
			names[tempCount] = input[countMain];
			countMain++;
		}
		
		temp = input[countMain].split("\\s");
		pointsPlaced = new int[input.length - countMain][temp.length];
		
		for (int count3 = 0; countMain < input.length; count3++)
		{
			if (count3 != 0)
			{
				temp = input[countMain].split("\\s");
			}
			for (int count2 = 0; count2 < temp.length; count2++)
			{
				temp2 = temp[count2].split(":");
				if (Integer.parseInt(temp2[0]) == 0)
				{
					//buttons[count3][count2].setClickable(false);
					//buttons[count3][count2].setBackgroundColor(Color.WHITE);
				}
				else
				{
					//buttons[count3][count2].setClickable(true);
					//buttons[count3][count2].setBackgroundColor(Color.BLACK);
				}
				tempValue = Integer.parseInt(temp2[1]);
				pointsPlaced[count3][count2] = tempValue;
			}
			countMain++;
		}
		DataWrapper result = new DataWrapper(pointsPlaced, currentPlayer, turnCount, names, captures);
		return result;
	}
	
	private static void addFile(Context c, Game g, String name)
	{
		MyQueue<String> queue = new MyQueue<String>();
		//String temp;
		String[] filenames;
		boolean isPresent = false;
		
		//temp = assembleFileName(FileSaver.PLAYER_FILE_STORAGE, SaveType.DATA, g);
		//File storageFile = new File(c.getApplicationContext().getFilesDir(), temp);
		//FileInputStream tempWriter;
		
		//try 
		//{
			//BufferedWriter storageWriter = new BufferedWriter(new FileWriter(storageFile, false));
			filenames = getFilenames(c, g);
			if (filenames == null)
			{
				filenames = new String[] {name};
				writeFilenames(c, filenames, g);
				return;
			}
			
			for (int count = 0; count < filenames.length; count++)
			{
				if (checkFile(c, filenames[count], SaveType.GAMES, g) == false)
				{
					filenames[count] = null;
				}
				
				if (filenames[count] != null)
				{
					queue.enqueue(filenames[count]);
				}
				if (isPresent == true || name.equalsIgnoreCase(filenames[count]))
				{
					isPresent = true;
				}
			}
			if (isPresent == false)
			{
				queue.enqueue(name);
				//appendToFile(c, "\n" + name, temp);
			}
			//storageWriter.close();
			filenames = new String[queue.size()];
			
			for (int count = 0; count < filenames.length; count++)
			{
				filenames[count] = queue.dequeue();
			}
			writeFilenames(c, filenames, g);
		//}
		//catch (IOException e) 
		//{
		//	e.printStackTrace();
		//}
	}
	
	protected static void writeFilenames(Context c, String[] names, Game g)
	{
		File storageFile;
		storageFile = new File(c.getApplicationContext().getFilesDir(), 
				assembleFileName(null, SaveType.DATA, g));
		
		try 
		{
			BufferedWriter storageWriter = new BufferedWriter(new FileWriter(storageFile, false));
			int limit = names.length;
			for (int count = 0; count < names.length; count++)
			{
				if (names[count] == null)
				{
					limit--;
				}
			}
			storageWriter.append(limit + "\n");
			for (int count = 0; count < names.length; count++)
			{
				if (names[count] != null)
				{
					storageWriter.append(names[count] + "\n");
				}
			}
			storageWriter.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	public static String[] getFilenames(Context c, Game g)
	{
		MyQueue<String> queue = new MyQueue<String>();
		BufferedReader inputReader;
		String temp = "";
		int fileCount;
		SaveType t;
		if (g == null)
		{
			t = SaveType.PLAYERS;
		}
		else
		{
			t = SaveType.GAMES;
		}
		
		try 
		{
			inputReader = new BufferedReader(new InputStreamReader(c.openFileInput
					(assembleFileName(null, SaveType.DATA, g))));
			fileCount = Integer.parseInt(inputReader.readLine());
			for (int count = 0; count < fileCount; count++)
			{
				temp = inputReader.readLine();
				if (temp != null && temp != "")
				{
					if (checkFile(c, temp, t, g) == true)
					{
						queue.enqueue(temp);
					}
					//count = 0;
				}
			}
			inputReader.close();
			if (queue.size() < FileSaver.MIN_PLAYERS && g == null)
			{
				throw new FileNotFoundException();
			}
		}
		catch (FileNotFoundException f)
		{
			return null;
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		String[] result = new String[queue.size()];
		for (int count = 0; count < result.length; count++)
		{
			if (queue.peek() != null)
			{
				result[count] = queue.dequeue();
			}
			else
			{
				queue.dequeue();
			}
		}
		
		return result;
	}
	
	private static boolean checkFile(Context c, String name, SaveType t, Game g)
	{
		FileInputStream tempWriter;
		try
		{
			tempWriter = new FileInputStream(new File(c.getFilesDir(), 
					assembleFileName(name, t, g)));
			tempWriter.close();
		}
		catch (FileNotFoundException f)
		{
			return false;
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		return true;
	}
	
	public static void deleteSave(Context c, String file, SaveType t, Game g)
	{
		c.deleteFile(assembleFileName(file, t, g));
		//File deleted = new File(c.getApplicationContext().getFilesDir(), assembleFileName(file, t, g));
		//deleted.delete();
	}
	
	public static void appendToFile(Context c, String text, String name, SaveType t, Game g)
	{
    	appendToFile(c, text, assembleFileName(name, t, g));
	}
	
	private static void appendToFile(Context c, String text, String fullName)
	{
		File file = new File(c.getApplicationContext().getFilesDir(), fullName);
		BufferedWriter writer;
		
		try 
		{
			writer = new BufferedWriter(new FileWriter(file, true));
			
			writer.append(text);
			
			writer.flush();
			writer.close();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	private static String assembleFileName(String coreName, SaveType t, Game g)
	{
		String result = "";
		if (t == SaveType.PLAYERS)
		{
			result = FileSaver.PLAYER_FILE_STORAGE;
			result += FileSaver.SPECIFIC_SAVE_PREFIX;
			result += coreName;
		}
		else if (t == SaveType.DATA)
		{
			if (g == null)
			{
				//result = coreName;
				result = FileSaver.PLAYER_FILE_STORAGE;
			}
			else
			{
				GameByLayout gbl = FileSaver.GameByLayout.values()[g.ordinal()];
				result = gbl.toString() + FileSaver.SAVE_GAME_FILE_STORAGE;
			}
		}
		else
		{
			result = GameByLayout.values()[g.ordinal()].toString();
			//if (t == SaveType.GAMES)
			//{
				result += FileSaver.SPECIFIC_SAVE_PREFIX;
				result += coreName;
			//}
			//else
			//{
			//	result += FileSaver.SAVE_GAME_FILE_STORAGE;
			//}
		}
		result += FileSaver.GLOBAL_SUFFIX;
		return result;
	}
	
	public static void writePlayersToFile(Context c, Player[] players)
	{
		//File playerFile;
		File storageFile = new File(c.getApplicationContext().getFilesDir(), 
				assembleFileName(FileSaver.PLAYER_FILE_STORAGE, SaveType.DATA, null));
		Player tempPlayer;
		try 
		{
			BufferedWriter storageWriter = new BufferedWriter(new FileWriter(storageFile, false));
			storageWriter.append(players.length + "\n");
			for (int count = 0; count < players.length; count++)
			{
				tempPlayer = players[count];
				if (tempPlayer != null)
				{
					storageWriter.append(tempPlayer.getName() + "\n");
				}
			}
			storageWriter.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		//BufferedWriter writer;
		//String temp;
		
		for (int count = 0; count < players.length; count++)
		{
			if (players[count] != null)
			{
				writeSinglePlayer(c, players[count]);
			}
		}
	}
	
	public static void writeSinglePlayer(Context c, Player player)
	{
		BufferedWriter writer;
		String temp;
		
		try 
		{
			File playerFile = new File(c.getFilesDir(), assembleFileName(player.getName(), SaveType.PLAYERS, null));
			writer = new BufferedWriter(new FileWriter(playerFile, false));
			int limit = FileSaver.Game.values().length;
			int[] currentWins;
			int[] currentMatches;
			
			writer.append(player.getName() + "\n");
			currentWins = player.getWins();
			currentMatches = player.getMatches();
			for (int count2 = 0; count2 < limit; count2++)
			{
				temp = currentWins[count2] + ":" + currentMatches[count2] + " ";
				writer.append(temp);
			}
			writer.append("\n");
			writer.flush();
			writer.close();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	public static String[] createDefaultPlayers(Context c)
	{
		String[] names = new String[FileSaver.MIN_PLAYERS];
		names[0] = "Joe";
		names[1] = "Jane";
		names[2] = "Bob";
		names[3] = "Kate";
		Player[] temp = new Player[FileSaver.MIN_PLAYERS];
		for (int count = 0; count < names.length; count++)
		{
			temp[count] = new Player(names[count]);
		}
		FileSaver.writePlayersToFile(c, temp);
		
		return names;
	}
	
	public static Player[] getPlayerList(Context c)
	{
		String[] names;
		MyQueue<Player> players = new MyQueue<Player>();
		Player temp;
		try
		{
			names = FileSaver.readPlayerNames(c);
		}
		catch (FileNotFoundException e)
		{
			names = FileSaver.createDefaultPlayers(c);
		}
		
		for (int count = 0; count < names.length; count++)
		{
			try
			{
				temp = readPlayerFromFile(c, names[count]);
				if (temp != null)
				{
					players.enqueue(temp);
				}
			}
			catch (FileNotFoundException f)
			{
				//Skip and move on to the next player.
			}
		}
		Player[] result = new Player[players.size()];
		for (int count = 0; count < result.length; count++)
		{
			result[count] = players.dequeue();
		}
		
		return result;
	}
	
	public static String[] readPlayerNames(Context c) throws FileNotFoundException
	{
		MyQueue<String> queue = new MyQueue<String>();
		BufferedReader inputReader;
		int playerCount;
		String temp;
		try 
		{
			inputReader = new BufferedReader(new InputStreamReader(c.openFileInput
					(assembleFileName(FileSaver.PLAYER_FILE_STORAGE, SaveType.DATA, null))));
			playerCount = Integer.parseInt(inputReader.readLine());
			for (int count = 0; count < playerCount; count++)
			{
				temp = inputReader.readLine();
				if (temp != null && temp != "")
				{
					queue.enqueue(temp);
				}
			}
			inputReader.close();
			if (queue.size() < FileSaver.MIN_PLAYERS)
			{
				throw new FileNotFoundException();
			}
		}
		catch (FileNotFoundException f)
		{
			throw new FileNotFoundException();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		String[] result = new String[queue.size()];
		for (int count = 0; count < result.length; count++)
		{
			result[count] = queue.dequeue();
		}
		
		return result;
	}
	
	public static Player readPlayerFromFile(Context c, String corePlayerName) throws FileNotFoundException
	{
		int limit = FileSaver.Game.values().length;
		String name = "";
		int[] wins = new int[limit];
		int[] matches = new int[limit];
		String[] middleInput;
		String[] inputString;
		BufferedReader inputReader;
		try 
		{
			inputReader = new BufferedReader(new InputStreamReader(c.openFileInput
					(assembleFileName(corePlayerName, SaveType.PLAYERS, null))));
			name = inputReader.readLine();
			inputString = inputReader.readLine().split("\\s");
			for (int count = 0; count < limit; count++)
			{
				middleInput = inputString[count].split(":");
				wins[count] = Integer.parseInt(middleInput[0]);
				matches[count] = Integer.parseInt(middleInput[1]);
			}
			inputReader.close();
		}
		catch (FileNotFoundException f)
		{
			throw new FileNotFoundException();
			//Catch this in the activity itself, which can call its own resetData() method.
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		Player result = new Player(name, wins, matches);
		return result;
	}
	
	
	
	
	
}
