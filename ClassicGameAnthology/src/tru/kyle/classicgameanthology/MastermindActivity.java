package tru.kyle.classicgameanthology;

/*
This file (MastermindActivity) is a part of the Classic Game Anthology application.
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

import java.util.Random;

import tru.kyle.classicgameanthology.FileSaver.*;
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
import android.view.Menu;
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
Retrieve the extra options: how?
	-Format as a string that should be split into integers?
		-This would allow the MainMenuActivity to pull options based on spinner values
			without having to know what the extras actually represent.
	-Use a series of keys, like OPTIONS_(count), and use a loop to pull the options into an array?
	-General solution: use a 2D string array in each class with string versions of the extra options?
		-When a game is selected in the MainMenuActivity, that activity would assemble the class name
			and use it to grab the extras. If no extras are found, simply return null.
			Alternatively, simply create the extras in each class, but extras=null within the class
			if no extras are needed.
		-Have a set of "extras" spinners that are hidden and shown based on how many entries are found.
			-Due to the limited amount of space for spinners, separate boolean and non-boolean extras?
		-Place them in the intent as "extras_1", "extras_2", etc., for the game activity to deal with.
-Parsing the extra options should be split into another function.
*/

public class MastermindActivity extends Activity 
{
	public static final Integer[] PLAYERS = {1};
	
	//These are pulled by the MainMenuActivity and used to fill spinners.
	private static final String GUESSES_12 = "12 Guesses";
	private static final String GUESSES_10 = "10 Guesses";
	private static final String GUESSES_8 = "8 Guesses";
	private static final String LENGTH_6 = "Code Length = 6";
	private static final String LENGTH_5 = "Code Length = 5";
	private static final String LENGTH_4 = "Code Length = 4";
	private static final String COLORS_10 = "10 Colors";
	private static final String COLORS_8 = "8 Colors";
	private static final String COLORS_6 = "6 Colors";
	private String guessesString;
	private String codeLengthString;
	private String colorsString;
	protected static final String[][] EXTRAS = {
		{GUESSES_12, GUESSES_10, GUESSES_8},
		{LENGTH_6, LENGTH_5, LENGTH_4},
		{COLORS_10, COLORS_8, COLORS_6}
	};
	
	//These are pulled by the MainMenuActivity and used to title checkboxes.
	//Defaults are to false.
	//When loading from a save file, retrieve as integers.
	//		If value > 0, then read as true.
	private static final String DUPLICATES = "Allow Duplicates?";
	private static final String DIFFICULTY = "Easy Difficulty?";
	protected static final String[] BOOLEAN_EXTRAS = {
		DUPLICATES,
		DIFFICULTY
	};
	public static final Game THIS_GAME = Game.Mastermind;
	public static final GameByLayout THIS_LAYOUT = GameByLayout.mastermind;
	
	private static final String COLOR_DRAG_KEY = "Index";
	
	public static final int VERTICAL_MAXIMUM = 12;
	public static final int HORIZONTAL_MAXIMUM = 6;
	public static final int VERTICAL_MINIMUM = 12;
	public static final int HORIZONTAL_MINIMUM = 4;
	private int vertLimit = VERTICAL_MAXIMUM;
	private int horizLimit = HORIZONTAL_MAXIMUM;
	final char PLAYER_ONE_MARK = 'X';
	final char PLAYER_TWO_MARK = 'O';
	final char BLANK_MARK = ' ';
	
	boolean gameInProgress = false;
	boolean gameEnded = false;
	boolean canPressButton;
	boolean usingGuestNames = false;
	
	char currentMark;
	int playerOneScore;
	int playerTwoScore;
	
	TextView duplicateHintDisplay;
	TextView easyModeHintDisplay;
	TextView activePlayerDisplay;
	
	MediaPlayer soundPlayer;
	RelativeLayout mainLayout;
	
	Player playerOne;
	Player playerTwo;
	String playerOneName;
	String playerTwoName;
	
	private static final String UNPLACED_PEG_STRING = "button_peg_unplaced_" + THIS_LAYOUT.toString();
	private static final String CORRECT_MARKER_STRING = "button_marker_correct_" + THIS_LAYOUT.toString();
	private static final String MISPLACED_MARKER_STRING = "button_marker_misplaced_" + THIS_LAYOUT.toString();
	private static final String WRONG_MARKER_STRING = "button_marker_wrong_" + THIS_LAYOUT.toString();
	private static final String UNPLACED_MARKER_STRING = "button_marker_unplaced_" + THIS_LAYOUT.toString();
	
	private static final String[] PEG_BACK_STRINGS = {
			"button_peg_red_" + THIS_LAYOUT.toString(),
			"button_peg_green_" + THIS_LAYOUT.toString(),
			"button_peg_blue_" + THIS_LAYOUT.toString(),
			"button_peg_magenta_" + THIS_LAYOUT.toString(),
			"button_peg_yellow_" + THIS_LAYOUT.toString(),
			"button_peg_cyan_" + THIS_LAYOUT.toString(),
			"button_peg_black_" + THIS_LAYOUT.toString(),
			"button_peg_white_" + THIS_LAYOUT.toString(),
			"button_peg_orange_" + THIS_LAYOUT.toString(),
			"button_peg_pink_" + THIS_LAYOUT.toString(),
	};
	
