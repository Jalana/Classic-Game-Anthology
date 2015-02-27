package tru.kyle.classicgameanthology;

/*
This file (ConnectFourActivity) is a part of the Classic Game Anthology application.
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

import tru.kyle.classicgameanthology.FileSaver.Game;
import tru.kyle.classicgameanthology.FileSaver.GameByLayout;
import tru.kyle.databases.DBInterface;
import tru.kyle.mylists.MyQueue;

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
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


public class ConnectFourActivity extends Activity 
{
	protected static final String[][] EXTRAS = null;
	
	//These are pulled by the MainMenuActivity and used to title checkboxes.
	protected static final String[] BOOLEAN_EXTRAS = {
		"Highlight Moves?"
	};
	
	public static final Integer[] PLAYERS = {2};
	public final Game THIS_GAME = Game.ConnectFour;
	public final GameByLayout THIS_LAYOUT = GameByLayout.connect_four;
	private final String BUTTON_BACK_NORMAL_STRING = "button_border_normal_" + THIS_LAYOUT.toString();
	private final String BUTTON_BACK_GLOW_STRING = "button_border_glowing_" + THIS_LAYOUT.toString();
	private final String BUTTON_BACK_WARNING_STRING = "button_border_warning_" + THIS_LAYOUT.toString();
	
	public static final int VERTICAL_LIMIT = 7;
	public static final int HORIZONTAL_LIMIT = 7;
	public static final int TURN_LIMIT = VERTICAL_LIMIT * HORIZONTAL_LIMIT;
	public static final int SEQUENCE_TO_WIN = 4;
	final char PLAYER_ONE_MARK = 'X';
	final char PLAYER_TWO_MARK = 'O';
	final char BLANK_MARK = ' ';
	
	boolean gameInProgress = false;
	boolean gameEnded = false;
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
	
	MediaPlayer soundPlayer;
	RelativeLayout mainLayout;
	
	Player playerOne;
	Player playerTwo;
	String playerOneName;
	String playerTwoName;
	
	int playerOneColour = Color.RED;
	int playerTwoColour = Color.BLUE;
	
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
	
	boolean newMatch;
	boolean endOfMatch = false;
	boolean highlightMoves = true;
	MyQueue<Button> lastLines = new MyQueue<Button>();
	MyQueue<Button> tempLines = new MyQueue<Button>();
	
	private Drawable BUTTON_BACK_NORMAL;
	private Drawable BUTTON_BACK_GLOW;
	private Drawable BUTTON_BACK_WARNING;
	
	protected int buttonSize;
	ViewTreeObserver observer;
	
	//The onCreate() function is used to set filenames and link variables to the displays.
	//It also links the buttons to the appropriate indexes of the buttons[][] array based on their names.
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_four);
        
        Resources res = getResources();
        BUTTON_BACK_NORMAL = res.getDrawable(res.getIdentifier(this.BUTTON_BACK_NORMAL_STRING, "drawable", getPackageName()));
    	BUTTON_BACK_GLOW = res.getDrawable(res.getIdentifier(this.BUTTON_BACK_GLOW_STRING, "drawable", getPackageName()));
    	BUTTON_BACK_WARNING = res.getDrawable(res.getIdentifier(this.BUTTON_BACK_WARNING_STRING, "drawable", getPackageName()));
    	
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
    	
		Log.d("Life Cycle", "Connect Four Activity: onCreate");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.connect_four, menu);
        return true;
    }
    
    @Override
	protected void onStart() 
    {
		super.onStart();
		Log.d("Life Cycle", "Connect Four Activity: onStart");
		//This is called between onCreate() and onResume().
		//onRestart() also leads to this.
	}

	//The onResume() function reads in data from files and sets it accordingly, then continues to the actual game.
	@Override
	protected void onResume() 
	{
		super.onResume();
		Log.d("Life Cycle", "Connect Four Activity: onResume");
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
		
		buttonSize = getButtonDimensions();
    	Log.d("ConnectFour Scaling", "buttonSize = " + buttonSize);
    	LayoutParams params;
		for (int count = 0; count < buttons.length; count++)
		{
			for (int count2 = 0; count2 < buttons[count].length; count2++)
			{
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
		Log.d("ConnectFour Scaling", "playerOneDisplay height = " + lowerBufferHeight);
		int statusBarHeight = 0;
		int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) 
		{
			statusBarHeight = getResources().getDimensionPixelSize(resourceId);
		}
		lowerBufferHeight += statusBarHeight;
		
		Log.d("ConnectFour Scaling", "Height = " + height + ", Width = " + width
				+ ", upperBuffer = " + upperBufferHeight + ", lowerBuffer = " + lowerBufferHeight
				+ ", actionBarSize = " + actionBarSize + ", statusBarHeight = " + statusBarHeight);
		
		height -= upperBufferHeight;
		height -= lowerBufferHeight;
		/*
		Point size = new Point();
		display.getSize(size);
		//int width = size.x;
		//int height = size.y;
		Log.d("Scaling", size.x + " wide by " + size.y);
		*/
		
		//		Problem: getRealMetrics is only supported by API 17 and higher.
		//display.getRealMetrics(metrics);
		//Log.d("Scaling", metrics.heightPixels + " high by " + metrics.widthPixels);
		
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
		turnCount = 0;
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
	
	
	
	
	
	//This function checks for any chips that need to fall.
	//Due to how the arrays are structured, the highest index of the array is the lowest button on the display.
	//As such, when making a chip "fall", the indexes must increase until it finds a match.
	public Button checkGravity(Button b)
	{
		Button result = b;
		boolean status = false;
		int vertIndex = 0;
		int horizIndex = 0;
		for (int count = 0; count < buttons.length && status == false; count++)
    	{
    		for (int count2 = 0; count2 < buttons[count].length && status == false; count2++)
    		{
    			if (buttons[count][count2] == b)
    			{
    				vertIndex = count;
    				horizIndex = count2;
    				status = true;
    			}
    		}
    	}
		status = false;
		
		for (int count = vertIndex; count < buttons.length && pointsPlaced[count][horizIndex] == 0; count++)
		{
			result = buttons[count][horizIndex];
		}
		
		return result;
	}
	
	
	
	private boolean loopLines(int vertStart, int horizStart, int player)
	{
		boolean result = false;
		int temp = 0;
		int vertInc = 0;
		int horizInc = 1;
		
		temp = checkLineBiDirectional(vertStart, horizStart, vertInc, horizInc, player);
		if (temp >= SEQUENCE_TO_WIN)
		{
			result = true;
		}
		//This first check is for a horizontal line.
		vertInc++;
		for (int count = 0; count < 3; count++)
		{
			temp = checkLineBiDirectional(vertStart, horizStart, vertInc, horizInc, player);
			if (temp >= SEQUENCE_TO_WIN)
			{
				result = true;
			}
			horizInc = (horizInc + 2) % 3 - 1;
			//This expression will make the value loop from 0 to 1, to -1, and back to 0.
		}
		
		return result;
	}
	
	private int checkLineBiDirectional(int vertStart, int horizStart, int vertInc, int horizInc, int player)
	{
		int result = checkLine(vertStart, horizStart, vertInc, horizInc, player);
		result += checkLine(vertStart, horizStart, vertInc * -1, horizInc * -1, player);
		result--;
		//This decrement is to prevent an off-by-one error.
		if ((result + 1) >= SEQUENCE_TO_WIN)
		{
			while (tempLines.isEmpty() == false)
			{
				lastLines.enqueue(tempLines.dequeue());
			}
		}
		tempLines.clear();
		
		return result;
	}
	
	private int checkLine(int vertStart, int horizStart, int vertInc, int horizInc, int player)
	{
		if (vertInc == 0 && horizInc == 0)
		{
			return 0;
		}
		//This statement prevents an infinite loop;
		//		if both increments were 0, the loop would be locked to its starting point.
		int lineLength = 1;
		int countVert = vertStart;
		int countHoriz = horizStart;
		try
		{
			for (countVert += vertInc, countHoriz += horizInc; ; countVert += vertInc, countHoriz += horizInc)
			{
				if (pointsPlaced[countVert][countHoriz] != player)
				{
					break;
				}
				else
				{
					lineLength++;
					if (true == highlightMoves)
					{
						tempLines.enqueue(buttons[countVert][countHoriz]);
					}
				}
			}
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			
		}
		return lineLength;
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
        	B.setText(PLAYER_ONE_MARK + "");
        	B.setTextColor(playerOneColour);
        	return 1;
		}
		else
		{
			B.setText(PLAYER_TWO_MARK + "");
    		B.setTextColor(playerTwoColour);
			return 2;
		}
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
        	if (endOfMatch == true)
        	{
        		canPressButton = false;
        		endMatch.onClick(v);
        	}
        	
        	if (canPressButton == true)
        	{
        		canPressButton = false;
            	
            	Button B = (Button) v;
            	boolean result = false;
            	boolean status = false;
            	int presentTurn = currentTurn;
            	turnCount++;
            	
            	B = checkGravity(B);
            	int value = addMark(presentTurn, B);
            	B.setClickable(false);
            	//B.setBackgroundColor(Color.WHITE);
            	if (previousMove == null)
            	{
            		previousMove = B;
            	}
            	highlightPrevious(B);
            	previousMove = B;
            	
            	int count = 0;
            	int count2 = 0;
            	for (count = 0; count < buttons.length && status == false; count++)
            	{
            		for (count2 = 0; count2 < buttons[count].length && status == false; count2++)
            		{
            			if (buttons[count][count2] == B)
            			{
            				pointsPlaced[count][count2] = value;
            				status = true;
            			}
            		}
            	}
            	count--;
            	count2--;
            	//This decrement is to prevent an off-by-one error. When the for loops end, 
            	//		the counts are each one too high relative to the actual index of the button.
            	if (lastLines.isEmpty() == false)
            	{
            		clearLineHighlights();
            	}
            	
            	result = loopLines(count, count2, presentTurn);
            	if (lastLines.isEmpty() == false && true == highlightMoves)
            	{
            		highlightMoves(B);
            	}
            	if (soundPlayer != null)
    			{
    				soundPlayer.pause();
    				//soundPlayer.stop();
    			}
            	if (lastLines.isEmpty() == false && true == highlightMoves)
            	{
            		soundPlayer = MediaPlayer.create(ConnectFourActivity.this, R.raw.fireworks_explosion);
            	}
            	else
            	{
            		soundPlayer = MediaPlayer.create(ConnectFourActivity.this, R.raw.doorbell_one);
            	}
            	soundPlayer.start();
            	
            	swapTurn();
            	
            	if (result == true)
            	{
            		displayWinner(presentTurn);
            	}
            	else if (turnCount == TURN_LIMIT)
            	{
            		displayTie();
            	}
            	else
            	{
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
            	}
        	}
        }
    };
    
    public void highlightPrevious(Button B)
    {
    	if (highlightMoves == true)
    	{
    		if (previousMove != null)
        	{
        		previousMove.setBackground(BUTTON_BACK_NORMAL);
        	}
    		previousMove = B;
    		previousMove.setBackground(BUTTON_BACK_GLOW);
    	}
    }
    
    public void highlightMoves(Button start)
    {
    	if (highlightMoves == true)
    	{
    		Button temp;
    		int temp2 = lastLines.size();
    		for (int count = 0; count < temp2; count++)
    		{
    			temp = lastLines.dequeue();
    			if (temp != null)
    			{
    				temp.setBackground(BUTTON_BACK_WARNING);
    			}
    			lastLines.enqueue(temp);
    		}
    	}
    }
    
    public void clearLineHighlights()
    {
    	if (highlightMoves == true)
    	{
    		Button temp;
    		while (lastLines.isEmpty() == false)
    		{
    			temp = lastLines.dequeue();
    			if (temp != null)
    			{
    				temp.setBackground(BUTTON_BACK_NORMAL);
    			}
    		}
    	}
    }
    
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
					Toast.makeText(ConnectFourActivity.this, "This game has been saved as: " + name, Toast.LENGTH_SHORT).show();
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
				Toast.makeText(ConnectFourActivity.this, "This game has been saved as: " + name, Toast.LENGTH_SHORT).show();
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
    	
    	canPressButton = false;
    	gameInProgress = false;
    	resetData();
    	
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
			ConnectFourActivity.this.finish();
		}
	};
    
	//This function takes the winner as a boolean and prints the appropriate player's name.
	//It declares that player the winner, complete with the sound of fireworks.
	//It then calls endOfMatch() with an integer representing who won.
    public void displayWinner(int winner)
    {
    	soundPlayer = MediaPlayer.create(ConnectFourActivity.this,R.raw.fireworks_finale);
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
		Log.d("Life Cycle", "Connect Four Activity: onPause");
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
		Log.d("Life Cycle", "Connect Four Activity: onStop");
		//This method turns up when the activity is hidden, like when the user switches to another app.
		//Release all unneeded resources here, as the system may occasionally skip the onDestroy() if memory is exhausted.
		//Also save any data necessary.
	}

	@Override
	protected void onDestroy() 
	{
		// TODO Auto-generated method stub
		super.onDestroy();
		
		Log.d("Life Cycle", "Connect Four Activity: onDestroy");
		//Note that the app is destroyed and recreated whenever the orientation changes.
	}

	@Override
	protected void onRestart() 
	{
		// TODO Auto-generated method stub
		super.onRestart();
		Log.d("Life Cycle", "Connect Four Activity: onRestart");
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
