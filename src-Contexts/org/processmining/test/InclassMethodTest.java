

package org.processmining.test;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;

import junit.framework.Assert;

import org.processmining.framework.annotations.TestMethod;
import org.processmining.test.factory.FactoryTest;

/**
 * Encapsulate a method (annotated with {@link TestMethod}) in a 
 * JUnit test. 
 * 
 * @author Dirk Fahland
 */
public class InclassMethodTest {
	
	private Method test;
	private String testFileRoot;

	/**
	 * Encapsulate the given method (annotated with {@link TestMethod}) in a 
	 * JUnit test. When executed, the test may access files stored at
	 * <code>testFileRoot</code>. 
	 * 
	 * @param method
	 * @param testFileRoot
	 */
	public InclassMethodTest(Method method, String testFileRoot) {
		this.test = method;
		this.testFileRoot = testFileRoot;
	}
	
	/**
	 * Invokes the method annotated with {@link TestMethod} and compares the
	 * result with an expected value. Comparison is done using
	 * {@link Assert#assertEquals(String, String, String)}
	 * 
	 * @throws Throwable
	 */
	@FactoryTest
	public void test() throws Throwable {
		

		// depending on the test, we redirect System.out to a new output stream
		// which we then compare against the expected result
		PrintStream oldSystemOut = System.out;
		OutputStream resultOutStream = new ByteArrayOutputStream();
		PrintStream tempSystemOut = new PrintStream(resultOutStream);
		
		if (AllInclassMethodTests.testResultFromSystemOut(test)) {
			// write System.out temporarily into the resultOutStream 
			System.setOut(tempSystemOut);
		}
		
		// run test and get test result
		String result = (String)test.invoke(null);
		if (AllInclassMethodTests.testResultFromSystemOut(test)) {
			// get test result from resultOutStream and restore old output system out
			result = resultOutStream.toString();
			System.setOut(oldSystemOut);
		}
		
		// load expected result
		String expected = null;
		if (AllInclassMethodTests.testExpectedFromOutputAnnotation(test)) {
			expected = test.getAnnotation(TestMethod.class).output();
		} else if (AllInclassMethodTests.testExpectedFromFile(test)) {
			expected = readFile(testFileRoot+"/"+test.getAnnotation(TestMethod.class).filename());
		}
		
		Assert.assertEquals(getTestName(test), expected, result);
	}

	/**
	 * @param scriptFileName
	 * @return contents of the file at the given scriptFileName
	 * @throws IOException
	 */
	private static String readFile(String scriptFileName) throws IOException {
		InputStream is = new FileInputStream(scriptFileName);
		String result = readWholeStream(is);
		is.close();
		return result;
	}

	/**
	 * Read an input stream into a string.
	 * 
	 * @param is
	 * @return contents of the input stream
	 * @throws IOException
	 */
	private static String readWholeStream(InputStream is) throws IOException {
		InputStreamReader reader = new InputStreamReader(new BufferedInputStream(is));
		StringBuffer result = new StringBuffer();
		int c;

		while ((c = reader.read()) != -1) {
			result.append((char) c);
		}
		return result.toString();
	}

	/**
	 * @param m
	 * @return qualified Java name pointing to the location of the test in the Java class
	 */
	private static String getTestName (Method m) {
		return m.getClass().toString()+"."+m.getName();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getTestName(test);
	}
}
