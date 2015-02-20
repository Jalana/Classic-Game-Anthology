package tru.kyle.classicgameanthology;

import java.util.ArrayList;
import java.util.List;

import tru.kyle.classicgameanthology.FileSaver.Game;
import tru.kyle.databases.DBInterface;

import android.app.Activity;
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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;

public class PlayerManagementActivity extends Activity 
{
	Button addPlayer;
	Button removePlayer;
	Button clearStandings;
	Button goBack;
	
	TextView currentPlayerView;
	TextView globalWins;
	TextView globalMatches;
	TextView localWins;
	TextView localMatches;
	
	Spinner playerList;
	Spinner gameList;
	
	AlertDialog clearStandingsDialog;
	AlertDialog deletePlayerDialog;
	AlertDialog addPlayerDialog;
	AlertDialog overwriteDialog;
	
	EditText addPlayerName;
	
	int playerCount = 0;
	int currentPlayer = 0;
	int currentGame = 0;
	Player[] players;
	
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
		setContentView(R.layout.activity_player_management);
		
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
		
		addPlayer = (Button) findViewById(R.id.addPlayer);
		removePlayer = (Button) findViewById(R.id.deletePlayer);
		clearStandings = (Button) findViewById(R.id.player_management_clearStandings);
		goBack = (Button) findViewById(R.id.player_management_return);
		
		addPlayer.setOnClickListener(add_player_listener);
		removePlayer.setOnClickListener(delete_player_listener);
		clearStandings.setOnClickListener(clear_standings_listener);
		goBack.setOnClickListener(return_listener);
		
		currentPlayerView = (TextView) findViewById(R.id.player_management_currentPlayer);
		globalWins = (TextView) findViewById(R.id.player_management_globalWins);
		localWins = (TextView) findViewById(R.id.player_management_localWins);
		globalMatches = (TextView) findViewById(R.id.player_management_globalMatches);
		localMatches = (TextView) findViewById(R.id.player_management_localMatches);
		
		playerList = (Spinner) findViewById(R.id.playerSpinner);
		gameList = (Spinner) findViewById(R.id.player_management_gameSpinner);
		addPlayersToSpinner();
		addGamesToSpinner();
		playerList.setOnItemSelectedListener(playerSpinnerListener);
		gameList.setOnItemSelectedListener(gameSpinnerListener);
		
		if (playerCount <= 4)
		{
			removePlayer.setVisibility(View.GONE);
		}
		else
		{
			removePlayer.setVisibility(View.VISIBLE);
		}
		
		currentPlayerView.setVisibility(View.GONE);
		
