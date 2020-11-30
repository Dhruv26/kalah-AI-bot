import protocol.InvalidMessageException;

import java.io.*;

/**
 * The main application class. It also provides methods for communication
 * with the game engine.
 */
public class Main {
    private static final int holes = 7;
    private static final int seeds = 7;
    /**
     * Input from the game engine.
     */
    private static final Reader input = new BufferedReader(new InputStreamReader(System.in));

    /**
     * Sends a message to the game engine.
     *
     * @param msg The message.
     */
    public static void sendMsg(String msg) {
        System.out.print(msg);
        System.out.flush();
    }

    /**
     * Receives a message from the game engine. Messages are terminated by
     * a '\n' character.
     *
     * @return The message.
     * @throws IOException if there has been an I/O error.
     */
    public static String recvMsg() throws IOException {
        StringBuilder message = new StringBuilder();
        int newCharacter;

        do {
            newCharacter = input.read();
            if (newCharacter == -1)
                throw new EOFException("Input ended unexpectedly.");
            message.append((char) newCharacter);
        } while ((char) newCharacter != '\n');

        return message.toString();
    }

    /**
     * The main method, invoked when the program is started.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        try {
            Agent agent = new Agent(holes, seeds);
            agent.play();
        } catch (InvalidMessageException | IOException e) {
            System.err.println("This shouldn't happen: " + e.getMessage());
        } catch (Exception e) {

        }
    }
}