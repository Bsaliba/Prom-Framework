package test.meta3TU.standalone;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.*;

public class TraceFormatter extends Formatter {

	static SimpleDateFormat _timeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	@Override
	public String format(LogRecord record) {
		if (record instanceof TraceRecord)
			return format((TraceRecord) record);
		else
			return format(record, null, null);
	}

	public String format(TraceRecord record) {
		return format(record, record.getMethodParameterNames(), record.getMethodParameterValues());
	}
	
	public String format(LogRecord record, String[] methodParameterNames, Object[] methodParameterValues) {
		StringBuilder messageBuilder = new StringBuilder(1000);

		// Print general info, such as time and severity
		java.util.Date currentDate = Calendar.getInstance().getTime();
		messageBuilder.append(_timeFormatter.format(currentDate));
		messageBuilder.append(' ');
		messageBuilder.append(record.getLevel());
		messageBuilder.append(' ');

		// Print method info with parameters (if any)
		messageBuilder.append(record.getSourceClassName());
		messageBuilder.append('.');
		messageBuilder.append(record.getSourceMethodName());

		// Print method parameters
		messageBuilder.append('(');
		if ((methodParameterValues != null) && (methodParameterValues.length > 0)) {
			if ((methodParameterNames != null) && (methodParameterNames.length == methodParameterValues.length)) {

				int lastIndex = methodParameterNames.length - 1;
				for (int i = 0; i < lastIndex; i++) {
					messageBuilder.append(methodParameterNames[i]);
					messageBuilder.append(':');
					messageBuilder.append(' ');
					messageBuilder.append(ValueToString(methodParameterValues[i]));
					messageBuilder.append(';');
					messageBuilder.append(' ');
				}
				messageBuilder.append(methodParameterNames[lastIndex]);
				messageBuilder.append(':');
				messageBuilder.append(' ');
				messageBuilder.append(ValueToString(methodParameterValues[lastIndex]));
			} else {
				int lastIndex = methodParameterValues.length - 1;
				for (int i = 0; i < lastIndex; i++) {
					messageBuilder.append(ValueToString(methodParameterValues[i]));
					messageBuilder.append(';');
					messageBuilder.append(' ');
				}
				messageBuilder.append(ValueToString(methodParameterValues[lastIndex]));
			}
		}
		messageBuilder.append(')');
		messageBuilder.append(':');

		// Print message
		messageBuilder.append(' ');
		messageBuilder.append(formatMessage(record));
		messageBuilder.append('\n');

		// Print stack trace if we are dealing with an exception of some sort
		if (record.getThrown() != null) {
			StringWriter writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			record.getThrown().printStackTrace(printWriter);
			messageBuilder.append(writer.toString());
		}
		return messageBuilder.toString();
	}

	static public String ValueToString(Object value) {
		if (value == null)
			return "NULL";
		else if (value instanceof Object[]) {
			Object[] valueArray = (Object[]) value;

			StringBuilder resultBuilder = new StringBuilder();
			resultBuilder.append('[');
			int lastIndex = valueArray.length - 1;
			if (valueArray.length > 0) {
				for (int i = 0; i < lastIndex; i++) {
					resultBuilder.append(ValueToString(valueArray[i]));
					resultBuilder.append(';');
				}
				resultBuilder.append(ValueToString(valueArray[lastIndex]));
			}
			resultBuilder.append(']');
			return resultBuilder.toString();
		} else
			return value.toString();
	}
}
