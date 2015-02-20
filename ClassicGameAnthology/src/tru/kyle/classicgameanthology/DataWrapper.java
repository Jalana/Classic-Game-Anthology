package tru.kyle.classicgameanthology;

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
