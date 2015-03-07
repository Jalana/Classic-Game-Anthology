package tru.kyle.classicgameanthology;

import java.util.ArrayList;

import tru.kyle.classicgameanthology.FileSaver.Game;
import tru.kyle.classicgameanthology.FileSaver.GameByLayout;
import tru.kyle.databases.DBInterface;
import tru.kyle.mylists.MyQueue;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.DragEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/*
 * Critical note: due to the nature of this game (incomplete information),
 * Stratego will most likely require network play or AI functionality
 * to be properly playable, as keeping one player's assets hidden from the
 * other player reliably while sharing a single screen is unlikely to be 
 * practical.
 */


public class StrategoActivity extends Activity 
{
	protected static final String[][] EXTRAS = null;
	
	//These are pulled by the MainMenuActivity and used to title checkboxes.
	private static final String REVEALS = "Permanent Reveals?";
	private static final String HIGHLIGHTS = "Highlight Moves?";
	protected static final String[] BOOLEAN_EXTRAS = {
		REVEALS,
		HIGHLIGHTS
	};
	
	public static final Integer[] PLAYERS = {2};
	public final Game THIS_GAME = Game.Stratego;
	public final GameByLayout THIS_LAYOUT = GameByLayout.stratego;
	private final String BUTTON_BACK_EMPTY_STRING = "button_space_empty_" + THIS_LAYOUT.toString();
	private final String BUTTON_BACK_P_1_STRING = "button_space_red_" + THIS_LAYOUT.toString();
	private final String BUTTON_BACK_P_2_STRING = "button_space_blue_" + THIS_LAYOUT.toString();
	private final String BUTTON_BACK_MOVABLE_EMPTY_STRING = "button_space_movable_empty_" + THIS_LAYOUT.toString();
	private final String BUTTON_BACK_MOVABLE_P_1_STRING = "button_space_movable_red_" + THIS_LAYOUT.toString();
	private final String BUTTON_BACK_MOVABLE_P_2_STRING = "button_space_movable_blue_" + THIS_LAYOUT.toString();
	
	private final String BUTTON_BACK_LAKE_STRING = "button_space_lake_" + THIS_LAYOUT.toString();
	
	public static final int VERTICAL_LIMIT = 10;
	public static final int HORIZONTAL_LIMIT = 10;
	static final int INVALID = Integer.MIN_VALUE;
	static final int VALID_P = Integer.MAX_VALUE;
	static final int PLACE_1 = Integer.MAX_VALUE - 1;
	static final int PLACE_2 = Integer.MAX_VALUE - 2;
	
	static final char UNKNOWN_MARK = '?';
	static final char BLANK_MARK = ' ';
	private final static String RANK_DRAG_KEY = "rank_value";
	
	
	boolean gameInProgress = false;
	boolean placementInProgress = true;
	boolean gameEnded = false;
	boolean usingGuestNames = false;
	boolean placementListenersSet = false;
	
	int currentTurn = 1;
	int turnCount;
	int playerOneScore;
	int playerTwoScore;
	
	TextView playerOneDisplay;
	TextView playerTwoDisplay;
	TextView activePlayerDisplay;
	
	MediaPlayer soundPlayer;
	RelativeLayout mainLayout;
	
	Player playerOne;
	Player playerTwo;
	String playerOneName;
	String playerTwoName;
	
	int playerOneColour = Color.RED;
	int playerTwoColour = Color.BLUE;
	//The empty 0 index is designed to allow currentTurn to retrieve the value directly.
	int[] remainingPieces = {0, 40, 40};
	
	Button[] placementButtons = new Button[StrategoPiece.RankValues.values().length];
	Button[] hintButtons = new Button[StrategoPiece.RankValues.values().length];
	
	StrategoPiece[][] gridPieces = new StrategoPiece[VERTICAL_LIMIT][HORIZONTAL_LIMIT];
	Button[][] gridButtons = new Button[VERTICAL_LIMIT][HORIZONTAL_LIMIT];
	private StrategoPiece currentAttacker = null;
	Button[] availableLocationButtons;
	Drawable[] availableLocationOldBackgrounds;
	
	//This checks for invalid locations, since the standard Stratego board has two lakes near the center
	//	that are unusable for movement.
	//If an option is ever added to allow variable grid sizes, multiple variations of this array
	//	may be required.
	private int[][] movementGridCheck = CLASSIC_GRID_MOVE_CHECK;
	private int[] pieceLimits = CLASSIC_PIECE_LIMITS;
	
	private static final int[][] CLASSIC_GRID_MOVE_CHECK = {
			{PLACE_1, PLACE_1, PLACE_1, PLACE_1, PLACE_1, PLACE_1, PLACE_1, PLACE_1, PLACE_1, PLACE_1},
			{PLACE_1, PLACE_1, PLACE_1, PLACE_1, PLACE_1, PLACE_1, PLACE_1, PLACE_1, PLACE_1, PLACE_1},
			{PLACE_1, PLACE_1, PLACE_1, PLACE_1, PLACE_1, PLACE_1, PLACE_1, PLACE_1, PLACE_1, PLACE_1},
			{PLACE_1, PLACE_1, PLACE_1, PLACE_1, PLACE_1, PLACE_1, PLACE_1, PLACE_1, PLACE_1, PLACE_1},
			{VALID_P, VALID_P, INVALID, INVALID, VALID_P, VALID_P, INVALID, INVALID, VALID_P, VALID_P},
			{VALID_P, VALID_P, INVALID, INVALID, VALID_P, VALID_P, INVALID, INVALID, VALID_P, VALID_P},
			{PLACE_2, PLACE_2, PLACE_2, PLACE_2, PLACE_2, PLACE_2, PLACE_2, PLACE_2, PLACE_2, PLACE_2},
			{PLACE_2, PLACE_2, PLACE_2, PLACE_2, PLACE_2, PLACE_2, PLACE_2, PLACE_2, PLACE_2, PLACE_2},
			{PLACE_2, PLACE_2, PLACE_2, PLACE_2, PLACE_2, PLACE_2, PLACE_2, PLACE_2, PLACE_2, PLACE_2},
			{PLACE_2, PLACE_2, PLACE_2, PLACE_2, PLACE_2, PLACE_2, PLACE_2, PLACE_2, PLACE_2, PLACE_2}
	};
	private static final int[] CLASSIC_PIECE_LIMITS = {
		6, 	//Bombs
		1, 	//Marshalls
		1, 	//Generals
		2, 	//etc.
		3, 
		4, 
		4, 
		4, 
		5, 	//Miners
		8, 	//Scouts
		1, 	//Spies
		1	//Flags
	};
	
	private int[] playerOneAvailablePieces = new int[pieceLimits.length];
	private int[] playerTwoAvailablePieces = new int[pieceLimits.length];
	
	int stalemateCheck = 7;
	
	String filenameGame;
	
	AlertDialog endMatchDialog;
	AlertDialog saveDialog;
	AlertDialog overwriteDialog;
	AlertDialog confirmPlacementDialog;
	AlertDialog turnChangeDialog;
	
	Button previousMove;
	Button saveGame;
	
