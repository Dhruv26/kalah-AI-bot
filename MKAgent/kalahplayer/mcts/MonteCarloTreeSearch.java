package kalahplayer.mcts;

import kalahgame.Kalah;
import kalahgame.Move;
import kalahplayer.KalahPlayer;
import kalahplayer.mcts.tree.Node;
import utils.Log;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MonteCarloTreeSearch implements KalahPlayer, Runnable {
    private static final Logger LOGGER = Log.getLogger(MonteCarloTreeSearch.class);
    private static final int MIN_SIMULATIONS = 50000;

    private Node root;
    private final Lock lock;

    public MonteCarloTreeSearch(Kalah state) {
        root = new Node(state.clone(), null, null);
        lock = new ReentrantLock();
    }

    @Override
    public void run() {
        build();
    }

    /**
     * Builds a MCTS from the root.
     */
    private void build() {
        LOGGER.info("Starting to build the tree on thread: " + Thread.currentThread().getName());
        while (!root.getState().gameOver()) {
            Node bestChild = MonteCarloTreeSearchActions.select(root);
            Kalah leafState = MonteCarloTreeSearchActions.simulate(bestChild);
            MonteCarloTreeSearchActions.backpropagate(bestChild, leafState);
        }
    }

    @Override
    public int getBestMove() {
        waitForMinSimulations();
        try {
            lock.lock();
            LOGGER.info("-----------------Acquired the lock. Number of visits of root " + root.getVisits());
            Node bestNode = root.getChildWithMaxVisits();
            Move bestMove = bestNode.getMove();
            LOGGER.info("Best move: hole " + bestMove.getHole() + ", reward: " + bestNode.getReward() +
                    ", visits: " + bestNode.getVisits() + ", Number of visits of root: " + root.getVisits());
            return bestMove.getHole();
        }
        finally {
            LOGGER.info("--------------------Released the lock.");
            lock.unlock();
        }
    }

    /**
     * Polls every second to check if MIN_SIMULATIONS from the root
     * have been performed.
     */
    private void waitForMinSimulations() {
        while (!minSimulationsDone()) {
            try {
                LOGGER.info("Waiting for the tree to build.");
                TimeUnit.SECONDS.sleep(5);
            }
            catch (InterruptedException e) {
                LOGGER.log(Level.SEVERE, "Error occurred while trying to sleep.", e);
            }
        }
    }

    private boolean minSimulationsDone() {
        try {
            lock.lock();
            return root.getVisits() >= MIN_SIMULATIONS;
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public void performMove(int move) {
        try {
            lock.lock();
            Move currMove = new Move(root.getState().getSideToMove(), move);
            root = findChildNodeWithMove(currMove);
            root.setParent(null);
        }
        finally {
            lock.unlock();
        }
    }

    /**
     * There should be a node from the root node which is obtained by applying the passed move
     * to the root's game state. This new node can either already be constructed and therefore
     * be one of the root's children, or it can be an unexplored move.
     *
     * @param move Move to apply to the current state of the game
     * @return Node that represents the new game state
     */
    private Node findChildNodeWithMove(Move move) {
        Optional<Node> childOpt = root.getChildren().stream()
                .filter(child -> move.equals(child.getMove()))
                .findAny();
        return childOpt.orElseGet(() -> getNodeFromUnexploredMove(move));
    }

    private Node getNodeFromUnexploredMove(Move move) {
        return root.getUnexploredMoves().stream()
                .filter(move::equals)
                .findAny()
                .map(unexploredMove -> {
                    root.getState().makeMove(unexploredMove);
                    return new Node(root.getState(), unexploredMove, null);
                })
                .orElseThrow(() ->
                        new IllegalStateException("No game state from current state using move " + move + " found.")
                );
    }

    /**
     * Just something to debug the number of nodes.
     *
     * @param node
     * @return
     */
    private static int getNumNodes(Node node) {
        int numNodesInChildren = 0;
        for (Node child: node.getChildren()) {
            numNodesInChildren += getNumNodes(child);
        }
        return 1 + numNodesInChildren;
    }
}
