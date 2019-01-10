package eu.qedv.tools.ootl.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.eti.kinoshita.testlinkjavaapi.TestLinkAPI;
import br.eti.kinoshita.testlinkjavaapi.util.TestLinkAPIException;
import eu.qedv.tools.ootl.TLTestProject;
import eu.qedv.tools.ootl.TestLink;
import eu.qedv.tools.ootl.TestLinkConfig;
import eu.qedv.tools.ootl.test.util.EmptyOptionalError;
import eu.qedv.tools.ootl.test.util.TJAUtil;

@ExtendWith(MockitoExtension.class)
class TestLinkTest {

	@Mock
	TestLinkAPI api;
	
	@Test
	void testGetTestProjectBasic() {
		TJAUtil.mockBasicProject(api);

		TestLink testlink = new TestLink(TestLinkConfig.NO_PERMISSIONS, api, "tester");
		TLTestProject project = testlink.getTestProject("project").orElseThrow(EmptyOptionalError::new);
		assertThat(project.getName()).isEqualTo("project");
		assertThat(project.getID()).isEqualTo(1);
	}

	@Test
	void testGetTestProjectCaching() {
		when(api.getTestProjectByName(anyString())).thenReturn(TJAUtil.getTestProject(1, "cached"))
				.thenReturn(TJAUtil.getTestProject(1, "project"));

		TestLink testlink = new TestLink(TestLinkConfig.NO_PERMISSIONS, api, "tester");
		TLTestProject cached1 = testlink.getTestProject("cached").orElseThrow(EmptyOptionalError::new);
		testlink.getTestProject("project");
		TLTestProject cached2 = testlink.getTestProject("cached").orElseThrow(EmptyOptionalError::new);

		assertThat(cached1).isSameAs(cached2);
		verify(api, times(2)).getTestProjectByName(anyString());
	}
	
	@Test
	void testGetNonExistantTestProject() {
		when(api.getTestProjectByName(anyString())).thenThrow(new TestLinkAPIException("No such test project"));
		
		TestLink testlink = new TestLink(TestLinkConfig.NO_PERMISSIONS, api, "tester");
		assertThat(testlink.getTestProject("no-exist").isPresent()).isFalse();
	}

}
