package kalahgame;

import java.util.Objects;

/**
 * Represents a move (not a turn) in the Kalah game.
 */
public class Move
{
	public static final int SWAP = -1;

	/**
	 * The side of the board the player making the move is playing on.
	 */
	private final Side side;
	/**
	 * The hole from which seeds are picked at the beginning of the move and
	 * distributed. It has to be >= 1.
	 */
	private final int hole;


	/**
     * @param side The side of the board the player making the move is playing
     *        on.
     * @param hole The hole from which seeds are picked at the beginning of
     *        the move and distributed. It has to be >= 1.
     * @throws IllegalArgumentException if the hole number is not >= 1.
     */
    public Move (Side side, int hole)
    {
    	if (hole < 1 && hole != SWAP)
    		throw new IllegalArgumentException("Hole numbers must be >= 1, but " + hole + " was given.");
    	if (side == null)
    		throw new NullPointerException("Side cannot be null");

    	this.side = side;
    	this.hole = hole;
    }

    /**
     * @return The side of the board the player making the move is playing on.
     */
    public Side getSide()
    {
		return side;
    }

    /**
     * @return The hole from which seeds are picked at the beginning of the
     *         move and distributed. It will be >= 1.
     */
    public int getHole()
    {
		return hole;
    }

    public boolean isSwap() {
    	return hole == SWAP && side == Side.NORTH;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Move move = (Move) o;
		return hole == move.hole && side == move.side;
	}

	@Override
	public int hashCode() {
		return Objects.hash(side, hole);
	}

	@Override
	public String toString() {
		return "Move{" +
				"side=" + side +
				", hole=" + hole +
				'}';
	}
}
