import kalahgame.Board;
import kalahgame.Kalah;
import kalahgame.Move;
import kalahgame.Side;
import kalahplayer.Heuristic;
import protocol.InvalidMessageException;
import protocol.MsgType;
import protocol.Protocol;

import java.io.IOException;


public class Agent {

  protected Side ourSide;
  protected Kalah kalah;
  protected int holes;
  // protected int maxDepth; // for future use

  public Agent(final int holes, final int seeds) throws IOException {
    this.ourSide = Side.SOUTH;
    this.holes = holes;
    this.kalah = new Kalah(new Board(holes, seeds));
    // this.maxDepth = 4; For future use (Alpha beta pruning or min max)
  }

//---------------------------------------------------------------------------------------------------------------


  protected void swap() {
    this.ourSide = this.ourSide.opposite();
  }
  // Method for choosing the next kalahgame.Move currently a stub
  protected int bestNextMove() throws CloneNotSupportedException, IOException {
    int bestMove = 0;

    for (int i = 1; i <= this.holes; ++i) {
      Move m = new Move(this.ourSide,i);
      if(this.kalah.isLegalMove(m))
      {
        bestMove = i;
        break;
      }
    }

    return bestMove;
  }

  public void play() throws IOException, InvalidMessageException, CloneNotSupportedException {

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
      this.ourSide = Side.SOUTH;
      // First kalahgame.Move
      Main.sendMsg(Heuristic.firstMove());
    }
    else {
      // Means we are second player
      this.ourSide = Side.NORTH;
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
