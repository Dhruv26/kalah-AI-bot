import kalahgame.Board;
import kalahgame.Kalah;
import kalahgame.Move;
import kalahgame.Side;
import kalahplayer.mcts.MonteCarloTreeSearch;
import protocol.InvalidMessageException;
import protocol.MsgType;
import protocol.Protocol;
import utils.Log;

import java.io.IOException;
import java.util.logging.Logger;

public class Agent {
    private static final Logger LOGGER = Log.getLogger(Agent.class);

    private final Kalah kalah;
    private final MonteCarloTreeSearch player;

    public Agent(final int holes, final int seeds) {
        this.kalah = new Kalah(new Board(holes, seeds), Side.SOUTH);
        this.player = new MonteCarloTreeSearch(kalah);
    }

    public void play() throws IOException, InvalidMessageException {
        String msg = Main.recvMsg();
        MsgType msgType = Protocol.getMessageType(msg);

        verifyMessageType(MsgType.START, msgType);
        if (Protocol.interpretStartMsg(msg)) {
            kalah.setMySide(Side.SOUTH);
            playMove();
        }
        else {
            kalah.setMySide(Side.NORTH);
        }
        LOGGER.info("My side is " + kalah.getMySide() + "\n");

        while (true) {
            msg = Main.recvMsg();
            msgType = Protocol.getMessageType(msg);
            LOGGER.info("Received message: " + msg.strip());

            if (msgType == MsgType.END) {
                LOGGER.info("Game has ended. The Game state is\n" + kalah);
                return;
            }

            verifyMessageType(MsgType.STATE, msgType);
            Protocol.MoveTurn moveTurn = Protocol.interpretStateMsg(msg);
            Side sideToMoveNext = kalah.makeMove(moveTurn.move);
            LOGGER.info("Received move: " + moveTurn + ", Next side to move (acc to me): " + sideToMoveNext);
            LOGGER.info("New state of the game:\n" + kalah);
            if (moveTurn.again) {
                playMove();
            }
        }
    }

    private void updateState(int moveHole) {
        //player.performMove(moveHole);
        kalah.makeMove(moveHole);
    }

    private void playMove() {
        String moveMsg;
        int bestMove = player.getBestMove(kalah);
        if (bestMove == Move.SWAP) {
            updateState(bestMove);
            moveMsg = Protocol.createSwapMsg();
        }
        else {
            moveMsg = Protocol.createMoveMsg(bestMove);
        }
        LOGGER.info("Making move: " + moveMsg);
        Main.sendMsg(moveMsg);
    }

    private static void verifyMessageType(MsgType expected, MsgType actual) throws InvalidMessageException {
        if (expected != actual) {
            throw new InvalidMessageException(String.format("Expected %s, Actual: %s", expected, actual));
        }
    }
}