		updateViews();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.player_management, menu);
		return true;
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
	
	
	
	
	public void addPlayersToSpinner()
    {
		List<String> spinnerArray = new ArrayList<String>();
        int tempCount;
        playerCount = 0;
        
        String[] playerNames = DBInterface.getNames(getApplicationContext(), null);
        players = new Player[playerNames.length];
        for (tempCount = 0; tempCount < playerNames.length; tempCount++)
		{
        	if (playerNames[tempCount] != null)
        	{
        		spinnerArray.add(playerNames[tempCount]);
        		Log.d("PlayerList", playerNames[tempCount]);
        		playerCount++;
        	}
		}
        players = new Player[playerCount];
        int otherCount = 0;
        for (tempCount = 0; tempCount < playerNames.length; tempCount++)
		{
        	if (playerNames[tempCount] != null)
        	{
        		players[otherCount] = new Player(DBInterface.retrieveSave(
        				getApplicationContext(), playerNames[tempCount], null));
        		otherCount++;
        	}
		}
        
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, bigSpinnerItemID, spinnerArray);
	    adapter.setDropDownViewResource(bigDropDownItemID);
	    playerList.setAdapter(adapter);
    }
	
	public void addGamesToSpinner()
    {
    	List<String> spinnerArray = new ArrayList<String>();
        
    	Game x;
    	int tempCount;
    	try
		{
			for (tempCount = 0; ; tempCount++)
			{
				x = Game.values()[tempCount];
				spinnerArray.add(x.toString());
				//Log.d("Games", x.toString());
			}
		}
		catch(Exception e)
		{
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, smallSpinnerItemID, spinnerArray);
		    adapter.setDropDownViewResource(smallDropDownItemID);
	        gameList.setAdapter(adapter);
		}
    }
	
	
	
	public void updateViews()
	{
		if (players[currentPlayer] != null)
		{
			int playerGlobalWins = players[currentPlayer].getGlobalWins();
			int playerGlobalMatches = players[currentPlayer].getGlobalMatches();
			int playerLocalWins = players[currentPlayer].getGameWins(currentGame);
			int playerLocalMatches = players[currentPlayer].getGameMatches(currentGame);
			
			currentPlayerView.setText(" " + players[currentPlayer].getName() + " ");
			globalWins.setText(" Total wins: " + playerGlobalWins + " ");
			globalMatches.setText(" Total matches: " + playerGlobalMatches + " ");
			localWins.setText(" " + FileSaver.Game.values()[currentGame].toString() + " wins: " + playerLocalWins + " ");
			localMatches.setText(" " + FileSaver.Game.values()[currentGame].toString() + " matches: " + playerLocalMatches + " ");
		}
	}
	
	
	
	private OnClickListener return_listener = new OnClickListener()
	{
		@Override
		public void onClick(View v) 
		{
			Intent intent = new Intent(PlayerManagementActivity.this, MainMenuActivity.class);
			startActivity(intent);
			PlayerManagementActivity.this.finish();
		}
	};
	
	private OnClickListener clear_standings_listener = new OnClickListener()
	{
		@Override
		public void onClick(View v) 
		{
			String temp = "If you clear " + players[currentPlayer].getName();
			temp += "'s standings, you cannot recover them.";
			temp += "\nAre you certain you wish to clear " + players[currentPlayer].getName() + "'s standings?";
			AlertDialog.Builder standingsBuilder = new AlertDialog.Builder(PlayerManagementActivity.this);
			
			standingsBuilder.setTitle("Warning!");
			standingsBuilder.setMessage(temp);
			standingsBuilder.setCancelable(true);
			standingsBuilder.setPositiveButton("Cancel", closeDialog);
			standingsBuilder.setNegativeButton("Clear your standings", confirmClear);
			
			clearStandingsDialog = standingsBuilder.create();
			clearStandingsDialog.show();
		}
	};
	
	private DialogInterface.OnClickListener closeDialog = new DialogInterface.OnClickListener() 
	{
		public void onClick(DialogInterface dialog,int id) 
		{
			addPlayerName = null;
			dialog.dismiss();
		}
	};
	
	private DialogInterface.OnClickListener confirmClear = new DialogInterface.OnClickListener() 
	{
		public void onClick(DialogInterface dialog,int id) 
		{
			ContentValues values = new ContentValues();
			values.put(DBInterface.PLAYER_NAME_KEY, players[currentPlayer].getName());
			DBInterface.insertSave(getApplicationContext(), values, null, true);
			addPlayersToSpinner();
			addGamesToSpinner();
			updateViews();
			clearStandingsDialog.dismiss();
		}
	};
	
	private OnClickListener add_player_listener = new OnClickListener()
	{
		@Override
		public void onClick(View v) 
		{
			//Launch a dialog that prompts for the new player's name.
			//Upon confirmation, expand the players[] array by one index, then add the new player.
			//		Write all players to file after that.
			//Have two buttons: confirm and cancel. The dialog should be cancelable.
			
			//playerCount++;
			//if (playerCount > 4)
			//{
			//	removePlayer.setVisibility(View.VISIBLE);
			//}
			String temp = "Enter the name of the new player:";
			AlertDialog.Builder addPlayerBuilder = new AlertDialog.Builder(PlayerManagementActivity.this);
			addPlayerName = new EditText(PlayerManagementActivity.this);
			addPlayerBuilder.setView(addPlayerName);
			addPlayerBuilder.setTitle("New player?");
			addPlayerBuilder.setMessage(temp);
			addPlayerBuilder.setCancelable(true);
			addPlayerBuilder.setPositiveButton("Cancel", closeDialog);
			addPlayerBuilder.setNegativeButton("Add player", confirmAdd);
			
			addPlayerDialog = addPlayerBuilder.create();
			addPlayerDialog.show();
		}
	};
	
	private OnClickListener delete_player_listener = new OnClickListener()
	{
		@Override
		public void onClick(View v) 
		{
			if (playerCount > 4)
			{
				String temp = "If you delete " + players[currentPlayer].getName();
				temp += ", any saved games with them may become corrupted.";
				temp += "\nAre you certain you wish to permanently delete " + players[currentPlayer].getName() + "?";
				AlertDialog.Builder deletePlayerBuilder = new AlertDialog.Builder(PlayerManagementActivity.this);
				deletePlayerBuilder.setTitle("Warning!");
				deletePlayerBuilder.setMessage(temp);
				deletePlayerBuilder.setCancelable(true);
				deletePlayerBuilder.setPositiveButton("Cancel", closeDialog);
				deletePlayerBuilder.setNegativeButton("Delete player " + players[currentPlayer].getName(), confirmDelete);
				
				deletePlayerDialog = deletePlayerBuilder.create();
				deletePlayerDialog.show();
			}
		}
	};
	
	private DialogInterface.OnClickListener confirmDelete = new DialogInterface.OnClickListener() 
	{
		public void onClick(DialogInterface dialog,int id) 
		{
			DBInterface.deleteSave(getApplicationContext(), players[currentPlayer].getName(), null);
			addPlayersToSpinner();
			addGamesToSpinner();
			
			currentPlayer = 0;
			updateViews();
			if (playerCount <= 4)
			{
				removePlayer.setVisibility(View.GONE);
			}
			else
			{
				removePlayer.setVisibility(View.VISIBLE);
			}
			deletePlayerDialog.dismiss();
		}
	};
	
	private DialogInterface.OnClickListener confirmAdd = new DialogInterface.OnClickListener() 
	{
		public void onClick(DialogInterface dialog,int id) 
		{
			if (addPlayerName != null)
			{
				String newName = addPlayerName.getText().toString();
				newName = newName.replaceAll("[^a-zA-Z_0-9 ]", "");
				if (newName != null && newName != "")
				{
					ContentValues values = new ContentValues();
					values.put(DBInterface.PLAYER_NAME_KEY, newName);
					boolean didSave = DBInterface.insertSave(getApplicationContext(), values, null, false);
					if (didSave == false)
					{
						confirmOverwrite(newName);
					}
					else
					{
						addPlayersToSpinner();
						addGamesToSpinner();
						updateViews();
						if (playerCount <= 4)
						{
							removePlayer.setVisibility(View.GONE);
						}
						else
						{
							removePlayer.setVisibility(View.VISIBLE);
						}
						addPlayerName = null;
						addPlayerDialog.dismiss();
					}
				}
			}
		}
	};
	
	public void confirmOverwrite(final String name)
    {
    	AlertDialog.Builder overwriteBuilder = new AlertDialog.Builder(this);
		overwriteBuilder.setTitle("The name \n" + name + "\n is already in use");
		overwriteBuilder.setMessage("Do you want to overwrite the previous player?");
		overwriteBuilder.setCancelable(true);
		overwriteBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog,int id) 
			{
				ContentValues values = new ContentValues();
				values.put(DBInterface.PLAYER_NAME_KEY, name);
				DBInterface.insertSave(getApplicationContext(), values, null, true);
				
				addPlayersToSpinner();
				addGamesToSpinner();
				updateViews();
				if (playerCount <= 4)
				{
					removePlayer.setVisibility(View.GONE);
				}
				else
				{
					removePlayer.setVisibility(View.VISIBLE);
				}
				addPlayerName = null;
				addPlayerDialog.dismiss();
				overwriteDialog.dismiss();
			}
		});
		overwriteBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog,int id) 
			{
				overwriteDialog.dismiss();
			}
		});
		
		overwriteDialog = overwriteBuilder.create();
		overwriteDialog.show();
    }
	
	
	
	private OnItemSelectedListener playerSpinnerListener = new OnItemSelectedListener()
	{
		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) 
		{
			if (view != null)
        	{
				currentPlayer = position;
				updateViews();
        	}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) 
		{
			
		}
	};
	
	private OnItemSelectedListener gameSpinnerListener = new OnItemSelectedListener()
	{
		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) 
		{
			if (view != null)
        	{
				currentGame = position;
				updateViews();
        	}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) 
		{
			
		}
	};
	
	
	
	
	
	
}
