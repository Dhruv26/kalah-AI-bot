package kalahplayer.mcts;

import kalahgame.Kalah;
import kalahplayer.KalahPlayer;
import kalahplayer.mcts.tree.Node;
import utils.Log;

import java.util.logging.Logger;

public class MonteCarloTreeSearch implements KalahPlayer {
    private static final Logger LOGGER = Log.getLogger(MonteCarloTreeSearch.class);
    private static final int NUM_SIMULATIONS = 500;

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
            LOGGER.info("==========================================================");
            Node bestChild = MonteCarloTreeSearchActions.select(node);
            Kalah leafState = MonteCarloTreeSearchActions.simulate(bestChild);
            MonteCarloTreeSearchActions.backpropagate(bestChild, leafState);
            LOGGER.info("Done simulation " + sims);
        }
        LOGGER.info("==========================================================");
        LOGGER.info("Completed building MCTS.");
    }

    @Override
    public int getBestMove(Kalah state) {
        Node node = new Node(state, null, null);
        search(node);
        // TODO: Select best child based on some policy
        return 5;
    }

    @Override
    public void performMove(int move) {
        // TODO: This should used to update the root of the tree
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
