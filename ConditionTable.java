package course.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.google.common.collect.Lists;

public class ConditionTable {
	
	public LinkedHashMap<Proposition, Tuple<Proposition>> table = new LinkedHashMap<>();
	public Proposition minProposition;
	
	public ConditionTable(Disjunction disjunction) {
		ArrayList<Conjunction> sortedConjunctions = new ArrayList<Conjunction>(disjunction.disjunction);
		Collections.sort(sortedConjunctions, (c1, c2)->c1.getKeyFactor().compareTo(c2.getKeyFactor()));
		Collections.reverse(sortedConjunctions);
				
		for (Conjunction conjunction : sortedConjunctions) {
			Proposition keyFactor = conjunction.getKeyFactor().trueCopy();
			for (Proposition proposition : Lists.reverse(conjunction.conjunction)) {
				Proposition propositionTrueCopy = proposition.trueCopy();
				table.putIfAbsent(propositionTrueCopy, new Tuple<Proposition>());
				if (proposition.isTrue) {				
					table.get(propositionTrueCopy).positive = keyFactor;
				} else {
					table.get(propositionTrueCopy).negative = keyFactor;
				}
			}
		}
		
		Iterator<Proposition> it = table.keySet().iterator();
		while (it.hasNext()) { minProposition  = it.next(); }
		debugPrintTable();
	}
	
	public void debugPrintTable() {
		System.out.println("Cn\t(+)\t(-)");
		for (Entry<Proposition, Tuple<Proposition>> entry : table.entrySet()) {
			System.out.println(entry.getKey().toString() + "\t" 
							 + (entry.getValue().positive != null ? entry.getValue().positive.toString() : "-0-") + "\t"
							 + (entry.getValue().negative != null ? entry.getValue().negative.toString() : "-0-"));
		}
		System.out.println();
	}
	
	public Proposition get(Proposition key, boolean positive) {
		return positive ? table.get(key).positive : table.get(key).negative;
	}
	
	class Tuple<T> {
		public T positive;
		public T negative;
		
		public Tuple() {
			positive = null;
			negative = null;
		}
	}
}
