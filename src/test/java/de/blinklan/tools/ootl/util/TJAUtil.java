package de.blinklan.tools.ootl.util;

import br.eti.kinoshita.testlinkjavaapi.model.TestProject;

/**
 * Utility class for initializing TestLink Java API Objects
 * 
 * @author dimasinger
 *
 */
public class TJAUtil {

	public static TestProject getTestProject(int id, String name, String notes) {
		String prefix = name.substring(0, Math.min(4, name.length())).toUpperCase();
		return new TestProject(id, name, prefix, notes, true, true, true, true, true, true);
	}
}
