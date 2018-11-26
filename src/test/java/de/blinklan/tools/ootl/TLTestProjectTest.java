package de.blinklan.tools.ootl;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.eti.kinoshita.testlinkjavaapi.TestLinkAPI;
import de.blinklan.tools.ootl.util.EmptyOptionalError;
import de.blinklan.tools.ootl.util.TJAUtil;

@ExtendWith(MockitoExtension.class)
class TLTestProjectTest {

	@Mock
	TestLinkAPI api;
	
	@Test
	void testBasicSuiteNavigation() {
		TJAUtil.mockBasicProject(api);
		TJAUtil.mockSuites(api, 1, "basic");
		
		TestLink testlink = new TestLink(TestLinkConfig.NO_PERMISSIONS, api, "tester");
		TLTestProject project = testlink.getTestProject("project").orElseThrow(EmptyOptionalError::new);
		
		List<TLTestSuite> suites = project.getFirstLevelTestSuites();
		assertThat(suites.size()).isEqualTo(2);
		
		TLTestSuite parent = project.getFirstLevelTestSuite("Parent").orElseThrow(EmptyOptionalError::new);
		assertThat(parent.getName()).isEqualTo("Parent");
		assertThat(parent.getID()).isEqualTo(2);
		assertThat(parent.getParent()).isEmpty();
		
		TLTestSuite child = parent.getTestSuite("Child 2").orElseThrow(EmptyOptionalError::new);
		assertThat(child.getName()).isEqualTo("Child 2");
		assertThat(child.getID()).isEqualTo(4);
		assertThat(child.getParent().orElseThrow(EmptyOptionalError::new)).isSameAs(parent);
	}
	
	@Test
	void testSuitePathResolution() {
		TJAUtil.mockBasicProject(api);
		TJAUtil.mockSuites(api, 1, "deep");
		
		TestLink testlink = new TestLink(TestLinkConfig.NO_PERMISSIONS, api, "tester");
		TLTestProject project = testlink.getTestProject("project").get();
		
		TLTestSuite firstLevel = project.getTestSuiteByPath("A").orElseThrow(EmptyOptionalError::new);
		assertThat(firstLevel.getName()).isEqualTo("A");
		assertThat(firstLevel.getID()).isEqualTo(1);
		assertThat(firstLevel.getParent()).isEmpty();
		
		TLTestSuite deep = project.getTestSuiteByPath("C/Child 1/Superchild/Test").orElseThrow(EmptyOptionalError::new);
		assertThat(deep.getName()).isEqualTo("Test");
		assertThat(deep.getID()).isEqualTo(17);
		assertThat(deep.getParent().orElseThrow(EmptyOptionalError::new).getName()).isEqualTo("Superchild");
	}

	@Test
	void testSuiteCaching() {
		TJAUtil.mockBasicProject(api);
		TJAUtil.mockSuites(api, 1, "deep");
		
		TestLink testlink = new TestLink(TestLinkConfig.NO_PERMISSIONS, api, "tester");
		TLTestProject project = testlink.getTestProject("project").get();
		
		project.getTestSuiteByPath("A/Kid A");
		verify(api, times(1)).getFirstLevelTestSuitesForTestProject(anyInt());
		verify(api, times(1)).getTestSuitesForTestSuite(anyInt());
		
		project.getTestSuiteByPath("C/Child 1/Superchild/Interesting");
		verify(api, times(1)).getFirstLevelTestSuitesForTestProject(anyInt());
		verify(api, times(4)).getTestSuitesForTestSuite(anyInt());
		
		project.getTestSuiteByPath("C/Child 1/Superchild/Names/Very");
		verify(api, times(1)).getFirstLevelTestSuitesForTestProject(anyInt());
		verify(api, times(5)).getTestSuitesForTestSuite(anyInt());
		
		project.getTestSuiteByPath("C/Child 3");
		verify(api, times(1)).getFirstLevelTestSuitesForTestProject(anyInt());
		verify(api, times(5)).getTestSuitesForTestSuite(anyInt());
		
		project.getFirstLevelTestSuite("A").orElseThrow(EmptyOptionalError::new).getTestSuite("Kid C");
		verify(api, times(1)).getFirstLevelTestSuitesForTestProject(anyInt());
		verify(api, times(5)).getTestSuitesForTestSuite(anyInt());
	}
	
	@Test
	void testGetBuildBasic() {
		TJAUtil.mockBasicProject(api);
		TJAUtil.mockBasicBuild(api);
		
		TestLink testlink = new TestLink(TestLinkConfig.NO_PERMISSIONS, api, "tester");
		TLTestProject project = testlink.getTestProject("project").get();
		
		TLBuild build = project.getBuild("plan", "build").orElseThrow(EmptyOptionalError::new);
		assertThat(build.getPlanName()).isEqualTo("plan");
		assertThat(build.getBuildName()).isEqualTo("build");
	}
}
