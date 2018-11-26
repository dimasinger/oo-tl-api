package de.blinklan.tools.ootl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.blinklan.tools.ootl.exception.FailedCreationException;
import de.blinklan.tools.ootl.exception.MissingPermissionException;
import de.blinklan.tools.ootl.exception.TestLinkException;
import de.blinklan.tools.ootl.structure.TLTestStep;

import br.eti.kinoshita.testlinkjavaapi.constants.ActionOnDuplicate;
import br.eti.kinoshita.testlinkjavaapi.constants.ExecutionType;
import br.eti.kinoshita.testlinkjavaapi.constants.TestCaseDetails;
import br.eti.kinoshita.testlinkjavaapi.constants.TestCaseStatus;
import br.eti.kinoshita.testlinkjavaapi.constants.TestImportance;
import br.eti.kinoshita.testlinkjavaapi.model.TestCase;
import br.eti.kinoshita.testlinkjavaapi.model.TestCaseStep;
import br.eti.kinoshita.testlinkjavaapi.model.TestSuite;
import br.eti.kinoshita.testlinkjavaapi.util.TestLinkAPIException;

/**
 * Wraps a TestSuite and related API calls
 * 
 * @author dimasinger
 *
 */
public class TLTestSuite {

	private static final Log log = LogFactory.getLog(TLTestSuite.class);

	protected final TestLink tl;

	protected final TLTestProject project;
	protected final TLTestSuite parent;

	protected final TestSuite suite;
	protected final int suiteID;
	protected final String suiteName;

	// cached content of this suite
	private List<TLTestSuite> childSuites = null;
	private List<TLTestCase> childTestcases = null;

	private boolean childSuitesCached = false;
	private boolean childTestcasesCached = false;

	protected TLTestSuite(TestLink tl, TLTestProject project, TLTestSuite parent, TestSuite suite) {
		this.tl = tl;
		this.project = project;
		this.parent = parent;
		this.suite = suite;
		this.suiteID = suite.getId();
		this.suiteName = suite.getName();
	}

	private void cacheChildSuites() {
		if(childSuitesCached) return;
		log.debug("Caching child test suites of " + suiteName);
		try {
			TestSuite[] suites = tl.api.getTestSuitesForTestSuite(suiteID);
			childSuites = Arrays.stream(suites).map(s -> new TLTestSuite(tl, project, this, s)).collect(Collectors
					.toList());
			childSuitesCached = true;
		} catch(TestLinkAPIException e) {
			throw new TestLinkException("Failed to cache child test suites", e);
		}
	}

	private void cacheChildTestcases() {
		if(childTestcasesCached) return;
		log.debug("Caching child test cases of " + suiteName);
		try {
			TestCase[] cases = tl.api.getTestCasesForTestSuite(suiteID, true, TestCaseDetails.FULL);
			childTestcases = Arrays.stream(cases).map(tc -> new TLTestCase(tl, project, this, tc)).collect(Collectors
					.toList());
			childTestcasesCached = true;
		} catch(TestLinkAPIException e) {
			throw new TestLinkException("Failed to cache child test cases", e);
		}
	}

	private TLTestCase createTestCase(String testCaseName, String summary, List<TestCaseStep> steps) {
		String key = suiteName + ":" + testCaseName;
		if(!tl.config.createTestCase) {
			throw new MissingPermissionException("Creating test cases not permitted");
		}

		log.debug("Creating test case " + key);
		TestCase testcase;
		try {
			testcase = tl.api.createTestCase(testCaseName, suiteID, project.getID(), tl.username, summary, steps, "",
					TestCaseStatus.FINAL, TestImportance.MEDIUM, ExecutionType.AUTOMATED, 0, 0, true,
					ActionOnDuplicate.BLOCK);
			if(testcase.getId() == -1) {
				throw new FailedCreationException("Failed to create test case " + key + " (testlink returned id -1)");
			}
		} catch(TestLinkAPIException e) {
			throw new FailedCreationException("Failed to create test case " + key, e);
		}
		TLTestCase tc = new TLTestCase(tl, project, this, testcase);
		log.debug("Created test case " + key);

		cacheChildTestcases();
		childTestcases.add(tc);
		return tc;
	}

	/*
	 * Getters
	 */

	public Optional<TLTestSuite> getParent() {
		return Optional.ofNullable(parent);
	}

	public String getName() {
		return suiteName;
	}

	public int getID() {
		return suiteID;
	}

	/*
	 * API calls
	 */

	/**
	 * Retrieves all child test suites of this test suite
	 * 
	 * @return an unmodifiable {@code List} of this test suite's children
	 */
	public List<TLTestSuite> getTestSuites() {
		cacheChildSuites();
		return Collections.unmodifiableList(childSuites);
	}

	/**
	 * Retrieves a specific child test suite by name
	 * 
	 * @param testSuiteName
	 *            the name of the child test suite
	 * @return An {@code Optional} containing the test suite
	 */
	public Optional<TLTestSuite> getTestSuite(String testSuiteName) {
		return getTestSuites().stream().filter(s -> s.getName().equals(testSuiteName)).findAny();
	}

	/**
	 * Creates a new test suite in this test suite
	 * 
	 * @param testSuiteName the name of the new test suite
	 * @return the newly created test suite
	 * @throws MissingPermissionException if missing the permission to create
	 *             test suites
	 * @throws FailedCreationException if test suite could not be created
	 */
	public TLTestSuite createTestSuite(String testSuiteName) {
		String key = suiteName + ":" + testSuiteName;
		if(!tl.config.createTestSuite) {
			throw new MissingPermissionException("Creating test suites not permitted");
		}

		log.debug("Creating test suite " + key);
		TestSuite suite;
		try {
			suite = tl.api.createTestSuite(project.getID(), testSuiteName, "", suiteID, 0, true,
					ActionOnDuplicate.BLOCK);

		} catch(TestLinkAPIException e) {
			throw new FailedCreationException("Failed to create test suite " + key, e);
		}
		TLTestSuite ts = new TLTestSuite(tl, project, this, suite);
		log.debug("Created test suite " + key);

		cacheChildSuites();
		childSuites.add(ts);
		return ts;
	}

	/**
	 * Retrieves all test cases in this test suite
	 * 
	 * @return an unmodifiable {@code List} of the child test cases
	 */
	public List<TLTestCase> getTestCases() {
		cacheChildTestcases();
		return Collections.unmodifiableList(childTestcases);
	}

	/**
	 * Retrieves a specific child test case by name
	 * 
	 * @param testCaseName
	 *            the name of the test case
	 * @return the test case if it exists, null otherwise
	 */
	public Optional<TLTestCase> getTestCase(String testCaseName) {
		return getTestCases().stream().filter(tc -> tc.getName().equals(testCaseName)).findAny();
	}

	/**
	 * Creates a new test case in this test suite
	 * 
	 * @param testCaseName
	 *            the name of the test case
	 * @param summary
	 *            the summary of the test case
	 * @param result
	 *            A {@code TLTestStep} describing the steps of the test case
	 * @return the created test case
	 * @throws MissingPermissionException if missing the permission to create
	 *             test cases
	 * @throws TestLinkException
	 *             if the test case could not be created
	 */
	public TLTestCase createTestCase(String testCaseName, String summary, TLTestStep result) {
		return createTestCase(testCaseName, summary, result.toSteps(1));
	}
}
