import kalahgame.Board;
import kalahgame.Kalah;
import kalahgame.Move;
import kalahgame.Side;
import kalahplayer.Heuristic;
import protocol.InvalidMessageException;
import protocol.MsgType;
import protocol.Protocol;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class Agent {
  private static final Logger LOGGER = Log.getLogger(Agent.class);

  protected Kalah kalah;
  protected int holes;
  // protected int maxDepth; // for future use

  public Agent(final int holes, final int seeds) throws IOException {
    this.holes = holes;
    this.kalah = new Kalah(new Board(holes, seeds), Side.SOUTH);
    // this.maxDepth = 4; For future use (Alpha beta pruning or min max)
  }

  // ---------------------------------------------------------------------------------------------------------------

  protected void swap() {
    kalah.setMySide(kalah.getMySide().opposite());
  }

  // Method for choosing the next kalahgame.Move currently a stub
  protected int bestNextMove() throws CloneNotSupportedException, IOException {
    int bestMove = 0;
    List<Move> allPossibleMoves = kalah.getAllPossibleMoves();
    bestMove = allPossibleMoves.get(allPossibleMoves.size() - 1).getHole();
    return bestMove;
  }

  public void play() throws IOException, InvalidMessageException, CloneNotSupportedException {

    LOGGER.info("Starting game...");
    // Receive Message
    String msg = Main.recvMsg();
    // Choose the Message Type
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
      // First kalahgame.Move
      Main.sendMsg(Heuristic.firstMove());
    }
    else {
      // Means we are second player
      kalah.setMySide(Side.NORTH);
    }
    // Game Loop
    while (true) {
      // Receive System Message
      msg = Main.recvMsg();
      // Get MSG type
      msgType = Protocol.getMessageType(msg);
      // If the MSG is END
      // Return to the main
      if (msgType == MsgType.END) {
        return;
      }
      // If the MSG is not the STATE
      // Throws Exception
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
      msg = null;

      int nextMove = this.bestNextMove();


     // If can swap
      if (msg == null) {
        msg = Protocol.createMoveMsg(nextMove);
      }
      Main.sendMsg(msg);
    }
  }
}
