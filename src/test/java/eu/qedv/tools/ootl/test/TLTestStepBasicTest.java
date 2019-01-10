package eu.qedv.tools.ootl.test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import eu.qedv.tools.ootl.structure.TLTestStep;

class TLTestStepBasicTest {

	@Test
	void testMerge() {
		TLTestStep parent = new TLTestStep(null).setName("parent").setDuration(Duration.ofMillis(40));
		new TLTestStep(parent).setName("first").setDuration(Duration.ofMillis(4));
		new TLTestStep(parent).setName("twin").setDuration(Duration.ofMillis(13));
		new TLTestStep(parent).setName("twin").setDuration(Duration.ofMillis(18));
		new TLTestStep(parent).setName("last").setDuration(Duration.ofMillis(5));
		
		// before merge
		assertEquals(4, parent.getSteps().size());
		assertEquals("first", parent.getStep(0).getName());
		assertEquals("twin", parent.getStep(1).getName());
		assertEquals("twin", parent.getStep(2).getName());
		assertEquals("last", parent.getStep(3).getName());
		
		assertEquals(40, parent.getDuration().toMillis());
		assertEquals(13, parent.getStep(1).getDuration().toMillis());
		assertEquals(18, parent.getStep(2).getDuration().toMillis());
		
		assertEquals(1, parent.getStep(0).getCount());
		assertEquals(1, parent.getStep(1).getCount());
		assertEquals(1, parent.getStep(2).getCount());
		assertEquals(1, parent.getStep(3).getCount());
		
		parent.mergeSteps();
		
		// after merge
		assertEquals(3, parent.getSteps().size());
		assertEquals("first", parent.getStep(0).getName());
		assertEquals("twin", parent.getStep(1).getName());
		assertEquals("last", parent.getStep(2).getName());
		
		assertEquals(40, parent.getDuration().toMillis());
		assertEquals(31, parent.getStep(1).getDuration().toMillis());
		
		assertEquals(1, parent.getStep(0).getCount());
		assertEquals(2, parent.getStep(1).getCount());
		assertEquals(1, parent.getStep(2).getCount());
	}

}
