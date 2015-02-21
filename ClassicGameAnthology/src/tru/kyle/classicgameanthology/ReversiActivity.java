package tru.kyle.classicgameanthology;

/*
This file (ReversiActivity) is a part of the Classic Game Anthology application.
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

import tru.kyle.classicgameanthology.FileSaver.CaptureType;
import tru.kyle.classicgameanthology.FileSaver.Game;
import tru.kyle.classicgameanthology.FileSaver.GameByLayout;
import tru.kyle.databases.DBInterface;

import tru.kyle.mylists.*;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class ReversiActivity extends Activity 
{
	protected static final String[][] EXTRAS = null;
	
	//These are pulled by the MainMenuActivity and used to title checkboxes.
	protected static final String[] BOOLEAN_EXTRAS = {
		"Highlight Moves?"
	};
	
	public static final Integer[] PLAYERS = {2};
	public final Game THIS_GAME = Game.Reversi;
	public final GameByLayout THIS_LAYOUT = GameByLayout.reversi;
	public final CaptureType THIS_CAPTURE = CaptureType.CHANGE;
	private final String BUTTON_BACK_NORMAL_STRING = "button_border_normal_" + THIS_LAYOUT.toString();
	private final String BUTTON_BACK_GLOW_STRING = "button_border_glowing_" + THIS_LAYOUT.toString();
	private final String BUTTON_BACK_CAPTURE_STRING = "button_border_capture_" + THIS_LAYOUT.toString();
	private final String BUTTON_BACK_FAILURE_STRING = "button_border_failure_" + THIS_LAYOUT.toString();
	
	public static final int VERTICAL_LIMIT = 8;
	public static final int HORIZONTAL_LIMIT = 8;
	public static final int TURN_LIMIT = VERTICAL_LIMIT * HORIZONTAL_LIMIT;
	public static final int SEQUENCE_TO_WIN = 5;
	public static final int CAPTURE_MIN = 1;
	public static final int CAPTURE_MAX = 7;
	public static final int CAPTURES_TO_WIN = 5;
	final char PLAYER_ONE_MARK = 'X';
	final char PLAYER_TWO_MARK = 'O';
	final char BLANK_MARK = ' ';
	
	boolean gameInProgress = false;
	boolean canPressButton;
	boolean usingGuestNames = false;
	
	int currentTurn = 1;
	
	char currentMark;
	
	int turnCount;
	
	int playerOneScore;
	
	int playerTwoScore;
	
	TextView playerOneDisplay;
	TextView playerTwoDisplay;
	TextView activePlayerDisplay;
	TextView playerOnePiecesDisplay;
	TextView playerTwoPiecesDisplay;
	
	MediaPlayer soundPlayer;
	
	Player playerOne;
	Player playerTwo;
	String playerOneName;
	String playerTwoName;
	
	int[][] pointsPlaced = new int[VERTICAL_LIMIT][HORIZONTAL_LIMIT];
	Button[][] buttons = new Button[VERTICAL_LIMIT][HORIZONTAL_LIMIT];
	String filenameGame;
	String filenameStandings;
	String filenameNames;
	
	AlertDialog endMatchDialog;
	AlertDialog saveDialog;
	AlertDialog overwriteDialog;
	
	Button previousMove;
	Button saveGame;
	int[] captures;
	
	int playerOneColour = Color.RED;
	int playerTwoColour = Color.BLUE;
	
	boolean newMatch;
	boolean endOfMatch = false;
	MyQueue<Button> lastCaptures = new MyQueue<Button>();
	MyQueue<Button> failedAttempts = new MyQueue<Button>();
	boolean lastPlayerSkipped = false;
	
	boolean highlightMoves = true;
	
	private Drawable BUTTON_BACK_NORMAL;
	private Drawable BUTTON_BACK_GLOW;
	private Drawable BUTTON_BACK_CAPTURE;
	private Drawable BUTTON_BACK_FAILURE;
	
	private static Toast warning;
	protected int buttonSize;
	
	//The onCreate() function is used to set filenames and link variables to the displays.
	//It also links the buttons to the appropriate indexes of the buttons[][] array based on their names.
	@SuppressLint("ShowToast")
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reversi);
        
        Resources res = getResources();
        BUTTON_BACK_NORMAL = res.getDrawable(res.getIdentifier(this.BUTTON_BACK_NORMAL_STRING, "drawable", getPackageName()));
    	BUTTON_BACK_GLOW = res.getDrawable(res.getIdentifier(this.BUTTON_BACK_GLOW_STRING, "drawable", getPackageName()));
    	BUTTON_BACK_CAPTURE = res.getDrawable(res.getIdentifier(this.BUTTON_BACK_CAPTURE_STRING, "drawable", getPackageName()));
    	BUTTON_BACK_FAILURE = res.getDrawable(res.getIdentifier(this.BUTTON_BACK_FAILURE_STRING, "drawable", getPackageName()));
    	
        //filenameGame = getString(R.string.filenameCurrentGame);
    	Intent intent = getIntent();
        filenameGame = intent.getStringExtra(MainMenuActivity.GAME_FILENAME_KEY);
        if (filenameGame == null)
        {
        	filenameGame = FileSaver.AUTOSAVE_NAME;
        }
    	filenameStandings = getString(R.string.filenameStandings);
    	filenameNames = getString(R.string.filenameNames);
    	newMatch = intent.getBooleanExtra(MainMenuActivity.NEW_MATCH_KEY, false);
    	intent.putExtra(MainMenuActivity.NEW_MATCH_KEY, false);
    	
    	playerOneName = intent.getStringExtra(MainMenuActivity.BASE_PLAYER_FILENAME_KEY + "1");
    	playerTwoName = intent.getStringExtra(MainMenuActivity.BASE_PLAYER_FILENAME_KEY + "2");
        
        playerOneDisplay = (TextView)findViewById(R.id.playerOne);
    	playerTwoDisplay = (TextView)findViewById(R.id.playerTwo);
    	activePlayerDisplay = (TextView)findViewById(R.id.activePlayer);
    	playerOnePiecesDisplay = (TextView)findViewById(R.id.reversi_playerOnePieces);
    	playerTwoPiecesDisplay = (TextView)findViewById(R.id.reversi_playerTwoPieces);
    	
    	saveGame = (Button) findViewById(R.id.saveGame);
    	saveGame.setOnClickListener(save_listener);
    	
    	if (newMatch == true)
    	{
    		parseExtras(intent);
    		setButtons(res);
    		resetData();
    	}
    	else
    	{
    		loadGame();
    	}
    	
        (warning = Toast.makeText(ReversiActivity.this, "Have fun!", Toast.LENGTH_SHORT)).show();
    	
		Log.d("Life Cycle", "Reversi Activity: onCreate");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.reversi, menu);
        return true;
    }
    
    @Override
	protected void onStart() 
    {
		super.onStart();
		Log.d("Life Cycle", "Reversi Activity: onStart");
		//This is called between onCreate() and onResume().
		//onRestart() also leads to this.
	}

	//The onResume() function reads in data from files and sets it accordingly, then continues to the actual game.
	@Override
	protected void onResume() 
	{
		super.onResume();
		Log.d("Life Cycle", "Reversi Activity: onResume");
		//This method is called every time the activity hits the foreground.
		//This includes the first time the app is created.
		//Initialization of data should be here, especially if it was released.
		
			
		playerOneDisplay.setTextColor(playerOneColour);
		playerTwoDisplay.setTextColor(playerTwoColour);
		playerOneDisplay.setText(playerOneName);
		playerTwoDisplay.setText(playerTwoName);
		int temp = currentTurn;
		swapTurn();
		while (currentTurn != temp)
		{
			swapTurn();
		}
		//This loop forces the display to be updated correctly.
		countPieces();
		
		buttonSize = getButtonDimensions();
    	LayoutParams params;
    	Log.d("Reversi Scaling", "buttonSize = " + buttonSize);
		for (int count = 0; count < buttons.length; count++)
		{
			for (int count2 = 0; count2 < buttons[count].length; count2++)
			{
				//buttons[count][count2].setHeight(buttonSize);
        		//buttons[count][count2].setWidth(buttonSize);
        		//buttons[count][count2].setLayoutParams(params);
				params = buttons[count][count2].getLayoutParams();
				params.height = buttonSize;
				params.width = buttonSize;
				buttons[count][count2].setLayoutParams(params);
			}
		}
		
		gameInProgress = true;
		canPressButton = true;
		soundPlayer = null;
	}
	
	
	
	private void parseExtras(Intent intent)
	{
		highlightMoves = intent.getBooleanExtra(MainMenuActivity.EXTRA_BOOL_BASE_KEY + "1", false);
	}
	
	private void setButtons(Resources res)
	{
		String currentID;
        int resID;
        for (int count = 0; count < buttons.length; count++)
        {
        	for (int count2 = 0; count2 < buttons[count].length; count2++)
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
        		buttons[count][count2] = (Button)findViewById(resID);
        		buttons[count][count2].setOnClickListener(grid_handler);
        		if (gameInProgress == false)
        		{
        			buttons[count][count2].setText(BLANK_MARK + "");
        		}
        	}
        }
	}
	
	private void updateInterface()
	{
		int countVert;
		int countHoriz;
        for (countVert = 0; countVert < buttons.length; countVert++)
        {
        	for (countHoriz = 0; countHoriz < buttons[countVert].length; countHoriz++)
        	{
        		switch (pointsPlaced[countVert][countHoriz])
        		{
	        		case 1:
	        		{
	        			addMark(pointsPlaced[countVert][countHoriz], buttons[countVert][countHoriz]);
	        			break;
	        		}
	        		case 2:
	        		{
	        			addMark(pointsPlaced[countVert][countHoriz], buttons[countVert][countHoriz]);
	        			break;
	        		}
	        		default:
	        		{
	        			buttons[countVert][countHoriz].setText(BLANK_MARK + "");
	        			break;
	        		}
        		}
        	}
        }
        countPieces();
	}
	
	public void loadGame()
	{
		ContentValues values = DBInterface.retrieveSave(this, filenameGame, THIS_GAME);
		int vertLimit = values.getAsInteger(DBInterface.GRID_HEIGHT_KEY);
		int horizLimit = values.getAsInteger(DBInterface.GRID_WIDTH_KEY);
		if (values.getAsInteger(DBInterface.EXTRA_BOOL_BASE_KEY + "1") > 0)
		{
			highlightMoves = true;
		}
		else
		{
			highlightMoves = false;
		}
		
		setButtons(this.getResources());
		
		pointsPlaced = DBInterface.stringToGrid(vertLimit, horizLimit, 
				values.getAsString(DBInterface.GRID_VALUES_KEY));
		for (int countVert = 0; countVert < buttons.length; countVert++)
		{
			for (int countHoriz = 0; countHoriz < buttons[countVert].length; countHoriz++)
			{
				if (pointsPlaced[countVert][countHoriz] != 0)
				{
					buttons[countVert][countHoriz].setClickable(false);
					addMark(pointsPlaced[countVert][countHoriz], buttons[countVert][countHoriz]);
				}
				else
				{
					buttons[countVert][countHoriz].setClickable(true);
				}
			}
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
	}
	
	public boolean saveGame(String name, boolean overwrite)
	{
		ContentValues values = new ContentValues();
		values.put(DBInterface.GAME_NAME_KEY, name);
		values.put(DBInterface.GRID_HEIGHT_KEY, pointsPlaced.length);
		values.put(DBInterface.GRID_WIDTH_KEY, pointsPlaced[0].length);
		values.put(DBInterface.GRID_VALUES_KEY, DBInterface.gridToString(pointsPlaced));
		values.put(DBInterface.CURRENT_PLAYER_KEY, currentTurn);
		values.put(DBInterface.TURN_COUNT_KEY, turnCount);
		values.put(DBInterface.PLAYER_BASE_KEY + "1", playerOneName);
		values.put(DBInterface.PLAYER_BASE_KEY + "2", playerTwoName);
		if (highlightMoves == true)
		{
			values.put(DBInterface.EXTRA_BOOL_BASE_KEY + "1", 1);
		}
		else
		{
			values.put(DBInterface.EXTRA_BOOL_BASE_KEY + "1", 0);
		}
		
		return DBInterface.insertSave(getApplicationContext(), values, THIS_GAME, overwrite);
	}
	
	//This function calculates the maximum dimensions allowable for the buttons, and returns the result.
		//		Note that the final result is in raw pixels, not density-independent pixels.
	private int getButtonDimensions()
	{
		final int UPPER_WINDOW_LIMIT_DP = 35;
		final int LOWER_WINDOW_LIMIT_DP = 60;
		
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
		Log.d("Reversi Scaling", "playerOneDisplay height = " + lowerBufferHeight);
		int statusBarHeight = 0;
		int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) 
		{
			statusBarHeight = getResources().getDimensionPixelSize(resourceId);
		}
		lowerBufferHeight += statusBarHeight;
		
		Log.d("Reversi Scaling", "Height = " + height + ", Width = " + width
				+ ", upperBuffer = " + upperBufferHeight + ", lowerBuffer = " + lowerBufferHeight
				+ ", actionBarSize = " + actionBarSize + ", statusBarHeight = " + statusBarHeight);
		
		height -= upperBufferHeight;
		height -= lowerBufferHeight;
		
		if (height > width)
		{
			result = width;
		}
		else
		{
			result = height;
		}
		
		int divisor = 1;
		if (buttons.length < buttons[0].length)
    	{
    		divisor = buttons[0].length;
    	}
    	else
    	{
    		divisor = buttons.length;
    	}
		result = result / divisor;
		
		return result;
	}
	
	//This function sets game data to its default state.
	public void resetData()
	{
		for (int count = 0; count < buttons.length; count++)
		{
			for (int count2 = 0; count2 < buttons[count].length; count2++)
			{
				buttons[count][count2].setClickable(true);
				//buttons[count][count2].setBackgroundColor(Color.WHITE);
			}
		}
		for (int count = 0; count < pointsPlaced.length; count++)
    	{
    		for (int count2 = 0; count2 < pointsPlaced[count].length; count2++)
    		{
    			pointsPlaced[count][count2] = 0;
    		}
    	}
		
		//These set the starting values, as Reversi does not begin with an empty board.
		pointsPlaced[3][3] = 1;
		pointsPlaced[4][4] = 1;
		pointsPlaced[4][3] = 2;
		pointsPlaced[3][4] = 2;
		addMark(1, buttons[3][3]);
		addMark(1, buttons[4][4]);
		addMark(2, buttons[4][3]);
		addMark(2, buttons[3][4]);
		
		captures = new int[2];
		turnCount = 4;
		currentMark = PLAYER_ONE_MARK;
	}
	
	public void resetScores()
	{
		playerOneScore = 0;
		playerTwoScore = 0;
	}
	
	public void useGuestNames()
	{
		playerOne = new Player("Guest One");
		playerTwo = new Player("Guest Two");
		usingGuestNames = true;
		//Create a dialog that warns the user of how one or more players could not be found.
		//Inform them that default names are being used and that the game cannot be saved.
		//Also force a deletion of the current file in use.
		//Use a toast instead?
		String temp = "One or more players could not be found. Default names are being used.";
		temp += "\nThis game cannot be saved.";
		Toast.makeText(this, temp, Toast.LENGTH_LONG).show();
	}
	
	
	
	
	
	private int loopCaptures(int vertStart, int horizStart, int player, boolean executeCaptures)
	{
		int totalCapturedPieces = 0;
		int temp = 0;
		int vertInc = 0;
		int horizInc = 0;
		for (int count = 0; count < 3; count++)
		{
			vertInc = (vertInc + 2) % 3 - 1;
			//This expression will make the value loop from 0 to 1, to -1, and back to 0.
			
			temp = checkCapture(vertStart, horizStart, vertInc, horizInc, player, false);
			if (temp > 0 && executeCaptures == true)
			{
				temp = checkCapture(vertStart, horizStart, vertInc, horizInc, player, true);
			}
			totalCapturedPieces += temp;
			
			temp = checkCapture(vertStart, horizStart, horizInc, vertInc, player, false);
			if (temp > 0 && executeCaptures == true)
			{
				temp = checkCapture(vertStart, horizStart, horizInc, vertInc, player, true);
			}
			totalCapturedPieces += temp;
			
			temp = checkCapture(vertStart, horizStart, vertInc, vertInc, player, false);
			if (temp > 0 && executeCaptures == true)
			{
				temp = checkCapture(vertStart, horizStart, vertInc, vertInc, player, true);
			}
			totalCapturedPieces += temp;
			//The checks with temp are to prevent buttons from being highlighted unless a capture is actually present.
			
			horizInc = (horizInc + 2) % 3 - 1;
		}
		//The loop repeats a fixed three times (multiplied by the three captures in each repetition) in order to 
		//		iterate through all nine possible 2-value combinations of 0, 1, and -1.
		//		In the first repetition, it would check with increments 1/0, then 0/1, and finally 1/1.
		//		In the second repetition, it would check with increments -1/1, then 1/-1, and finally -1/-1.
		//		In the final repetition, it would check with increments 0/-1, then -1/0, and finally 0/0.
		
		return totalCapturedPieces;
	}
	
	//The increments determine which direction the function will check in.
	//Positive vertical increments will cause the function to check downwards, and 
	//		positive horizontal increments will cause the function to check rightwards.
	//For instance, if vertInc == 1 and horizInc == -1, the function will check a diagonal to the lower left of
	//		the starting point, so to the south-west.
	private int checkCapture(int vertStart, int horizStart, int vertInc, int horizInc, int player, boolean executeCaptures)
	{
		if (vertInc == 0 && horizInc == 0)
		{
			return 0;
		}
		//This statement prevents an infinite loop;
		//		if both increments were 0, the loop would be locked to its starting point.
		int piecesCaptured = 0;
		boolean failure = false;
		int spaceCount = 0;
		int countVert = vertStart;
		int countHoriz = horizStart;
		try
		{
			for (countVert += vertInc, countHoriz += horizInc; spaceCount < CAPTURE_MAX && failure == false; 
					countVert += vertInc, countHoriz += horizInc)
			{
				if (pointsPlaced[countVert][countHoriz] == 0)
				{
					failure = true;
					break;
				}
				else if (pointsPlaced[countVert][countHoriz] == player)
				{
					break;
				}
				else
				{
					piecesCaptured++;
					if (true == executeCaptures)
					{
						captureIndex(countVert, countHoriz, player);
						highlightCapture(buttons[countVert][countHoriz]);
						lastCaptures.enqueue(buttons[countVert][countHoriz]);
					}
				}
				spaceCount++;
			}
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			piecesCaptured = 0;
			failure = true;
			return piecesCaptured;
		}
		
		try
		{
			if (failure == true || piecesCaptured < CAPTURE_MIN || piecesCaptured > CAPTURE_MAX
					|| pointsPlaced[countVert][countHoriz] != player)
			{
				piecesCaptured = 0;
				failure = true;
			}
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			piecesCaptured = 0;
			failure = true;
			return piecesCaptured;
		}
		
		return piecesCaptured;
	}
	
	public void captureIndex(int vert, int horiz, int player)
	{
		if (THIS_CAPTURE == CaptureType.CHANGE)
		{
			addMark(player, buttons[vert][horiz]);
			pointsPlaced[vert][horiz] = player;
		}
		else
		{
			buttons[vert][horiz].setClickable(true);
			buttons[vert][horiz].setText(BLANK_MARK + "");
			pointsPlaced[vert][horiz] = 0;
		}
	}
	
	//This function swaps the active player, updating the display accordingly.
	public void swapTurn()
	{
		String temp = "It's ";
		if (currentTurn == 1)
		{
			temp += playerTwoDisplay.getText().toString();
			currentTurn = 2;
			currentMark = PLAYER_TWO_MARK;
			activePlayerDisplay.setTextColor(playerTwoColour);
		}
		else
		{
			temp += playerOneDisplay.getText().toString();
			currentTurn = 1;
			currentMark = PLAYER_ONE_MARK;
			activePlayerDisplay.setTextColor(playerOneColour);
		}
		temp += "'s turn!";
		activePlayerDisplay.setText(temp);
	}
	
	//This function places the appropriate mark in the button that was pressed.
	//It then returns a value to be stored later in the pointsPlaced[][] array.
	public int addMark(int player, Button B)
	{
		if (player == 1)
		{
			B.setClickable(false);
        	B.setText(PLAYER_ONE_MARK + "");
        	B.setTextColor(playerOneColour);
        	return 1;
		}
		else
		{
			B.setClickable(false);
			B.setText(PLAYER_TWO_MARK + "");
    		B.setTextColor(playerTwoColour);
			return 2;
		}
	}
	
	private int[] countPieces()
	{
		int playerOneCount = 0;
    	int playerTwoCount = 0;
    	int value;
    	
    	for (int count = 0; count < pointsPlaced.length; count++)
    	{
    		for (int count2 = 0; count2 < pointsPlaced[count].length; count2++)
    		{
    			value = pointsPlaced[count][count2];
    			if (value == 1)
    			{
    				playerOneCount++;
    			}
    			else if (value == 2)
    			{
    				playerTwoCount++;
    			}
    		}
    	}
    	playerOnePiecesDisplay.setText(playerOneCount + "");
    	playerTwoPiecesDisplay.setText(playerTwoCount + "");
    	
    	return new int[] {playerOneCount, playerTwoCount};
	}
	
	//This function stops the sound player if it is already running, then plays a sound.
	//It then increments turnCount, which is used to track ties.
	//The button is set to be unclickable, to prevent it from responding to future presses.
	//addMark() gets the player and button, and adds the appropriate X or O to that spot.
	//It then cycles through the array of buttons until it finds the one that corresponds to the current button
	//		and saves a value to the appropriate index of the pointsPlaced[][] array.
	//		This is used to track evaluation and already placed points.
	//The function then evaluates the various possible ways to achieve victory (rows, columns, and diagonals).
	//If one of them returns true, the function skips to displaying the winner.
	//If none return true, then it simply ends.
	//If none return true and the turnCount has hit its maximum, then it displays the tie results.
	//It also swaps whose turn it is.
    private OnClickListener grid_handler = new OnClickListener() 
    { 
        public void onClick(View v) 
        { 
        	if (canPressButton == true)
        	{
        		canPressButton = false;
            	Button B = (Button) v;
            	boolean status = false;
            	int count = 0;
            	int count2 = 0;
            	for (count = 0; count < buttons.length && status == false; count++)
            	{
            		for (count2 = 0; count2 < buttons[count].length && status == false; count2++)
            		{
            			if (buttons[count][count2] == B)
            			{
            				status = true;
            			}
            		}
            	}
            	count--;
            	count2--;
            	//This decrement is to prevent an off-by-one error. When the for loops end, 
            	//		the counts are each one too high relative to the actual index of the button.
            	int presentTurn = currentTurn;
            	int capturedPieces = loopCaptures(count, count2, presentTurn, false);
            	
            	if (capturedPieces == 0)
            	{
            		String message = "That move is invalid.";
            		message += "\nAll moves must capture at least one of the opponent's pieces.";
            		warning.setText(message);
            		warning.setDuration(Toast.LENGTH_LONG);
            		warning.show();
            		//Reveal some button to allow a forced skip to the next player's turn here?
            		//		This button would be hidden on a successful move, and not be shown until a move has been failed.
            		
            		if ((failedAttempts.size() + turnCount + 1) >= TURN_LIMIT)
					{
						if (true == lastPlayerSkipped)
						{
							turnCount = TURN_LIMIT;
						}
						else
						{
							lastPlayerSkipped = true;
							clearFailures();
							String outOfMoves = "No legal moves are available.\n";
							if (currentTurn == 1)
							{
								outOfMoves += playerOneName;
							}
							else
							{
								outOfMoves += playerTwoName;
							}
							outOfMoves += "'s turn has been skipped.";
							warning.setText(outOfMoves);
		            		warning.show();
							swapTurn();
							canPressButton = true;
						}
					}
            		else
            		{
            			B.setClickable(false);
            			highlightFailedAttempt(B);
            			failedAttempts.enqueue(B);
            		}
            	}
            	else
            	{
            		lastPlayerSkipped = false;
            		if (lastCaptures.isEmpty() == false)
            		{
            			clearCaptureHighlights();
            		}
            		clearFailures();
            		capturedPieces = loopCaptures(count, count2, presentTurn, true);
	            	turnCount++;
	            	int value = addMark(presentTurn, B);
	            	B.setClickable(false);
	            	//B.setBackgroundColor(Color.WHITE);
	            	if (previousMove == null)
	            	{
	            		previousMove = B;
	            	}
	            	highlightPrevious(B);
	            	previousMove = B;
	            	pointsPlaced[count][count2] = value;
	            	
	            	if (soundPlayer != null)
	    			{
	    				soundPlayer.pause();
	    				//soundPlayer.stop();
	    			}
	            	soundPlayer = MediaPlayer.create(ReversiActivity.this, R.raw.doorbell_one);
	            	soundPlayer.start();
	            	countPieces();
	            	
	            	swapTurn();
            	}
            	
            	new CountDownTimer(500, 300)
            	{
        			@Override
        			public void onTick(long millisUntilFinished) 
        			{
        				
        			}

        			@Override
        			public void onFinish() 
        			{
        				canPressButton = true;
        			}
            	}.start();
            	
            	if (turnCount == TURN_LIMIT)
            	{
            		findWinner();
            	}
        	}
        }
    };
    
    public void clearFailures()
    {
    	while (failedAttempts.isEmpty() == false)
    	{
    		resetButtonBackground(failedAttempts.dequeue()).setClickable(true);
    	}
    }
    
    public Button highlightPrevious(Button B)
    {
    	if (highlightMoves == true)
    	{
    		if (previousMove != null && previousMove.getBackground() != BUTTON_BACK_CAPTURE)
        	{
        		previousMove.setBackground(BUTTON_BACK_NORMAL);
        	}
    		previousMove = B;
    		previousMove.setBackground(BUTTON_BACK_GLOW);
    	}
    	return B;
    }
    
    public Button highlightCapture(Button captured)
    {
    	if (highlightMoves == true)
    	{
    		captured.setBackground(BUTTON_BACK_CAPTURE);
    	}
    	return captured;
    }
    
    public Button highlightFailedAttempt(Button failure)
    {
    	if (highlightMoves == true)
    	{
    		failure.setBackground(BUTTON_BACK_FAILURE);
    	}
    	return failure;
    }
    
    public Button resetButtonBackground(Button B)
    {
    	if (highlightMoves == true)
    	{
    		B.setBackground(BUTTON_BACK_NORMAL);
    	}
    	return B;
    }
    
    public void clearCaptureHighlights()
    {
    	if (highlightMoves == true)
    	{
    		Button temp;
    		while (lastCaptures.isEmpty() == false)
    		{
    			temp = lastCaptures.dequeue();
    			if (temp != null)
    			{
    				resetButtonBackground(temp);
    			}
    		}
    	}
    }
    
    private OnClickListener save_listener = new OnClickListener()
    {
		@Override
		public void onClick(View v) 
		{
			if (usingGuestNames == false && gameInProgress == true)
			{
				saveFile();
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
					Toast.makeText(ReversiActivity.this, "This game has been saved as: " + name, Toast.LENGTH_SHORT).show();
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
				Toast.makeText(ReversiActivity.this, "This game has been saved as: " + name, Toast.LENGTH_SHORT).show();
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
    
    public void findWinner()
    {
    	int[] counts = countPieces();
    	int playerOneCount = counts[0];
    	int playerTwoCount = counts[1];
    	
    	if (playerOneCount > playerTwoCount)
    	{
    		displayWinner(1);
    	}
    	else if (playerTwoCount > playerOneCount)
    	{
    		displayWinner(2);
    	}
    	else
    	{
    		displayTie();
    	}
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
    	canPressButton = false;
    	resetData();
    	
    	endOfMatch = true;
    	final String temp2 = temp;
    	Toast.makeText(this, temp2, Toast.LENGTH_SHORT).show();
    	new CountDownTimer(3500, 2500)
    	{
			@Override
			public void onTick(long millisUntilFinished) 
			{
				
			}

			@Override
			public void onFinish() 
			{
				AlertDialog.Builder endMatchBuilder = new AlertDialog.Builder(ReversiActivity.this);
				endMatchBuilder.setTitle("The match has ended");
				endMatchBuilder.setMessage(temp2);
				endMatchBuilder.setCancelable(false);
				endMatchBuilder.setNeutralButton("Return to Main Menu", endMatch);
				
				endMatchDialog = endMatchBuilder.create();
				endMatchDialog.show();
			}
    	}.start();
    }
    
    //This is the dialog called when the match is finished.
    //It releases the sound player and launches the main menu, then destroys this activity.
    private DialogInterface.OnClickListener endMatch = new DialogInterface.OnClickListener() 
	{
		public void onClick(DialogInterface dialog,int id) 
		{
			if (soundPlayer != null)
			{
				soundPlayer.pause();
				soundPlayer.stop();
				soundPlayer.release();
			}
			dialog.dismiss();
			Intent intent = new Intent(ReversiActivity.this, MainMenuActivity.class);
			startActivity(intent);
			ReversiActivity.this.finish();
		}
	};
    
	//This function takes the winner as a boolean and prints the appropriate player's name.
	//It declares that player the winner, complete with the sound of fireworks.
	//It then calls endOfMatch() with an integer representing who won.
    public void displayWinner(int winner)
    {
    	soundPlayer = MediaPlayer.create(ReversiActivity.this,R.raw.fireworks_finale);
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
    
    

	//The onPause() function is used to save data from the game before it can be closed.
    //It also releases the media player if it is in use.
	@Override
	protected void onPause() 
	{
		// TODO Auto-generated method stub
		super.onPause();
		Log.d("Life Cycle", "Reversi Activity: onPause");
		//This method turns up when something partially obscures the activity.
		//A semi-transparent dialog box, for instance, would trigger this.
		//In most cases, however, the app will continue to onStop().
		//Release unneeded resources here, save data, etc.
		if (gameInProgress == true && usingGuestNames == false && endOfMatch == false)
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
		Log.d("Life Cycle", "Reversi Activity: onStop");
		//This method turns up when the activity is hidden, like when the user switches to another app.
		//Release all unneeded resources here, as the system may occasionally skip the onDestroy() if memory is exhausted.
		//Also save any data necessary.
	}

	@Override
	protected void onDestroy() 
	{
		// TODO Auto-generated method stub
		super.onDestroy();
		
		Log.d("Life Cycle", "Reversi Activity: onDestroy");
		//Note that the app is destroyed and recreated whenever the orientation changes.
	}

	@Override
	protected void onRestart() 
	{
		// TODO Auto-generated method stub
		super.onRestart();
		Log.d("Life Cycle", "Reversi Activity: onRestart");
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
        if (id == R.id.action_menu_about) 
        {
        	AboutMenu.displayAboutDialog(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    protected static Integer[] getPlayerCounts()
    {
    	return PLAYERS;
    }
    
    protected static String[][] getExtras()
    {
    	return EXTRAS;
    }
    
    protected static String[] getBoolExtras()
    {
    	return BOOLEAN_EXTRAS;
    }
}
