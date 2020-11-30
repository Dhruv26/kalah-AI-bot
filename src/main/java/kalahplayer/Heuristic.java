package kalahplayer;

import protocol.Protocol;

/*
 ** player.Heuristic Class which helps evaluating each move turn
 */
public class Heuristic {

    public static String firstMove() {
        return Protocol.createMoveMsg(3);
    }
}
