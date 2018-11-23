package de.blinklan.tools.ootl;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.blinklan.tools.ootl.structure.ResultCode;
import de.blinklan.tools.ootl.structure.TLTestStep;

import br.eti.kinoshita.testlinkjavaapi.constants.ActionOnDuplicate;
import br.eti.kinoshita.testlinkjavaapi.constants.ExecutionStatus;
import br.eti.kinoshita.testlinkjavaapi.constants.ExecutionType;
import br.eti.kinoshita.testlinkjavaapi.constants.TestCaseStatus;
import br.eti.kinoshita.testlinkjavaapi.constants.TestImportance;
import br.eti.kinoshita.testlinkjavaapi.model.TestCase;
import br.eti.kinoshita.testlinkjavaapi.model.TestCaseStep;
import br.eti.kinoshita.testlinkjavaapi.util.TestLinkAPIException;

public class TLTestCase {

    private static final Logger log = LogManager.getLogger(TLTestCase.class);
    
    final TestLink tl;

    final TLTestProject project;
    final TLTestSuite parent;

    final TestCase testcase;
    final int testcaseID;
    final String testcaseName;

    TLTestCase(TestLink tl, TLTestProject project, TLTestSuite parent, TestCase testcase) {
        this.tl = tl;
        this.project = project;
        this.parent = parent;
        this.testcase = testcase;
        this.testcaseID = testcase.getId();
        this.testcaseName = testcase.getName();
    }

    private boolean stepsChanged(TLTestStep result) {
        List<TestCaseStep> steps = testcase.getSteps();
        List<TestCaseStep> newSteps = result.toSteps(-1);
        if (newSteps.size() != steps.size())
            return true;
        for (int i = 0; i < newSteps.size(); ++i) {
            TestCaseStep step = steps.get(i);
            TestCaseStep newStep = newSteps.get(i);
            if (!step.getActions().equals(newStep.getActions()))
                return true;
            if (!step.getExpectedResults().equals(newStep.getExpectedResults()))
                return true;
        }
        return false;
    }

    /*
     * Getters
     */

    public TLTestSuite getParent() {
        return parent;
    }

    public String getName() {
        return testcaseName;
    }

    public int getID() {
        return testcase.getId();
    }

    public int getVersion() {
        return testcase.getVersion();
    }

    /*
     * API calls
     */

    /**
     * Updates the summary and steps of a test case. Does nothing if neither summary nor steps changed.
     * @param summary the new summary
     * @param result an execution result the steps can be derived from
     * @return the updated test case
     */
    public TLTestCase update(String summary, TLTestStep result) {
        if (summary.equals(testcase.getSummary()) && !stepsChanged(result))
            return this;

        String logCase = "Test case '" + testcaseName + "'";
        if (!tl.config.updateTestCase) {
            log.debug(logCase + " changed but testcase.update is false. Not updating test case");
        }
        if (result.getResult() != ResultCode.SUCCESS) {
            log.debug(logCase + " changed but test failed. Not updating test case");
            return this;
        }

        int version = getVersion() + 1;
        log.info(logCase + " changed. Updating to version " + version);

        try {
            TestCase tc = tl.api.createTestCase(testcaseName, parent.getID(), project.getID(), tl.username, summary,
                    result.toSteps(version), "", TestCaseStatus.FINAL, TestImportance.MEDIUM, ExecutionType.AUTOMATED, 0, -1,
                    true, ActionOnDuplicate.CREATE_NEW_VERSION);
            return new TLTestCase(tl, project, parent, tc);
        } catch (TestLinkAPIException e) {
            log.error("Failed to update test case '" + testcaseName + "':", e);
        }
        return this;
    }

    /**
     * Executes a test case.
     * @param build the build to execute the test case in
     * @param result the result code (a result of NOT_RUN is aborts execution)
     * @param notes the execution notes
     * @return true if test case executed successfully, false otherwise
     */
    public boolean execute(TLBuild build, ResultCode result, String notes) {
        if (!tl.config.executeTestCase) {
            log.warn("Execution disabled. Not executing test case '" + testcaseName + "'");
            return false;
        }
        log.debug("Executing test case '" + testcaseName + "' (Result: " + result.toString() + ")");

        ExecutionStatus status = result.toExecutionStatus();
        if (status == ExecutionStatus.NOT_RUN) {
            log.warn("Test case '" + testcaseName + "' not run! Not executing test case");
            return false;
        }

        int v = build.addTestcaseToTestPlan(this);
        if (v == -1)
            return false;
        if (v > 0 && v != getVersion()) {
            log.debug("Executing version " + v + ", adding comment with version " + getVersion() + " to execution notes");
            notes += "\n\nActual executed version: " + getVersion() + "\nGo to Test Specification to see changes";
        }

        try {
            tl.api.setTestCaseExecutionResult(testcaseID, -1, build.planID, status, build.buildID, "", notes.trim(), false, "", -1,
                    "", null, true);
            return true;
        } catch (TestLinkAPIException e) {
            log.error("Failed to execute test case '" + testcaseName + "':", e);
            return false;
        }
    }
}
