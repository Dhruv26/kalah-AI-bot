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

    private final int holes;
    private final int seeds;

    public Agent(final int holes, final int seeds) {
        this.holes = holes;
        this.seeds = seeds;
    }

    public void play() throws IOException, InvalidMessageException {
        Kalah kalah = new Kalah(new Board(holes, seeds), Side.NORTH);

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
        LOGGER.finest("Start message: " + startMsg + ", My side is " + kalah.getMySide() + "\n");
        MonteCarloTreeSearch player = new MonteCarloTreeSearch(kalah);
        Thread runner = new Thread(player, "mcts-tree-builder");
        runner.start();
        if (kalah.getMySide() == Side.SOUTH) {
            playMove(player);
        }

        while (true) {
            String msg = Main.recvMsg();
            MsgType msgType = Protocol.getMessageType(msg);
            LOGGER.finest("Received message: " + msg.trim());

            if (msgType == MsgType.END) {
                LOGGER.finest("Game has ended.");
                return;
            }

            verifyMessageType(MsgType.STATE, msgType);
            Protocol.MoveTurn moveTurn = Protocol.interpretStateMsg(msg);
            updateState(player, moveTurn.move);
            LOGGER.finest("Received move: " + moveTurn);
            if (moveTurn.again) {
                playMove(player);
            }
        }
    }

    private void updateState(MonteCarloTreeSearch player, int moveHole) {
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
        LOGGER.finest("Making move: " + moveMsg);
        Main.sendMsg(moveMsg);
    }

    private static void verifyMessageType(MsgType expected, MsgType actual) throws InvalidMessageException {
        if (expected != actual) {
            throw new InvalidMessageException(String.format("Expected %s, Actual: %s", expected, actual));
        }
    }
}
