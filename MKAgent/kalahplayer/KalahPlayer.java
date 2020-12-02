package kalahplayer;

import kalahgame.Kalah;

/**
 * Represents a Kalah player.
 */
public interface KalahPlayer {
    /**
     * Should return the best possible move from the current state of the game.
     *
     * @return The hole number to move
     * @param state
     */
    int getBestMove(Kalah state);

    /**
     * Updates the state of the Board and in-turn the root of the tree by making a move
     * from the current state and setting the root to be whichever child which has the
     * same state.
     *
     * @param move Move to make to the current state
     */
    void performMove(int move);
}
