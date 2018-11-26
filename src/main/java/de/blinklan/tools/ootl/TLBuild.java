package de.blinklan.tools.ootl;

import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.blinklan.tools.ootl.exception.TestLinkException;

import br.eti.kinoshita.testlinkjavaapi.model.Build;
import br.eti.kinoshita.testlinkjavaapi.model.Execution;
import br.eti.kinoshita.testlinkjavaapi.model.TestPlan;
import br.eti.kinoshita.testlinkjavaapi.util.TestLinkAPIException;

/**
 * Wraps a Build, the TestPlan it belongs to and related API calls
 * 
 * @author dimasinger
 * 
 */
public class TLBuild {

	private static final Log log = LogFactory.getLog(TLBuild.class);

	protected final TestLink tl;

	protected final TLTestProject project;

	protected final int planID;
	protected final String planName;

	protected final int buildID;
	protected final String buildName;

	protected TLBuild(TestLink tl, TLTestProject project, TestPlan plan, Build build) {
		this.tl = tl;
		this.project = project;
		this.planID = plan.getId();
		this.planName = plan.getName();
		this.buildID = build.getId();
		this.buildName = build.getName();
	}

	/*
	 * Getters
	 */

	public String getPlanName() {
		return planName;
	}

	public String getBuildName() {
		return buildName;
	}

	/*
	 * API calls
	 */

	/**
	 * Retrieves the latest execution result of a test case in this build
	 * 
	 * @return Execution result if testcase executed in this build
	 */
	public Optional<TLExecution> getLastExecution(TLTestCase testcase) {
		Execution execution;
		try {
			execution = tl.api.getLastExecutionResult(planID, testcase.getID(), -1);
		} catch(TestLinkAPIException e) {
			throw new TestLinkException("Failed to retrieve last execution result of test case '" + testcase.getName()
					+ "' in test plan '" + planName + "':", e);
		}
		if(execution == null) {
			log.debug("Test case '" + testcase.getName() + "' not executed in test plan '" + planName + "'");
			return Optional.empty();
		}
		if(execution.getBuildId() != buildID) {
			log.debug("Test case '" + testcase.getName() + "' not executed in build '" + buildName + "'");
			return Optional.empty();
		}
		return Optional.of(new TLExecution(tl, this, testcase, execution));
	}

	/**
	 * Adds a test case to this test plan
	 * 
	 * @param testcase
	 *            the test case to add
	 * @return -1 on error; 0 on success; version number (>0) if test case already
	 *         in test plan
	 */
	public int addTestcaseToTestPlan(TLTestCase testcase) {
		try {
			tl.api.addTestCaseToTestPlan(project.getID(), planID, testcase.getID(), testcase.getVersion(), -1, 0, 0);
			return 0;
		} catch(TestLinkAPIException e) {
			/*
			 * API function getTestCasesForTestPlan is broken and unusable. Workaround: Just
			 * try to add the test case to the test plan and parse the exception thrown if
			 * test case is already added.
			 */
			String message = e.getMessage();
			if(!message.matches(".*Test Case version number.*requested version.*is already linked to Test Plan.*")) {
				log.error("Failed to add test case '" + testcase.getName() + "' to test plan '" + planName + "':", e);
				return -1;
			}
			String version = message.replaceAll(".*version number ", "")
					.replaceAll(" <> \\d+ \\(requested version\\).*", "");
			log.debug("Test case '" + testcase.getName() + "' already in test plan '" + planName + "'");
			return Integer.parseInt(version);
		}
	}
}
