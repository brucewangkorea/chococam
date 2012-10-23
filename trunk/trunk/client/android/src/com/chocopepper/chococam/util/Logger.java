package com.chocopepper.chococam.util;

/**
 * Log Manager for ANDROID.
 * @author KHAN0405
 *
 */
public class Logger {

	public static final int ALL = 0;
	public static final int NONE 	= 6;
	public static final int VERBOSE = 1;
	public static final int DEBUG 	= 2;
	public static final int INFO 	= 3;
	public static final int WARNING = 4;
	public static final int ERROR	= 5;
	public static final int DEFAULT = NONE;
	private static final String LOGTAG = "ProjectG_";
	private static int logLevel = ALL;
	
	/**
	 * Setup log level filter.
	 * @param level is Log Level.
	 */
	public static void setLogLevel(int level) {
		logLevel = level;
	}
	
	/**
	 * Make the Tag.
	 * @param cls Target Class.
	 * @return Tag
	 */
    public static String makeLogTag(Class<?> cls) {
        return LOGTAG + cls.getSimpleName();
    }
    
    /**
     * Send a VERBOSE log message and log the exception.
     * @param tag 	Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param msg 	The message you would like logged. 
     * @param tr	An exception to log
     */
    public static void v(String tag, String msg, Throwable tr) {
    	if (VERBOSE > logLevel)
    		android.util.Log.v(tag, msg, tr);
    }

    /**
     * Send a VERBOSE log message and log the exception.
     * @param tag 	Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param msg 	The message you would like logged. 
     */
    public static void v(String tag, String msg) {
    	if (VERBOSE > logLevel)
    		android.util.Log.v(tag, msg);
    }

    /**
     * Send a DEBUG log message and log the exception.
     * @param tag 	Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param msg 	The message you would like logged. 
     * @param tr	An exception to log
     */
    public static void d(String tag, String msg, Throwable tr) {
    	if (DEBUG > logLevel)
    		android.util.Log.d(tag, msg, tr);
    }

    /**
     * Send a DEBUG log message and log the exception.
     * @param tag 	Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param msg 	The message you would like logged. 
     */
    public static void d(String tag, String msg) {
    	if (DEBUG > logLevel)
    		android.util.Log.d(tag, msg);
    }

    /**
     * Send a INFO log message and log the exception.
     * @param tag 	Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param msg 	The message you would like logged. 
     * @param tr	An exception to log
     */
    public static void i(String tag, String msg, Throwable tr) {
    	if (INFO > logLevel)
    		android.util.Log.i(tag, msg, tr);
    }

    /**
     * Send a INFO log message and log the exception.
     * @param tag 	Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param msg 	The message you would like logged. 
     */
    public static void i(String tag, String msg) {
    	if (INFO > logLevel)
    		android.util.Log.i(tag, msg);
    }

    /**
     * Send a WARNING log message and log the exception.
     * @param tag 	Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param msg 	The message you would like logged. 
     * @param tr	An exception to log
     */
    public static void w(String tag, String msg, Throwable tr) {
    	if (WARNING > logLevel)
    		android.util.Log.w(tag, msg, tr);
    }

    /**
     * Send a WARNING log message and log the exception.
     * @param tag 	Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param msg 	The message you would like logged. 
     */
    public static void w(String tag, String msg) {
    	if (WARNING > logLevel)
    		android.util.Log.w(tag, msg);
    }

    /**
     * Send a ERROR log message and log the exception.
     * @param tag 	Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param msg 	The message you would like logged. 
     * @param tr	An exception to log
     */
    public static void e(String tag, String msg, Throwable tr) {
    	if (ERROR > logLevel)
    		android.util.Log.e(tag, msg, tr);
    }

    /**
     * Send a ERROR log message and log the exception.
     * @param tag 	Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param msg 	The message you would like logged. 
     */
    public static void e(String tag, String msg) {
    	if (ERROR > logLevel)
    		android.util.Log.e(tag, msg);
    }
}
