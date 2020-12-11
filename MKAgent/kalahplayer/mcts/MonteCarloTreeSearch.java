package kalahplayer.mcts;

import kalahgame.Kalah;
import kalahgame.Move;
import kalahplayer.KalahPlayer;
import kalahplayer.mcts.tree.Node;
import utils.Log;

import java.util.Optional;
import java.util.logging.Logger;

public class MonteCarloTreeSearch implements KalahPlayer {
    private static final Logger LOGGER = Log.getLogger(MonteCarloTreeSearch.class);
    private static final int NUM_SIMULATIONS = 50000;

    private Node root;

    public MonteCarloTreeSearch(Kalah state) {
        root = new Node(state.clone(), null, null);
    }

    /**
     * Builds a MCTS from the root.
     */
    private void build() {
        for (int sims = 0; sims < NUM_SIMULATIONS; sims++) {
            Node bestChild = MonteCarloTreeSearchActions.select(root);
            Kalah leafState = MonteCarloTreeSearchActions.simulate(bestChild);
            MonteCarloTreeSearchActions.backpropagate(bestChild, leafState);
        }
        LOGGER.info("Completed building MCTS.");
    }

    @Override
    public int getBestMove() {
        build();
        // Getting child with maximum number of visits outperform child with maximum UTC score.
        Node bestNode = root.getChildWithMaxVisits();
        Move bestMove = bestNode.getMove();
        LOGGER.info("Best move: hole " + bestMove.getHole() + ", reward: " + bestNode.getReward() + ", visits: " + bestNode.getVisits());
        return bestMove.getHole();
    }

    @Override
    public void performMove(int move) {
        Move currMove = new Move(root.getState().getSideToMove(), move);
        root = findChildNodeWithMove(currMove);
        root.setParent(null);
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
