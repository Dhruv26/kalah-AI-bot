# AI bot for the game Mancala (Kalah)

We try two approaches for the game:
* Monte Carlo Tree Search (MCTS)
* Minimax with Alpha-Beta Pruning

**MCTS** is defined in the master branch. **Minimax with Alpha Beta Pruning** is defined in the *heuristic* branch.

**ManKalah.jar** is the game engine. It accepts two string, ie the commands to run two bots against each other.

Some test agents are defined in the *Test_Agents* folder. These have been provided by The University of Manchester.

#### Compile
To compile the bot, run the script *compile.sh*

#### Run
To run the bot, run the script *run.sh*

#### Future Improvements
* Add game specific knowledge to MCTS
* Apply heuristics (like RAVE, greedy simulation) to MCTS
