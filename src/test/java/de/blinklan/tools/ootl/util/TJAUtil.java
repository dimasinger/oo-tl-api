package de.blinklan.tools.ootl.util;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Scanner;

import br.eti.kinoshita.testlinkjavaapi.TestLinkAPI;
import br.eti.kinoshita.testlinkjavaapi.constants.ActionOnDuplicate;
import br.eti.kinoshita.testlinkjavaapi.constants.ExecutionStatus;
import br.eti.kinoshita.testlinkjavaapi.constants.ExecutionType;
import br.eti.kinoshita.testlinkjavaapi.constants.TestCaseDetails;
import br.eti.kinoshita.testlinkjavaapi.constants.TestCaseStatus;
import br.eti.kinoshita.testlinkjavaapi.constants.TestImportance;
import br.eti.kinoshita.testlinkjavaapi.model.Build;
import br.eti.kinoshita.testlinkjavaapi.model.TestCase;
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

	private static String getPrefix(String projectName) {
		return projectName.substring(0, Math.min(4, projectName.length())).toUpperCase();
	}

	public static TestProject getTestProject(int id, String name) {
		return new TestProject(id, name, getPrefix(name), "", true, true, true, true, true, true);
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

	public static TestCase getTestCase(int id, int suiteId, int projectId, String name) {
		return new TestCase(id, name, suiteId, projectId, "author", "", null, "", TestCaseStatus.FINAL,
				TestImportance.MEDIUM, ExecutionType.AUTOMATED, 0, 0, id,
				getPrefix("project") + "-" + Integer.toString(id), false, ActionOnDuplicate.BLOCK, 1, 1, suiteId,
				Collections.emptyList(), ExecutionStatus.NOT_RUN, null, 0);
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
		Build[] builds = { getBuild(100, 10, "build") };
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
		when(api.getFirstLevelTestSuitesForTestProject(anyInt()))
				.thenReturn(suites.stream().filter(ts -> ts.getParentId() == -1).toArray(TestSuite[]::new));
		when(api.getTestSuitesForTestSuite(anyInt())).thenAnswer(inv -> suites.stream()
				.filter(ts -> ts.getParentId().equals(inv.getArgument(0))).toArray(TestSuite[]::new));
	}

	public static void mockTestcases(TestLinkAPI api, int projectID, int suiteID, String fileName) {
		List<TestCase> testcases = new ArrayList<>();
		try(InputStream is = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("testcases/" + fileName)) {
			Scanner scanner = new Scanner(is);
			while(scanner.hasNext()) {
				String[] s = scanner.nextLine().split(":", 2);
				testcases.add(getTestCase(projectID, suiteID, Integer.parseInt(s[0]), s[1]));
			}
			scanner.close();
		} catch(IOException e) {
			throw new AssertionError("Exception while loading test resource", e);
		}
		when(api.getTestCasesForTestSuite(anyInt(), anyBoolean(), eq(TestCaseDetails.FULL)))
				.thenReturn(testcases.stream().toArray(TestCase[]::new));
	}
}
