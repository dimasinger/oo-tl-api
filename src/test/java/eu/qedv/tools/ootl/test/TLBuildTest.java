package eu.qedv.tools.ootl.test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import br.eti.kinoshita.testlinkjavaapi.TestLinkAPI;
import br.eti.kinoshita.testlinkjavaapi.constants.ExecutionStatus;
import br.eti.kinoshita.testlinkjavaapi.util.TestLinkAPIException;
import eu.qedv.tools.ootl.TLBuild;
import eu.qedv.tools.ootl.TLExecution;
import eu.qedv.tools.ootl.TLTestCase;
import eu.qedv.tools.ootl.TLTestProject;
import eu.qedv.tools.ootl.TLTestSuite;
import eu.qedv.tools.ootl.TestLink;
import eu.qedv.tools.ootl.TestLinkConfig;
import eu.qedv.tools.ootl.exception.TestLinkException;
import eu.qedv.tools.ootl.structure.ResultCode;
import eu.qedv.tools.ootl.test.util.EmptyOptionalError;
import eu.qedv.tools.ootl.test.util.TJAUtil;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TLBuildTest {

	@Mock
	TestLinkAPI api;

	@Test
	void testGetLastExecutionBasic() {
		TJAUtil.mockBasicProject(api);
		TJAUtil.mockBasicBuild(api);
		TJAUtil.mockSuites(api, 1, "single");
		TJAUtil.mockTestcases(api, 1, 1, "single");
		when(api.getLastExecutionResult(eq(10), eq(1), anyInt())).thenReturn(TJAUtil.getExecution(120, 10, 100, ExecutionStatus.PASSED));

		TestLink testlink = new TestLink(TestLinkConfig.NO_PERMISSIONS, api, "tester");
		TLTestProject project = testlink.getTestProject("project").orElseThrow(EmptyOptionalError::new);
		TLBuild build = project.getBuild("plan", "build").orElseThrow(EmptyOptionalError::new);
		TLTestSuite suite = project.getFirstLevelTestSuite("suite").orElseThrow(EmptyOptionalError::new);
		TLTestCase testcase =  suite.getTestCase("testcase").orElseThrow(EmptyOptionalError::new);
		
		TLExecution execution = build.getLastExecution(testcase).orElseThrow(EmptyOptionalError::new);
		assertThat(execution.getExecutionResult()).isEqualTo(ResultCode.SUCCESS);
		assertThat(execution.getTestcase()).isSameAs(testcase);
		assertThat(execution.getBuild()).isSameAs(build);
	}

	@Test
	void testGetLastExecutionFailure() {
		TJAUtil.mockBasicProject(api);
		TJAUtil.mockBasicBuild(api);
		TJAUtil.mockSuites(api, 1, "single");
		TJAUtil.mockTestcases(api, 1, 1, "single");
		when(api.getLastExecutionResult(eq(10), eq(1), anyInt())).thenThrow(TestLinkAPIException.class);

		TestLink testlink = new TestLink(TestLinkConfig.NO_PERMISSIONS, api, "tester");
		TLTestProject project = testlink.getTestProject("project").orElseThrow(EmptyOptionalError::new);
		TLBuild build = project.getBuild("plan", "build").orElseThrow(EmptyOptionalError::new);
		TLTestSuite suite = project.getFirstLevelTestSuite("suite").orElseThrow(EmptyOptionalError::new);
		TLTestCase testcase =  suite.getTestCase("testcase").orElseThrow(EmptyOptionalError::new);
		
		assertThatThrownBy(() -> build.getLastExecution(testcase)).isInstanceOf(TestLinkException.class);
	}
}
