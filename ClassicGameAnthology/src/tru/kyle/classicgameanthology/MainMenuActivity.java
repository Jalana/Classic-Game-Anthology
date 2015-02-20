package tru.kyle.classicgameanthology;

//Try changing the theme of an activity within the manifest file.
//Changing it to a dialog, for instance, could avoid using the full screen for simply entering names.
//android:theme="@android:style/Theme.Dialog" is one example

/*
BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
if (bluetooth.isEnabled()) 
{
	String deviceName = bluetooth.getName();
	String macAddress = bluetooth.getAddress();
} 
else 
{
	final int ENABLE_BLUETOOTH = 12345;
	Intent enableBluetooth =
	new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	startActivityForResult(enableBluetooth, ENABLE_BLUETOOTH);
}

final int REQUEST_DISCOVERABLE = 12346;
Intent discoverable = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
discoverable.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
startActivityForResult(discoverable, REQUEST_DISCOVERABLE);

@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) 
{
	switch (requestCode)
	{
	case ENABLE_BLUETOOTH:
	{
		if (responseCode == RESULT_OK) 
		{
			BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
			String deviceName = bluetooth.getName();
			String macAddress = bluetooth.getAddress();
			// do something bluetoothy
		}
		break;
	}
	case REQUEST_DISCOVERABLE:
	{
		if (responseCode == RESULT_CANCELLED) 
		{
			// go without Bluetooth?
		} 
		else 
		{
			int howLong = responseCode;
			// we'll be discoverable for howLong seconds
		}
		break;
	}
	}
}

if (bluetooth.isEnabled()) 
{
	if (!bluetooth.isDiscovering()) 
	{
		bluetooth.startDiscovery(); // non-blocking
		BroadcastReceiver deviceFoundRcvr = new BroadcastReceiver() 
		{
			@Override
			public void onReceive(Context context, Intent intent) 
			{
				if (intent.getAction() == BluetoothDevice.ACTION_FOUND) 
				{
					String deviceName = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
					BluetoothDevice device =
					intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					// do whatever with the device
				}
			}
		};
	}
}

Set<BluetoothDevice> pairedDevices = bluetooth.getBondedDevices();
if (pairedDevices.size() > 0) 
{
	for (BluetoothDevice device : pairedDevices) 
	{
		// do something with the device
	}
}

String name = "myBluetoothServer";
UUID uuid = ...;
BluetoothServerSocket serverSocket = bluetooth.listenUsingRfcommWithServiceRecord(name, uuid);
BluetoothSocket socket = serverSocket.accept(); // blocking
… use 'socket' to communicate

	//Listening for connection
String name = "myBluetoothServer";
UUID uuid = ...;
try 
{
	BluetoothServerSocket serverSocket =
	bluetooth.listenUsingRfcommWithServiceRecord(name, uuid);
	Thread bgThread = new Thread(new Runnable() 
	{
		try 
		{
			BluetoothSocket socket = serverSocket.accept();
			BufferedReader in = new BufferedReader(new
			InputStreamReader(socket.getInputStream()));
			PrintWriter out = new PrintWriter(socket.getOutputStream());
			// Assuming a text-based communication protocol:
			// read from socket using in.readLine()
			// write to socket using out.println()
			socket.close();
			serverSocket.close(); // assuming we are done accepting conns
		} 
		catch (IOException e) 
		{ … }
	});
} 
catch (IOException e) 
{ … }

	//Establishing connection
UUID uuid = ...;
Thread bgThread = new Thread(new Runnable() 
{
	try 
	{
		BluetoothSocket socket =
		device.createRfcommSocketToServiceRecord(uuid);
		bluetooth.cancelDiscovery();
		BufferedReader in = new BufferedReader(new
		InputStreamReader(socket.getInputStream()));
		PrintWriter out = new PrintWriter(socket.getOutputStream());
		// Assuming a text-based communication protocol:
		// read from socket using in.readLine()
		// write to socket using out.println()
		socket.close();
	} 
	catch (IOException e) 
	{ … }
});

 */

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import tru.kyle.databases.DBInterface;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;


