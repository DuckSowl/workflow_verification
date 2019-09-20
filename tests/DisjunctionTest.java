package course.project.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import course.project.Disjunction;
import course.project.Proposition;

public class DisjunctionTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testToString() {
		Disjunction c0 = new Disjunction(new Proposition(0, true));
		assertEquals("C0", c0.toString());
		
		Disjunction c1 = new Disjunction(new Proposition(1, false));
		assertEquals("-C1", c1.toString());
		
		Disjunction c2 = c0.Union(c1);
		
		String c2ToString = c2.toString();
		assertTrue(c2ToString.equals("-C1VC0") || c2ToString.equals("C0V-C1"));
	}
	
	@Test
	public void testUnion() {
		Disjunction c0 = new Disjunction(new Proposition(0, true));
		Disjunction c1 = new Disjunction(new Proposition(0, false));
		
		Disjunction c2 = null;
		c2 = c0.Union(c1);
		
		assertEquals("true", c2.toString());	
	}
	
	@Test
	public void testSync() {
		Disjunction c1 = new Disjunction(new Proposition(1, true));
		Disjunction c2 = new Disjunction(new Proposition(1, false));
		c2 = c2.Union(new Proposition(2, false));
		
		c2.isSynchronised(c1);
		
		Disjunction c3 = c2.Union(c1);
		c3.printTable();
		
	}
	
	@Test
	public void testSync2() {
		Disjunction c1 = new Disjunction(new Proposition(1, true));
		Disjunction c2 = new Disjunction(new Proposition(1, false));
		c2 = c2.Union(new Proposition(2, false));
		
		assertTrue(c2.isSynchronised(c1));
		assertTrue(c1.isSynchronised(c2));
		
		Integer aInteger =null;
		System.out.println(aInteger.equals(0));

		
//		try {
//			Disjunction c3 = c2.Union(c1);
//			c3.printTable();
//		} catch (LackOfSynchronizationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
}
