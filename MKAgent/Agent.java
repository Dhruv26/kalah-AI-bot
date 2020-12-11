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

    public Agent(final int holes, final int seeds) {
        this.kalah = new Kalah(new Board(holes, seeds), Side.SOUTH);
    }

    public void play() throws IOException, InvalidMessageException {
        String startMsg = Main.recvMsg();
        MsgType startMsgType = Protocol.getMessageType(startMsg);

        verifyMessageType(MsgType.START, startMsgType);
        boolean iAmFirst = Protocol.interpretStartMsg(startMsg);
        if (iAmFirst) {
            kalah.setMySide(Side.SOUTH);
        }
        else {
            kalah.setMySide(Side.NORTH);
        }
        LOGGER.info("Start message: " + startMsg + ", My side is " + kalah.getMySide() + "\n");
        MonteCarloTreeSearch player = new MonteCarloTreeSearch(kalah);
        Thread runner = new Thread(player, "mcts-tree-builder");
        runner.start();
        if (iAmFirst) {
            playMove(player);
        }

        while (true) {
            String msg = Main.recvMsg();
            MsgType msgType = Protocol.getMessageType(msg);
            LOGGER.info("Received message: " + msg.strip());

            if (msgType == MsgType.END) {
                LOGGER.info("Game has ended. The Game state is\n" + kalah);
                return;
            }

            verifyMessageType(MsgType.STATE, msgType);
            Protocol.MoveTurn moveTurn = Protocol.interpretStateMsg(msg);
            updateState(player, moveTurn.move);
            LOGGER.info("Received move: " + moveTurn);
            LOGGER.info("New state of the game:\n" + kalah);
            if (moveTurn.again) {
                playMove(player);
            }
        }
    }

    private void updateState(MonteCarloTreeSearch player, int moveHole) {
        kalah.makeMove(moveHole);
        player.performMove(moveHole);
    }

    private void playMove(MonteCarloTreeSearch player) {
        String moveMsg;
        int bestMove = player.getBestMove();
        if (bestMove == Move.SWAP) {
            updateState(player, bestMove);
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
