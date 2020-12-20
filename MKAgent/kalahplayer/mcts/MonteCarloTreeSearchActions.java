package kalahplayer.mcts;

import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import kalahgame.Board;
import kalahgame.Kalah;
import kalahgame.Move;
import kalahgame.Side;
import kalahplayer.mcts.tree.Node;
import utils.Log;

class MonteCarloTreeSearchActions {
    private static final Logger LOGGER = Log.getLogger(MonteCarloTreeSearchActions.class);
    private static final Random RANDOM = new Random();

    /**
     * Selects best child node based on UTC score. Expands the child
     * if all it's children nodes are not created.
     *
     * @param root Node whose best child is to be selected
     * @return Best child node based on UTC score
     */
    static Node select(Node root) {
        Node bestChild = root;
        while (!bestChild.isTerminalNode()) {
            if (!bestChild.allMovesExplored()) {
                return expand(bestChild);
            }
            bestChild = bestChild.getChildWithHighestUTCReward();
        }
        return bestChild;
    }

    /**
     * Randomly adds children to a node.
     *
     * @param root Node to which children are to be added
     * @return A random child node
     */
    private static Node expand(Node root) {
        Move unexploredMove = getRandomMove(root.getUnexploredMoves());

        Kalah childState = root.getState().clone();
        childState.makeMove(unexploredMove);
        Node childNode = new Node(childState, unexploredMove, root);
        root.addChild(childNode);

        return childNode;
    }

    /**
     * Simulates from the root node by randomly selecting a valid move from
     * the current state, until a terminal node is reached.
     *
     * @param root Node to start simulation from
     * @return Random terminal node reached from the root node
     */
    static Kalah simulate(Node root) {
        Node node = root.clone();
        while (!node.isTerminalNode()) {
            Move legalMove = getRandomMove(node.getState().getAllPossibleMoves());
            node.getState().makeMove(legalMove);
        }
        return node.getState();
    }

    /**
     * Backpropagates from a leaf node, updating the score for its parent
     * on the way.
     *
     * @param node       Node to start backpropagation from
     * @param finalState Final state, ie result of the simulation
     */
    static void backpropagate(Node node, Kalah finalState) {
        Node parentNode = node;
        while (parentNode != null) {
            Side side = parentNode.getParent() != null ?
                    parentNode.getParent().getState().getSideToMove() : parentNode.getState().getSideToMove();
            parentNode.update(computeEndGameReward(finalState, side));
            parentNode = parentNode.getParent();
        }
    }

    private static Move getRandomMove(List<Move> possibleMoves) {
        return possibleMoves.get(RANDOM.nextInt(possibleMoves.size()));
    }

    private static int computeEndGameReward(Kalah finalState, Side side) {
        if (!finalState.gameOver()) {
            throw new IllegalStateException("Access to end results before MKAgent.game finish");
        }
        Board board = finalState.getBoard();
        // Whoever has more seeds at the end of the game wins
        return Integer.compare(board.getSeedsInStore(side), board.getSeedsInStore(side.opposite()));
    }
}
