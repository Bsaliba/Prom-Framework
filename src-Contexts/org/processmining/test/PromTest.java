package org.processmining.test;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.runner.RunWith;
import org.processmining.framework.annotations.TestMethod;
import org.processmining.test.factory.FactoryRunner;
import org.processmining.test.factory.TestFactory;

/**
 * The base class for unit tests in ProM. A class the extends this class is a JUnit
 * test suite that gets automatically enriched with two kinds of tests.
 * 
 * 1) Inclass Method Tests: these are tests generated from all methods in the plugin
 *    code that are annotated with {@link TestMethod} and the corresponding attributes.
 *    Tests are generated from all classes stored in {@value #defaultClassFileLocations}.
 *    
 * 2) Automated test scripts: these are tests that run a sequence of scripted tests
 *    that are stored in a specified location: {@link #defaultTestScriptDir}.
 *    
 * The tests may access files/compare results to files stored in {@link #defaultTestDir}.
 * 
 * @author Dirk Fahland
 */
@RunWith(FactoryRunner.class)
public class PromTest extends TestCase {

	/**
	 * Default location of test files that is used if the system property
	 * <code>test.testFileRoot</code> is not set. Overwrite this value if
	 * necessary. Default value {@value #defaultTestDir}.
	 */
	public static String defaultTestDir = "./tests/testfiles";
	
	/**
	 * Default location of test script files inside {@link #defaultTestDir}
	 * that is used if the system property <code>test.testScriptRoot</code>
	 * is not set. By default, this location is {@value #defaultTestScriptDir}.
	 * Overwrite this value if necessary.
	 */
	public static String defaultTestScriptDir = "autoscripts";
	
	/**
	 * Default location of class files that contain methods which are annotated
	 * with {@link TestMethod} and which shall be run in the JUnit test. Overwrite
	 * this value if necessary. Default value {@value #defaultTestDir}.
	 */
	public static String[] defaultClassFileLocations = { "./bin" };
	
	/**
	 * @return default location of the test files; defaults to
	 *         {@value #defaultTestDir}; override this method to set a different
	 *         default location
	 */
	public String getDefaultTestDir() {
		return defaultTestDir;
	}
	
	/**
	 * @return default location of the test scripts that can be run
	 *         automatically; defaults to {@value #defaultTestScriptDir} within
	 *         {@value #defaultTestDir}; override this method to set a different
	 *         default location
	 */
	public String getDefaultTestScriptDir() {
		return getDefaultTestDir()+"/"+defaultTestScriptDir;
	}

	@TestFactory
	public static Collection<? extends Object> testScripts() {

		String testFileRoot = System.getProperty("test.testFileRoot", defaultTestDir);
	    String testScriptRoot = System.getProperty("test.testScriptRoot", testFileRoot+"/"+defaultTestScriptDir);
	    
		List<String> testScripts = AllStandardScriptTests.getAllTestScripts(testScriptRoot);
		List<StandardScriptTest> tests = new LinkedList<StandardScriptTest>();
		for (String scriptFile : testScripts) {
			tests.add(new StandardScriptTest(testScriptRoot+"/"+scriptFile));	
		}
		
		return tests;
	}
	
	@TestFactory
	public static Collection<? extends Object> inlineTests() {

		String testFileRoot = System.getProperty("test.testFileRoot", defaultTestDir);
		
		AllInclassMethodTests testCollector = new AllInclassMethodTests();
		for (String classFileLocation : defaultClassFileLocations)
			testCollector.collectAllTestMethods(classFileLocation);
		
		List<Method> testMethods = testCollector.getAllTestMethods();
		List<InclassMethodTest> tests = new LinkedList<InclassMethodTest>();
		for (Method m : testMethods) {
			tests.add(new InclassMethodTest(m, testFileRoot));	
		}

		return tests;
	}
}
