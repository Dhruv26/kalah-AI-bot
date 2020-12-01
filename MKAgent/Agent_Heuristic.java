import java.io.*;
import java.util.concurrent.ThreadLocalRandom;


public class Agent_Heuristic {

  protected Side ourSide;
  protected Kalah kalah;
  protected int holes;
  protected int maxDepth;
  //protected File log_file;
  //protected BufferedWriter log;
  //protected Path currentRelativePath;
  //protected String absolutePath;


  public Agent_Heuristic(final int holes, final int seeds) throws IOException {
    this.ourSide = Side.SOUTH;
    this.holes = holes;
    this.kalah = new Kalah(new Board(holes, seeds));
    this.maxDepth = 4; // MinMax only compute next 4 moves only, 
    //this.currentRelativePath = Paths.get("");
    //this.absolutePath = currentRelativePath.toAbsolutePath().toString();
    //this.log_file = new File(this.absolutePath+"/log.txt");

    //this.log = new BufferedWriter(new FileWriter(this.log_file));
    //this.log.write("Agent Initialised");
    //this.log.write("\n");
    //this.log.close();

  }

//---------------------------------------------------------------------------------------------------------------
  // how many seeds in our port - num seeds in opponent port
  protected int h1_seedsDiffinPort(Board b, Side s) throws IOException {
    //this.log.write("board: "+b.toString());
    //this.log.write("\n");
    int num = b.getSeedsInStore(s);
    int numO = b.getSeedsInStore(s.opposite());
    return num - numO;
  }

// overall side num of seeds
  protected int h2_sideSeedsDiff(Board b, Side s) {
    int num_my_seeds = 0;
    for (int h = 1; h <= 7; h ++)
    {
      num_my_seeds += b.getSeeds(s, h);
    }
    int num_opp_seeds = 0;
    for (int h = 1; h <= 7; h ++)
    {
      num_opp_seeds += b.getSeeds(s.opposite(), h);
    }
    return num_my_seeds - num_opp_seeds;
  }

// if it leads to a last move, call function from board
  protected int h3_numLastMove(Board b, Side s) {
      return b.getNoLastmove();
  }

// see if there exists extra move after this move
  protected int h4_numLastMove(Board b, Side s) {
    int count = 0;
    for (int h = 1; h <= 7; h ++)
    {
      if(b.getSeeds(s, h) == h)
        count++;
    }
    return count;
  }

//---------------------------------------------------------------------------------------------------------------

  protected double evaluate(Board b, Side s) throws IOException
  {

    double weight_evaluate = 0;
    weight_evaluate = 8*h1_seedsDiffinPort(b,s) + 2*h2_sideSeedsDiff(b,s) + 0.8*h3_numLastMove(b,s) + 1*h4_numLastMove(b,s);
    return weight_evaluate;
  }

  protected double RandomAction(Board board, Side s)throws CloneNotSupportedException,  IOException {
    if(Kalah.gameOver(board)){
      if(board.getSeedsInStore(s) > 49)
        return 0.1;
      else if(board.getSeedsInStore(s) == 49)
        return 0;
      else
        return -0.1;
    }
    else{
      Board cloneBoard = board.clone();
      Kalah k = new Kalah(cloneBoard);
      Move m;
      int n = ThreadLocalRandom.current().nextInt(1, 8);
      while(cloneBoard.getSeeds(s, n) == 0){
        n = ThreadLocalRandom.current().nextInt(1, 8);
      }
      m = new Move(s,n);
      k.makeMove(m);
      return RandomAction(cloneBoard, s.opposite());
    }
  }

