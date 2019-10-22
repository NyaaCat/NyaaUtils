package cat.nyaa.nyaautils.misc.journeymap.common.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JourneyMapLogHelper {
    public static JourneyMapLogHelper log = new JourneyMapLogHelper();

    private static boolean configured;

    private Logger myLog;

    private static void configureLogging() {
        log.myLog = LogManager.getLogger("JourneyMapServer");
        configured = true;
    }

    public static void log(Level level, Object o) {
        if (!configured) {
            configureLogging();
        }
        log.myLog.log(level, String.valueOf(o));
    }


    public static void all(Object o) {
        log(Level.ALL, o);
    }


    public static void debug(Object o) {
        log(Level.DEBUG, o);
    }


    public static void error(Object o) {
        log(Level.ERROR, o);
    }


    public static void fatal(Object o) {
        log(Level.FATAL, o);
    }


    public static void info(Object o) {
        log(Level.INFO, o);
    }


    public static void off(Object o) {
        log(Level.OFF, o);
    }


    public static void trace(Object o) {
        log(Level.TRACE, o);
    }


    public static void warn(Object o) {
        log(Level.WARN, o);
    }


    public Logger getLogger() {
        return this.myLog;
    }
}