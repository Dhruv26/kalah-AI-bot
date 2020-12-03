package kalahplayer.mcts;
import java.util.logging.Logger;

import kalahgame.Kalah;
import kalahgame.Move;
import kalahplayer.KalahPlayer;
import kalahplayer.mcts.tree.Node;
import utils.Log;

public class MonteCarloTreeSearch implements KalahPlayer {
    private static final Logger LOGGER = Log.getLogger(MonteCarloTreeSearch.class);
    private static final int NUM_SIMULATIONS = 50000;

    private Node root;

    public MonteCarloTreeSearch(Kalah state) {
        root = new Node(state, null, null);
    }

    /**
     * Creates a MCTS from the given node.
     *
     * @param node Node to start search from
     */
    private void search(Node node) {
        for (int sims = 0; sims < NUM_SIMULATIONS; sims++) {
            Node bestChild = MonteCarloTreeSearchActions.select(node);
            Kalah leafState = MonteCarloTreeSearchActions.simulate(bestChild);
            MonteCarloTreeSearchActions.backpropagate(bestChild, leafState);
        }
        LOGGER.info("Completed building MCTS.");
    }

    @Override
    public int getBestMove(Kalah state) {
        Node node = new Node(state, null, null);
        search(node);
        Node bestNode = node.getChildWithHighestUTCReward();
        Move bestMove = bestNode.getMove();
        LOGGER.info("Best move: hole " + bestMove.getHole() + ", reward: " + bestNode.getReward() + ", visits: " + bestNode.getVisits());
        return bestMove.getHole();
    }

    @Override
    public void performMove(int move) {
        root = root.getChildren().get(move);
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