public class MainMenuActivity extends Activity 
{
	public final static String PACKAGE_NAME = "tru.kyle.classicgameanthology";
	public final static String GAME_FILENAME_KEY = PACKAGE_NAME + ".gameFilename";
	public final static String BASE_PLAYER_FILENAME_KEY = PACKAGE_NAME + ".player_";
	public final static String NEW_MATCH_KEY = PACKAGE_NAME + ".newMatch";
	public final static String EXTRA_STRING_BASE_KEY = PACKAGE_NAME + ".extra_string_";
	public final static String EXTRA_BOOL_BASE_KEY = PACKAGE_NAME + ".extra_bool_";
	
	//These are keys for specific methods in the game activities.
	public final static String GET_PLAYERS_METHOD = "getPlayerCounts";
	public final static String GET_EXTRAS_METHOD = "getExtras";
	public final static String GET_BOOLS_METHOD = "getBoolExtras";
	
	
	AlertDialog mainMenuDialog;
	AlertDialog deleteSaveDialog;
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
	
	Button newMatch;
	Button savedGames;
	Button deleteSave;
	Button goToPlayers;
	
	Player[] players;
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
    	
    	players = FileSaver.getPlayerList(getApplicationContext());
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
    	newMatch.setOnClickListener(newMatchListener);
    	savedGames.setOnClickListener(loadGameListener);
    	goToPlayers.setOnClickListener(goToPlayersListener);
    	deleteSave.setOnClickListener(deleteGameListener);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
	protected void onStart() 
    {
		// TODO Auto-generated method stub
		super.onStart();
		Log.d("Life Cycle", "Main Activity: onStart");
		//This is called between onCreate() and onResume().
		//onRestart() also leads to this.
	}
    
	@Override
	protected void onResume() 
	{
		// TODO Auto-generated method stub
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
        String gameName = getClassName(FileSaver.Game.values()[currentGame]);
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
        int limit = FileSaver.Game.values().length;
        
        for (tempCount = 0; tempCount < limit; tempCount++)
		{
        	spinnerArray.add(FileSaver.Game.values()[tempCount].toString());
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
	        		spinnerArray.add(players[tempCount].getName());
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
		currentFiles = DBInterface.getNames(getApplicationContext(), FileSaver.Game.values()[currentGame]);
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
				
				String className = getPackageName() + ".";
				className += FileSaver.Game.values()[currentGame].toString();
				className += FileSaver.ACTIVITY_SUFFIX;
				Intent intent;
				try 
				{
					intent = new Intent(MainMenuActivity.this, Class.forName(className));
					for (int count = 0; count < currentPlayers.length; count++)
					{
						intent.putExtra(MainMenuActivity.BASE_PLAYER_FILENAME_KEY + (count + 1), 
								players[currentPlayers[count]].getName());
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
	
	private String getClassName(FileSaver.Game game)
	{
		String result = getPackageName() + ".";
		result += FileSaver.Game.values()[currentGame].toString();
		result += FileSaver.ACTIVITY_SUFFIX;
		return result;
	}
	
	private OnClickListener loadGameListener = new OnClickListener() 
	{
		@Override
		public void onClick(View v) 
		{
			activityShift = true;
			String className = getClassName(FileSaver.Game.values()[currentGame]);
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
				FileSaver.deleteSave(getApplicationContext(), currentFiles[currentSaveFile], 
						FileSaver.SaveType.GAMES, FileSaver.Game.values()[currentGame]);
				currentFiles[currentSaveFile] = null;
				FileSaver.writeFilenames(getApplicationContext(), currentFiles, FileSaver.Game.values()[currentGame]);
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
	}

	//The onStop() function is used to dismiss any dialogs that may have been created.
	@Override
	protected void onStop() 
	{
		// TODO Auto-generated method stub
		super.onStop();
		if (mainMenuDialog != null && mainMenuDialog.isShowing())
		{
	        mainMenuDialog.dismiss();
	    }
		activityShift = false;
		Log.d("Life Cycle", "Main Activity: onStop");
		//This method turns up when the activity is hidden, like when the user switches to another app.
		//Release all unneeded resources here, as the system may occasionally skip the onDestroy() if memory is exhausted.
		//Also save any data necessary.
	}

	@Override
	protected void onDestroy() 
	{
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.d("Life Cycle", "Main Activity: onDestroy");
		//Note that the app is destroyed and recreated whenever the orientation changes.
	}

	@Override
	protected void onRestart() 
	{
		// TODO Auto-generated method stub
		super.onRestart();
		Log.d("Life Cycle", "Main Activity: onRestart");
		//This is called when the activity is being resumed from onStop().
		//It then goes to onStart() and onResume().
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) 
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
