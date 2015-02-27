package tru.kyle.classicgameanthology;

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
	private static final String HIGHLIGHTS = "Highlight Moves?";
	private static final String REVEALS = "Keep Pieces Revealed?";
	protected static final String[] BOOLEAN_EXTRAS = {
		HIGHLIGHTS,
		REVEALS
	};
	
	public static final Integer[] PLAYERS = {2};
	public final Game THIS_GAME = Game.Stratego;
	public final GameByLayout THIS_LAYOUT = GameByLayout.stratego;
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stratego);
		
		Resources res = getResources();
        BUTTON_BACK_NORMAL = res.getDrawable(res.getIdentifier(this.BUTTON_BACK_NORMAL_STRING, "drawable", getPackageName()));
    	BUTTON_BACK_GLOW = res.getDrawable(res.getIdentifier(this.BUTTON_BACK_GLOW_STRING, "drawable", getPackageName()));
    	BUTTON_BACK_WARNING = res.getDrawable(res.getIdentifier(this.BUTTON_BACK_WARNING_STRING, "drawable", getPackageName()));
    	
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
		int temp = currentTurn;
		swapTurn();
		while (currentTurn != temp)
		{
			swapTurn();
		}
		//This loop forces the display to be updated correctly.
		
		buttonSize = getButtonDimensions();
    	Log.d("Stratego Scaling", "buttonSize = " + buttonSize);
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
		// TODO Auto-generated method stub
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
		highlightMoves = intent.getBooleanExtra(MainMenuActivity.EXTRA_BOOL_BASE_KEY + "1", false);
	}
	
	
	//Note that Stratego has two lake areas near the center of the board.
	//Those must not have listeners registered for them.
	//Additionally, onDrag listeners must only be registered for the player's pieces,
	//		and the player needs to be able to set up pieces before play.
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
	
	//
	//	Main Game Functions
	//
	
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
	
	//A function to check if a move would enter one of the "lakes" near the center is required.
	//		Take the destination button as a parameter, and have a separate 2D array with 
	//			the buttons that make up the lakes?
	
	//A function to hide all pieces from view, pending a canceled dialog to change turns.
	
	//A function that uses the player provided to hide/show pieces.
	
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
			    	int index;
			    	for (index = 0; index < currentRow.length && currentRow[index] != b; index++)
					{
						;
					}
			        //This state is used when the user drops the view on your drop zone. If you want to accept the drop,
			        //set the Handled value to true like before.
			        //
			        handled = true;
			        //It's also probably time to get a bit of the data associated with the drag to know what
			        //you want to do with the information.
			        //
			        int draggedColor = Integer.parseInt(event.getClipData().getItemAt(0).getText().toString());
			        rowValues[remainingGuesses][index] = draggedColor;
			        currentRow[index].setBackground(PEG_BACKGROUNDS[draggedColor]);
			        //Log.d("Mastermind", rowValues[remainingGuesses][index] + "");
			        break;
			    }
			    case DragEvent.ACTION_DRAG_ENDED:
			    {
			        //This is the final state, where you still have possibility to cancel the drop happened.
			        //You will generally want to set Handled to true.
			        //
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
			
			//Calculate the button's location, determine the piece currently there,
	        //		and use those parameters to calculate available movement points.
			//Register the onDragHandler for the appropriate buttons based on those results.
			//	For movement: 
			//		-Scouts have infinite straight-line movement (until they hit a barrier of any sort).
			//		-Bombs and the Flag cannot move at all.
			//		-All other pieces move a single square vertically or horizontally.
			//			-No diagonal movement is allowed for any piece, nor can pieces pass through other pieces.
			
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

