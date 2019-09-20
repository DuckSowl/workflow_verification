package course.project.tests;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.PriorityQueue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import course.project.Proposition;

public class PropositionTest {

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
	public void testHashCode() {
		Proposition p1 = new Proposition(1, true);
		Proposition p2 = new Proposition(1, true);
		HashSet<Proposition> set = new HashSet<Proposition>();
		set.add(p1);		
		set.add(p2);
		assertEquals(true, p1.equals(p2));
		assertEquals(true, p1.hashCode() == p2.hashCode());
		assertEquals(1, set.size());
	}
	
	@Test
	public void testSorting() {
		ArrayList<Proposition> propositions = new ArrayList<Proposition>();
		propositions.addAll(Arrays.asList(new Proposition(3, false), new Proposition(2, true),
				new Proposition(6, false), new Proposition(1, false), new Proposition(5, true)));
		
		Collections.sort(propositions);
		
		for (Proposition proposition : propositions) {
			System.out.println(proposition.toString());
		}
	}
	
	@Test
	public void testPQ() {
		PriorityQueue<Proposition> pQueue = new PriorityQueue<Proposition>();
		System.out.println("pq");
		pQueue.add(new Proposition(4, false));
		pQueue.add(new Proposition(8, false));
		pQueue.add(new Proposition(3, true));
		pQueue.add(new Proposition(1, false));
		pQueue.add(new Proposition(5, false));
		pQueue.add(new Proposition(2, true));
		pQueue.add(new Proposition(9, false));
		pQueue.add(new Proposition(0, true));

		while (!pQueue.isEmpty()) {
			System.out.println(pQueue.poll().toString());
		}
		System.out.println("pq");

	}

}