  protected double abMinMax(int move, int currentDepth, Boolean maxScore, Board board, double alpha, double beta) throws CloneNotSupportedException, IOException {

    // Clone the board and evaluate the current board
    Board cloneBoard[] = new Board[8];
    Kalah k[] = new Kalah[8];
    for(int i = 1; i <= this.holes; ++i)
    {
      cloneBoard[i] = board.clone();
      k[i] = new Kalah(cloneBoard[i]);
    }

    double score;
    Move m;

    if(currentDepth == 0 || Kalah.gameOver(board))
    {
      Kalah kalah = new Kalah(board);
      if(maxScore)
      {
          score = evaluate(board, this.ourSide);
          score += (7-move) * 3;
          for(int l = 0; l < 30; l++){
            score += RandomAction(board, this.ourSide);
          }

          //this.log.write("score: "+Double.toString(score));
          //this.log.write("\n");

      } else
      {
        m = new Move(this.ourSide.opposite(),move);
        
          score = evaluate(board, this.ourSide);
          score += (7-move) * 3;
          for(int l = 0; l < 30; l++){
            score += RandomAction(board, this.ourSide);
          }

      }

      return score;
    }

    if(maxScore) {
      double best = Integer.MIN_VALUE;

      // recur for left and right children
      for(int i = 1; i <= this.holes; ++i)
      {
        m = new Move(this.ourSide,i);
        if(k[i].isLegalMove(m))
        {
          k[i].makeMove(m);

          // Evaluation Function
          double val = abMinMax(i, currentDepth-1, false, cloneBoard[i], alpha, beta);
          val += (7-move) * 3;

          best = Math.max(best, val);
          alpha = Math.max(alpha, val);

          //Alpha Beta Pruning
          if (beta <= alpha)
            break;
        } else
          continue;
      }
      return best;
    } else
    {
      double best = Integer.MAX_VALUE;

      // Recur for left and right children
      for(int i = 1; i <= this.holes; ++i)
      {
        m = new Move(this.ourSide.opposite(),i);
        if(k[i].isLegalMove(m))
        {
          k[i].makeMove(m);
          // Evaluation Function
          double val = abMinMax(i, currentDepth-1, true, cloneBoard[i], alpha, beta);

          best = Math.min(best, val);
          beta = Math.min(beta, val);

          //Alpha Beta Pruning
          if (beta <= alpha)
            break;
        } else
          continue;
      }
      return best;
    }
  }
  // Method for choosing the next Move currently a stub
  protected int bestNextMove() throws CloneNotSupportedException, IOException {

    double scores[] = new double[8];
    scores[0] = Integer.MIN_VALUE;
    int bestMove = 0;
    Board cloneBoard[] = new Board[8];
    Kalah k[] = new Kalah[8];
    for(int i = 1; i <= this.holes; ++i)
    {
      cloneBoard[i] = this.kalah.getBoard().clone();
      k[i] = new Kalah(cloneBoard[i]);
    }

    for (int i = 1; i <= this.holes; ++i) {
      Move m = new Move(this.ourSide,i);
      if(this.kalah.isLegalMove(m))
      {
        k[i].makeMove(m);
        //this.log = new BufferedWriter(new FileWriter(this.log_file,true));
        //this.log.write("C====:\n"+cloneBoard[i].toString());
        //this.log.write("======\n");
        scores[i] = abMinMax(i, this.maxDepth-1, false, cloneBoard[i], Integer.MIN_VALUE, Integer.MAX_VALUE);
        //this.log.close();
        if(scores[i] >= scores[bestMove])
        {
          bestMove = i;
        }
      }
    }

    return bestMove;
  }

  protected void swap() {
    this.ourSide = this.ourSide.opposite();
  }

  public void play() throws IOException, InvalidMessageException, CloneNotSupportedException {
    // Receive Message
    String msg = Main.recvMsg();
    // Choose the Message Type
    MsgType msgType = Protocol.getMessageType(msg);
    // If the message is END, end the game.
    if (msgType == MsgType.END) {
      return;
    }
    // If the message is not START, throws error.
    if (msgType != MsgType.START) {
      throw new InvalidMessageException("Expected a start message but got something else.");
    }
    // If the start message is SOUTH
    // Means we are first player
    if (Protocol.interpretStartMsg(msg)) {
      this.ourSide = Side.SOUTH;
      // First Move
      Main.sendMsg(Heuristic.firstMove());
    }
    else {
      // Means we are second player
      this.ourSide = Side.NORTH;
      // Pie Rule is available.
      //TODO

    }
    // Game Loop
    while (true) {
      // Receive System Message
      msg = Main.recvMsg();
      // Get MSG type
      msgType = Protocol.getMessageType(msg);
      // If the MSG is END
      // Return to the main
      if (msgType == MsgType.END) {
        return;
      }
      // If the MSG is not the STATE
      // Throws Exception
      if (msgType != MsgType.STATE) {
        throw new InvalidMessageException("Expected a state message.");
      }
      // Check if extra move turn
      final Protocol.MoveTurn moveTurn = Protocol.interpretStateMsg(msg, this.kalah.getBoard());
      if (moveTurn.move == -1) {
        this.swap();
      }
      // If not skip the current iteration
      if (!moveTurn.again || moveTurn.end) {
        continue;
      }
      msg = null;

      int nextMove = this.bestNextMove();

      if (msg == null) {
        msg = Protocol.createMoveMsg(nextMove);
      }
      Main.sendMsg(msg);
    }
  }
}
