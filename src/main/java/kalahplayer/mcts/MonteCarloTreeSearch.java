package kalahplayer.mcts;

import kalahgame.Kalah;
import kalahplayer.KalahPlayer;
import kalahplayer.mcts.tree.Node;

public class MonteCarloTreeSearch implements KalahPlayer {
    private Node root;

    public MonteCarloTreeSearch(Kalah state) {
        root = new Node(state, null, null);
    }

    private void search() {
        // TODO: Timeout/Max depth or some sort of limit?
        while (true) {
            Node bestChild = MonteCarloTreeSearchActions.select(root);
            Node leafNode = MonteCarloTreeSearchActions.simulate(bestChild);
            MonteCarloTreeSearchActions.backpropagate(bestChild, leafNode.getState());
        }
    }

    @Override
    public int getBestMove() {
        // TODO: This should be used when we have to make the first move or repetitive moves
        return 0;
    }

    @Override
    public void performMove(int move) {
        // TODO: This should used to update the root of the tree
    }
}
