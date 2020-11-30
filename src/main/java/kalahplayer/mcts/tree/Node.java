package kalahplayer.mcts.tree;

import kalahgame.Kalah;
import kalahgame.Move;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static java.util.Comparator.comparing;

public class Node {
    private static final double EXPLORATION_CONSTANT = 1.0 / Math.sqrt(2);

    private final Kalah state;
    private final Move move;
    private final Node parent;
    private final List<Node> children;
    private final List<Move> unexploredMoves;
    private int visits;
    private double reward;

    public Node(Kalah state, Move move, Node parent) {
        this.state = state;
        this.move = move;
        this.parent = parent;
        this.visits = 0;
        this.reward = 0;
        this.children = new ArrayList<>();
        // All moves from this node are yet to be explored
        this.unexploredMoves = state.getAllPossibleMoves();
    }

    public Node clone() {
        Node clone = new Node(state.clone(), move, parent);
        clone.setVisits(visits);
        clone.setReward(reward);
        children.forEach(clone::addChild);
        unexploredMoves.forEach(clone::addUnexploredMoves);
        return clone;
    }

    public Kalah getState() {
        return state;
    }

    public Move getMove() {
        return move;
    }

    public Node getParent() {
        return parent;
    }

    public int getVisits() {
        return visits;
    }

    private void setVisits(int visits) {
        this.visits = visits;
    }

    public void addVisit() {
        setVisits(getVisits() + 1);
    }

    public double getReward() {
        return reward;
    }

    private void setReward(double reward) {
        this.reward = reward;
    }

    public List<Node> getChildren() {
        return new ArrayList<>(children);
    }

    public void addChild(Node childNode) {
        children.add(childNode);
        unexploredMoves.remove(childNode.getMove());
    }

    public List<Move> getUnexploredMoves() {
        return new ArrayList<>(unexploredMoves);
    }

    private void addUnexploredMoves(Move unexploredMove) {
        unexploredMoves.add(unexploredMove);
    }

    public boolean isTerminalNode() {
        // A node cannot have children if no more moves are possible
        // from the current state
        return state.getAllPossibleMoves().size() == 0;
    }

    public boolean allMovesExplored() {
        return unexploredMoves.size() == 0;
    }

    public Node getChildWithHighestUTCReward() {
        return getChildren().stream()
                .max(comparing(child -> uctReward(this, child)))
                .orElseThrow(NoSuchElementException::new);
    }

    public void update(double reward) {
        setReward(getReward() + reward);
        setVisits(getVisits() + 1);
    }

    private static double uctReward(Node node, Node child) {
        return (child.getReward() / child.getVisits())
                + (EXPLORATION_CONSTANT * Math.sqrt(2 * Math.log(node.getVisits()) / child.getVisits()));
    }

    @Override
    public String toString() {
        return "Node{" +
                "state=" + state +
                ", move=" + move +
                ", parent=" + parent +
                ", children=" + children +
                ", unexploredMoves=" + unexploredMoves +
                ", visits=" + visits +
                ", reward=" + reward +
                '}';
    }
}
