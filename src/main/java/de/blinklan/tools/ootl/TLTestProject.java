package de.blinklan.tools.ootl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import br.eti.kinoshita.testlinkjavaapi.model.Build;
import br.eti.kinoshita.testlinkjavaapi.model.TestPlan;
import br.eti.kinoshita.testlinkjavaapi.model.TestProject;
import br.eti.kinoshita.testlinkjavaapi.model.TestSuite;
import br.eti.kinoshita.testlinkjavaapi.util.TestLinkAPIException;
import de.blinklan.tools.ootl.exception.MissingPermissionException;
import de.blinklan.tools.ootl.exception.TestLinkException;

/**
 * Wraps a TestProject and related API calls
 * 
 * @author dimasinger
 *
 */
public class TLTestProject {

	private static final Log log = LogFactory.getLog(TLTestProject.class);

	protected final TestLink tl;

	protected final int projectID;
	protected final String projectName;

	// cached first level test suites and builds
	private List<TLTestSuite> cachedFirstLevelTestSuites = null;
	private Map<List<String>, Optional<TLTestSuite>> cachedTestSuitePaths = new HashMap<>();
	private Map<String, Optional<TLBuild>> cachedBuilds = new HashMap<>();

	protected TLTestProject(TestLink tl, TestProject project) {
		this.tl = tl;
		this.projectID = project.getId();
		this.projectName = project.getName();
	}

	private Optional<TLTestSuite> resolveTestSuiteByPath(List<String> testSuitePath) {
		String logPath = "'" + String.join("/", testSuitePath) + "'";
		log.debug("Caching test suite path " + logPath);

		if(testSuitePath.isEmpty()) {
			log.warn("Can not resolve test suite path: path empty");
			return Optional.empty();
		}
		Optional<TLTestSuite> current = getFirstLevelTestSuite(testSuitePath.get(0));
		for(int i = 1; i < testSuitePath.size(); ++i) {
			String path = testSuitePath.get(i);
			current = current.flatMap(c -> c.getTestSuite(path));
		}
		return current;
	}

	/*
	 * Getters
	 */

	public int getID() {
		return projectID;
	}

	public String getName() {
		return projectName;
	}

	/*
	 * API calls
	 */

	/**
	 * Creates a build in this test project
	 * 
	 * @param testPlanName
	 *            the name of the test plan the build will be created in
	 * @param buildName
	 *            the name of the new build
	 * @return the created build
	 * @throws MissingPermissionException
	 *             if missing the permission to create builds
	 * @throws TestLinkException
	 *             if the build could not be created
	 */
	public TLBuild createBuild(String testPlanName, String buildName) {
		String key = testPlanName + ":" + buildName;
		if(tl.config.createBuild) {
			log.info("Creating build " + key);
			try {
				TestPlan plan = tl.api.getTestPlanByName(testPlanName, projectName);
				return new TLBuild(tl, this, plan, tl.api.createBuild(plan.getId(), buildName, buildName));
			} catch(TestLinkAPIException e) {
				throw new TestLinkException("Failed to create build " + key, e);
			}
		} else throw new MissingPermissionException("Creating builds not permitted");
	}

	/**
	 * Retrieves a build of this test project by name.
	 * 
	 * @param testPlanName
	 *            the name of the test plan the build is in
	 * @param buildName
	 *            the name of the build
	 * @return An {@code Optional} containing the build if it exists
	 */
	public Optional<TLBuild> getBuild(String testPlanName, String buildName) {
		String key = testPlanName + ":" + buildName;
		if(cachedBuilds.containsKey(key)) return cachedBuilds.get(key);

		log.debug("Caching build " + key);
		TestPlan plan = tl.api.getTestPlanByName(testPlanName, projectName);
		Build[] builds = tl.api.getBuildsForTestPlan(plan.getId());
		Optional<TLBuild> build = Arrays.stream(builds).filter(b -> b.getName().equals(buildName)).findAny()
				.map(b -> new TLBuild(tl, this, plan, b));
		cachedBuilds.put(key, build);
		return build;
	}

	/**
	 * Retrieves all first level test suites of this test project.
	 * 
	 * @return an unmodifiable {@code List} containing the first level test suites
	 */
	public List<TLTestSuite> getFirstLevelTestSuites() {
		if(cachedFirstLevelTestSuites == null) {
			log.debug("Caching first level test suites for project " + projectName);
			cachedFirstLevelTestSuites = new ArrayList<>();
			TestSuite[] suites = tl.api.getFirstLevelTestSuitesForTestProject(projectID);
			Arrays.stream(suites).map(s -> new TLTestSuite(tl, this, null, s)).forEach(cachedFirstLevelTestSuites::add);
		}
		return Collections.unmodifiableList(cachedFirstLevelTestSuites);
	}

	/**
	 * Retrieves a specific first level test suite of this test project.
	 * 
	 * @param testSuiteName
	 *            the name of the test suite
	 * @return An {@code Optional} containing the suite if it exists
	 */
	public Optional<TLTestSuite> getFirstLevelTestSuite(String testSuiteName) {
		return getFirstLevelTestSuites().stream().filter(s -> s.getName().equals(testSuiteName)).findAny();
	}

	/**
	 * Retrieves a test suite specified by its path through the test suite
	 * hierarchy.<br>
	 * The path is passed already split in form of a {@code List<String>}, with the
	 * name of the first level test suite as the first element, the name of one of
	 * its children as the second and so on.
	 * 
	 * @param testSuitePath
	 *            the path to the test suite
	 * @return An {@code Optional} containing the suite if the path exists
	 */
	public Optional<TLTestSuite> getTestSuiteByPath(List<String> testSuitePath) {
		return cachedTestSuitePaths.computeIfAbsent(testSuitePath, this::resolveTestSuiteByPath);
	}

	/**
	 * Retrieves a test suite specified by its path through the test suite
	 * hierarchy.<br>
	 * The path is passed in form of a {@code String}, which holds the path through
	 * the test suite hierarchy. Forward slashes and backslashes are both accepted
	 * as a path separator.
	 * 
	 * @param testSuitePath
	 *            the path to the test suite
	 * @return An {@code Optional} containing the suite if the path exists
	 */
	public Optional<TLTestSuite> getTestSuiteByPath(String testSuitePath) {
		return getTestSuiteByPath(Arrays.asList(testSuitePath.split("[/\\\\]")));
	}

}