	private static final int[] AVAILABLE_PEG_COLORS = {Color.RED, Color.GREEN, Color.BLUE, 
			Color.MAGENTA, Color.YELLOW, Color.CYAN, 
			Color.BLACK, Color.WHITE,
			Color.rgb(255, 128, 0),		//Orange
			Color.rgb(255, 0, 128)		//Pink
	};
	
	private static final int CORRECT_COLOR = Color.BLACK;
	private static final int MISPLACED_COLOR = Color.WHITE;
	private static final int WRONG_COLOR = Color.TRANSPARENT;
	
	int[][] rowValues = new int[VERTICAL_MAXIMUM][HORIZONTAL_MAXIMUM];
	int[][] markerValues = new int[VERTICAL_MAXIMUM][HORIZONTAL_MAXIMUM];
	int[] codeSequence = new int[HORIZONTAL_MAXIMUM];
	
	Button[] currentRow = null;
	Button[] codeButtons = new Button[HORIZONTAL_MAXIMUM];
	Button[] colorButtons = new Button[AVAILABLE_PEG_COLORS.length];
	Button[][] buttons = new Button[VERTICAL_MAXIMUM][HORIZONTAL_MAXIMUM];
	Button[][] markers = new Button[VERTICAL_MAXIMUM][HORIZONTAL_MAXIMUM];
	String filenameGame;
	String filenameStandings;
	String filenameNames;
	
	AlertDialog endMatchDialog;
	AlertDialog saveDialog;
	AlertDialog overwriteDialog;
	
	Button saveGame;
	Button confirmGuess;
	
	boolean newMatch;
	boolean endOfMatch = false;
	
	private Drawable[] PEG_BACKGROUNDS = new Drawable[PEG_BACK_STRINGS.length];
	private Drawable UNPLACED_PEG;
	private Drawable CORRECT_MARKER;
	private Drawable MISPLACED_MARKER;
	private Drawable WRONG_MARKER;
	private Drawable UNPLACED_MARKER;
	
	MyQueue<Integer> colorsNoDuplicates = new MyQueue<Integer>();
	
	private static final int INVALID_ROW_VALUE = -1;
	private static final int INVALID_COLOR_VALUE = -3;
	private static final int CANCELED_GUESS = -4;
	private static final int CANCELED_CODE = -5;
	protected int buttonSize;
	int remainingGuesses = VERTICAL_MAXIMUM;
	int colorOptions = colorButtons.length;
	boolean isEasyDifficulty = false;
	boolean canHaveDuplicates = false;
	
