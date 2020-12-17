package utils;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Log {
    private static final String LOG_FILE_NAME = "kalah.log";
    private static final Handler LOG_HANDLER;

    static {
        Handler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter());
        LOG_HANDLER = handler;
    }

    public Log() {
        // No instance for you
    }

    /**
     * Creates a logger. Sets the level to {@code Level.INFO}.
     *
     * @param clz Class for which the logger is to be created
     * @return
     */
    public static Logger getLogger(Class<?> clz) {
        return getLogger(clz, Level.INFO);
    }

    public static Logger getLogger(Class<?> clz, Level logLevel) {
        Logger logger = Logger.getLogger(clz.getName());
        configureLogger(logger, logLevel);
        return logger;
    }

    private static void configureLogger(Logger logger, Level logLevel) {
        logger.addHandler(LOG_HANDLER);
        logger.setLevel(logLevel);
    }
}
