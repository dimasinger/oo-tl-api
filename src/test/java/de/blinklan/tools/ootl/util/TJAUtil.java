package de.blinklan.tools.ootl.util;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Scanner;

import br.eti.kinoshita.testlinkjavaapi.TestLinkAPI;
import br.eti.kinoshita.testlinkjavaapi.constants.ActionOnDuplicate;
import br.eti.kinoshita.testlinkjavaapi.model.Build;
import br.eti.kinoshita.testlinkjavaapi.model.TestPlan;
import br.eti.kinoshita.testlinkjavaapi.model.TestProject;
import br.eti.kinoshita.testlinkjavaapi.model.TestSuite;

/**
 * Utility class for initializing TestLink Java API Objects
 * 
 * @author dimasinger
 *
 */
public class TJAUtil {

	public static TestProject getTestProject(int id, String name) {
		String prefix = name.substring(0, Math.min(4, name.length())).toUpperCase();
		return new TestProject(id, name, prefix, "", true, true, true, true, true, true);
	}
	
	public static TestPlan getTestPlan(int id, String name, String projectName) {
		return new TestPlan(id, name, projectName, "", true, true);
	}
	
	public static Build getBuild(int id, int testPlanId, String name) {
		return new Build(id, testPlanId, name, "");
	}

	public static TestSuite getTestSuite(int id, int parentId, int projectId, String name) {
		return new TestSuite(id, projectId, name, "", parentId, -1, false, ActionOnDuplicate.BLOCK);
	}

	private static class TS {
		int id;
		String name;

		TS(int id, String name) {
			this.id = id;
			this.name = name;
		}
	}

	private static TS parseTestSuite(String line) {
		String[] s = line.split(":");
		return new TS(Integer.parseInt(s[0]), s[1]);
	}

	public static void mockBasicProject(TestLinkAPI api) {
		when(api.getTestProjectByName("project")).thenReturn(TJAUtil.getTestProject(1, "project"));
	}
	
	public static void mockBasicBuild(TestLinkAPI api) {
		when(api.getTestPlanByName("plan", "project")).thenReturn(getTestPlan(10, "plan", "project"));
		Build[] builds = {getBuild(100, 10, "build")};
		when(api.getBuildsForTestPlan(10)).thenReturn(builds);
	}

	public static void mockSuites(TestLinkAPI api, int projectID, String fileName) {
		List<TestSuite> suites = new ArrayList<>();
		try(InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("suites/" + fileName)) {
			Scanner scanner = new Scanner(is);
			int depth = 0;
			Deque<TS> stack = new ArrayDeque<>();
			TS last = null;
			while(scanner.hasNext()) {
				String s = scanner.nextLine();
				int l = s.length();
				s = s.replaceAll("^\t+", "");
				int tabs = l - s.length();
				if(tabs > depth) {
					stack.push(last);
					depth = tabs;
				}
				while(tabs < depth) {
					stack.pop();
					--depth;
				}
				last = parseTestSuite(s);
				TestSuite ts = getTestSuite(last.id, stack.isEmpty() ? -1 : stack.peek().id, projectID, last.name);
				suites.add(ts);
			}
			scanner.close();
		} catch(IOException e) {
			throw new AssertionError("Exception while loading test resource", e);
		}
		when(api.getFirstLevelTestSuitesForTestProject(projectID))
				.thenReturn(suites.stream().filter(ts -> ts.getParentId() == -1).toArray(TestSuite[]::new));
		when(api.getTestSuitesForTestSuite(anyInt())).thenAnswer(inv -> suites.stream()
				.filter(ts -> ts.getParentId().equals(inv.getArgument(0))).toArray(TestSuite[]::new));
	}
}