	Button[][] tempButtons = new Button[vertLimit][horizLimit];
	Button[][] tempMarkers = new Button[vertLimit][horizLimit];
	Button[] tempColors = new Button[colorOptions];
	
	
	/*****
	 * Life Cycle Management
	 ****/
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mastermind);
		
		Resources res = getResources();
		String packageName = getPackageName();
		UNPLACED_PEG = res.getDrawable(res.getIdentifier(UNPLACED_PEG_STRING, "drawable", packageName));
		CORRECT_MARKER = res.getDrawable(res.getIdentifier(CORRECT_MARKER_STRING, "drawable", packageName));
		MISPLACED_MARKER = res.getDrawable(res.getIdentifier(MISPLACED_MARKER_STRING, "drawable", packageName));
		WRONG_MARKER = res.getDrawable(res.getIdentifier(WRONG_MARKER_STRING, "drawable", packageName));
		UNPLACED_MARKER = res.getDrawable(res.getIdentifier(UNPLACED_MARKER_STRING, "drawable", packageName));
		for (int count = 0; count < PEG_BACKGROUNDS.length; count++)
		{
			PEG_BACKGROUNDS[count] = res.getDrawable(res.getIdentifier(
					PEG_BACK_STRINGS[count], "drawable", packageName));
		}
    	
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
        
        duplicateHintDisplay = (TextView)findViewById(R.id.playerOne);
    	easyModeHintDisplay = (TextView)findViewById(R.id.playerTwo);
    	activePlayerDisplay = (TextView)findViewById(R.id.activePlayer);
    	
    	saveGame = (Button) findViewById(R.id.saveGame);
    	saveGame.setOnClickListener(save_listener);
    	confirmGuess = (Button) findViewById(R.id.confirmGuess);
    	confirmGuess.setOnClickListener(confirm_guess_handler);
        
        //Note that the rows count up from the bottom, approaching 0: they do not start at 0 or 1.
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
    	
		Log.d("Life Cycle", "Mastermind Activity: onCreate");
	}
	
	@Override
	protected void onRestart() 
	{
		// TODO Auto-generated method stub
		super.onRestart();
		Log.d("Life Cycle", "Mastermind Activity: onRestart");
	}
	
	@Override
	protected void onStart() 
	{
		// TODO Auto-generated method stub
		super.onStart();
		Log.d("Life Cycle", "Mastermind Activity: onStart");
	}
	
	@Override
	protected void onResume() 
	{
		// TODO Auto-generated method stub
		super.onResume();
		//This is a debugging sequence, checking to see if the buttons are assigned properly based on the corners.
		//If any null values turn up, that is a problem that will crash the program,
		//		but these logs will show where any null values might be.
		if (buttons[0][0] == null || buttons[buttons.length - 1][buttons[buttons.length - 1].length - 1] == null)
		{
			Log.d("Mastermind", "Null button found with length = " + buttons.length + "," + buttons[buttons.length - 1].length);
			for (int count = 0; count < buttons.length; count++)
			{
				for (int count2 = 0; count2 < buttons[count].length; count2++)
				{
					if (buttons[count][count2] == null)
					{
						Log.d("Mastermind", "Null button found at " + count + "," + count2);
					}
				}
			}
		}
		
		buttonSize = getButtonDimensions();
		int markerSize = buttonSize / 2;
		int colorSize = buttonSize * 4 / 3;
    	LayoutParams params;
    	Log.d("Mastermind Scaling", "buttonSize = " + buttonSize);
		for (int count = 0; count < buttons.length; count++)
		{
			for (int count2 = 0; count2 < buttons[count].length; count2++)
			{
				params = buttons[count][count2].getLayoutParams();
				params.height = buttonSize;
				params.width = buttonSize;
				buttons[count][count2].setLayoutParams(params);
				
				params = markers[count][count2].getLayoutParams();
				params.height = markerSize;
				params.width = markerSize;
				markers[count][count2].setLayoutParams(params);
			}
		}
		for (int count = 0; count < colorButtons.length; count++)
		{
			params = colorButtons[count].getLayoutParams();
			params.height = colorSize;
			params.width = colorSize;
			colorButtons[count].setLayoutParams(params);
		}
		for (int count = 0; count < codeButtons.length; count++)
		{
			params = codeButtons[count].getLayoutParams();
			params.height = buttonSize;
			params.width = buttonSize;
			codeButtons[count].setLayoutParams(params);
		}
		
		if (canHaveDuplicates == true)
		{
			duplicateHintDisplay.setText("Duplicates\nare allowed");
		}
		else
		{
			duplicateHintDisplay.setText("Duplicates\nare banned");
		}
		if (isEasyDifficulty == true)
		{
			easyModeHintDisplay.setText("Easy\nDifficulty");
		}
		else
		{
			easyModeHintDisplay.setText("Normal\nDifficulty");
		}
		
		Log.d("Life Cycle", "Mastermind Activity: onResume");
		
		if (endOfMatch == false)
		{
			activePlayerDisplay.setText("");
			gameInProgress = true;
		}
		
		soundPlayer = null;
	}
	
	@Override
	protected void onPause() 
	{
		// TODO Auto-generated method stub
		super.onPause();
		if (gameInProgress == true && usingGuestNames == false && endOfMatch == false)
		{
			saveGame(FileSaver.AUTOSAVE_NAME, true);
		}
		if (soundPlayer != null)
		{
			soundPlayer.release();
		}
		Log.d("Life Cycle", "Mastermind Activity: onPause");
	}
	
	@Override
	protected void onStop() 
	{
		// TODO Auto-generated method stub
		super.onStop();
		Log.d("Life Cycle", "Mastermind Activity: onStop");
	}
	
	@Override
	protected void onDestroy() 
	{
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.d("Life Cycle", "Mastermind Activity: onDestroy");
	}
	
	/*****
	 * Other Initialization Functions
	 ****/
	
	private void parseExtras(Intent intent)
	{
		canHaveDuplicates = intent.getBooleanExtra(MainMenuActivity.EXTRA_BOOL_BASE_KEY + "1", false);
		isEasyDifficulty = intent.getBooleanExtra(MainMenuActivity.EXTRA_BOOL_BASE_KEY + "2", false);
		
		switch (intent.getStringExtra(MainMenuActivity.EXTRA_STRING_BASE_KEY + "1"))
		{
			case GUESSES_12:
			{
				vertLimit = 12;
				guessesString = GUESSES_12;
				break;
			}
			case GUESSES_10:
			{
				vertLimit = 10;
				guessesString = GUESSES_10;
				break;
			}
			case GUESSES_8:
			{
				guessesString = GUESSES_8;
				vertLimit = 8;
				break;
			}
		}
		switch (intent.getStringExtra(MainMenuActivity.EXTRA_STRING_BASE_KEY + "2"))
		{
			case LENGTH_6:
			{
				codeLengthString = LENGTH_6;
				horizLimit = 6;
				break;
			}
			case LENGTH_5:
			{
				codeLengthString = LENGTH_5;
				horizLimit = 5;
				break;
			}
			case LENGTH_4:
			{
				codeLengthString = LENGTH_4;
				horizLimit = 4;
				break;
			}
		}
		switch (intent.getStringExtra(MainMenuActivity.EXTRA_STRING_BASE_KEY + "3"))
		{
			case COLORS_10:
			{
				colorsString = COLORS_10;
				colorOptions = 10;
				break;
			}
			case COLORS_8:
			{
				colorsString = COLORS_8;
				colorOptions = 8;
				break;
			}
			case COLORS_6:
			{
				colorsString = COLORS_6;
				colorOptions = 6;
				break;
			}
		}
	}
	
	private void generateCode()
	{
		codeSequence = new int[horizLimit];
		Random random = new Random();
		if (canHaveDuplicates == true)
		{
			for (int count = 0; count < horizLimit; count++)
			{
				codeSequence[count] = random.nextInt(colorOptions);
			}
		}
		else
		{
			int temp;
			colorsNoDuplicates.clear();
			for (int count = 0; count < colorOptions; count++)
			{
				colorsNoDuplicates.enqueue(count);
			}
			
			for (int count = 0; count < horizLimit; count++)
			{
				temp = random.nextInt(colorOptions - count);
				codeSequence[count] = colorsNoDuplicates.getAtIndex(temp);
				colorsNoDuplicates.remove(codeSequence[count]);
				Log.d("Mastermind Code", "count = " + count + ", temp = " + temp + ", codeSequence[count] = " + codeSequence[count]);
			}
		}
	}
	
	private void setButtons(Resources res)
	{
		buttons = new Button[VERTICAL_MAXIMUM][HORIZONTAL_MAXIMUM];
		markers = new Button[VERTICAL_MAXIMUM][HORIZONTAL_MAXIMUM];
		colorButtons = new Button[AVAILABLE_PEG_COLORS.length];
		currentRow = new Button[horizLimit];
    	rowValues = new int[vertLimit][horizLimit];
    	markerValues = new int[vertLimit][horizLimit];
    	codeSequence = new int[horizLimit];
    	remainingGuesses = vertLimit;
    	
        String currentID;
        int resID;
        int countVert = 0;
        int countHoriz = 0;
        
        //Important note: columns are cut only from the left for pegs and code, but only from the right for markers.
        //Rows are cut in pairs (one each from top and bottom at a time).
        int leftBound = (HORIZONTAL_MAXIMUM - horizLimit);
        int rightBound = HORIZONTAL_MAXIMUM;
        int topBound = (VERTICAL_MAXIMUM - vertLimit) / 2;
        int bottomBound = VERTICAL_MAXIMUM - (VERTICAL_MAXIMUM - vertLimit + 1) / 2;
        for (countVert = 0; countVert < buttons.length; countVert++)
        {
        	for (countHoriz = 0; countHoriz < buttons[countVert].length; countHoriz++)
        	{
        		currentID = "button_";
        		if ((countVert + 1) < 10)
        		{
        			currentID += '0';
        		}
        		currentID += (countVert + 1) + "_";
        		if ((countHoriz + 1) < 10)
        		{
        			currentID += '0';
        		}
        		currentID += (countHoriz + 1);
        		resID = res.getIdentifier(currentID, "id", getPackageName());
        		buttons[countVert][countHoriz] = (Button)findViewById(resID);
        		
        		//If the button's coordinates are outside any of the bounds, it must be excluded from the grid.
        		//	The buttons are initially registered so that they can be hidden if necessary,
        		//		since the default size for the grid is the maximum.
        		if (countVert < topBound || countVert >= bottomBound || 
        				countHoriz < leftBound || countHoriz >= rightBound)
        		{
        			buttons[countVert][countHoriz].setVisibility(View.GONE);
        		}
        		else
        		{
        			//buttons[countVert][countHoriz].setOnDragListener(grid_handler);
        			buttons[countVert][countHoriz].setBackground(UNPLACED_PEG);
        		}
        		//Given how the game plays, should listeners only be registered when the appropriate row comes up?
        	}
        }
        
        for (countHoriz = 0; countHoriz < codeButtons.length; countHoriz++)
    	{
    		currentID = "code_";
    		if ((countHoriz + 1) < 10)
    		{
    			currentID += '0';
    		}
    		currentID += (countHoriz + 1);
    		resID = res.getIdentifier(currentID, "id", getPackageName());
    		codeButtons[countHoriz] = (Button)findViewById(resID);
    		
    		//The code buttons should never be visible until the game ends.
    		codeButtons[countHoriz].setVisibility(View.GONE);
    	}
        
        for (int count = 0; count < colorButtons.length; count++)
    	{
    		currentID = "color_";
    		if ((count + 1) < 10)
    		{
    			currentID += '0';
    		}
    		currentID += (count + 1);
    		resID = res.getIdentifier(currentID, "id", getPackageName());
    		colorButtons[count] = (Button)findViewById(resID);
    		
    		if (count >= colorOptions)
    		{
    			colorButtons[count].setVisibility(View.GONE);
    		}
    		else
    		{
    			colorButtons[count].setOnTouchListener(start_drag_handler);
    			//colorButtons[count].setOnClickListener(start_drag_handler_2);
    		}
    	}
        
        leftBound = 0;
        rightBound = HORIZONTAL_MAXIMUM - (HORIZONTAL_MAXIMUM - horizLimit);
        topBound = (VERTICAL_MAXIMUM - vertLimit) / 2;
        bottomBound = VERTICAL_MAXIMUM - (VERTICAL_MAXIMUM - vertLimit + 1) / 2;
        for (countVert = 0; countVert < buttons.length; countVert++)
        {
        	for (countHoriz = 0; countHoriz < buttons[countVert].length; countHoriz++)
        	{
        		currentID = "marker_";
        		if ((countVert + 1) < 10)
        		{
        			currentID += '0';
        		}
        		currentID += (countVert + 1) + "_";
        		if ((countHoriz + 1) < 10)
        		{
        			currentID += '0';
        		}
        		currentID += (countHoriz + 1);
        		resID = res.getIdentifier(currentID, "id", getPackageName());
        		markers[countVert][countHoriz] = (Button)findViewById(resID);
        		
        		//If the button's coordinates are outside any of the bounds, it must be excluded from the grid.
        		//	The buttons are initially registered so that they can be hidden if necessary,
        		//		since the default size for the grid is the maximum.
        		if (countVert < topBound || countVert >= bottomBound || 
        				countHoriz < leftBound || countHoriz >= rightBound)
        		{
        			markers[countVert][countHoriz].setVisibility(View.GONE);
        		}
        		else
        		{
        			//Note that the markers do not require listeners.
        			markers[countVert][countHoriz].setBackground(UNPLACED_MARKER);
        		}
        	}
        }
        
        for (countVert = 0; countVert < rowValues.length; countVert++)
        {
        	for (countHoriz = 0; countHoriz < rowValues[countVert].length; countHoriz++)
        	{
        		rowValues[countVert][countHoriz] = INVALID_ROW_VALUE;
        		markerValues[countVert][countHoriz] = INVALID_COLOR_VALUE;
        	}
        }
        
        pruneArrays();
	}
	
	//Buttons need to be cut from the left (first) indexes of the buttons array.
	//With markers, however, they are cut from the right instead of the left.
	//Rows are cut equally from top and bottom.
	//A false return means that no pruning was necessary.
	private boolean pruneArrays()
	{
		final int leftBound = HORIZONTAL_MAXIMUM - horizLimit;
		final int rightBound = horizLimit;
		final int upperBound = (VERTICAL_MAXIMUM - vertLimit) / 2;
        final int lowerBound = VERTICAL_MAXIMUM - (VERTICAL_MAXIMUM - vertLimit + 1) / 2;
		if (rightBound == HORIZONTAL_MAXIMUM && lowerBound == VERTICAL_MAXIMUM)
		{
			return false;
		}
		
		tempButtons = new Button[vertLimit][horizLimit];
		tempMarkers = new Button[vertLimit][horizLimit];
		tempColors = new Button[colorOptions];
		Log.d("Mastermind", leftBound + ", " + rightBound + ", " + upperBound + ", " + lowerBound);
		
		for (int countVert = upperBound; countVert < lowerBound; countVert++)
		{
			for (int countHoriz = 0; countHoriz < HORIZONTAL_MAXIMUM; countHoriz++)
			{
				if (countHoriz >= leftBound)
				{
					rowValues[countVert - upperBound][countHoriz - leftBound] = INVALID_ROW_VALUE;
					tempButtons[countVert - upperBound][countHoriz - leftBound] = buttons[countVert][countHoriz];
				}
				if (countHoriz < rightBound)
				{
					markerValues[countVert - upperBound][countHoriz] = INVALID_COLOR_VALUE;
					tempMarkers[countVert - upperBound][countHoriz] = markers[countVert][countHoriz];
				}
			}
		}
		
		for (int countHoriz = 0; countHoriz < colorOptions; countHoriz++)
		{
			tempColors[countHoriz] = colorButtons[countHoriz];
		}
		
		buttons = tempButtons;
		markers = tempMarkers;
		colorButtons = tempColors;
		
		return true;
	}
	
	private int getButtonDimensions()
	{
		final int UPPER_WINDOW_LIMIT_DP = 10;
		final int LOWER_WINDOW_LIMIT_DP = 0;
		
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
		int statusBarHeight = 0;
		int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) 
		{
			statusBarHeight = getResources().getDimensionPixelSize(resourceId);
		}
		lowerBufferHeight += statusBarHeight;
		
		Log.d("Mastermind Scaling", "Height = " + height + ", Width = " + width
				+ ", upperBuffer = " + upperBufferHeight + ", lowerBuffer = " + lowerBufferHeight
				+ ", actionBarSize = " + actionBarSize + ", statusBarHeight = " + statusBarHeight);
		
		height -= upperBufferHeight;
		height -= lowerBufferHeight;
		
		//Due to the fact that Mastermind requires separate layout files for landscape versus portrait,
		//		the calculations will have to be modified based on the orientation.
		if (height > width)
		{
			//This indicates that the app is in portrait view.
			result = width;
			int divisor = 1;
			//This is a fixed buffer size designed to accommodate the buttons outside of the main grid:
			//		the code sequence, the two rows of buttons representing available colors, and some additional white space.
			final int BUTTON_BUFFER_COUNT = 4;
			if ((height / buttons.length) > (width / (buttons[0].length + (markers[0].length / 2))))
	    	{
	    		divisor = buttons[0].length + (markers[0].length / 2) + BUTTON_BUFFER_COUNT;
	    		Log.d("Mastermind Scaling", "divisor = width: " + divisor);
	    	}
	    	else
	    	{
	    		divisor = buttons.length + BUTTON_BUFFER_COUNT;
	    	}
			result = result / divisor;
		}
		else
		{
			//This indicates that the device is in landscape view.
			result = height;
			int divisor = 1;
			final int BUTTON_BUFFER_COUNT = 2;
			if (buttons.length < buttons[0].length)
	    	{
	    		divisor = buttons[0].length + (markers.length / 2) + BUTTON_BUFFER_COUNT;
	    	}
	    	else
	    	{
	    		divisor = buttons.length + BUTTON_BUFFER_COUNT;
	    	}
			result = result / divisor;
		}
		
		return result;
	}
	
	/*****
	 * Data Management Functions
	 ****/
	
	public void resetData()
	{
		for (int count = 0; count < buttons.length; count++)
		{
			for (int count2 = 0; count2 < buttons[count].length; count2++)
			{
				buttons[count][count2].setBackground(UNPLACED_PEG);
				markers[count][count2].setBackground(UNPLACED_MARKER);
			}
		}
		for (int count = 0; count < rowValues.length; count++)
    	{
    		for (int count2 = 0; count2 < rowValues[count].length; count2++)
    		{
    			rowValues[count][count2] = INVALID_ROW_VALUE;
    			markerValues[count][count2] = INVALID_COLOR_VALUE;
    		}
    	}
		remainingGuesses = vertLimit;
		generateCode();
		updateCurrentRow();
	}
	
	public void useGuestNames()
	{
		usingGuestNames = true;
		String temp = "One or more players could not be found. Default names are being used.";
		temp += "\nThis game cannot be saved.";
		Toast.makeText(this, temp, Toast.LENGTH_LONG).show();
	}
	
	//This particular function is only used with the loadGame() function.
	private void updateInterface()
	{
		int countVert;
		int countHoriz;
        for (countVert = 0; countVert < buttons.length; countVert++)
        {
        	for (countHoriz = 0; countHoriz < buttons[countVert].length; countHoriz++)
        	{
        		if (rowValues[countVert][countHoriz] >= 0)
        		{
        			buttons[countVert][countHoriz].setBackground(PEG_BACKGROUNDS[rowValues[countVert][countHoriz]]);
        		}
        		else
        		{
        			buttons[countVert][countHoriz].setBackground(UNPLACED_PEG);
        		}
        	}
        }
        
        for (countVert = 0; countVert < buttons.length; countVert++)
        {
        	for (countHoriz = 0; countHoriz < buttons[countVert].length; countHoriz++)
        	{
        		switch (markerValues[countVert][countHoriz])
        		{
	        		case CORRECT_COLOR:
					{
						markers[countVert][countHoriz].setBackground(CORRECT_MARKER);
						break;
					}
					case MISPLACED_COLOR:
					{
						markers[countVert][countHoriz].setBackground(MISPLACED_MARKER);
						break;
					}
					case WRONG_COLOR:
					{
						markers[countVert][countHoriz].setBackground(WRONG_MARKER);
						break;
					}
					default:
					{
						markers[countVert][countHoriz].setBackground(UNPLACED_MARKER);
						break;
					}
        		}
        	}
        }
	}
	
	public void loadGame()
	{
		ContentValues values = DBInterface.retrieveSave(this, filenameGame, THIS_GAME);
		vertLimit = values.getAsInteger(DBInterface.GRID_HEIGHT_KEY);
		horizLimit = values.getAsInteger(DBInterface.GRID_WIDTH_KEY);
		colorOptions = values.getAsInteger(DBInterface.COLOR_COUNT_KEY);
		if (values.getAsInteger(DBInterface.EXTRA_BOOL_BASE_KEY + "1") > 0)
		{
			canHaveDuplicates = true;
		}
		else
		{
			canHaveDuplicates = false;
		}
		if (values.getAsInteger(DBInterface.EXTRA_BOOL_BASE_KEY + "2") > 0)
		{
			isEasyDifficulty = true;
		}
		else
		{
			isEasyDifficulty = false;
		}
		
		setButtons(this.getResources());
		
		rowValues = DBInterface.stringToGrid(vertLimit, horizLimit, values.getAsString(DBInterface.GRID_VALUES_KEY));
		markerValues = DBInterface.stringToGrid(vertLimit, horizLimit, values.getAsString(DBInterface.MARKER_VALUES_KEY));
		codeSequence = DBInterface.stringToArray(values.getAsString(DBInterface.CODE_VALUES_KEY), horizLimit);
		
		updateInterface();
		
		remainingGuesses = values.getAsInteger(DBInterface.REMAINING_TURNS_KEY) + 1;
		updateCurrentRow();
		playerOneName = values.getAsString(DBInterface.PLAYER_BASE_KEY + "1");
		if (playerOneName == null)
		{
			useGuestNames();
		}
		
		guessesString = values.getAsString(DBInterface.EXTRA_STRING_BASE_KEY + "1");
		codeLengthString = values.getAsString(DBInterface.EXTRA_STRING_BASE_KEY + "2");
		colorsString = values.getAsString(DBInterface.EXTRA_STRING_BASE_KEY + "3");
	}
	
	public boolean saveGame(String name, boolean overwrite)
	{
		ContentValues values = new ContentValues();
		values.put(DBInterface.GAME_NAME_KEY, name);
		values.put(DBInterface.GRID_HEIGHT_KEY, rowValues.length);
		values.put(DBInterface.GRID_WIDTH_KEY, rowValues[0].length);
		values.put(DBInterface.GRID_VALUES_KEY, DBInterface.gridToString(rowValues));
		values.put(DBInterface.MARKER_VALUES_KEY, DBInterface.gridToString(markerValues));
		values.put(DBInterface.CODE_VALUES_KEY, DBInterface.arrayToString(codeSequence));
		values.put(DBInterface.COLOR_COUNT_KEY, colorOptions);
		values.put(DBInterface.REMAINING_TURNS_KEY, remainingGuesses);
		values.put(DBInterface.PLAYER_BASE_KEY + "1", playerOneName);
		
		values.put(DBInterface.EXTRA_STRING_BASE_KEY + "1", guessesString);
		values.put(DBInterface.EXTRA_STRING_BASE_KEY + "2", codeLengthString);
		values.put(DBInterface.EXTRA_STRING_BASE_KEY + "3", colorsString);
		if (canHaveDuplicates == true)
		{
			values.put(DBInterface.EXTRA_BOOL_BASE_KEY + "1", 1);
		}
		else
		{
			values.put(DBInterface.EXTRA_BOOL_BASE_KEY + "1", 0);
		}
		if (isEasyDifficulty == true)
		{
			values.put(DBInterface.EXTRA_BOOL_BASE_KEY + "2", 1);
		}
		else
		{
			values.put(DBInterface.EXTRA_BOOL_BASE_KEY + "2", 0);
		}
		
		return DBInterface.insertSave(getApplicationContext(), values, THIS_GAME, overwrite);
	}
	
	/*****
	 * Main Functions
	 ****/
	
	private void updateCurrentRow()
	{
		for (int count = 0; count < currentRow.length; count++)
		{
			if (currentRow[count] != null)
			{
				currentRow[count].setOnDragListener(null);
			}
		}
		remainingGuesses--;
		if (remainingGuesses >= 0)
		{
			currentRow = buttons[remainingGuesses];
			for (int count = 0; count < currentRow.length; count++)
			{
				currentRow[count].setOnDragListener(drag_handler);
			}
		}
		else
		{
			currentRow = null;
		}
	}
	
	
	
	
	
	private void displayCode()
	{
		for (int count = 0; count < codeSequence.length; count++)
		{
			codeButtons[count].setVisibility(View.VISIBLE);
			codeButtons[count].setBackground(PEG_BACKGROUNDS[codeSequence[count]]);
		}
	}
	
	/*****
	 * Listeners and Associated Functions
	 ****/
	
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
	
	//Useful link:
	//	http://developer.android.com/guide/topics/ui/drag-drop.html
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
			        /* To register your view as a potential drop zone for the current view being dragged
			         * you need to set the event as handled
			         */
			        handled = true;
			        /* An important thing to know is that drop zones need to be visible (i.e. their Visibility)
			         * property set to something other than Gone or Invisible) in order to be considered. A nice workaround
			         * if you need them hidden initially is to have their layout_height set to 1.
			         */
			        break;
			    }
			    case DragEvent.ACTION_DRAG_ENTERED:
			    case DragEvent.ACTION_DRAG_EXITED:
			    {
			        /* These two states allows you to know when the dragged view is contained atop your drop zone.
			         * Traditionally you will use that tip to display a focus ring or any other similar mechanism
			         * to advertise your view as a drop zone to the user.
			         */
			    	handled = true;
			        break;
			    }
			    case DragEvent.ACTION_DROP:
			    {
			    	Button b = (Button) v;
			    	int index;
			    	for (index = 0; index < currentRow.length && currentRow[index] != b; index++)
					{
						;
					}
			        /* This state is used when the user drops the view on your drop zone. If you want to accept the drop,
			         *  set the Handled value to true like before.
			         */
			        handled = true;
			        /* It's also probably time to get a bit of the data associated with the drag to know what
			         * you want to do with the information.
			         */
			        int draggedColor = Integer.parseInt(event.getClipData().getItemAt(0).getText().toString());
			        rowValues[remainingGuesses][index] = draggedColor;
			        currentRow[index].setBackground(PEG_BACKGROUNDS[draggedColor]);
			        //Log.d("Mastermind", rowValues[remainingGuesses][index] + "");
			        break;
			    }
			    case DragEvent.ACTION_DRAG_ENDED:
			    {
			        /* This is the final state, where you still have possibility to cancel the drop happened.
			         * You will generally want to set Handled to true.
			         */
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
	private View.OnTouchListener start_drag_handler = new OnTouchListener()
	{
		@Override
		public boolean onTouch(View v, final MotionEvent event) 
		{
			Button b = (Button) v;
			//b.performClick();
			int index = 0;
			for (index = 0; index < colorButtons.length && colorButtons[index] != b; index++)
			{
				;
			}
			ClipData data = ClipData.newPlainText(COLOR_DRAG_KEY, (index + ""));
			if (event.getAction() == MotionEvent.ACTION_DOWN)
			{
				b.startDrag(data, new View.DragShadowBuilder(b), null, 0);
			}
			return false;
		}
	};
	
	/*
	 * private View.OnClickListener start_drag_handler_2 = new OnClickListener()
	{
		@Override
		public void onClick(View v) 
		{
			Button b = (Button) v;
			int index = 0;
			for (index = 0; index < colorButtons.length && colorButtons[index] != b; index++)
			{
				;
			}
			ClipData data = ClipData.newPlainText(COLOR_DRAG_KEY, (index + ""));
			b.startDrag(data, new View.DragShadowBuilder(b), null, 0);
		}
	};
	*/
	
	//Use this when the user has filled in every peg in the row.
	private View.OnClickListener confirm_guess_handler = new OnClickListener()
	{
		@Override
		public void onClick(View v) 
		{
			boolean validGuess = true;
			for (int count = 0; count < rowValues[remainingGuesses].length; count++)
			{
				//Iterate through the current row. If any button is still flagged as unplaced, the guess is invalid.
				if (rowValues[remainingGuesses][count] < 0)
				{
					validGuess = false;
					break;
				}
			}
			
			if (validGuess == true)
			{
				int[] guess = rowValues[remainingGuesses];
				
				boolean result = evaluateGuess(guess, codeSequence);
				
				if (result == true)
				{
					//Run the end-of-match code.
					//Show the code, display toasts, etc.
					endOfMatch(true);
				}
				else if (remainingGuesses == 0)
				{
					//This means the player ran out of guesses.
					//End the match as a loss, show the code, display toasts, etc.
					endOfMatch(false);
				}
				else
				{
					//Continue the game.
					updateCurrentRow();
				}
			}
			else
			{
				Toast.makeText(MastermindActivity.this, "You must fill in all spaces of the row.", Toast.LENGTH_SHORT).show();
			}
		}
	};
	
	private boolean evaluateGuess(final int[] guess, final int[] code)
	{
		boolean finalResult = false;
		int correctCount = 0;
		
		//Copy the guess and the code sequence, since the evaluation process modifies the arrays.
		int[] results = new int[horizLimit];
		int[] tempSequence = new int[horizLimit];
		int[] tempGuess = new int[horizLimit];
		
		for (int count = 0; count < horizLimit; count++)
		{
			if (guess[count] == code[count])
			{
				//The canceled values are to ensure that an equality comparison with any other value will return false.
				tempSequence[count] = CANCELED_CODE;
				tempGuess[count] = CANCELED_GUESS;
				results[count] = CORRECT_COLOR;
				correctCount++;
			}
			else
			{
				tempSequence[count] = code[count];
				tempGuess[count] = guess[count];
				results[count] = WRONG_COLOR;
			}
		}
		
		if (correctCount >= horizLimit)
		{
			finalResult = true;
		}
		else
		{
			for (int countCode = 0; countCode < horizLimit; countCode++)
			{
				//If this condition check fails, there is no point to iterating through its combinations.
				if (tempSequence[countCode] != CANCELED_CODE)
				{
					for (int countGuess = 0; countGuess < horizLimit; countGuess++)
					{
						if (tempSequence[countCode] == tempGuess[countGuess])
						{
							results[countCode] = MISPLACED_COLOR;
						}
					}
				}
			}
		}
		
		//Evaluate pegs first by correct, then by misplaced, and wrong as a default.
		//		Save the results in an array.
		//			Use the CORRECT_COLOR, etc., constants to represent the values?
		displayGuessResults(results);
		return finalResult;
	}
	
	private void displayGuessResults(int[] results)
	{
		final Button[] currentRow = buttons[remainingGuesses];
		if (isEasyDifficulty == false)
		{
			results = sortGuessResults(results);
			//If on normal difficulty, sort the results (correct first, wrong last) before displaying the results.
			//	On easy difficulty, leave them unsorted.
		}
		
		for (int count = 0; count < currentRow.length; count++)
		{
			switch (results[count])
			{
				case CORRECT_COLOR:
				{
					markerValues[remainingGuesses][count] = CORRECT_COLOR;
					markers[remainingGuesses][count].setBackground(CORRECT_MARKER);
					break;
				}
				case MISPLACED_COLOR:
				{
					markerValues[remainingGuesses][count] = MISPLACED_COLOR;
					markers[remainingGuesses][count].setBackground(MISPLACED_MARKER);
					break;
				}
				case WRONG_COLOR:
				{
					markerValues[remainingGuesses][count] = WRONG_COLOR;
					markers[remainingGuesses][count].setBackground(WRONG_MARKER);
					break;
				}
			}
		}
	}
	
	public int[] sortGuessResults(int[] results)
	{
		int count = 0;
		int correctCount = 0;
		int misplacedCount = 0;
		int wrongCount = 0;
		for (count = 0; count < results.length; count++)
		{
			switch (results[count])
			{
				case CORRECT_COLOR:
				{
					correctCount++;
					break;
				}
				case MISPLACED_COLOR:
				{
					misplacedCount++;
					break;
				}
				case WRONG_COLOR:
				{
					wrongCount++;
					break;
				}
			}
		}
		
		count = 0;
		while (correctCount > 0)
		{
			results[count] = CORRECT_COLOR;
			count++;
			correctCount--;
		}
		while (misplacedCount > 0)
		{
			results[count] = MISPLACED_COLOR;
			count++;
			misplacedCount--;
		}
		while (wrongCount > 0)
		{
			results[count] = WRONG_COLOR;
			count++;
			wrongCount--;
		}
		return results;
	}
	
	public void endOfMatch(boolean playerWon)
    {
		displayCode();
		String temp = "";
		String[] winners = new String[1];
    	if (playerWon == true)
    	{
    		soundPlayer = MediaPlayer.create(MastermindActivity.this,R.raw.fireworks_finale);
        	soundPlayer.start();
        	temp = "Congratulations on cracking the code!";
        	winners[0] = playerOneName;
    	}
    	else
    	{
    		//If available, find an appropriate sound effect to play here.
    		temp = "You failed to crack the code.";
    		winners = null;
    	}
    	activePlayerDisplay.setText(temp);
    	
    	if (usingGuestNames == false)
    	{
    		DBInterface.updatePlayerScores(getApplicationContext(), 
    				new String[]{playerOneName}, THIS_GAME, winners);
    	}
    	//final String temp2 = temp;
    	//Toast.makeText(this, temp2, Toast.LENGTH_SHORT).show();
    	//activePlayer.setText(temp2);
    	
    	gameInProgress = false;
    	canPressButton = false;
    	endOfMatch = true;
    	
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
			MastermindActivity.this.finish();
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
					Toast.makeText(MastermindActivity.this, "This game has been saved as: " + name, Toast.LENGTH_SHORT).show();
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
				Toast.makeText(MastermindActivity.this, "This game has been saved as: " + name, Toast.LENGTH_SHORT).show();
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
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.mastermind, menu);
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
