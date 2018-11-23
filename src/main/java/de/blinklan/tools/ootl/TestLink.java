package de.blinklan.tools.ootl;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import br.eti.kinoshita.testlinkjavaapi.TestLinkAPI;
import br.eti.kinoshita.testlinkjavaapi.model.TestProject;
import br.eti.kinoshita.testlinkjavaapi.util.TestLinkAPIException;
import de.blinklan.tools.ootl.exception.ForcedExitException;

/**
 * Holds the active connection to testlink as represented by a TestLinkAPI object.<br>
 * Acts as the entry point to the testlink API
 * <p>
 * This API wraps {@link TestLinkAPI}.<br>
 * Instead of the complex methods with many parameters of {@code TestLinkAPI}, this API uses handles for testlink
 * constructs (like test suites or builds) to hide implementation details (like IDs) from the user. It aims to make
 * using the API more intuitive and less error-prone. It improves performance by caching almost everything, which is
 * crucial for large operations such as copying hundreds of test cases.
 * 
 * @author dvo
 * 
 */
public class TestLink {

    private static final Logger log = LogManager.getLogger(TestLink.class);
    
    String username;

    TestLinkAPI api;
    TestLinkConfig config;

    // cached test projects
    private Map<String, TLTestProject> projects = new HashMap<>();

    private String getConnectionProperty(Properties connection, String key) {
        String value = connection.getProperty(key);
        if (value == null)
            throw new ForcedExitException("Missing property '" + key + "' in connection.properties");
        return value;
    }

    /**
     * Initializes a new connection to testlink from connection.properties
     * 
     * @param config
     *        permissions given to the API
     */
    public TestLink(TestLinkConfig config) {
        this.config = config;
        Properties connection = new Properties();
        log.debug("Loading connection.properties...");
        try (InputStream is = TestLink.class.getResourceAsStream("/connection.properties")) {
            connection.load(is);
        } catch (IOException | NullPointerException e) {
            throw new ForcedExitException("Failed to load connection.properties", e);
        }
        String testlinkUrl = getConnectionProperty(connection, "testlink-url");
        String devKey = getConnectionProperty(connection, "developer-key");
        username = getConnectionProperty(connection, "username");

        log.debug("Initializing TestLinkAPI...");
        try {
            api = new TestLinkAPI(new URL(testlinkUrl), devKey);
        } catch (TestLinkAPIException e) {
            throw new ForcedExitException("Error while initializing TestLinkAPI:", e);
        } catch (MalformedURLException e) {
            throw new ForcedExitException("Testlink URL malformed: " + testlinkUrl, e);
        }
    }
    
    /**
     * Initializes a new connection to testlink from connection.properties with no permissions
     * 
     */
    public TestLink() {
        this(new TestLinkConfig());
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
    public TLTestProject getTestProject(String testProjectName) {
        return projects.computeIfAbsent(testProjectName, this::loadTestProject);
    }
}
