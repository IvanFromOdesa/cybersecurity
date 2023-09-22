package encryption.commons.log;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

public class LoggerFactory extends Logger {
    /**
     * Protected method to construct a logger for a named subsystem.
     * <p>
     * The logger will be initially configured with a null Level
     * and with useParentHandlers set to true.
     *
     * @param name               A name for the logger.  This should
     *                           be a dot-separated name and should normally
     *                           be based on the package name or class name
     *                           of the subsystem, such as java.net
     *                           or javax.swing.  It may be null for anonymous Loggers.
     * @param resourceBundleName name of ResourceBundle to be used for localizing
     *                           messages for this logger.  May be null if none
     *                           of the messages require localization.
     * @throws MissingResourceException if the resourceBundleName is non-null and
     *                                  no corresponding resource can be found.
     */
    protected LoggerFactory(String name, String resourceBundleName) {
        super(name, resourceBundleName);
        Handler consoleHandler = new ConsoleHandler();
        addHandler(consoleHandler);
    }

    public static LoggerFactory of(String name, String resourceBundleName) {
        return new LoggerFactory(name, resourceBundleName);
    }

    public static LoggerFactory of(String name) {
        return new LoggerFactory(name, null);
    }
}
