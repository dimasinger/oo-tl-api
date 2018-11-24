package de.blinklan.tools.ootl;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.eti.kinoshita.testlinkjavaapi.TestLinkAPI;
import de.blinklan.tools.ootl.util.TJAUtil;

@ExtendWith(MockitoExtension.class)
class TLTestProjectTest {

	@Mock
	TestLinkAPI api;
	
	@Test
	void testBasicSuiteNavigation() {
		when(api.getTestProjectByName(anyString())).thenReturn(TJAUtil.getTestProject(1, "project", ""));
		TJAUtil.mockSuites(api, 1, "basic");
		
		TestLink testlink = new TestLink(TestLinkConfig.NO_PERMISSIONS, api, "tester");
		TLTestProject project = testlink.getTestProject("project");
		
		List<TLTestSuite> suites = project.getFirstLevelTestSuites();
		assertThat(suites.size()).isEqualTo(2);
		
		TLTestSuite parent = project.getFirstLevelTestSuite("Parent");
		assertThat(parent.getName()).isEqualTo("Parent");
		assertThat(parent.getID()).isEqualTo(2);
		assertThat(parent.getParent()).isNull();
		
		TLTestSuite child = parent.getTestSuite("Child 2");
		assertThat(child.getName()).isEqualTo("Child 2");
		assertThat(child.getID()).isEqualTo(4);
		assertThat(child.getParent()).isSameAs(parent);
	}
	
	@Test
	void testSuitePathResolution() {
		when(api.getTestProjectByName(anyString())).thenReturn(TJAUtil.getTestProject(1, "project", ""));
		TJAUtil.mockSuites(api, 1, "deep");
		
		TestLink testlink = new TestLink(TestLinkConfig.NO_PERMISSIONS, api, "tester");
		TLTestProject project = testlink.getTestProject("project");
		
		TLTestSuite firstLevel = project.getTestSuiteByPath("A");
		assertThat(firstLevel.getName()).isEqualTo("A");
		assertThat(firstLevel.getID()).isEqualTo(1);
		assertThat(firstLevel.getParent()).isNull();
		
		TLTestSuite deep = project.getTestSuiteByPath("C/Child 1/Superchild/Test");
		assertThat(deep.getName()).isEqualTo("Test");
		assertThat(deep.getID()).isEqualTo(17);
		assertThat(deep.getParent().getName()).isEqualTo("Superchild");
	}

}
