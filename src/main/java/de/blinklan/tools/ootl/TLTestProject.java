package de.blinklan.tools.ootl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import br.eti.kinoshita.testlinkjavaapi.model.Build;
import br.eti.kinoshita.testlinkjavaapi.model.TestPlan;
import br.eti.kinoshita.testlinkjavaapi.model.TestProject;
import br.eti.kinoshita.testlinkjavaapi.model.TestSuite;
import br.eti.kinoshita.testlinkjavaapi.util.TestLinkAPIException;

/**
 * Wraps a TestProject and related API calls
 * 
 * @author dvo
 *
 */
public class TLTestProject {
    
    private static final Logger log = LogManager.getLogger(TLTestProject.class);

    final TestLink tl;

    final int projectID;
    final String projectName;

    // cached first level test suites and builds
    private List<TLTestSuite> cachedFirstLevelTestSuites = null;
    private Map<List<String>, TLTestSuite> cachedTestSuitePaths = new HashMap<>();
    private Map<String, TLBuild> cachedBuilds = new HashMap<>();

    TLTestProject(TestLink tl, TestProject project) {
        this.tl = tl;
        this.projectID = project.getId();
        this.projectName = project.getName();
    }

    private TLTestSuite resolveTestSuiteByPath(List<String> testSuitePath) {
        String logPath = "'" + String.join("/", testSuitePath) + "'";
        log.debug("Caching test suite path " + logPath);

        if (testSuitePath.isEmpty()) {
            log.error("Can not resolve test suite path: path empty");
            return null;
        }
        TLTestSuite current = getFirstLevelTestSuite(testSuitePath.get(0));
        if (current == null) {
            log.error("Can not create top level test suite: " + testSuitePath.get(0));
            return null;
        }
        for (int i = 1; i < testSuitePath.size(); ++i) {
            current = current.getOrCreateTestSuite(testSuitePath.get(i));
            if (current == null) {
                log.error("Failed to resolve test suite path " + logPath + "!");
                return null;
            }
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
     * Retrieves a build of this test project by name.
     * 
     * @param testPlanName
     *        the name of the test plan the build is in
     * @param buildName
     *        the name of the build
     */
    public TLBuild getBuild(String testPlanName, String buildName) {
        String key = testPlanName + ":" + buildName;
        if (cachedBuilds.containsKey(key))
            return cachedBuilds.get(key);

        String logBuild = "'" + buildName + "' in test plan '" + testPlanName + "'";
        log.debug("Caching build " + logBuild);

        TestPlan plan = tl.api.getTestPlanByName(testPlanName, projectName);
        Build[] builds = tl.api.getBuildsForTestPlan(plan.getId());
        Build build = Arrays.stream(builds).filter(b -> b.getName().equals(buildName)).findAny().orElse(null);
        if (build == null) {
            if (tl.config.createBuild) {
                log.info("Creating build " + logBuild);
                try {
                    build = tl.api.createBuild(plan.getId(), buildName, buildName);
                } catch (TestLinkAPIException e) {
                    log.error("Failed to create build " + logBuild, e);
                    return null;
                }
            } else {
                log.error("No such build: " + logBuild);
                return null;
            }
        }
        TLBuild b = new TLBuild(tl, this, plan, build);
        cachedBuilds.put(key, b);
        return b;
    }

    /**
     * Retrieves all first level test suites of this test project.
     * 
     * @return an unmodifiable {@code List} containing the first level test suites
     */
    public List<TLTestSuite> getFirstLevelTestSuites() {
        if (cachedFirstLevelTestSuites == null) {
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
     *        the name of the test suite
     * @return the test suite if it exists, null otherwise
     */
    public TLTestSuite getFirstLevelTestSuite(String testSuiteName) {
        return getFirstLevelTestSuites().stream().filter(s -> s.getName().equals(testSuiteName)).findAny().orElse(null);
    }

    /**
     * Retrieves a test suite specified by its path through the test suite hierarchy.<br>
     * The path is passed already split in form of a {@code List<String>}, with the name of the first level test suite
     * as the first element, the name of one of its child as the second and so on.
     * 
     * @param testSuitePath
     *        the path to the test suite
     * @return the test suite if it exists, null otherwise
     */
    public TLTestSuite getTestSuiteByPath(List<String> testSuitePath) {
        return cachedTestSuitePaths.computeIfAbsent(testSuitePath, this::resolveTestSuiteByPath);
    }
    
    public TLTestSuite getTestSuiteByPath(String testSuitePath) {
        return getTestSuiteByPath(Arrays.asList(testSuitePath.split("[/\\\\]")));
    }

}
