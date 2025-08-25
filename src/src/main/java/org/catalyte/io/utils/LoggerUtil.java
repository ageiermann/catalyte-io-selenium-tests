package org.catalyte.io.utils;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerUtil {

  private LoggerUtil() {
  }

  /**
   * Get a configured logger for a class.
   *
   * @param clazz Class requesting the logger
   * @return Logger instance
   */
  public static Logger getLogger(Class<?> clazz) {
    Logger logger = Logger.getLogger(clazz.getName());

    if (logger.getHandlers().length == 0) {
      ConsoleHandler handler = new ConsoleHandler();
      handler.setLevel(Level.ALL);
      handler.setFormatter(new SimpleFormatter());
      logger.addHandler(handler);
      logger.setUseParentHandlers(false);
    }

    logger.setLevel(Level.ALL);
    return logger;
  }

  /**
   * Convenience methods for common logging levels
   */
  public static void info(Class<?> info, String message) {
    getLogger(info).info(message);
  }

  public static void warning(Class<?> warning, String message) {
    getLogger(warning).warning(message);
  }

  public static void severe(Class<?> severe, String message) {
    getLogger(severe).severe(message);
  }

  public static void fine(Class<?> fine, String message) {
    getLogger(fine).fine(message);
  }
}