package course.project.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

//import org.junit.After;
//import org.junit.AfterClass;
//import org.junit.Before;
//import org.junit.BeforeClass;
import org.junit.Test;

import course.project.Conjunction;
import course.project.Proposition;

public class ConjunctionTest {

//	@BeforeClass
//	public static void setUpBeforeClass() throws Exception {
//	}
//
//	@AfterClass
//	public static void tearDownAfterClass() throws Exception {
//	}
//
//	@Before
//	public void setUp() throws Exception {
//	}
//
//	@After
//	public void tearDown() throws Exception {
//	}

	@Test
	public void testConjunction() {
		fail("Not yet implemented");
	}

	@Test
	public void testConjunctionProposition() {
		fail("Not yet implemented");
	}

	@Test
	public void testConjunctionConjunction() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetKeyFactor() {
		fail("Not yet implemented");
	}

	@Test
	public void testUpdateKeyFactor() {
		fail("Not yet implemented");
	}

	@Test
	public void testToString() {
		Conjunction c1 = new Conjunction(new Proposition(0, true));
		assertEquals("C0", c1.toString());
		
		Conjunction c2 = new Conjunction(new Proposition(1, false));
		assertEquals("-C1", c2.toString());
	}

}
