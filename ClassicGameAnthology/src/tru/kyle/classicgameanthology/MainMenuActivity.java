package tru.kyle.classicgameanthology;

/*
This file (MainMenuActivity) is a part of the Classic Game Anthology application.
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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import tru.kyle.databases.DBInterface;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;




public class MainMenuActivity extends BaseActivity 
{
	public final static String PACKAGE_NAME = "tru.kyle.classicgameanthology";
	public final static String GAME_FILENAME_KEY = PACKAGE_NAME + ".gameFilename";
	public final static String BASE_PLAYER_FILENAME_KEY = PACKAGE_NAME + ".player_";
	public final static String NEW_MATCH_KEY = PACKAGE_NAME + ".newMatch";
	public final static String USING_BLUETOOTH_KEY = PACKAGE_NAME + ".usingBluetooth";
	public final static String OTHER_DEVICE_KEY = PACKAGE_NAME + ".otherDevice";
	public final static String IS_HOST_KEY = PACKAGE_NAME + ".host";
	public final static String EXTRA_STRING_BASE_KEY = PACKAGE_NAME + ".extra_string_";
	public final static String EXTRA_BOOL_BASE_KEY = PACKAGE_NAME + ".extra_bool_";
	
	//These are keys for specific methods in the game activities.
	public final static String GET_PLAYERS_METHOD = "getPlayerCounts";
	public final static String GET_EXTRAS_METHOD = "getExtras";
	public final static String GET_BOOLS_METHOD = "getBoolExtras";
	
	protected static final int NORMAL_MOVE_SOUND = R.raw.normal_move;
	protected static final int LOST_PIECE_SOUND = R.raw.lost_piece;
	protected static final int ENEMY_MOVE_SOUND = R.raw.enemy_move;
	protected static final int WARNING_SOUND = R.raw.warning;
	protected static final int VICTORY_SOUND = R.raw.victory;
	protected static final int DEFEAT_SOUND = R.raw.defeat;
	
	AlertDialog mainMenuDialog;
	AlertDialog deleteSaveDialog;
	AlertDialog bluetoothDialog;
	TextView playerOneDisplay;
	TextView playerTwoDisplay;
	boolean activityShift = false;
	
	Integer[] playerOptions = null;
    String[] boolExtras = null;
    String[][] extras = null;
	
    
	Spinner gameSpinner;
	Spinner[] playerSpinners;
	Spinner[] extraSpinners;
	CheckedTextView[] extraCheckBoxes;
	Spinner savedGamesSpinner;
	
	Spinner deviceSpinner;
	BluetoothDevice chosenDevice;
	String opponentName;
	int[] gameOptionsFromHost;
	
	Button newMatch;
	Button savedGames;
	Button deleteSave;
	Button goToPlayers;
	Button versusMatch;
	
	String[] players;
	boolean[] isPlaying;
	
	int playerLimit = 2;
	int currentGame = 0;
	int[] currentPlayers;
	int currentSaveFile = 0;
	String filename;
	String[] currentFiles;
	
	int screenHeight;
	int screenWidth;
	float screenDensity;
	boolean useLargeScreen;
	
	CheckedTextView bigDropDownItem;
	CheckedTextView smallDropDownItem;
	TextView bigSpinnerItem;
	TextView smallSpinnerItem;
	
	int bigDropDownItemID;
	int smallDropDownItemID;
	int bigSpinnerItemID;
	int smallSpinnerItemID;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
		Log.d("Life Cycle", "Main Activity: onCreate");
    	activityShift = false;
    	
    	useLargeScreen = getScreenDimensions();
		if (useLargeScreen == true)
		{
			bigDropDownItemID = R.layout.menu_spinner_dropdown_item_large;
			smallDropDownItemID = R.layout.menu_spinner_dropdown_item_medium;
			bigSpinnerItemID = R.layout.menu_spinner_item_large;
			smallSpinnerItemID = R.layout.menu_spinner_item_medium;
		}
		else
		{
			bigDropDownItemID = R.layout.menu_spinner_dropdown_item_medium;
			smallDropDownItemID = R.layout.menu_spinner_dropdown_item_small;
			bigSpinnerItemID = R.layout.menu_spinner_item_medium;
			smallSpinnerItemID = R.layout.menu_spinner_item_small;
			//bigDropDownItem = (CheckedTextView) findViewById(bigDropDownItemID);
			//smallDropDownItem = (CheckedTextView) findViewById(smallDropDownItemID);
			//bigSpinnerItem = (TextView) findViewById(bigSpinnerItemID);
			//smallSpinnerItem = (TextView) findViewById(smallSpinnerItemID);
		}
    	
    	players = DBInterface.getNames(getApplicationContext(), null);
    	isPlaying = new boolean[players.length];
    	for (int count = 0; count < isPlaying.length; count++)
    	{
    		isPlaying[count] = false;
    	}
    	
    	gameSpinner = (Spinner) findViewById(R.id.main_menu_gameSpinner);
    	playerSpinners = new Spinner[2];
    	playerSpinners[0] = (Spinner) findViewById(R.id.main_menu_firstPlayerSpinner);
    	playerSpinners[1] = (Spinner) findViewById(R.id.main_menu_secondPlayerSpinner);
    	currentPlayers = new int[playerSpinners.length];
    	
    	extraSpinners = new Spinner[3];
    	extraSpinners[0] = (Spinner) findViewById(R.id.main_menu_extrasSpinner_1);
    	extraSpinners[1] = (Spinner) findViewById(R.id.main_menu_extrasSpinner_2);
    	extraSpinners[2] = (Spinner) findViewById(R.id.main_menu_extrasSpinner_3);
    	
    	extraCheckBoxes = new CheckedTextView[2];
    	extraCheckBoxes[0] = (CheckedTextView) findViewById(R.id.main_menu_extrasCheckedView_1);
    	extraCheckBoxes[1] = (CheckedTextView) findViewById(R.id.main_menu_extrasCheckedView_2);
    	
    	savedGamesSpinner = (Spinner) findViewById(R.id.main_menu_savedGamesSpinner);
    	
    	gameSpinner.setOnItemSelectedListener(game_spinner_listener);
    	savedGamesSpinner.setOnItemSelectedListener(load_game_spinner_listener);
    	
    	newMatch = (Button) findViewById(R.id.main_menu_newMatch);
    	savedGames = (Button) findViewById(R.id.main_menu_loadSave);
    	goToPlayers = (Button) findViewById(R.id.main_menu_goToPlayers);
    	deleteSave = (Button) findViewById(R.id.main_menu_deleteSave);
    	versusMatch = (Button) findViewById(R.id.main_menu_bluetoothMatch);
    	newMatch.setOnClickListener(newMatchListener);
    	savedGames.setOnClickListener(loadGameListener);
    	goToPlayers.setOnClickListener(goToPlayersListener);
    	deleteSave.setOnClickListener(deleteGameListener);
    	versusMatch.setOnClickListener(bluetooth_match_listener);
    }
    
    @Override
	protected void onStart() 
    {
		super.onStart();
		Log.d("Life Cycle", "Main Activity: onStart");
		//This is called between onCreate() and onResume().
		//onRestart() also leads to this.
	}
    
	@Override
	protected void onResume() 
	{
		super.onResume();
		Log.d("Life Cycle", "Main Activity: onResume");
		//This method is called every time the activity hits the foreground.
		//This includes the first time the app is created.
		//Initialization of data should be here, especially if it was released.
		for (int count = 0; count < playerSpinners.length; count++)
    	{
    		currentPlayers[count] = 0;
    		playerSpinners[count].setOnItemSelectedListener(player_listener);
    	}
    	addGamesToSpinner();
    	Intent launchIntent = getIntent();
    	if (launchIntent.hasExtra(MainMenuActivity.OTHER_DEVICE_KEY))
    	{
	    	chosenDevice = launchIntent.getParcelableExtra(MainMenuActivity.OTHER_DEVICE_KEY);
	    	launchIntent.removeExtra(MainMenuActivity.OTHER_DEVICE_KEY);
    	}
    	if (chosenDevice == null)
    	{
    		Log.d("Main Menu", "No connected device at onResume()");
    	}
    	else
    	{
    		Log.d("Main Menu", "Connected to a device at onResume()");
    	}
	}
	
	
	
	private boolean getScreenDimensions()
	{
		int actionBarSize = 0;
		DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
		TypedValue tv = new TypedValue();
		if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
		{
		    actionBarSize = TypedValue.complexToDimensionPixelSize(tv.data, metrics);
		}
		Display display = getWindowManager().getDefaultDisplay();
		display.getMetrics(metrics);
		screenHeight = metrics.heightPixels;
		screenWidth = metrics.widthPixels;
		screenDensity = metrics.density;
		
		int statusBarHeight = 0;
		int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) 
		{
			statusBarHeight = getResources().getDimensionPixelSize(resourceId);
		}
		screenHeight -= actionBarSize;
		screenHeight -= statusBarHeight;
		
		int smallerDimension;
		int result;
		int limit;
		if (screenHeight < screenWidth)
		{
			//Landscape mode allows for a lower limit due to the altered layout.
			smallerDimension = screenHeight;
			limit = 400;
		}
		else
		{
			//Portrait mode requires a higher limit, experimentally.
			smallerDimension = screenWidth;
			limit = 500;
		}
		
		result = (int) (smallerDimension / screenDensity + 0.5f);
		Log.d("Main Menu", "result = " + result + ", height = " + screenHeight + 
				", width = " + screenWidth + ", density = " + screenDensity);
		
		if (result >= limit)
		{
			Log.d("Main Menu", "Returned true from getScreenDimensions");
			return true;
		}
		return false;
	}
	
	
	
	public void determineExtras()
    {
        String gameName = getClassName(DBInterface.Game.values()[currentGame]);
        //Class<?> gameClass;
        Class<?> gameClass;
        playerOptions = null;
        boolExtras = null;
        extras = null;
        try 
        {
			gameClass = Class.forName(gameName);
			
			playerOptions = (Integer[]) gameClass.getDeclaredMethod(MainMenuActivity.GET_PLAYERS_METHOD)
					.invoke(null);
			boolExtras = (String[]) gameClass.getDeclaredMethod(MainMenuActivity.GET_BOOLS_METHOD)
					.invoke(null);
			extras = (String[][]) gameClass.getDeclaredMethod(MainMenuActivity.GET_EXTRAS_METHOD)
					.invoke(null);
		} 
        catch (ClassNotFoundException e) 
        {
        	Log.d("Main Menu", "Unable to find class name.");
			e.printStackTrace();
		} 
        catch (NoSuchMethodException e) 
        {
        	Log.d("Main Menu", "Unable to find method name.");
			e.printStackTrace();
		} 
        catch (IllegalAccessException e) 
        {
        	Log.d("Main Menu", "Unable to access method.");
			e.printStackTrace();
		} 
        catch (IllegalArgumentException e) 
        {
        	Log.d("Main Menu", "Method arguments were incorrect.");
			e.printStackTrace();
		} 
        catch (InvocationTargetException e) 
        {
        	Log.d("Main Menu", "Invoked method threw an exception.");
			e.printStackTrace();
		}
    }
	
	public void setExtraViews()
	{
		List<String> spinnerArray;
		int tempCount = 0;
		int outerCount = 0;
        determineExtras();
        
        if (playerOptions != null)
        {
        	//If any games are made that allow for varying numbers of players, complete this section.
        	playerLimit = playerOptions[0];
        	if (playerLimit == 1)
        	{
        		versusMatch.setVisibility(View.GONE);
        	}
        	else
        	{
        		versusMatch.setVisibility(View.VISIBLE);
        	}
        }
        
        if (boolExtras != null)
        {
        	for (outerCount = 0; outerCount < boolExtras.length; outerCount++)
            {
        		extraCheckBoxes[outerCount].setVisibility(View.VISIBLE);
        		extraCheckBoxes[outerCount].setText(boolExtras[outerCount]);
        		extraCheckBoxes[outerCount].setOnClickListener(checked_view_listener);
            }
        }
        for ( ; outerCount < extraCheckBoxes.length; outerCount++)
        {
        	extraCheckBoxes[outerCount].setVisibility(View.GONE);
        	extraCheckBoxes[outerCount].setOnClickListener(null);
        }
        
        outerCount = 0;
        if (extras != null)
        {
        	for (outerCount = 0; outerCount < extras.length; outerCount++)
            {
        		spinnerArray = new ArrayList<String>();
    	        for (tempCount = 0; tempCount < extras[outerCount].length; tempCount++)
    			{
    	        	if (extras[outerCount][tempCount] != null)
    	        	{
    	        		spinnerArray.add(extras[outerCount][tempCount]);
    	        	}
    			}
    	        extraSpinners[outerCount].setVisibility(View.VISIBLE);
    	        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, smallSpinnerItemID, spinnerArray);
    		    adapter.setDropDownViewResource(smallDropDownItemID);
    		    extraSpinners[outerCount].setAdapter(adapter);
            }
        }
        for ( ; outerCount < extraSpinners.length; outerCount++)
        {
        	extraSpinners[outerCount].setVisibility(View.GONE);
        }
	}
	
	public void addGamesToSpinner()
    {
    	List<String> spinnerArray = new ArrayList<String>();
        int tempCount = 0;
        int limit = DBInterface.Game.values().length;
        
        for (tempCount = 0; tempCount < limit; tempCount++)
		{
        	spinnerArray.add(DBInterface.Game.values()[tempCount].toString());
		}
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, bigSpinnerItemID, spinnerArray);
	    adapter.setDropDownViewResource(bigDropDownItemID);
	    gameSpinner.setAdapter(adapter);
    }
	
	public void addPlayersToSpinner(int playerLimit)
    {
    	List<String> spinnerArray;
    	int tempCount;
    	int outerCount;
    	spinnerArray = new ArrayList<String>();
        
        for (outerCount = 0; outerCount < playerLimit; outerCount++)
        {
        	spinnerArray = new ArrayList<String>();
	        for (tempCount = 0; tempCount < players.length; tempCount++)
			{
	        	if (players[tempCount] != null)
	        	{
	        		spinnerArray.add(players[tempCount]);
	        	}
			}
	        playerSpinners[outerCount].setVisibility(View.VISIBLE);
	        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, smallSpinnerItemID, spinnerArray);
		    adapter.setDropDownViewResource(smallDropDownItemID);
		    playerSpinners[outerCount].setAdapter(adapter);
        }
        
        for ( ; outerCount < playerSpinners.length; outerCount++)
        {
        	playerSpinners[outerCount].setVisibility(View.GONE);
        }
    }
	
	public void addSaveFilesToSpinner()
    {
		currentFiles = DBInterface.getNames(getApplicationContext(), DBInterface.Game.values()[currentGame]);
    	List<String> spinnerArray = new ArrayList<String>();
        int tempCount = 0;
        if (currentFiles == null)
        {
        	savedGamesSpinner.setAdapter(null);
        	return;
        }
        
        for (tempCount = 0; tempCount < currentFiles.length; tempCount++)
		{
        	if (currentFiles[tempCount] != null)
        	{
        		spinnerArray.add(currentFiles[tempCount]);
        	}
		}
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, smallSpinnerItemID, spinnerArray);
	    adapter.setDropDownViewResource(smallDropDownItemID);
	    savedGamesSpinner.setAdapter(adapter);
    }
	
	private OnItemSelectedListener game_spinner_listener = new OnItemSelectedListener()
	{
		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) 
		{
			currentGame = position;
			currentSaveFile = 0;
			setExtraViews();
			addPlayersToSpinner(playerOptions[0]);
			addSaveFilesToSpinner();
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) 
		{
			
		}
	};
	
	private OnItemSelectedListener player_listener = new OnItemSelectedListener()
	{
		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) 
		{
			if (view != null)
        	{
				int index = 0;
				Spinner temp = (Spinner) parent;
				while (playerSpinners[index] != temp)
				{
					index++;
				}
				currentPlayers[index] = position;
        	}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) 
		{
			
		}
	};
	
	private OnItemSelectedListener load_game_spinner_listener = new OnItemSelectedListener()
	{
		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) 
		{
			if (view != null)
        	{
				currentSaveFile = position;
				filename = (String) savedGamesSpinner.getAdapter().getItem(position);
        	}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) 
		{
			
		}
	};
	
	
	
	private OnClickListener checked_view_listener = new OnClickListener() 
	{
		@Override
		public void onClick(View v) 
		{
			CheckedTextView temp = (CheckedTextView) v;
			temp.toggle();
		}
	};
	
	//These listeners respond to the various button clicks and launch the appropriate activity accordingly.
	private OnClickListener goToPlayersListener = new OnClickListener() 
	{
		@Override
		public void onClick(View v) 
		{
			activityShift = true;
			Intent intent = new Intent(MainMenuActivity.this, PlayerManagementActivity.class);
			startActivity(intent);
		}
	};
	
	private OnClickListener newMatchListener = new OnClickListener() 
	{
		@Override
		public void onClick(View v) 
		{
			if (checkPlayerConflicts() == false)
			{
				activityShift = true;
				
				String className = getClassName(DBInterface.Game.values()[currentGame]);
				Intent intent;
				try 
				{
					intent = new Intent(MainMenuActivity.this, Class.forName(className));
					for (int count = 0; count < currentPlayers.length; count++)
					{
						intent.putExtra(MainMenuActivity.BASE_PLAYER_FILENAME_KEY + (count + 1), 
								players[currentPlayers[count]]);
					}
					if (extras != null)
					{
						for (int count = 0; count < extras.length; count++)
						{
							intent.putExtra(MainMenuActivity.EXTRA_STRING_BASE_KEY + (count + 1), 
									extras[count][extraSpinners[count].getSelectedItemPosition()]);
						}
					}
					if (boolExtras != null)
					{
						for (int count = 0; count < extraCheckBoxes.length; count++)
						{
							intent.putExtra(MainMenuActivity.EXTRA_BOOL_BASE_KEY + (count + 1), 
									extraCheckBoxes[count].isChecked());
						}
					}
					
					intent.putExtra(MainMenuActivity.NEW_MATCH_KEY, true);
					startActivity(intent);
					//MainMenuActivity.this.finish();
				} 
				catch (ClassNotFoundException e) 
				{
					Log.d("Main Menu", "Unable to find class name.");
					e.printStackTrace();
					activityShift = false;
				}
			}
			else
			{
				String temp = "One person cannot act as more than one player.";
				Toast.makeText(MainMenuActivity.this, temp, Toast.LENGTH_LONG).show();
			}
		}
	};
	
	private OnClickListener bluetooth_match_listener = new OnClickListener()
	{
		@Override
		public void onClick(View v) 
		{
			if (bluetooth == null)
			{
				return;
			}
			
			if (bluetooth.getAdapter().isEnabled() == false)
			{
				enableBluetooth();
				return;
			}
			
			if (bluetoothDialog != null && bluetoothDialog.isShowing() == true)
			{
				bluetoothDialog.dismiss();
			}
			
			AlertDialog.Builder bluetoothBuilder = new AlertDialog.Builder(MainMenuActivity.this);
			bluetoothBuilder.setTitle("Network Match");
			if (chosenDevice == null)
			{
				bluetoothBuilder.setMessage("Not Connected");
			}
			else
			{
				bluetoothBuilder.setMessage("Connected to: " + chosenDevice.getName());
			}
			deviceSpinner = new Spinner(MainMenuActivity.this);
			bluetoothBuilder.setView(deviceSpinner);
			bluetoothBuilder.setCancelable(true);
			if (bluetooth.isHost() == true && chosenDevice != null)
			{
				bluetoothBuilder.setPositiveButton("Launch " + DBInterface.Game.values()[currentGame].toString(), 
						bluetooth_dialog_listener);
			}
			bluetoothBuilder.setNeutralButton("Find Opponents", bluetooth_dialog_listener);
			bluetoothBuilder.setNegativeButton("Connect to Opponent", bluetooth_dialog_listener);
			
			bluetoothDialog = bluetoothBuilder.show();
			bluetoothDialog.setCanceledOnTouchOutside(true);
			
			/*
			if (bluetooth.isHost() == true && chosenDevice != null)
			{
				bluetoothDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(null);
			}
			bluetoothDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(null);
			bluetoothDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(null);
			*/
		}
	};
	
	private DialogInterface.OnClickListener bluetooth_dialog_listener = new DialogInterface.OnClickListener()
	{
		@Override
		public void onClick(DialogInterface dialog, int which) 
		{
			switch (which)
			{
			case DialogInterface.BUTTON_POSITIVE:
			{
				//Launch game, if possible
				if (chosenDevice != null)
				{
					bluetoothDialog.dismiss();
					launchBluetoothGame(bluetooth.isHost());
				}
				break;
			}
			case DialogInterface.BUTTON_NEUTRAL:
			{
				bluetooth.stopThreads();
				chosenDevice = null;
				startDiscovery();
				break;
			}
			case DialogInterface.BUTTON_NEGATIVE:
			{
				if (availableDevices.size() > 0 && deviceSpinner.getAdapter() != null && deviceSpinner.getAdapter().getCount() > 0)
				{
					BluetoothDevice device = availableDevices.get(deviceSpinner.getSelectedItemPosition());
					bluetooth.connect(device);
				}
				else
				{
					Log.d("Bluetooth Logs", "Tried to connect with no devices available.");
				}
				break;
			}
			}
		}
	};
	
	private void launchBluetoothGame(boolean isHost)
	{
		activityShift = true;
		
		String className;
		Intent intent;
		try 
		{
			if (opponentName == null || opponentName == "")
			{
				Log.d("Main Menu", "Error: opponent's name was not received over Bluetooth");
				opponentName = chosenDevice.getName();
			}
			if (bluetooth.isHost() == true)
			{
				Log.d("Main Menu", "Launching as host");
				className = getClassName(DBInterface.Game.values()[currentGame]);
				intent = new Intent(MainMenuActivity.this, Class.forName(className));
				String extraInfo = "";
				intent.putExtra(MainMenuActivity.BASE_PLAYER_FILENAME_KEY + "1", 
						players[currentPlayers[0]]);
				intent.putExtra(MainMenuActivity.BASE_PLAYER_FILENAME_KEY + "2", 
						opponentName);
				extraInfo = players[currentPlayers[0]] + DBInterface.GRID_ROW_SEPARATOR;
				extraInfo += currentGame + DBInterface.GRID_ITEM_SEPARATOR;
				if (extras != null)
				{
					for (int count = 0; count < extras.length; count++)
					{
						intent.putExtra(MainMenuActivity.EXTRA_STRING_BASE_KEY + (count + 1), 
								extras[count][extraSpinners[count].getSelectedItemPosition()]);
						extraInfo += extraSpinners[count].getSelectedItemPosition() + DBInterface.GRID_ITEM_SEPARATOR;
					}
				}
				if (boolExtras != null)
				{
					for (int count = 0; count < boolExtras.length; count++)
					{
						intent.putExtra(MainMenuActivity.EXTRA_BOOL_BASE_KEY + (count + 1), 
								extraCheckBoxes[count].isChecked());
						if (extraCheckBoxes[count].isChecked() == true)
						{
							extraInfo += "1" + DBInterface.GRID_ITEM_SEPARATOR;
						}
						else
						{
							extraInfo += "0" + DBInterface.GRID_ITEM_SEPARATOR;
						}
					}
				}
				//Drop the trailing marker and send the data to the other device.
				extraInfo = extraInfo.substring(0, extraInfo.length() - DBInterface.GRID_ITEM_SEPARATOR.length());
				bluetooth.write(extraInfo);
			}
			else
			{
				Log.d("Main Menu", "Launching as client");
				className = getClassName(DBInterface.Game.values()[gameOptionsFromHost[0]]);
				determineExtras();
				intent = new Intent(MainMenuActivity.this, Class.forName(className));
				
				intent.putExtra(MainMenuActivity.BASE_PLAYER_FILENAME_KEY + "1", 
						opponentName);
				intent.putExtra(MainMenuActivity.BASE_PLAYER_FILENAME_KEY + "2", 
						players[currentPlayers[0]]);
				int index = 1;
				if (extras != null)
				{
					for (int count = 0; count < extras.length; count++)
					{
						intent.putExtra(MainMenuActivity.EXTRA_STRING_BASE_KEY + (count + 1), 
								extras[count][gameOptionsFromHost[index]]);
						index++;
					}
				}
				if (boolExtras != null)
				{
					for (int count = 0; count < boolExtras.length; count++)
					{
						if (gameOptionsFromHost[index] == 1)
						{
							intent.putExtra(MainMenuActivity.EXTRA_BOOL_BASE_KEY + (count + 1), true);
						}
						else
						{
							intent.putExtra(MainMenuActivity.EXTRA_BOOL_BASE_KEY + (count + 1), false);
						}
						index++;
					}
				}
				bluetooth.write(players[currentPlayers[0]]);
			}
			
			if (bluetoothDialog != null && bluetoothDialog.isShowing())
			{
				bluetoothDialog.dismiss();
			}
			
			if (bluetooth.isHost() != isHost)
			{
				Log.d("Bluetooth Logs", "Error: bluetooth.isHost() != host parameter");
			}
			intent.putExtra(MainMenuActivity.OTHER_DEVICE_KEY, chosenDevice);
			intent.putExtra(MainMenuActivity.IS_HOST_KEY, bluetooth.isHost());
			intent.putExtra(MainMenuActivity.NEW_MATCH_KEY, true);
			intent.putExtra(MainMenuActivity.USING_BLUETOOTH_KEY, true);
			startActivity(intent);
			//MainMenuActivity.this.finish();
		} 
		catch (ClassNotFoundException e) 
		{
			Log.d("Main Menu", "Unable to find class name.");
			e.printStackTrace();
			activityShift = false;
		}
	}
	
	//A false return means that no conflicts were detected.
	public boolean checkPlayerConflicts()
	{
		boolean result = false;
		//Compare each index of currentPlayers to all other indexes.
		//		If, at any point, an equality check passes, return true.
		//		If all values are different, return false.
		
		for (int count = 0; count < playerLimit && result == false; count++)
		{
			for (int count2 = playerLimit - 1; count2 > count && result == false; count2--)
			{
				if (currentPlayers[count] == currentPlayers[count2])
				{
					result = true;
				}
			}
		}
		return result;
	}
	
	private String getClassName(DBInterface.Game game)
	{
		String result = getPackageName() + ".";
		result += DBInterface.Game.values()[currentGame].toString();
		result += DBInterface.ACTIVITY_SUFFIX;
		return result;
	}
	
	private OnClickListener loadGameListener = new OnClickListener() 
	{
		@Override
		public void onClick(View v) 
		{
			activityShift = true;
			String className = getClassName(DBInterface.Game.values()[currentGame]);
			Intent intent;
			if (filename != null)
			{
				try 
				{
					intent = new Intent(MainMenuActivity.this, Class.forName(className));
					intent.putExtra(MainMenuActivity.GAME_FILENAME_KEY, filename);
					intent.putExtra(MainMenuActivity.NEW_MATCH_KEY, false);
					startActivity(intent);
					//MainMenuActivity.this.finish();
				} 
				catch (ClassNotFoundException e) 
				{
					Log.d("Main Menu", "Unable to find class name.");
					e.printStackTrace();
				}
			}
		}
	};
	
	private OnClickListener deleteGameListener = new OnClickListener() 
	{
		@Override
		public void onClick(View v) 
		{
			if (filename != null)
			{
				String temp = "If you delete this save file";
				temp += ", it cannot be undone.";
				temp += "\nAre you certain you wish to permanently delete this save: " + filename;
				AlertDialog.Builder deleteSaveFileBuilder = new AlertDialog.Builder(MainMenuActivity.this);
				deleteSaveFileBuilder.setTitle("Warning!");
				deleteSaveFileBuilder.setMessage(temp);
				deleteSaveFileBuilder.setCancelable(true);
				deleteSaveFileBuilder.setPositiveButton("Cancel", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which) 
					{
						deleteSaveDialog.dismiss();
					}
				});
				deleteSaveFileBuilder.setNegativeButton("Delete save file", confirmDeleteGame);
				
				deleteSaveDialog = deleteSaveFileBuilder.create();
				deleteSaveDialog.show();
			}
		}
	};
	
	private DialogInterface.OnClickListener confirmDeleteGame = new DialogInterface.OnClickListener() 
	{
		public void onClick(DialogInterface dialog,int id) 
		{
			if (filename != null)
			{
				DBInterface.deleteSave(getApplicationContext(), currentFiles[currentSaveFile], 
						DBInterface.Game.values()[currentGame]);
				currentFiles[currentSaveFile] = null;
				addSaveFilesToSpinner();
			}
			deleteSaveDialog.dismiss();
		}
	};
	
	/*
	private DialogInterface.OnClickListener viewStandings = new DialogInterface.OnClickListener() 
	{
		public void onClick(DialogInterface dialog,int id) 
		{
			activityShift = true;
			dialog.dismiss();
			Intent intent = new Intent(MainMenuActivity.this, ViewStandingsActivity.class);
			startActivity(intent);
			MainMenuActivity.this.finish();
		}
	};
	*/
	
	
	
	@Override
	protected void onPause() 
	{
		// TODO Auto-generated method stub
		super.onPause();
		Log.d("Life Cycle", "Main Activity: onPause");
		//This method turns up when something partially obscures the activity.
		//A semi-transparent dialog box, for instance, would trigger this.
		//In most cases, however, the app will continue to onStop().
		//Release unneeded resources here, save data, etc.
		if (isFinishing() == true)
		{
			Log.d("Life Cycle", "isFinishing() in MainMenuActivity (onPause) returned true.");
		}
		else
		{
			if (chosenDevice != null)
			{
				getIntent().putExtra(MainMenuActivity.OTHER_DEVICE_KEY, chosenDevice);
			}
		}
	}

	//The onStop() function is used to dismiss any dialogs that may have been created.
	@Override
	protected void onStop() 
	{
		super.onStop();
		if (mainMenuDialog != null && mainMenuDialog.isShowing())
		{
	        mainMenuDialog.dismiss();
	    }
		activityShift = false;
		Log.d("Life Cycle", "Main Activity: onStop");
		if (isFinishing() == true)
		{
			Log.d("Life Cycle", "isFinishing() in MainMenuActivity (onStop) returned true.");
		}
		//This method turns up when the activity is hidden, like when the user switches to another app.
		//Release all unneeded resources here, as the system may occasionally skip the onDestroy() if memory is exhausted.
		//Also save any data necessary.
	}

	@Override
	protected void onDestroy() 
	{
		super.onDestroy();
		Log.d("Life Cycle", "Main Activity: onDestroy");
		if (isFinishing() == true)
		{
			Log.d("Life Cycle", "isFinishing() in MainMenuActivity (onDestroy) returned true.");
		}
		//Note that the app is destroyed and recreated whenever the orientation changes.
	}

	@Override
	protected void onRestart() 
	{
		super.onRestart();
		Log.d("Life Cycle", "Main Activity: onRestart");
		//This is called when the activity is being resumed from onStop().
		//It then goes to onStart() and onResume().
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_menu_about) 
        {
        	ActionMenu.displayAboutDialog(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onDiscoveryEnabled(int howLong)
    {
    	super.onDiscoveryEnabled(howLong);
    	bluetooth_match_listener.onClick(null);
    }
    
    @Override
    protected void onConnection(BluetoothDevice device)
    {
    	Log.d("Bluetooth Logs", "onConnection() in Main menu");
    	if (bluetooth.isHost() == false)
    	{
    		bluetooth.write(players[currentPlayers[0]] + DBInterface.GRID_ROW_SEPARATOR);
    	}
    	
    	chosenDevice = device;
    	Toast.makeText(this, "Connection formed with: " + chosenDevice.getName(), Toast.LENGTH_SHORT).show();
    	bluetooth_match_listener.onClick(null);
    }
    
    @Override
    protected void onDeviceFound(Context context, Intent intent)
    {
    	super.onDeviceFound(context, intent);
    	if (deviceSpinner != null)
    	{
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(
					MainMenuActivity.this, android.R.layout.simple_spinner_item, deviceNames);
		    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			deviceSpinner.setAdapter(adapter);
    	}
    }

	@Override
	protected void onWrite(String data) 
	{
		Log.d("Bluetooth Logs", "MainMenu wrote: " + data);
	}


	@Override
	protected void onRead(String data) 
	{
		Log.d("Bluetooth Logs", "MainMenu read: " + data);
		String[] dataPieces = data.split(DBInterface.GRID_ROW_SEPARATOR);
		opponentName = dataPieces[0];
		try
		{
			String[] values = dataPieces[1].split(DBInterface.GRID_ITEM_SEPARATOR);
			if (values.length > 1)
			{
				currentGame = Integer.parseInt(values[0]);
				setExtraViews();
				gameOptionsFromHost = new int[values.length];
				gameOptionsFromHost[0] = currentGame;
				for (int count = 1; count < values.length; count++)
				{
					gameOptionsFromHost[count] = Integer.parseInt(values[count]);
				}
				launchBluetoothGame(false);
			}
			else
			{
				Log.d("Bluetooth Logs", "Read info was taken as a name in MainMenu.");
				opponentName = data;
			}
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			Log.d("Main Menu", "Out of bounds in onRead()");
		}
	}
	
	@Override
	protected void onConnectionLost()
	{
		bluetooth.stopThreads();
		chosenDevice = null;
		if (bluetoothDialog != null && bluetoothDialog.isShowing() == true)
		{
			bluetoothDialog.dismiss();
		}
	}
}
