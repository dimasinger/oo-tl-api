package de.blinklan.tools.ootl.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import de.blinklan.tools.ootl.TLTestCase;
import de.blinklan.tools.ootl.TLTestProject;
import de.blinklan.tools.ootl.TLTestSuite;
import de.blinklan.tools.ootl.TestLink;
import de.blinklan.tools.ootl.TestLinkConfig;
import de.blinklan.tools.ootl.exception.FailedCreationException;
import de.blinklan.tools.ootl.exception.MissingPermissionException;
import de.blinklan.tools.ootl.structure.TLTestResult;
import de.blinklan.tools.ootl.test.util.EmptyOptionalError;
import de.blinklan.tools.ootl.test.util.TJAUtil;

import br.eti.kinoshita.testlinkjavaapi.TestLinkAPI;
import br.eti.kinoshita.testlinkjavaapi.constants.ActionOnDuplicate;
import br.eti.kinoshita.testlinkjavaapi.constants.ExecutionType;
import br.eti.kinoshita.testlinkjavaapi.constants.TestCaseDetails;
import br.eti.kinoshita.testlinkjavaapi.constants.TestCaseStatus;
import br.eti.kinoshita.testlinkjavaapi.constants.TestImportance;
import br.eti.kinoshita.testlinkjavaapi.util.TestLinkAPIException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TLTestSuiteTest {

	@Mock
	TestLinkAPI api;

	TLTestSuite initTestSuiteWithTestCases(String testCasesFileName, TestLinkConfig permissions) {
		TJAUtil.mockBasicProject(api);
		TJAUtil.mockSuites(api, 1, "single");
		TJAUtil.mockTestcases(api, 1, 1, testCasesFileName);

		TestLink testlink = new TestLink(permissions, api, "tester");
		TLTestProject project = testlink.getTestProject("project").orElseThrow(EmptyOptionalError::new);
		return project.getFirstLevelTestSuite("suite").orElseThrow(EmptyOptionalError::new);
	}

	@Test
	void testGetTestCaseBasic() {
		TLTestSuite suite = initTestSuiteWithTestCases("basic", TestLinkConfig.NO_PERMISSIONS);

		TLTestCase testcase = suite.getTestCase("Test Case").orElseThrow(EmptyOptionalError::new);
		assertThat(testcase.getName()).isEqualTo("Test Case");
		assertThat(testcase.getID()).isEqualTo(1);
		assertThat(testcase.getParent()).isSameAs(suite);

		List<TLTestCase> testcases = suite.getTestCases();
		assertThat(testcases.size()).isEqualTo(4);
		assertThat(testcases.contains(testcase)).isTrue();
	}

	@Test
	void testGetTestCaseCaching() {
		TLTestSuite suite = initTestSuiteWithTestCases("basic", TestLinkConfig.NO_PERMISSIONS);

		suite.getTestCase("Test Case");
		verify(api, times(1)).getTestCasesForTestSuite(anyInt(), anyBoolean(), any(TestCaseDetails.class));
		suite.getTestCase("Case 2");
		suite.getTestCase("A");
		verify(api, times(1)).getTestCasesForTestSuite(anyInt(), anyBoolean(), any(TestCaseDetails.class));
		suite.getTestCases();
		verify(api, times(1)).getTestCasesForTestSuite(anyInt(), anyBoolean(), any(TestCaseDetails.class));
	}

	@Test
	void testCreateTestCaseBasic() {
		TLTestSuite suite = initTestSuiteWithTestCases("basic", new TestLinkConfig(true, false, false, false, false));
		when(api.createTestCase("case", 1, 1, "tester", "", Collections.emptyList(), "", TestCaseStatus.FINAL,
				TestImportance.MEDIUM, ExecutionType.AUTOMATED, 0, 0, true, ActionOnDuplicate.BLOCK)).thenReturn(TJAUtil
						.getTestCase(1800, 1, 1, "case"));

		TLTestCase testcase = suite.createTestCase("case", "", new TLTestResult());
		assertThat(testcase.getName()).isEqualTo("case");
		assertThat(testcase.getID()).isEqualTo(1800);
		assertThat(testcase.getParent()).isSameAs(suite);
		verify(api, times(1)).createTestCase("case", 1, 1, "tester", "", Collections.emptyList(), "",
				TestCaseStatus.FINAL, TestImportance.MEDIUM, ExecutionType.AUTOMATED, 0, 0, true,
				ActionOnDuplicate.BLOCK);
	}

	@Test
	void testCreateTestCaseNoPermissions() {
		TLTestSuite suite = initTestSuiteWithTestCases("basic", TestLinkConfig.NO_PERMISSIONS);

		assertThatThrownBy(() -> suite.createTestCase("case", "", new TLTestResult())).isInstanceOf(
				MissingPermissionException.class);
		verify(api, never()).createTestCase(anyString(), anyInt(), anyInt(), anyString(), anyString(), anyList(),
				anyString(), any(TestCaseStatus.class), any(TestImportance.class), any(ExecutionType.class), anyInt(),
				anyInt(), anyBoolean(), any(ActionOnDuplicate.class));
	}

	@Test
	void testCreateTestCaseFailure() {
		TLTestSuite suite = initTestSuiteWithTestCases("basic", new TestLinkConfig(true, false, false, false, false));
		when(api.createTestCase("case", 1, 1, "tester", "", Collections.emptyList(), "", TestCaseStatus.FINAL,
				TestImportance.MEDIUM, ExecutionType.AUTOMATED, 0, 0, true, ActionOnDuplicate.BLOCK)).thenThrow(
						TestLinkAPIException.class);

		assertThatThrownBy(() -> suite.createTestCase("case", "", new TLTestResult())).isInstanceOf(
				FailedCreationException.class);
	}

	@Test
	void testCreateTestSuiteBasic() {
		TLTestSuite suite = initTestSuiteWithTestCases("basic", new TestLinkConfig(false, false, false, true, false));
		when(api.createTestSuite(eq(1), eq("new suite"), eq(""), eq(1), anyInt(), anyBoolean(), any(
				ActionOnDuplicate.class))).thenReturn(TJAUtil.getTestSuite(1300, 1, 1, "new suite"));

		TLTestSuite newSuite = suite.createTestSuite("new suite");
		assertThat(newSuite.getName()).isEqualTo("new suite");
		assertThat(newSuite.getID()).isEqualTo(1300);
		assertThat(newSuite.getParent().orElseThrow(EmptyOptionalError::new)).isSameAs(suite);
		verify(api, times(1)).createTestSuite(eq(1), eq("new suite"), eq(""), eq(1), anyInt(), anyBoolean(), any(
				ActionOnDuplicate.class));
	}

	@Test
	void testCreateTestSuiteNoPermissions() {
		TLTestSuite suite = initTestSuiteWithTestCases("basic", TestLinkConfig.NO_PERMISSIONS);

		assertThatThrownBy(() -> suite.createTestSuite("new suite")).isInstanceOf(MissingPermissionException.class);
		verify(api, never()).createTestSuite(anyInt(), anyString(), anyString(), anyInt(), anyInt(), anyBoolean(), any(
				ActionOnDuplicate.class));
	}
	
	@Test
	void testCreateTestSuiteFailure() {
		TLTestSuite suite = initTestSuiteWithTestCases("basic", new TestLinkConfig(false, false, false, true, false));
		when(api.createTestSuite(eq(1), eq("new suite"), eq(""), eq(1), anyInt(), anyBoolean(), any(
				ActionOnDuplicate.class))).thenThrow(TestLinkAPIException.class);

		assertThatThrownBy(() -> suite.createTestSuite("new suite")).isInstanceOf(FailedCreationException.class);
	}
}
