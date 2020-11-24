package kalahplayer.mcts;

import kalahgame.Kalah;
import kalahgame.Move;
import kalahplayer.mcts.tree.Node;

import java.util.List;
import java.util.Random;

public class MonteCarloTreeSearchActions {
    private static final Random RANDOM = new Random();

    public Node select(Node node) {
        while (!node.isLeaf()) {
            node = node.getChildWithHighestUTCReward();
        }
        return node;
    }

    public void expand(Node root) {
        root.getUnexploredMoves().forEach(unexploredMove -> {
            Kalah childState = root.getState().clone();
            childState.makeMove(unexploredMove);
            Node childNode = new Node(childState, unexploredMove, root);
            root.addChild(childNode);
        });
    }

    public Kalah simulate(Node root) {
        Node node = root.clone();
        while (!node.isLeaf()) {
            List<Move> legalMoves = node.getState().getAllPossibleMoves();
            Move legalMove = legalMoves.get(RANDOM.nextInt(legalMoves.size()));
            node.getState().makeMove(legalMove);
        }
        return node.getState();
    }

    public void backpropagate() {
        
    }
}
