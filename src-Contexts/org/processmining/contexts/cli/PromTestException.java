package org.processmining.contexts.cli;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Exception that stores all collected failed tests that arised in {@link PromTestFramework}.
 * Call {@link #toString()} for a formatted test report.
 * 
 * @author dfahland
 */
public class PromTestException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3476724712772295479L;
	
	/**
	 * Associates a test case to a thrown exception that occurred during
	 * its execution.
	 */
	protected static class WrappedException {
		protected Method test = null;
		protected Throwable thrown = null;
		
		public WrappedException(Method test, Throwable thrown) {
			this.test = test;
			this.thrown = thrown;
		}
	}
	
	/**
	 * Associates a test case to expected and returned results in case the test was not
	 * successful.
	 */
	protected static class ResultMismatch {
		protected Method test = null;
		protected String expected = null;
		protected String result = null;
		
		public ResultMismatch(Method test, String expected, String result) {
			this.test = test;
			this.expected = expected;
			this.result = result;
		}
	}
	
	private List<ResultMismatch> failures;
	private List<WrappedException> errors;
	
	public PromTestException(List<ResultMismatch> failures, List<WrappedException> errors) {
		this.failures = failures;
		this.errors = errors;
	}
	
	public String toString() {
		
		StringBuilder sb = new StringBuilder();

		sb.append("============================================================\n");
		sb.append("Number of failed tests: "+failures.size()+"\n");
		sb.append("============================================================\n");
		for (ResultMismatch fail : failures) {
			sb.append(getTestName(fail.test)+"\n");
			sb.append("--- RESULT -------------------------------------------------\n");
			sb.append(fail.result+"\n");
			sb.append("--- EXPECTED -----------------------------------------------\n");
			sb.append(fail.expected+"\n");
			sb.append("============================================================\n");
		}
		
		sb.append("============================================================\n");
		sb.append("Number of errors: "+errors.size()+"\n");
		sb.append("============================================================\n");
		for (WrappedException error : errors) {
			sb.append(getTestName(error.test)+"\n");
			sb.append("--- EXCEPTION ----------------------------------------------\n");
			sb.append(error.thrown.toString()+"\n");
			sb.append("============================================================\n");
		}

		return sb.toString();
	}

	private static String getTestName (Method m) {
		return m.getClass().toString()+"."+m.getName();
	}
}
