package de.blinklan.tools.ootl;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import br.eti.kinoshita.testlinkjavaapi.TestLinkAPI;
import br.eti.kinoshita.testlinkjavaapi.model.TestProject;
import br.eti.kinoshita.testlinkjavaapi.util.TestLinkAPIException;

/**
 * Holds the active connection to testlink as represented by a TestLinkAPI
 * object.<br>
 * Acts as the entry point to the testlink API
 * <p>
 * This API wraps {@link TestLinkAPI}.<br>
 * Instead of the complex methods with many parameters of {@code TestLinkAPI},
 * this API uses handles for testlink constructs (like test suites or builds) to
 * hide implementation details (like IDs) from the user. It aims to make using
 * the API more intuitive and less error-prone. It improves performance by
 * caching almost everything, which is crucial for large operations such as
 * copying hundreds of test cases.
 * 
 * @author dimasinger
 * 
 */
public class TestLink {

    private static final Log log = LogFactory.getLog(TestLink.class);
    
    protected String username;

    protected TestLinkAPI api;
    protected TestLinkConfig config;

    // cached test projects
    private Map<String, TLTestProject> projects = new HashMap<>();
    
    public TestLink(TestLinkConfig config, TestLinkAPI api, String username) {
    	this.username = username;
    	this.api = api;
    	this.config = config;
    }
    
    public TestLink(TestLinkConfig config, URL testlinkURL, String developerKey, String username) {
    	this(config, new TestLinkAPI(testlinkURL, developerKey), username);
    }

    /*
     * API calls
     */

    private TLTestProject loadTestProject(String testProjectName) {
        try {
            TestProject project = api.getTestProjectByName(testProjectName);
            return new TLTestProject(this, project);
        } catch (TestLinkAPIException e) {
            log.error("No such test project: " + testProjectName, e);
            return null;
        }
    }

    /**
     * Creates a handle to a test project, through which all API calls are then made
     */
    public Optional<TLTestProject> getTestProject(String testProjectName) {
        return Optional.ofNullable(projects.computeIfAbsent(testProjectName, this::loadTestProject));
    }
}
