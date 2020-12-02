import kalahgame.Board;
import kalahgame.Kalah;
import kalahgame.Move;
import kalahgame.Side;
import kalahplayer.Heuristic;
import kalahplayer.KalahPlayer;
import kalahplayer.mcts.MonteCarloTreeSearch;
import protocol.InvalidMessageException;
import protocol.MsgType;
import protocol.Protocol;
import utils.Log;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class Agent {
  private static final Logger LOGGER = Log.getLogger(Agent.class);

  protected Kalah kalah;
  protected int holes;
  // protected int maxDepth; // for future use

  public Agent(final int holes, final int seeds) {
    this.holes = holes;
    this.kalah = new Kalah(new Board(holes, seeds), Side.SOUTH);
    // this.maxDepth = 4; For future use (Alpha beta pruning or min max)
  }

  protected void swap() {
    kalah.setMySide(kalah.getMySide().opposite());
  }

  // Method for choosing the next kalahgame.Move currently a stub
  protected int bestNextMove() {
    List<Move> allPossibleMoves = kalah.getAllPossibleMoves();
    return allPossibleMoves.get(allPossibleMoves.size() - 1).getHole();
  }

  public void play() throws IOException, InvalidMessageException {
    LOGGER.info("Starting game...");

    KalahPlayer player = new MonteCarloTreeSearch(kalah);
    int bestMove = player.getBestMove(kalah);
    LOGGER.info("Best move for current state is " + bestMove);

    String msg = Main.recvMsg();
    MsgType msgType = Protocol.getMessageType(msg);
    // If the message is END, end the game.
    if (msgType == MsgType.END) {
      return;
    }
    // If the message is not START, throws error.
    if (msgType != MsgType.START) {
      throw new InvalidMessageException("Expected a start message but got something else.");
    }
    // If the start message is SOUTH
    // Means we are first player
    if (Protocol.interpretStartMsg(msg)) {
      kalah.setMySide(Side.SOUTH);
      Main.sendMsg(Heuristic.firstMove());
    }
    else {
      kalah.setMySide(Side.NORTH);
    }

    while (true) {
      msg = Main.recvMsg();
      msgType = Protocol.getMessageType(msg);

      if (msgType == MsgType.END) {
        return;
      }
      if (msgType != MsgType.STATE) {
        throw new InvalidMessageException("Expected a state message but got something else.");
      }
      // Check if extra move turn
      final Protocol.MoveTurn moveTurn = Protocol.interpretStateMsg(msg, this.kalah.getBoard());
      if (moveTurn.move == -1) {
        this.swap();
      }
      // If not skip the current iteration
      if (!moveTurn.again || moveTurn.end) {
        continue;
      }

      // If can swap
      msg = Protocol.createMoveMsg(this.bestNextMove());
      Main.sendMsg(msg);
    }
  }
}
