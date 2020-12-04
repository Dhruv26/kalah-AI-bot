import kalahgame.Board;
import kalahgame.Kalah;
import kalahgame.Move;
import kalahgame.Side;
import kalahplayer.KalahPlayer;
import kalahplayer.mcts.MonteCarloTreeSearch;
import protocol.InvalidMessageException;
import protocol.MsgType;
import protocol.Protocol;
import utils.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Agent {
    private static final Logger LOGGER = Log.getLogger(Agent.class);

    private final Kalah kalah;

    public Agent(final int holes, final int seeds) {
        this.kalah = new Kalah(new Board(holes, seeds), Side.SOUTH);
    }

    private static void verifyMessageType(MsgType expected, MsgType actual) throws InvalidMessageException {
        if (expected != actual) {
            throw new InvalidMessageException(String.format("Expected %s, Actual: %s", expected, actual));
        }
    }

    protected int bestNextMove() {
        List<Move> allPossibleMoves = new ArrayList<>();
        for (int moveHole = 1; moveHole <= kalah.getBoard().getNoOfHoles(); moveHole++) {
            Move move = new Move(kalah.getMySide(), moveHole);
            if (kalah.isLegalMove(move))
                allPossibleMoves.add(move);
        }
        return allPossibleMoves.get(allPossibleMoves.size() - 1).getHole();
    }

    public void play() throws IOException, InvalidMessageException {
        KalahPlayer player = new MonteCarloTreeSearch(kalah);
        LOGGER.info("Start game state is:\n" + kalah);

        String msg = Main.recvMsg();
        MsgType msgType = Protocol.getMessageType(msg);

        verifyMessageType(MsgType.START, msgType);
        if (Protocol.interpretStartMsg(msg)) {
            kalah.setMySide(Side.SOUTH);
            int bestMove = player.getBestMove(kalah);
            Main.sendMsg(Protocol.createMoveMsg(bestMove));
        }
        else {
            kalah.setMySide(Side.NORTH);
        }
        LOGGER.info("My side is " + msgType + ". Game state is\n" + kalah);

        while (true) {
            msg = Main.recvMsg();
            msgType = Protocol.getMessageType(msg);

            if (msgType == MsgType.END) {
                LOGGER.info("Game has ended. The Game state is\n" + kalah);
                return;
            }

            verifyMessageType(MsgType.STATE, msgType);
            final Protocol.MoveTurn moveTurn = Protocol.interpretStateMsg(msg, this.kalah.getBoard());
            if (moveTurn.move == -1) {
                kalah.swapMySide();
            }
            if (!moveTurn.again || moveTurn.end) {
                continue;
            }

            String moveMsg = Protocol.createMoveMsg(this.bestNextMove());
            LOGGER.info("Making move: " + moveMsg.strip());
            Main.sendMsg(moveMsg);
        }
    }
}
