package test.meta3TU.standalone;

import java.util.logging.Level;
import java.util.logging.LogRecord;

public class TraceRecord extends LogRecord {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TraceRecord(Level level, String msg) {
		super(level, msg);
		// TODO Auto-generated constructor stub
	}

	protected String[] methodParameterNames;
	protected Object[] methodParameterValues;

	public Object[] getMethodParameterValues() {
		return methodParameterValues;
	}

	public void setMethodParameterValues(Object[] methodParameterValues) {
		this.methodParameterValues = methodParameterValues;
	}

	public String[] getMethodParameterNames() {
		return methodParameterNames;
	}

	public void setMethodParameterNames(String[] methodParameterNames) {
		this.methodParameterNames = methodParameterNames;
	}
	
}
