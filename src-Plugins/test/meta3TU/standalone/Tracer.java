package test.meta3TU.standalone;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.*;

public class Tracer {

	static Logger _logger;

	static {
		try {
			// Remove default output to console
			_logger = Logger.getLogger("Tracer");
			Handler[] handlers = _logger.getHandlers();
			for (int i = 0; i < handlers.length; i++) {
				if (handlers[i] instanceof ConsoleHandler) {
					_logger.removeHandler(handlers[i]);
				}
			}

			// Check if log folder exists
			String folderName = "log\\";
			File logDirectory = new File(folderName);
			if (!logDirectory.exists() || !logDirectory.isDirectory())
				logDirectory.mkdir();

			// Write trace output to file
			int sizeLimitBytes = 1024 * 1024; // 1 Mb
			int numLogFiles = 10;
			java.util.Date currentDate = Calendar.getInstance().getTime();
			String fileName = String.format("%1$sLog_%2$s.txt", folderName, new SimpleDateFormat("yyyyMMdd")
					.format(currentDate));
			FileHandler fh = new FileHandler(fileName, sizeLimitBytes, numLogFiles, true);
			fh.setFormatter(new TraceFormatter());
			_logger.addHandler(fh);

			Log(Level.OFF, "Tracer", "Initialization", "--------------------Tracing started----------------------");
			Log(Level.OFF, "Tracer", "Initialization", "Trace level: " + _logger.getLevel());
			Log(Level.OFF, "Tracer", "Initialization", "Filter: " + _logger.getFilter());
			SetLevel(Level.ALL);

		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void SetLevel(Level level) {
		Log(Level.OFF, "Tracer", "SetLevel", "Trace level changed from " + _logger.getLevel() + " to " + level);
		_logger.setLevel(level);
	}

	public static void SetFilter(Filter filter) {
		Log(Level.OFF, "Tracer", "SetLevel", "Filter changed from " + _logger.getFilter() + " to " + filter);
		_logger.setFilter(filter);
	}

	public static void Log(Level level, String sourceClassName, String sourceMethodName, String message,
			Object[] messageParameters, String[] methodParamNames, Object[] methodParamValues, Throwable thrown) {
		if (_logger.isLoggable(level)) {
			TraceRecord record = new TraceRecord(level, message);
			record.setParameters(messageParameters);
			record.setLoggerName(_logger.getName());
			record.setSourceClassName(sourceClassName);
			record.setSourceMethodName(sourceMethodName);			
			record.setThrown(thrown);
			record.setMethodParameterNames(methodParamNames);
			record.setMethodParameterValues(methodParamValues);

			_logger.log(record);
		}
	}

	//Log methods for exception handling
	public static void Log(Level level, String sourceClassName, String sourceMethodName, Throwable thrown) {
		Log(level, sourceClassName, sourceMethodName, "Exception occured: ", thrown);
	}
	
	public static void Log(Level level, String sourceClassName, String sourceMethodName, String message,
			Throwable thrown) {
		Log(level, sourceClassName, sourceMethodName, message, null, thrown);
	}
	
	public static void Log(Level level, String sourceClassName, String sourceMethodName, String message,
			Object[] messageParameters, Throwable thrown) {
		Log(level, sourceClassName, sourceMethodName, message, messageParameters, null, null, thrown);
	}
	
	public static void Log(Level level, String sourceClassName, String sourceMethodName, String[] methodParamNames, Object[] methodParamValues, Throwable thrown) {
		Log(level, sourceClassName, sourceMethodName, "Exception occured: ", null, methodParamNames, methodParamValues, thrown);
	}

	//Log methods for normal situations 
	public static void Log(Level level, String sourceClassName, String sourceMethodName, String message,
			Object[] messageParameters, String[] methodParamNames, Object[] methodParamValues) {
		Log(level, sourceClassName, sourceMethodName, message, messageParameters, methodParamNames, methodParamValues, null);
	}
	public static void Log(Level level, String sourceClassName, String sourceMethodName, String message,
			String[] methodParamNames, Object[] methodParamValues) {
		Log(level, sourceClassName, sourceMethodName, message, null, methodParamNames, methodParamValues);
	}

	public static void Log(Level level, String sourceClassName, String sourceMethodName, String message, Object[] messageParameters) {
		Log(level, sourceClassName, sourceMethodName, message, messageParameters, null, null);
	}

	public static void Log(Level level, String sourceClassName, String sourceMethodName, String message) {
		Log(level, sourceClassName, sourceMethodName, message, (Object[]) null);
	}

	public static void Log(Level level, String message) {
		Log(level, null, null, message);
	}
}
