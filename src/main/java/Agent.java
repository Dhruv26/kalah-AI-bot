import kalahgame.Board;
import kalahgame.Kalah;
import kalahgame.Move;
import kalahgame.Side;
import kalahplayer.Heuristic;
import protocol.InvalidMessageException;
import protocol.MsgType;
import protocol.Protocol;

import java.io.IOException;
import java.util.logging.Level;


public class Agent {

    protected Side ourSide;
    protected Kalah kalah;
    protected int holes;

    public Agent(final int holes, final int seeds) {
        this.ourSide = Side.SOUTH;
        this.holes = holes;
        this.kalah = new Kalah(new Board(holes, seeds));
    }

    protected void swap() {
        this.ourSide = this.ourSide.opposite();
    }

    protected int bestNextMove() {
        int bestMove = 0;
        for (int i = 1; i <= this.holes; ++i) {
            Move m = new Move(this.ourSide, i);
            if (this.kalah.isLegalMove(m)) {
                bestMove = i;
                break;
            }
        }
        return bestMove;
    }

    public void play() throws IOException, InvalidMessageException {
        Log log = new Log(Agent.class.getName());
        log.logger.setLevel(Level.ALL);

        String msg = Main.recvMsg();
        MsgType msgType = Protocol.getMessageType(msg);

        if (msgType == MsgType.END) {
            return;
        }
        if (msgType != MsgType.START) {
            throw new InvalidMessageException("Expected a start message but got something else.");
        }

        log.logger.info("Game start message: " + msg);
        if (Protocol.interpretStartMsg(msg)) {
            this.ourSide = Side.SOUTH;
            Main.sendMsg(Heuristic.firstMove());
        } else {
            this.ourSide = Side.NORTH;
        }
        log.logger.info("Our side is: " + ourSide.name());

        while (true) {
            log.logger.info("Entered game loop.");
            msg = Main.recvMsg();
            msgType = Protocol.getMessageType(msg);

            if (msgType == MsgType.END) {
                return;
            }
            if (msgType != MsgType.STATE) {
                throw new InvalidMessageException("Expected a state message but got something else.");
            }

            final Protocol.MoveTurn moveTurn = Protocol.interpretStateMsg(msg, this.kalah.getBoard());
            if (moveTurn.move == -1) {
                this.swap();
            }
            if (!moveTurn.again || moveTurn.end) {
                continue;
            }

            msg = Protocol.createMoveMsg(bestNextMove());
            Main.sendMsg(msg);
        }
    }
}
