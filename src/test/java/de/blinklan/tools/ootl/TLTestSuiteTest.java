package de.blinklan.tools.ootl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import br.eti.kinoshita.testlinkjavaapi.TestLinkAPI;
import br.eti.kinoshita.testlinkjavaapi.constants.TestCaseDetails;
import de.blinklan.tools.ootl.util.EmptyOptionalError;
import de.blinklan.tools.ootl.util.TJAUtil;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TLTestSuiteTest {

	@Mock
	TestLinkAPI api;

	TLTestSuite initTestSuiteWithTestCases(String testCasesFileName) {
		TJAUtil.mockBasicProject(api);
		TJAUtil.mockSuites(api, 1, "single");
		TJAUtil.mockTestcases(api, 1, 1, testCasesFileName);

		TestLink testlink = new TestLink(TestLinkConfig.NO_PERMISSIONS, api, "tester");
		TLTestProject project = testlink.getTestProject("project").orElseThrow(EmptyOptionalError::new);
		return project.getFirstLevelTestSuite("suite").orElseThrow(EmptyOptionalError::new);
	}

	@Test
	void testGetTestCaseBasic() {
		TLTestSuite suite = initTestSuiteWithTestCases("basic");

		TLTestCase testcase = suite.getTestCase("Test Case");
		assertThat(testcase.getName()).isEqualTo("Test Case");
		assertThat(testcase.getID()).isEqualTo(1);
		assertThat(testcase.getParent()).isSameAs(suite);

		List<TLTestCase> testcases = suite.getTestCases();
		assertThat(testcases.size()).isEqualTo(4);
		assertThat(testcases.contains(testcase)).isTrue();
	}

	@Test
	void testGetTestCaseCaching() {
		TLTestSuite suite = initTestSuiteWithTestCases("basic");

		suite.getTestCase("Test Case");
		verify(api, times(1)).getTestCasesForTestSuite(anyInt(), anyBoolean(), any(TestCaseDetails.class));
		suite.getTestCase("Case 2");
		suite.getTestCase("A");
		verify(api, times(1)).getTestCasesForTestSuite(anyInt(), anyBoolean(), any(TestCaseDetails.class));
		suite.getTestCases();
		verify(api, times(1)).getTestCasesForTestSuite(anyInt(), anyBoolean(), any(TestCaseDetails.class));
	}
}
