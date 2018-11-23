package de.blinklan.tools.ootl;

/**
 * Structure holding permissions for actions in TestLink
 * 
 * @author dvo
 *
 */
public class TestLinkConfig {

    final boolean createTestCase;
    final boolean updateTestCase;
    final boolean executeTestCase;

    final boolean createTestSuite;

    final boolean createBuild;

    public static final TestLinkConfig NO_PERMISSIONS = new TestLinkConfig();
    public static final TestLinkConfig ALL_PERMISSIONS = new TestLinkConfig(true, true, true, true, true);
    
    /**
     * Creates a configuration with the specified permissions
     */
    public TestLinkConfig(boolean createTestCase, boolean updateTestCase, boolean executeTestCase, boolean createTestSuite,
            boolean createBuild) {
        this.createTestCase = createTestCase;
        this.updateTestCase = updateTestCase;
        this.executeTestCase = executeTestCase;
        this.createTestSuite = createTestSuite;
        this.createBuild = createBuild;
    }

    /**
     * Creates a configuration without any permissions
     */
    public TestLinkConfig() {
        this(false, false, false, false, false);
    }
}
