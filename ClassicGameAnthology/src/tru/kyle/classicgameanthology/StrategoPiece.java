package tru.kyle.classicgameanthology;

public class StrategoPiece 
{
	private static final String LOGTAG = "StrategoPiece";
	
	//Note that the rank order is very specific.
	//As encoded, match-ups can be compared using ordinals.
	//		Note that this game is using the lowest-rank-wins ordering, where the Marshall is 1,
	//			instead of the European sorting where the highest rank wins.
	//		Exceptional cases (Spy attacking Marshall, Miner attacking Bomb)
	//			must still be accounted for.
	public static enum RankValues {
		Bomb,
		Marshall,
		General,
		Colonel,
		Major,
		Captain,
		Lieutenant,
		Sergeant,
		Miner,
		Scout,
		Spy,
		Flag
	};
	
	private RankValues rank;
	private boolean exposed;
	private int owner;
	private int location_x;
	private int location_y;
	
	public StrategoPiece(RankValues newRank, int newOwner, int x, int y)
	{
		exposed = false;
		rank = newRank;
		owner = newOwner;
		location_x = x;
		location_y = y;
	}
	
	public RankValues getRank()
	{
		return rank;
	}
	
	public int getOwner()
	{
		return owner;
	}
	
	/*****
	 * Retrieves the x-coordinate of the piece.
	 * @return an int representing the piece's current x-coordinate.
	 *****/
	public int getLocationX()
	{
		return location_x;
	}
	
	/*****
	 * Retrieves the y-coordinate of the piece.
	 * @return an int representing the piece's current y-coordinate.
	 *****/
	public int getLocationY()
	{
		return location_y;
	}
	
	public boolean isExposed()
	{
		return exposed;
	}
	
	public void revealPiece()
	{
		exposed = false;
	}
	
	public void hidePiece()
	{
		exposed = true;
	}
	
	public void updateCoordinates(int x, int y)
	{
		location_x = x;
		location_y = y;
	}
	
	/****
	 * This function returns a representation of the rank for use in displaying on the board.
	 * <p>
	 * Although it returns a String, it can be used as a char without loss of information unless
	 * more ranks are added, as at present all possible return values are of a single-character 
	 * String.
	 ****/
	public String getRankAsString()
	{
		RankValues tempRank = getRank();
		if (tempRank == RankValues.Bomb)
		{
			return "B";
		}
		else if (tempRank == RankValues.Flag)
		{
			return "F";
		}
		else if (tempRank == RankValues.Spy)
		{
			return "S";
		}
		else
		{
			return (tempRank.ordinal() + "");
		}
	}
	
	/****
	 * This function determines how far a unit can move based on its rank.
	 * <br>
	 * The typical return is one space, but some units cannot move or are able to
	 * move more than one space per turn.
	 * 
	 * @return an integer representing how far the unit can move.
	 */
	public int getMovementRange()
	{
		RankValues tempRank = getRank();
		if (tempRank == RankValues.Bomb || tempRank == RankValues.Flag)
		{
			return 0;
		}
		else if (tempRank == RankValues.Scout)
		{
			//Scouts have indefinite movement in any direction.
			return 10;
		}
		else
		{
			return 1;
		}
	}
	
	/****
	 * Evaluates the result of combat between the two pieces provided.
	 * 
	 * @param attacker : the piece initiating the attack.
	 * @param defender : the piece under attack.
	 * @return the victorious piece, or null if a tie occurred.
	 */
	public static StrategoPiece evaluateCombat(StrategoPiece attacker, StrategoPiece defender)
	{
		int offense = attacker.getRank().ordinal();
		int defense = defender.getRank().ordinal();
		
		if (offense == RankValues.Spy.ordinal() && defense == RankValues.Marshall.ordinal())
		{
			//Spy attacking Marshall results in a win for the Spy, even though the Spy loses
			//		all other combats (apart from capturing the Flag).
			return attacker;
		}
		else if (offense == RankValues.Miner.ordinal() && defense == RankValues.Bomb.ordinal())
		{
			//Miner attacking Bomb results in a win for the Miner, even though the Bomb wins
			//		over any other unit.
			//Coding a case for the reverse scenario is unnecessary, since Bombs cannot be attackers.
			return attacker;
		}
		else if (offense == defense)
		{
			return null;
		}
		else if (offense < defense)
		{
			return attacker;
		}
		else
		{
			return defender;
		}
	}
}
