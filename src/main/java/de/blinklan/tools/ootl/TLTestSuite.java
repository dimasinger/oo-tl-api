package de.blinklan.tools.ootl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

    protected TLTestSuite(TestLink tl, TLTestProject project, TLTestSuite parent, TestSuite suite) {
        this.tl = tl;
        this.project = project;
        this.parent = parent;
        this.suite = suite;
        this.suiteID = suite.getId();
        this.suiteName = suite.getName();
    }

    private void initAsNewSuite() {
        childSuites = new ArrayList<>();
        childTestcases = new ArrayList<>();
    }

    private TLTestCase createTestCase(String testCaseName, String summary, List<TestCaseStep> steps) {
        try {
            TLTestCase tc = new TLTestCase(tl, project, this,
                    tl.api.createTestCase(testCaseName, suiteID, project.getID(), tl.username, summary, steps, "",
                            TestCaseStatus.FINAL, TestImportance.MEDIUM, ExecutionType.AUTOMATED, 0, 0, true,
                            ActionOnDuplicate.BLOCK));
            if (tc.getID() == -1) {
                log.error("Failed to create test case '" + testCaseName + "' in '" + suiteName + "'");
                return null;
            }
            log.debug("Created test case " + testCaseName);
            if (childTestcases == null)
                getTestCases();
            childTestcases.add(tc);
            return tc;
        } catch (TestLinkAPIException e) {
            log.error("Failed to create test case '" + testCaseName + "' in '" + suiteName + "':", e);
            return null;
        }
    }

    private TLTestSuite createTestSuite(String testSuiteName) {
        TLTestSuite ts;
        String logSuite = "'" + testSuiteName + "' in '" + suiteName + "'";
        if (!tl.config.createTestSuite) {
            log.error("No such test suite: " + logSuite);
            return null;
        }

        log.info("Creating new test suite " + logSuite);
        try {
            ts = new TLTestSuite(tl, project, this,
                    tl.api.createTestSuite(project.getID(), testSuiteName, "", suiteID, 0, true, ActionOnDuplicate.BLOCK));
            ts.initAsNewSuite();
            childSuites.add(ts);
            return ts;
        } catch (TestLinkAPIException e) {
            log.error("Failed to create test suite " + logSuite, e);
            return null;
        }
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
        if (childSuites == null) {
            log.debug("Caching child test suites of " + suiteName);
            childSuites = new ArrayList<>();
            TestSuite[] suites = tl.api.getTestSuitesForTestSuite(suiteID);
            Arrays.stream(suites).map(s -> new TLTestSuite(tl, project, this, s)).forEach(childSuites::add);
        }
        return Collections.unmodifiableList(childSuites);
    }

    /**
     * Retrieves a specific child test suite by name
     * 
     * @param testSuiteName
     *        the name of the child test suite
     * @return An {@code Optional} containing the test suite
     */
    public Optional<TLTestSuite> getTestSuite(String testSuiteName) {
        return getTestSuites().stream().filter(s -> s.getName().equals(testSuiteName)).findAny();
    }

    /**
     * Retrieves a specific child test suite by name. If it doesn't exist, creates
     * it.
     * 
     * @param testSuiteName
     *        the name of the child test suite
     * @return An {@code Optional} containing the found or created test suite
     */
    public Optional<TLTestSuite> getOrCreateTestSuite(String testSuiteName) {
        return Optional.ofNullable(getTestSuite(testSuiteName).orElseGet(() -> createTestSuite(testSuiteName)));
    }

    /**
     * Retrieves all test cases in this test suite
     * 
     * @return an unmodifiable {@code List} of the child test cases
     */
    public List<TLTestCase> getTestCases() {
        if (childTestcases == null) {
            log.debug("Caching child test cases of " + suiteName);
            childTestcases = new ArrayList<>();
            TestCase[] cases = tl.api.getTestCasesForTestSuite(suiteID, true, TestCaseDetails.FULL);
            Arrays.stream(cases).map(tc -> new TLTestCase(tl, project, this, tc)).forEach(childTestcases::add);
        }
        return Collections.unmodifiableList(childTestcases);
    }

    /**
     * Retrieves a specific child test case by name
     * 
     * @param testCaseName
     *        the name of the test case
     * @return the test case if it exists, null otherwise
     */
    public Optional<TLTestCase> getTestCase(String testCaseName) {
        log.debug("Resolving test case '" + testCaseName + "' in '" + suiteName + "'");
        return getTestCases().stream().filter(tc -> tc.getName().equals(testCaseName)).findAny();
    }

    /**
     * Creates a new test case in this test suite
     * 
     * @param testCaseName
     *        the name of the test case
     * @param summary
     *        the summary of the test case
     * @param result
     *        A {@code TLTestStep} describing the steps of the test case
     * @return the created test case
     * @throws TestLinkException
     *         if the test case could not be created
     */
    public TLTestCase createTestCase(String testCaseName, String summary, TLTestStep result) {
        return createTestCase(testCaseName, summary, result.toSteps(1));
    }

    /**
     * Creates a new test case in this test suite or updates an existing one with
     * the same name.
     * 
     * @param testCaseName
     *        the test case name
     * @param summary
     *        the test case summary
     * @param steps
     *        an ordered list of the test steps
     * @return the test case, or null if missing permission to create new test cases
     */
    public TLTestCase createOrUpdateTestCase(String testCaseName, String summary, TLTestStep result) {
        TLTestCase tc = getTestCase(testCaseName).orElse(null);
        if (tc != null) {
            TLTestCase tcNew = tc.update(summary, result);
            if (childTestcases == null)
                getTestCases();
            childTestcases.remove(tc);
            childTestcases.add(tcNew);
            return tcNew;
        }
        if (!tl.config.createTestCase) {
            log.error("No such test case: '" + testCaseName + "' in '" + suiteName + "'");
            return null;
        }

        log.info("Creating new test case '" + testCaseName + "' in '" + suiteName + "'");
        return createTestCase(testCaseName, summary, result);
    }

    /**
     * Creates a new test case as a copy of an already existing one.<br>
     * If a test case with this name already exists, returns it instead.
     * 
     * @param newName
     *        the name of the new test case
     * @param blueprint
     *        the test case to be copied
     * @return the created test case, or null if missing permission to create new
     *         test cases
     */
    public TLTestCase copyTestCase(String newName, TLTestCase blueprint) {
        String oldName = blueprint.getName();
        TLTestCase destination = getTestCase(newName).orElse(null);
        if (destination != null) {
            log.debug("Test case '" + newName + "' already exists in '" + suiteName + "'");
            return destination;
        }
        log.info("Creating copy of test case '" + oldName + "' in '" + suiteName
                + (oldName.equals(newName) ? "'" : "' with new name '" + newName + "'"));
        return createTestCase(newName, blueprint.testcase.getSummary(), blueprint.testcase.getSteps());
    }
}
