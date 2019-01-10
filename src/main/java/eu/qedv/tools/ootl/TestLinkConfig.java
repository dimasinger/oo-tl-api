package eu.qedv.tools.ootl;

/**
 * Structure holding permissions for actions in TestLink
 * 
 * @author dimasinger
 *
 */
public class TestLinkConfig {

	protected final boolean createTestCase;
	protected final boolean updateTestCase;
	protected final boolean executeTestCase;

	protected final boolean createTestSuite;

	protected final boolean createBuild;

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
