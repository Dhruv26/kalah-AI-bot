package kalahplayer.mcts;

import kalahgame.Kalah;
import kalahgame.Move;
import kalahplayer.KalahPlayer;
import kalahplayer.mcts.tree.Node;
import utils.Log;

import java.util.Optional;
import java.util.logging.Logger;

class BuildTree extends Thread { 
    private static final Logger LOGGER = Log.getLogger(MonteCarloTreeSearch.class);
    private static final int NUM_SIMULATIONS = 50000;
    
    private Node root;

    public BuildTree(Node root) {
        this.root = root;
    }

    public void run() {
        try
        {
            for (int sims = 0; sims < NUM_SIMULATIONS; sims++) {
                Node bestChild = MonteCarloTreeSearchActions.select(root);
                Kalah leafState = MonteCarloTreeSearchActions.simulate(bestChild);
                MonteCarloTreeSearchActions.backpropagate(bestChild, leafState);

                LOGGER.info("Current thread: " + Thread.currentThread().getId());
                LOGGER.info("Current simulation: " + sims);
            }
        }
        catch (Exception e)
        {
            // Throwing an exception 
            System.out.println ("Exception is caught");
        }
    }
} 