	boolean newMatch;
	boolean endOfMatch = false;
	boolean highlightMoves = true;
	boolean keepReveals = true;
	
	private Drawable BUTTON_BACK_EMPTY;
	private Drawable BUTTON_BACK_P_1;
	private Drawable BUTTON_BACK_P_2;
	private Drawable BUTTON_BACK_MOVABLE_EMPTY;
	private Drawable BUTTON_BACK_MOVABLE_P_1;
	private Drawable BUTTON_BACK_MOVABLE_P_2;
	
	private Drawable BUTTON_BACK_LAKE;
	
	protected int buttonSize;
	MyQueue<Button> tempMoves = new MyQueue<Button>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stratego);
		
		Resources res = getResources();
        BUTTON_BACK_EMPTY = res.getDrawable(res.getIdentifier(this.BUTTON_BACK_EMPTY_STRING, "drawable", getPackageName()));
    	BUTTON_BACK_P_1 = res.getDrawable(res.getIdentifier(this.BUTTON_BACK_P_1_STRING, "drawable", getPackageName()));
    	BUTTON_BACK_P_2 = res.getDrawable(res.getIdentifier(this.BUTTON_BACK_P_2_STRING, "drawable", getPackageName()));
    	BUTTON_BACK_MOVABLE_EMPTY = res.getDrawable(res.getIdentifier(this.BUTTON_BACK_MOVABLE_EMPTY_STRING, "drawable", getPackageName()));
    	BUTTON_BACK_MOVABLE_P_1 = res.getDrawable(res.getIdentifier(this.BUTTON_BACK_MOVABLE_P_1_STRING, "drawable", getPackageName()));
    	BUTTON_BACK_MOVABLE_P_2 = res.getDrawable(res.getIdentifier(this.BUTTON_BACK_MOVABLE_P_2_STRING, "drawable", getPackageName()));
    	BUTTON_BACK_LAKE = res.getDrawable(res.getIdentifier(this.BUTTON_BACK_LAKE_STRING, "drawable", getPackageName()));
    	
    	Intent intent = getIntent();
        filenameGame = intent.getStringExtra(MainMenuActivity.GAME_FILENAME_KEY);
        if (filenameGame == null)
        {
        	filenameGame = FileSaver.AUTOSAVE_NAME;
        }
    	newMatch = intent.getBooleanExtra(MainMenuActivity.NEW_MATCH_KEY, false);
    	intent.putExtra(MainMenuActivity.NEW_MATCH_KEY, false);
    	
    	playerOneName = intent.getStringExtra(MainMenuActivity.BASE_PLAYER_FILENAME_KEY + "1");
    	playerTwoName = intent.getStringExtra(MainMenuActivity.BASE_PLAYER_FILENAME_KEY + "2");
        
        playerOneDisplay = (TextView)findViewById(R.id.playerOne);
    	playerTwoDisplay = (TextView)findViewById(R.id.playerTwo);
    	activePlayerDisplay = (TextView)findViewById(R.id.activePlayer);
    	
    	saveGame = (Button) findViewById(R.id.saveGame);
    	saveGame.setOnClickListener(save_listener);
    	
    	if (newMatch == true)
    	{
    		currentTurn = 1;
    		parseExtras(intent);
    		setButtons(res);
    		resetData();
    		setUpPlacement(currentTurn, true);
    	}
    	else
    	{
    		loadGame();
    	}
    	
		Log.d("Life Cycle", "Stratego Activity: onCreate");
	}
	
	@Override
	protected void onStart() 
    {
		super.onStart();
		Log.d("Life Cycle", "Stratego Activity: onStart");
		//This is called between onCreate() and onResume().
		//onRestart() also leads to this.
	}

	//The onResume() function reads in data from files and sets it accordingly, then continues to the actual game.
	@Override
	protected void onResume() 
	{
		super.onResume();
		Log.d("Life Cycle", "Stratego Activity: onResume");
		//This method is called every time the activity hits the foreground.
		//This includes the first time the app is created.
		//Initialization of data should be here, especially if it was released.
		
		playerOneDisplay.setTextColor(playerOneColour);
		playerTwoDisplay.setTextColor(playerTwoColour);
		playerOneDisplay.setText(playerOneName);
		playerTwoDisplay.setText(playerTwoName);
		//This loop forces the display to be updated correctly.
		
		buttonSize = getButtonDimensions();
    	Log.d("Stratego Scaling", "buttonSize = " + buttonSize);
    	LayoutParams params;
		for (int count = 0; count < gridButtons.length; count++)
		{
			for (int count2 = 0; count2 < gridButtons[count].length; count2++)
			{
				params = gridButtons[count][count2].getLayoutParams();
				params.height = buttonSize;
				params.width = buttonSize;
				gridButtons[count][count2].setLayoutParams(params);
			}
		}
		
		for (int count = 0; count < hintButtons.length; count++)
		{
			params = hintButtons[count].getLayoutParams();
			params.height = buttonSize;
			params.width = buttonSize;
			hintButtons[count].setLayoutParams(params);
			params = placementButtons[count].getLayoutParams();
			params.height = buttonSize;
			params.width = buttonSize;
			placementButtons[count].setLayoutParams(params);
		}
		
		if (placementInProgress == false)
		{
			endPlacement();
			
			gameInProgress = true;
			int temp = currentTurn;
			swapTurn();
			while (currentTurn != temp)
			{
				swapTurn();
			}
		}
		soundPlayer = null;
	}
	
	//The onPause() function is used to save data from the game before it can be closed.
    //It also releases the media player if it is in use.
	@Override
	protected void onPause() 
	{
		// TODO Auto-generated method stub
		super.onPause();
		Log.d("Life Cycle", "Stratego Activity: onPause");
		//This method turns up when something partially obscures the activity.
		//A semi-transparent dialog box, for instance, would trigger this.
		//In most cases, however, the app will continue to onStop().
		//Release unneeded resources here, save data, etc.
		if (usingGuestNames == false && endOfMatch == false)
		{
			saveGame(FileSaver.AUTOSAVE_NAME, true);
		}
		if (soundPlayer != null)
		{
			soundPlayer.release();
		}
		
	}
	
	//The onStop() function is used to dismiss any dialogs that may have been created.
	@Override
	protected void onStop() 
	{
		// TODO Auto-generated method stub
		super.onStop();
		if (endMatchDialog != null && endMatchDialog.isShowing())
		{
	        endMatchDialog.dismiss();
	    }
		if (turnChangeDialog != null && turnChangeDialog.isShowing())
		{
			turnChangeDialog.dismiss();
			swapTurn();
			if (usingGuestNames == false && endOfMatch == false)
			{
				saveGame(FileSaver.AUTOSAVE_NAME, true);
			}
		}
		Log.d("Life Cycle", "Stratego Activity: onStop");
		//This method turns up when the activity is hidden, like when the user switches to another app.
		//Release all unneeded resources here, as the system may occasionally skip the onDestroy() if memory is exhausted.
		//Also save any data necessary.
	}

	@Override
	protected void onDestroy() 
	{
		// TODO Auto-generated method stub
		super.onDestroy();
		
		Log.d("Life Cycle", "Stratego Activity: onDestroy");
		//Note that the app is destroyed and recreated whenever the orientation changes.
	}

	@Override
	protected void onRestart() 
	{
		super.onRestart();
		Log.d("Life Cycle", "Stratego Activity: onRestart");
		//This is called when the activity is being resumed from onStop().
		//It then goes to onStart() and onResume().
	}
	
	//
	//	Other Initialization Functions
	//
	
	private void parseExtras(Intent intent)
	{
		keepReveals = intent.getBooleanExtra(MainMenuActivity.EXTRA_BOOL_BASE_KEY + "1", false);
		highlightMoves = intent.getBooleanExtra(MainMenuActivity.EXTRA_BOOL_BASE_KEY + "2", false);
	}
	
	
	//Note that Stratego has two lake areas near the center of the board.
	//Those must not have listeners registered for them.
	//Additionally, onTouch listeners must only be registered for the player's pieces,
	//		and the player needs to be able to set up pieces before play.
	private void setButtons(Resources res)
	{
		String currentID;
        int resID;
        for (int count = 0; count < gridButtons.length; count++)
        {
        	for (int count2 = 0; count2 < gridButtons[count].length; count2++)
        	{
        		currentID = "button_";
        		if ((count + 1) < 10)
        		{
        			currentID += '0';
        		}
        		currentID += (count + 1) + "_";
        		if ((count2 + 1) < 10)
        		{
        			currentID += '0';
        		}
        		currentID += (count2 + 1);
        		resID = res.getIdentifier(currentID, "id", getPackageName());
        		gridButtons[count][count2] = (Button)findViewById(resID);
        		//gridButtons[count][count2].setOnClickListener(grid_handler);
        		if (gameInProgress == false)
        		{
        			gridButtons[count][count2].setText(BLANK_MARK + "");
        		}
        		if (movementGridCheck[count][count2] == INVALID)
        		{
        			gridButtons[count][count2].setBackground(BUTTON_BACK_LAKE);
        		}
        	}
        }
        
        for (int count = 0; count < placementButtons.length; count++)
        {
    		currentID = "placement_button_";
    		if ((count) < 10)
    		{
    			currentID += '0';
    		}
    		currentID += (count);
    		resID = res.getIdentifier(currentID, "id", getPackageName());
    		placementButtons[count] = (Button)findViewById(resID);
    		if (placementInProgress == true && endOfMatch == false)
    		{
    			placementButtons[count].setOnDragListener(placement_drag_handler);
    		}
    		else
    		{
    			placementButtons[count].setVisibility(View.GONE);
    		}
        }
        
        for (int count = 0; count < hintButtons.length; count++)
        {
    		currentID = "placement_hint_";
    		if ((count) < 10)
    		{
    			currentID += '0';
    		}
    		currentID += (count);
    		resID = res.getIdentifier(currentID, "id", getPackageName());
    		hintButtons[count] = (Button)findViewById(resID);
    		if (placementInProgress == true && endOfMatch == false)
    		{
    			hintButtons[count].setText(pieceLimits[count] + "");
    		}
    		else
    		{
    			hintButtons[count].setVisibility(View.GONE);
    		}
        }
	}
	
	private void updateInterface()
	{
		int countVert;
		int countHoriz;
        for (countVert = 0; countVert < gridButtons.length; countVert++)
        {
        	for (countHoriz = 0; countHoriz < gridButtons[countVert].length; countHoriz++)
        	{
        		addMark(gridPieces[countVert][countHoriz], gridButtons[countVert][countHoriz]);
        	}
        }
        displayLakes();
	}
	
	public void loadGame()
	{
		ContentValues values = DBInterface.retrieveSave(this, filenameGame, THIS_GAME);
		if (values.getAsInteger(DBInterface.EXTRA_BOOL_BASE_KEY + "1") > 0)
		{
			keepReveals = true;
		}
		else
		{
			keepReveals = false;
		}
		if (values.getAsInteger(DBInterface.EXTRA_BOOL_BASE_KEY + "2") > 0)
		{
			highlightMoves = true;
		}
		else
		{
			highlightMoves = false;
		}
		if (values.getAsInteger(DBInterface.IN_PLACEMENT_KEY) > 0)
		{
			placementInProgress = true;
		}
		else
		{
			placementInProgress = false;
		}
		
		setButtons(this.getResources());
		
		String[] piecesAsStrings = DBInterface.stringToData(values.getAsString(DBInterface.GRID_VALUES_KEY));
		StrategoPiece tempPiece;
		int vertIndex = 0;
		int horizIndex = 0;
		for (int count = 0; count < piecesAsStrings.length; count++)
		{
			tempPiece = new StrategoPiece(piecesAsStrings[count]);
			vertIndex = tempPiece.getLocationX();
			horizIndex = tempPiece.getLocationY();
			gridPieces[vertIndex][horizIndex] = tempPiece;
			addMark(tempPiece, gridButtons[vertIndex][horizIndex]);
		}
		
		currentTurn = values.getAsInteger(DBInterface.CURRENT_PLAYER_KEY);
		turnCount = values.getAsInteger(DBInterface.TURN_COUNT_KEY);
		playerOneName = values.getAsString(DBInterface.PLAYER_BASE_KEY + "1");
		playerTwoName = values.getAsString(DBInterface.PLAYER_BASE_KEY + "2");
		if (playerOneName == null || playerTwoName == null)
		{
			useGuestNames();
		}
		updateInterface();
		if (placementInProgress == true)
		{
			setUpPlacement(currentTurn, false);
		}
	}
	
	public boolean saveGame(String name, boolean overwrite)
	{
		ContentValues values = new ContentValues();
		values.put(DBInterface.GAME_NAME_KEY, name);
		values.put(DBInterface.GRID_HEIGHT_KEY, gridButtons.length);
		values.put(DBInterface.GRID_WIDTH_KEY, gridButtons[0].length);
		ArrayList<String> pieces = new ArrayList<String>();
		for (int vertIndex = 0; vertIndex < gridPieces.length; vertIndex++)
		{
			for (int horizIndex = 0; horizIndex < gridPieces[vertIndex].length; horizIndex++)
			{
				if (gridPieces[vertIndex][horizIndex] != null)
				{
					pieces.add(gridPieces[vertIndex][horizIndex].toDBString());
				}
			}
		}
		values.put(DBInterface.GRID_VALUES_KEY, DBInterface.dataToString(pieces));
		values.put(DBInterface.CURRENT_PLAYER_KEY, currentTurn);
		values.put(DBInterface.TURN_COUNT_KEY, turnCount);
		if (placementInProgress == true)
		{
			values.put(DBInterface.IN_PLACEMENT_KEY, currentTurn);
		}
		else
		{
			values.put(DBInterface.IN_PLACEMENT_KEY, 0);
		}
		values.put(DBInterface.PLAYER_BASE_KEY + "1", playerOneName);
		values.put(DBInterface.PLAYER_BASE_KEY + "2", playerTwoName);
		
		if (keepReveals == true)
		{
			values.put(DBInterface.EXTRA_BOOL_BASE_KEY + "1", 1);
		}
		else
		{
			values.put(DBInterface.EXTRA_BOOL_BASE_KEY + "1", 0);
		}
		if (highlightMoves == true)
		{
			values.put(DBInterface.EXTRA_BOOL_BASE_KEY + "2", 1);
		}
		else
		{
			values.put(DBInterface.EXTRA_BOOL_BASE_KEY + "2", 0);
		}
		
		return DBInterface.insertSave(getApplicationContext(), values, THIS_GAME, overwrite);
	}
	
	/****
	 * 
	 * @return an integer representing the dimensions to be used for the (square) grid buttons, in raw pixels.
	 ****/
	private int getButtonDimensions()
	{
		final int UPPER_WINDOW_LIMIT_DP = 10;
		final int LOWER_WINDOW_LIMIT_DP = 20;
		
		int result = 0;
		int actionBarSize = 0;
		DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
		TypedValue tv = new TypedValue();
		if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
		{
		    actionBarSize = TypedValue.complexToDimensionPixelSize(tv.data, metrics);
		}
		Display display = getWindowManager().getDefaultDisplay();
		display.getMetrics(metrics);
		int height = metrics.heightPixels;
		int width = metrics.widthPixels;
		int density = metrics.densityDpi;
		
		int upperBufferHeight = UPPER_WINDOW_LIMIT_DP * density / DisplayMetrics.DENSITY_MEDIUM;
		int lowerBufferHeight = LOWER_WINDOW_LIMIT_DP * density / DisplayMetrics.DENSITY_MEDIUM;
		if (height > width)
		{
			upperBufferHeight = upperBufferHeight * 3 / 2;
			lowerBufferHeight = lowerBufferHeight * 3 / 2;
		}
		upperBufferHeight += actionBarSize;
		Log.d("Stratego Scaling", "playerOneDisplay height = " + lowerBufferHeight);
		int statusBarHeight = 0;
		int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) 
		{
			statusBarHeight = getResources().getDimensionPixelSize(resourceId);
		}
		lowerBufferHeight += statusBarHeight;
		
		Log.d("Stratego Scaling", "Height = " + height + ", Width = " + width
				+ ", upperBuffer = " + upperBufferHeight + ", lowerBuffer = " + lowerBufferHeight
				+ ", actionBarSize = " + actionBarSize + ", statusBarHeight = " + statusBarHeight);
		
		height -= upperBufferHeight;
		height -= lowerBufferHeight;
		
		int divisor = 1;
		if (height > width)
		{
			result = width;
		}
		else
		{
			result = height;
		}
		
		if (placementInProgress == true)
		{
			divisor = placementButtons.length + 3;
		}
		else
		{
			if (gridButtons.length < gridButtons[0].length)
	    	{
	    		divisor = gridButtons[0].length + 1;
	    	}
	    	else
	    	{
	    		divisor = gridButtons.length + 1;
	    	}
		}
		result = result / divisor;
		
		return result;
	}
	
	//This function sets game data to its default state.
	public void resetData()
	{
		for (int count = 0; count < gridButtons.length; count++)
		{
			for (int count2 = 0; count2 < gridButtons[count].length; count2++)
			{
				gridButtons[count][count2].setOnDragListener(null);
				gridButtons[count][count2].setOnTouchListener(null);
				if (movementGridCheck[count][count2] == INVALID)
				{
					gridButtons[count][count2].setBackground(BUTTON_BACK_LAKE);
				}
				else
				{
					gridButtons[count][count2].setBackground(BUTTON_BACK_EMPTY);
				}
				gridPieces[count][count2] = null;
			}
		}
		turnCount = 0;
		currentTurn = 1;
	}
	
	public void useGuestNames()
	{
		playerOne = new Player("Guest One");
		playerTwo = new Player("Guest Two");
		usingGuestNames = true;
		String temp = "One or more players could not be found. Default names are being used.";
		temp += "\nThis game cannot be saved.";
		Toast.makeText(this, temp, Toast.LENGTH_LONG).show();
	}
	
	private void displayLakes()
	{
		for (int vertIndex = 0; vertIndex < gridButtons.length; vertIndex++)
		{
			for (int horizIndex = 0; horizIndex < gridButtons[vertIndex].length; horizIndex++)
			{
				if (movementGridCheck[vertIndex][horizIndex] == INVALID)
				{
					gridButtons[vertIndex][horizIndex].setBackground(BUTTON_BACK_LAKE);
				}
			}
		}
	}
	
	//
	//	Placement Functions
	//
	
	/****
	 * This function prepares the grid for the given player to place their pieces.
	 * It can also act as a reset function for that player, clearing the grid of anything
	 * that they had placed previously, like if they chose not to confirm their final placement
	 * on an earlier occasion.
	 * @param player 
	 * @param overwrite : true indicates that previous placements should be reset.
	 ****/
	private void setUpPlacement(final int player, boolean overwrite)
	{
		hideAllPieces();
		showPlayerPieces(player);
		placementInProgress = true;
		
		for (int count = 0; count < pieceLimits.length; count++)
		{
			if (player == 1)
			{
				playerOneAvailablePieces[count] = pieceLimits[count];
				placementButtons[count].setBackground(BUTTON_BACK_P_1);
				placementButtons[count].setTextColor(playerOneColour);
				activePlayerDisplay.setText(playerOneName + ", place your pieces.");
				activePlayerDisplay.setTextColor(playerOneColour);
			}
			else
			{
				playerTwoAvailablePieces[count] = pieceLimits[count];
				placementButtons[count].setBackground(BUTTON_BACK_P_2);
				placementButtons[count].setTextColor(playerTwoColour);
				activePlayerDisplay.setText(playerTwoName + ", place your pieces.");
				activePlayerDisplay.setTextColor(playerTwoColour);
			}
			placementButtons[count].setOnTouchListener(start_placement_handler);
			hintButtons[count].setText(pieceLimits[count] + "");
		}
		
		if (overwrite == false)
		{
			for (int vertIndex = 0; vertIndex < gridButtons.length; vertIndex++)
			{
				for (int horizIndex = 0; horizIndex < gridButtons[vertIndex].length; horizIndex++)
				{
					if (gridPieces[vertIndex][horizIndex] != null)
					{
						if (player == 1 && gridPieces[vertIndex][horizIndex].getOwner() == player)
						{
							playerOneAvailablePieces[gridPieces[vertIndex][horizIndex].getRank().ordinal()] -= 1;
						}
						else if (player == 2 && gridPieces[vertIndex][horizIndex].getOwner() == player)
						{
							playerTwoAvailablePieces[gridPieces[vertIndex][horizIndex].getRank().ordinal()] -= 1;
						}
					}
				}
			}
		}
		
		for (int count = 0; count < pieceLimits.length; count++)
		{
			if (player == 1)
			{
				hintButtons[count].setText(playerOneAvailablePieces[count] + "");
			}
			else
			{
				hintButtons[count].setText(playerTwoAvailablePieces[count] + "");
			}
		}
		
		int vertIndex;
		int horizIndex;
		
		for (vertIndex = 0; vertIndex < gridButtons.length; vertIndex++)
		{
			for (horizIndex = 0; horizIndex < gridButtons[vertIndex].length; horizIndex++)
			{
				if ((player == 1 && movementGridCheck[vertIndex][horizIndex] == PLACE_1) ||
						(player == 2 && movementGridCheck[vertIndex][horizIndex] == PLACE_2))
				{
					if (overwrite == true || gridPieces[vertIndex][horizIndex] == null)
					{
						gridPieces[vertIndex][horizIndex] = null;
						addMark(null, gridButtons[vertIndex][horizIndex]);
					}
					gridButtons[vertIndex][horizIndex].setOnDragListener(placement_drag_handler);
				}
			}
		}
		updateAvailablePieces(player);
	}
	
	/****
	 * Updates the hint displays showing how many of each piece the current player can
	 * still place.
	 * @param player : the player to be checked.
	 * @return an integer representing how many pieces in total that player can still place.
	 ****/
	public int updateAvailablePieces(final int player)
	{
		int remainingPieces = 0;
		
		if (player == 1)
		{	
			for (int count = 0; count < hintButtons.length; count++)
			{
				remainingPieces += playerOneAvailablePieces[count];
				hintButtons[count].setText(playerOneAvailablePieces[count] + "");
			}
		}
		else
		{
			for (int count = 0; count < hintButtons.length; count++)
			{
				remainingPieces += playerTwoAvailablePieces[count];
				hintButtons[count].setText(playerTwoAvailablePieces[count] + "");
			}
		}
		return remainingPieces;
	}
	
	private void confirmPlacement()
	{
		AlertDialog.Builder confirmPlacementBuilder = new AlertDialog.Builder(this);
		confirmPlacementBuilder.setTitle("All your pieces have been placed.");
		confirmPlacementBuilder.setMessage("Do you wish to use this layout?");
		confirmPlacementBuilder.setCancelable(true);
		confirmPlacementBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog, int id) 
			{
				if (currentTurn == 1)
				{
					currentTurn = 2;
					setUpPlacement(currentTurn, true);
				}
				else
				{
					endPlacement();
					swapTurn();
				}
				confirmPlacementDialog.dismiss();
			}
		});
		confirmPlacementBuilder.setNegativeButton("Reset", new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog,int id) 
			{
				setUpPlacement(currentTurn, true);
				confirmPlacementDialog.dismiss();
			}
		});
		
		confirmPlacementDialog = confirmPlacementBuilder.create();
		confirmPlacementDialog.show();
	}
	
	private void endPlacement()
	{
		for (int count = 0; count < hintButtons.length; count++)
		{
			hintButtons[count].setVisibility(View.GONE);
			placementButtons[count].setOnDragListener(null);
			placementButtons[count].setVisibility(View.GONE);
		}
		TextView temp = (TextView) findViewById(R.id.placementHintView);
		temp.setVisibility(View.GONE);
		temp = (TextView) findViewById(R.id.placementPiecesView);
		temp.setVisibility(View.GONE);
		placementInProgress = false;
		gameInProgress = true;
	}
	
	//
	//	Main Game Functions
	//
	
	/****
	 * Sets the provided button's text and background based on the piece provided.
	 * @param piece : the piece at a particular location. May be null to represent an empty location.
	 * @param location : a button from the playing grid.
	 ****/
	public void addMark(StrategoPiece piece, Button location)
	{
		if (piece == null)
		{
			location.setText(BLANK_MARK + "");
			location.setBackground(BUTTON_BACK_EMPTY);
		}
		else
		{
			if (currentTurn != piece.getOwner() && 
					(piece.isExposed() == false || keepReveals == false))
			{
				location.setText(UNKNOWN_MARK + "");
			}
			else
			{
				location.setText(piece.getRankAsString());
			}
			if (piece.getOwner() == 1)
			{
				location.setTextColor(playerOneColour);
				location.setBackground(BUTTON_BACK_P_1);
			}
			else
			{
				location.setTextColor(playerTwoColour);
				location.setBackground(BUTTON_BACK_P_2);
			}
		}
	}
	
	public void swapTurn()
	{
		hideAllPieces();
		String temp = "It's ";
		int previous = currentTurn;
		if (currentTurn == 1)
		{
			temp += playerTwoDisplay.getText().toString();
			currentTurn = 2;
			activePlayerDisplay.setTextColor(playerTwoColour);
		}
		else
		{
			temp += playerOneDisplay.getText().toString();
			currentTurn = 1;
			activePlayerDisplay.setTextColor(playerOneColour);
		}
		temp += "'s turn!";
		activePlayerDisplay.setText(temp);
		showPlayerPieces(currentTurn);
		
		if (remainingPieces[currentTurn] <= stalemateCheck)
		{
			if (checkForStalemate(currentTurn) == true)
			{
				displayWinner(previous);
				return;
			}
		}
	}
	
	//A function to hide all pieces from view, pending a canceled dialog to change turns.
	public void hideAllPieces()
	{
		for (int countVert = 0; countVert < VERTICAL_LIMIT; countVert++)
		{
			for (int countHoriz = 0; countHoriz < HORIZONTAL_LIMIT; countHoriz++)
			{
				if (gridPieces[countVert][countHoriz] == null)
				{
					gridButtons[countVert][countHoriz].setText(BLANK_MARK + "");
				}
				else //if (gridPieces[countVert][countHoriz].isExposed() == false || keepReveals == false)
				{
					gridButtons[countVert][countHoriz].setText(UNKNOWN_MARK + "");
				}
				gridButtons[countVert][countHoriz].setOnTouchListener(null);
				gridButtons[countVert][countHoriz].setOnDragListener(null);
			}
		}
	}
	
	//A function that uses the player provided to hide/show pieces.
	public void showPlayerPieces(final int player)
	{
		StrategoPiece currentPiece;
		for (int countVert = 0; countVert < VERTICAL_LIMIT; countVert++)
		{
			for (int countHoriz = 0; countHoriz < HORIZONTAL_LIMIT; countHoriz++)
			{
				currentPiece = gridPieces[countVert][countHoriz];
				if (currentPiece != null)
				{
					if (currentPiece.getOwner() == player ||
							(currentPiece.isExposed() == true && keepReveals == true))
					{
						addMark(currentPiece, gridButtons[countVert][countHoriz]);
						gridButtons[countVert][countHoriz].setOnTouchListener(start_drag_handler);
					}
				}
			}
		}
	}
	
	public void prepareTurnChange(final int player)
	{
		hideAllPieces();
		String name = "";
		if (player == 1)
		{
			name = playerTwoName;
		}
		else
		{
			name = playerOneName;
		}
		AlertDialog.Builder turnChangeBuilder = new AlertDialog.Builder(this);
		turnChangeBuilder.setTitle("It is now " + name + "'s turn.");
		turnChangeBuilder.setMessage("");
		turnChangeBuilder.setCancelable(false);
		turnChangeBuilder.setNeutralButton("OK", new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog,int id) 
			{
				turnChangeDialog.dismiss();
				swapTurn();
			}
		});
		
		turnChangeDialog = turnChangeBuilder.create();
		turnChangeDialog.show();
	}
	
	//A function to calculate the potential locations for movement.
	//		Set an array of Buttons to be highlighted as potential movement points.
	//		availableLocationButtons is a class-level variable set here.
	//	Also make sure to register drag_handler for these buttons.
	//	Also check for the lakes; no unit can move into or through those.
	private void findPotentialMoves(Button b, final int player, final StrategoPiece piece, final int xLoc, final int yLoc)
	{
		int range = piece.getMovementRange();
		scanMovementLine(player, xLoc, yLoc, range, 1, 0);
		scanMovementLine(player, xLoc, yLoc, range, 0, 1);
		scanMovementLine(player, xLoc, yLoc, range, -1, 0);
		scanMovementLine(player, xLoc, yLoc, range, 0, -1);
		availableLocationButtons = new Button[tempMoves.size()];
		for (int count = 0; count < availableLocationButtons.length; count++)
		{
			availableLocationButtons[count] = tempMoves.dequeue();
			availableLocationButtons[count].setOnDragListener(drag_handler);
		}
		tempMoves.clear();
	}
	
	private void scanMovementLine(final int player, final int xLoc, final int yLoc, 
			final int range, final int xDir, final int yDir)
	{
		int vertCount = 0;
		int horizCount = 0;
		int vertIndex = 0;
		int horizIndex = 0;
		for (vertIndex = xLoc + xDir, horizIndex = yLoc + yDir;
				vertCount < range && vertIndex >= 0 && vertIndex < gridButtons.length && 
						horizCount < range && horizIndex >= 0 && horizIndex < gridButtons[vertIndex].length; 
				vertIndex += xDir, vertCount++, horizIndex += yDir, horizCount++)
		{
			if (movementGridCheck[vertIndex][horizIndex] != INVALID &&
					(gridPieces[vertIndex][horizIndex] == null || gridPieces[vertIndex][horizIndex].getOwner() != player))
			{
				tempMoves.enqueue(gridButtons[vertIndex][horizIndex]);
			}
			
			if (gridPieces[vertIndex][horizIndex] != null)
			{
				return;
			}
		}
	}
	
	private void highlightPotentialMoves()
	{
		availableLocationOldBackgrounds = new Drawable[availableLocationButtons.length];
		for (int count = 0; count < availableLocationButtons.length; count++)
		{
			availableLocationOldBackgrounds[count] = availableLocationButtons[count].getBackground();
			if (availableLocationOldBackgrounds[count] == BUTTON_BACK_P_1)
			{
				availableLocationButtons[count].setBackground(BUTTON_BACK_MOVABLE_P_1);
			}
			else if (availableLocationOldBackgrounds[count] == BUTTON_BACK_P_2)
			{
				availableLocationButtons[count].setBackground(BUTTON_BACK_MOVABLE_P_2);
			}
			else
			{
				availableLocationButtons[count].setBackground(BUTTON_BACK_MOVABLE_EMPTY);
			}
		}
	}
	
	private void clearPotentialMoves()
	{
		for (int count = 0; count < availableLocationButtons.length; count++)
		{
			availableLocationButtons[count].setBackground(availableLocationOldBackgrounds[count]);
		}
	}
	
	private void resetDragHandlers()
	{
		for (int count = 0; count < availableLocationButtons.length; count++)
		{
			availableLocationButtons[count].setOnDragListener(null);
		}
		availableLocationButtons = new Button[]{};
	}
	
	//
	//	Victory Checks
	//
	
	/****
	 * Checks if a player is unable to make any further moves.
	 * @param player : the player to check.
	 * @return true if a stalemate was found, or false if the player has a piece that can move.
	 */
	public boolean checkForStalemate(final int player)
	{
		int vertIndex = 0;
		int horizIndex = 0;
		boolean result = true;
		for (vertIndex = 0; vertIndex < gridButtons.length && result == true; vertIndex++)
		{
			for (horizIndex = 0; horizIndex < gridButtons[vertIndex].length && result == true; horizIndex++)
			{
				if (gridPieces[vertIndex][horizIndex] != null)
				{
					if (gridPieces[vertIndex][horizIndex].getOwner() == player &&
							gridPieces[vertIndex][horizIndex].getMovementRange() != 0)
					{
						result = false;
					}
				}
			}
		}
		return result;
	}
	
	//This function ends the match, saves the scores, and resets the game.
    //It then creates a dialog that displays the winner, with a single button to return to the main menu.
    public void endOfMatch(int result)
    {
    	String temp;
    	String[] winners = new String[1];
    	if (result == 2)
    	{
    		temp = playerTwoDisplay.getText().toString();
    		temp += " was the winner.";
    		winners[0] = playerTwoName;
    	}
    	else if (result == 1)
    	{
    		temp = playerOneDisplay.getText().toString();
    		temp += " was the winner";
    		winners[0] = playerOneName;
    	}
    	else
    	{
    		temp = "There was no winner.";
    		winners = null;
    	}
    	
    	if (usingGuestNames == false)
    	{
    		DBInterface.updatePlayerScores(getApplicationContext(), 
        			new String[]{playerOneName, playerTwoName}, THIS_GAME, winners);
    	}
    	
    	gameInProgress = false;
    	//resetData();
    	
    	final String temp2 = temp;
    	Toast.makeText(this, temp2, Toast.LENGTH_SHORT).show();
    	
    	mainLayout = (RelativeLayout) findViewById(R.id.mainLayout);
    	mainLayout.setOnClickListener(endMatch);
    }
    
    //This is the listener called when the match is finished and the user taps the screen.
    //It releases the sound player and launches the main menu, then destroys this activity.
    private View.OnClickListener endMatch = new View.OnClickListener() 
	{
		public void onClick(View v) 
		{
			if (soundPlayer != null)
			{
				try
				{
					soundPlayer.pause();
					soundPlayer.stop();
					soundPlayer.release();
				}
				catch (IllegalStateException i)
				{
					
				}
			}
			mainLayout.setOnClickListener(null);
			StrategoActivity.this.finish();
		}
	};
    
	//This function takes the winner as a boolean and prints the appropriate player's name.
	//It declares that player the winner, complete with the sound of fireworks.
	//It then calls endOfMatch() with an integer representing who won.
    public void displayWinner(int winner)
    {
    	soundPlayer = MediaPlayer.create(StrategoActivity.this,R.raw.fireworks_finale);
    	soundPlayer.start();
    	String temp;
    	int result;
    	if (winner == 1)
    	{
    		temp = playerOneDisplay.getText().toString();
    		playerOneScore++;
    		activePlayerDisplay.setTextColor(playerOneColour);
    		result = 1;
    	}
    	else
    	{
    		temp = playerTwoDisplay.getText().toString();
    		playerTwoScore++;
    		activePlayerDisplay.setTextColor(playerTwoColour);
    		result = 2;
    	}
    	
    	temp += " is the winner! Congratuations!";
    	activePlayerDisplay.setText(temp);
    	endOfMatch(result);
    	//Log.d("ID", temp);
    }
    
    //This function simply displays that there was a tie, then calls endOfMatch().
    public void displayTie()
    {
    	String temp;
    	temp = "Sorry, but nobody won this match.";
    	activePlayerDisplay.setText(temp);
    	endOfMatch(0);
    }
	
	//
	//	Listeners
	//
	
	private View.OnDragListener drag_handler = new OnDragListener()
	{
		@Override
		public boolean onDrag(View v, final DragEvent event) 
		{
			boolean handled = false;
		    switch (event.getAction()) 
		    {
			    case DragEvent.ACTION_DRAG_LOCATION:
			    {
			    	//Failing to set a true return here causes an exception of the form
			    	//		"String cannot be cast to Spannable"
			    	handled = true;
			    	break;
			    }
			    case DragEvent.ACTION_DRAG_STARTED:
			    {
			        //To register your view as a potential drop zone for the current view being dragged
			        //you need to set the event as handled
			        //
			        handled = true;
			        //An important thing to know is that drop zones need to be visible (i.e. their Visibility)
			        //property set to something other than Gone or Invisible) in order to be considered. A nice workaround
			        //if you need them hidden initially is to have their layout_height set to 1.
			        //
			        break;
			    }
			    case DragEvent.ACTION_DRAG_ENTERED:
			    case DragEvent.ACTION_DRAG_EXITED:
			    {
			        //These two states allows you to know when the dragged view is contained atop your drop zone.
			        //Traditionally you will use that tip to display a focus ring or any other similar mechanism
			        //to advertise your view as a drop zone to the user.
			        //
			    	handled = true;
			        break;
			    }
			    case DragEvent.ACTION_DROP:
			    {
			    	Button b = (Button) v;
			    	boolean victoryDetected = false;
					
					StrategoPiece defendingPiece = null;
					int vertIndex = 0;
					int horizIndex = 0;
					boolean foundMatch = false;
					for (vertIndex = 0; vertIndex < gridButtons.length; vertIndex++)
					{
						for (horizIndex = 0; horizIndex < gridButtons[vertIndex].length; horizIndex++)
						{
							if (gridButtons[vertIndex][horizIndex] == b)
							{
								defendingPiece = gridPieces[vertIndex][horizIndex];
								foundMatch = true;
								break;
							}
						}
						if (foundMatch == true)
						{
							break;
						}
					}
					
					clearPotentialMoves();
					
					if (defendingPiece != null)
					{
						gridPieces[currentAttacker.getLocationX()][currentAttacker.getLocationY()] = null;
						addMark(null, gridButtons[currentAttacker.getLocationX()][currentAttacker.getLocationY()]);
						defendingPiece.updateCoordinates(vertIndex, horizIndex);
						StrategoPiece victor = StrategoPiece.evaluateCombat(currentAttacker, defendingPiece);
						gridPieces[vertIndex][horizIndex] = victor;
						addMark(victor, gridButtons[vertIndex][horizIndex]);
						
						if (victor == currentAttacker)
						{
							remainingPieces[defendingPiece.getOwner()] -= 1;
							currentAttacker.updateCoordinates(vertIndex, horizIndex);
							if (defendingPiece.getRank() == StrategoPiece.RankValues.Flag)
							{
								displayWinner(currentAttacker.getOwner());
								victoryDetected = true;
							}
						}
						else
						{
							remainingPieces[currentAttacker.getOwner()] -= 1;
							if (victor == null)
							{
								remainingPieces[defendingPiece.getOwner()] -= 1;
							}
						}
						
						String message = "A " + currentAttacker.getRank().toString() + 
								" attacked a " + defendingPiece.getRank().toString() + ".";
						if (victor != null)
						{
							victor.revealPiece();
							message += "\nThe " + victor.getRank().toString() + " was victorious.";
						}
						else
						{
							message += "\nBoth pieces were destroyed.";
						}
						Toast.makeText(StrategoActivity.this, message, Toast.LENGTH_SHORT).show();
					}
					else
					{
						addMark(null, gridButtons[currentAttacker.getLocationX()][currentAttacker.getLocationY()]);
						gridPieces[vertIndex][horizIndex] = currentAttacker;
						gridPieces[currentAttacker.getLocationX()][currentAttacker.getLocationY()] = null;
						currentAttacker.updateCoordinates(vertIndex, horizIndex);
						addMark(currentAttacker, b);
					}
			        //This state is used when the user drops the view on your drop zone. If you want to accept the drop,
			        //set the Handled value to true like before.
			        //
			        handled = true;
			        resetDragHandlers();
			        if (victoryDetected == false)
			        {
			        	prepareTurnChange(currentAttacker.getOwner());
			        }
			        //It's also probably time to get a bit of the data associated with the drag to know what
			        //you want to do with the information.
			        //
			        break;
			    }
			    case DragEvent.ACTION_DRAG_ENDED:
			    {
			        //This is the final state, where you still have possibility to cancel the drop happened.
			        //You will generally want to set Handled to true.
			        //
			    	clearPotentialMoves();
			        handled = true;
			        break;
			    }
		    }
		    return handled;
		}
	};
	
	//Placement drag handlers are needed.
	//	They must account for pieces being replaced (second-guessing one's choices, bad placements, etc.),
	//		so when a drop is made the current value there must be considered (INVALID can be used for a null)
	//		and used to restock the array holding however many pieces the player can still place.
	
	//This warning must be ignored, as following its suggestion causes the onTouch() function
	//		to trigger the button's onClick() event, which leads to an infinite loop.
	@SuppressLint("ClickableViewAccessibility") 
	private View.OnTouchListener start_drag_handler = new OnTouchListener()
	{
		@Override
		public boolean onTouch(View v, final MotionEvent event) 
		{
			Button b = (Button) v;
			
			StrategoPiece selectedPiece = null;
			int vertIndex = 0;
			int horizIndex = 0;
			boolean foundMatch = false;
			for (vertIndex = 0; vertIndex < gridButtons.length; vertIndex++)
			{
				for (horizIndex = 0; horizIndex < gridButtons[vertIndex].length; horizIndex++)
				{
					if (gridButtons[vertIndex][horizIndex] == b)
					{
						selectedPiece = gridPieces[vertIndex][horizIndex];
						foundMatch = true;
						break;
					}
				}
				if (foundMatch == true)
				{
					break;
				}
			}
			
			if (selectedPiece != null && event.getAction() == MotionEvent.ACTION_DOWN)
			{
				selectedPiece.updateCoordinates(vertIndex, horizIndex);
				int range = selectedPiece.getMovementRange();
				if (range > 0)
				{
					currentAttacker = selectedPiece;
					findPotentialMoves(b, currentAttacker.getOwner(), selectedPiece, vertIndex, horizIndex);
					highlightPotentialMoves();
					//Calculate the button's location, determine the piece currently there,
			        //		and use those parameters to calculate available movement points.
					//Register the onDragHandler for the appropriate gridButtons based on those results.
					//	For movement: 
					//		-Scouts have infinite straight-line movement (until they hit a barrier of any sort).
					//		-Bombs and the Flag cannot move at all.
					//		-All other pieces move a single square vertically or horizontally.
					//			-No diagonal movement is allowed for any piece, nor can pieces pass through other pieces.
					
					b.startDrag(null, new View.DragShadowBuilder(b), null, 0);
				}
			}
			return false;
		}
	};
	
	private View.OnDragListener placement_drag_handler = new OnDragListener()
	{
		@Override
		public boolean onDrag(View v, final DragEvent event) 
		{
			boolean handled = false;
		    switch (event.getAction()) 
		    {
			    case DragEvent.ACTION_DRAG_LOCATION:
			    {
			    	handled = true;
			    	break;
			    }
			    case DragEvent.ACTION_DRAG_STARTED:
			    {
			        handled = true;
			        break;
			    }
			    case DragEvent.ACTION_DRAG_ENTERED:
			    case DragEvent.ACTION_DRAG_EXITED:
			    {
			    	handled = true;
			        break;
			    }
			    case DragEvent.ACTION_DROP:
			    {
			    	Button b = (Button) v;
					
					StrategoPiece oldPiece = null;
					int vertIndex = 0;
					int horizIndex = 0;
					boolean foundMatch = false;
					for (vertIndex = 0; vertIndex < gridButtons.length && foundMatch == false; vertIndex++)
					{
						for (horizIndex = 0; horizIndex < gridButtons[vertIndex].length; horizIndex++)
						{
							if (gridButtons[vertIndex][horizIndex] == b)
							{
								oldPiece = gridPieces[vertIndex][horizIndex];
								foundMatch = true;
								break;
							}
						}
						if (foundMatch == true)
						{
							break;
						}
					}
					
					if (foundMatch == false)
					{
						handled = true;
						break;
					}
					
					if (oldPiece != null)
					{
						int oldRank = oldPiece.getRank().ordinal();
						if (currentTurn == 1 && playerOneAvailablePieces[oldRank] < pieceLimits[oldRank])
						{
							playerOneAvailablePieces[oldRank] += 1;
						}
						else if (currentTurn == 2 && playerTwoAvailablePieces[oldRank] < pieceLimits[oldRank])
						{
							playerTwoAvailablePieces[oldRank] += 1;
						}
					}
					
					int newRank = Integer.parseInt(event.getClipData().getItemAt(0).getText().toString());
					if (currentTurn == 1 && playerOneAvailablePieces[newRank] > 0)
					{
						playerOneAvailablePieces[newRank] -= 1;
						gridPieces[vertIndex][horizIndex] = new StrategoPiece(
								StrategoPiece.RankValues.values()[newRank], currentTurn, vertIndex, horizIndex);
						addMark(gridPieces[vertIndex][horizIndex], gridButtons[vertIndex][horizIndex]);
					}
					else if (currentTurn == 2 && playerTwoAvailablePieces[newRank] > 0)
					{
						playerTwoAvailablePieces[newRank] -= 1;
						gridPieces[vertIndex][horizIndex] = new StrategoPiece(
								StrategoPiece.RankValues.values()[newRank], currentTurn, vertIndex, horizIndex);
						addMark(gridPieces[vertIndex][horizIndex], gridButtons[vertIndex][horizIndex]);
					}
					
					if (updateAvailablePieces(currentTurn) == 0)
					{
						for (vertIndex = 0; vertIndex < gridButtons.length; vertIndex++)
						{
							for (horizIndex = 0; horizIndex < gridButtons[vertIndex].length; horizIndex++)
							{
								gridButtons[vertIndex][horizIndex].setOnDragListener(null);
							}
						}
						confirmPlacement();
					}
					
			        handled = true;
			        break;
			    }
			    case DragEvent.ACTION_DRAG_ENDED:
			    {
			        handled = true;
			        break;
			    }
		    }
		    return handled;
		}
	};
	
	//This warning must be ignored, as following its suggestion causes the onTouch() function
	//		to trigger the button's onClick() event, which leads to an infinite loop.
	@SuppressLint("ClickableViewAccessibility") 
	private View.OnTouchListener start_placement_handler = new OnTouchListener()
	{
		@Override
		public boolean onTouch(View v, final MotionEvent event) 
		{
			Button b = (Button) v;
			
			int pieceRank;
			for (pieceRank = 0; pieceRank < placementButtons.length; pieceRank++)
			{
				if (placementButtons[pieceRank] == b)
				{
					break;
				}
			}
			
			if (event.getAction() == MotionEvent.ACTION_DOWN)
			{
				//This stops the drag from occurring if the current player cannot place any more of that piece.
				if ((currentTurn == 1 && playerOneAvailablePieces[pieceRank] <= 0) ||
						(currentTurn == 2 && playerTwoAvailablePieces[pieceRank] <= 0))
				{
					return false;
				}
				else
				{
					ClipData data = ClipData.newPlainText(RANK_DRAG_KEY, (pieceRank + ""));
					b.startDrag(data, new View.DragShadowBuilder(b), null, 0);
				}
			}
			return false;
		}
	};
	
	private OnClickListener save_listener = new OnClickListener()
    {
		@Override
		public void onClick(View v) 
		{
			if (usingGuestNames == false && gameInProgress == true && gameEnded == false)
			{
				saveFile();
				//Create a dialog to prompt the user for a filename.
				//Have a confirm button (make sure to check that the string provided is not null).
				//		If possible, also project the list of existing files in a spinner as part of the dialog.
				//		When confirmed, close the dialog and display a toast informing the user of the successful save.
				//Also have a cancel button, and allow the dialog itself to be cancelable.
				//Write an AlertDialog that allows a password input.
		    	
			}
		}
    };
    
    public void saveFile()
    {
    	AlertDialog.Builder saveBuilder = new AlertDialog.Builder(this);
		saveBuilder.setTitle("Enter the save game name:");
		final EditText input = new EditText(this);
		saveBuilder.setView(input);
		saveBuilder.setCancelable(true);
		saveBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog,int id) 
			{
				boolean didSave;
				String name = input.getText().toString();
				name = name.replaceAll("[^a-zA-Z_0-9 ]", "");
				didSave = saveGame(name, false);
				if (didSave == false)
				{
					confirmOverwrite(name);
				}
				else
				{
					saveDialog.dismiss();
					Toast.makeText(StrategoActivity.this, "This game has been saved as: " + name, Toast.LENGTH_SHORT).show();
				}
			}
		});
		saveBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog,int id) 
			{
				saveDialog.dismiss();
			}
		});
		
		saveDialog = saveBuilder.create();
		saveDialog.show();
    }
    
    public void confirmOverwrite(final String name)
    {
    	AlertDialog.Builder overwriteBuilder = new AlertDialog.Builder(this);
		overwriteBuilder.setTitle("The name \n" + name + "\n is already in use");
		overwriteBuilder.setMessage("Do you want to overwrite the previous file?");
		overwriteBuilder.setCancelable(true);
		overwriteBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog,int id) 
			{
				saveGame(name, true);
				overwriteDialog.dismiss();
				saveDialog.dismiss();
				Toast.makeText(StrategoActivity.this, "This game has been saved as: " + name, Toast.LENGTH_SHORT).show();
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
    
    public static Integer[] getPlayerCounts()
    {
    	return PLAYERS;
    }
    
    public static String[][] getExtras()
    {
    	return EXTRAS;
    }
    
    public static String[] getBoolExtras()
    {
    	return BOOLEAN_EXTRAS;
    }
}


