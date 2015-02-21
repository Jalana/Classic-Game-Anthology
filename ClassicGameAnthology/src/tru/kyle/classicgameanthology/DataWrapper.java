package tru.kyle.classicgameanthology;

//This class has been deprecated, and is no longer in use for the main program.

public class DataWrapper 
{
	public int[][] values;
	public int player;
	int turnCount;
	public String[] playerNames;
	public int[] captures;
	
	public DataWrapper(int[][] v, int p, int t, String[] n, int[] c)
	{
		values = v;
		player = p;
		turnCount = t;
		playerNames = n;
		captures = c;
	}
}
