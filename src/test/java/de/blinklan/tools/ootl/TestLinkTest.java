package de.blinklan.tools.ootl;

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
import de.blinklan.tools.ootl.util.TJAUtil;

@ExtendWith(MockitoExtension.class)
class TestLinkTest {

	@Mock
	TestLinkAPI api;
	
	@Test
	void testGetTestProjectBasic() {
		when(api.getTestProjectByName(anyString())).thenReturn(TJAUtil.getTestProject(1, "project", ""));

		TestLink testlink = new TestLink(TestLinkConfig.NO_PERMISSIONS, api, "tester");
		TLTestProject project = testlink.getTestProject("project");
		assertThat(project.getName()).isEqualTo("project");
		assertThat(project.getID()).isEqualTo(1);
	}

	@Test
	void testGetTestProjectCaching() {
		when(api.getTestProjectByName(anyString())).thenReturn(TJAUtil.getTestProject(1, "cached", ""))
				.thenReturn(TJAUtil.getTestProject(1, "project", ""));

		TestLink testlink = new TestLink(TestLinkConfig.NO_PERMISSIONS, api, "tester");
		TLTestProject cached1 = testlink.getTestProject("cached");
		testlink.getTestProject("project");
		TLTestProject cached2 = testlink.getTestProject("cached");

		assertThat(cached1).isSameAs(cached2);
		verify(api, times(2)).getTestProjectByName(anyString());
	}

}