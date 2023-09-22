package encryption.commons.log;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class MessageLogger {
    private static final Logger LOGGER = Logger.getLogger(MessageLogger.class.getName());

    public static void info(String msg) {
        LOGGER.info(msg);
    }

    public static void error(Throwable e) {
        LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }

    public static void error(String msg, Throwable e) {
        LOGGER.log(Level.SEVERE, msg, e);
    }

    public static void warning(String msg) {
        LOGGER.warning(msg);
    }

    public static void config(String msg) {
        LOGGER.config(msg);
    }
}
