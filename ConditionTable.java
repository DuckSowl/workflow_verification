package course.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.google.common.collect.Lists;

public class ConditionTable {
	
	public LinkedHashMap<Proposition, Tuple<Proposition, Proposition>> table = new LinkedHashMap<>();
	public Proposition minProposition;
	
	public ConditionTable(Disjunction disjunction) {
		ArrayList<Conjunction> sortedConjunctions = new ArrayList<Conjunction>(disjunction.disjunction);
		Collections.sort(sortedConjunctions, (c1, c2)->c1.getKeyFactor().compareTo(c2.getKeyFactor()));
		Collections.reverse(sortedConjunctions);
				
		for (Conjunction conjunction : sortedConjunctions) {
			Proposition keyFactor = conjunction.getKeyFactor().trueCopy();
			for (Proposition proposition : Lists.reverse(conjunction.conjunction)) {
				Proposition propositionTrueCopy = proposition.trueCopy();
				table.putIfAbsent(propositionTrueCopy, new Tuple<Proposition, Proposition>());
				if (proposition.isTrue) {				
					table.get(propositionTrueCopy).first = keyFactor;
				} else {
					table.get(propositionTrueCopy).second = keyFactor;
				}
			}
		}
		
		Iterator<Proposition> it = table.keySet().iterator();
		while (it.hasNext()) { minProposition  = it.next(); }
		debugPrintTable();
	}
	
	public void debugPrintTable() {
		System.out.println("Cn\t(+)\t(-)");
		for (Entry<Proposition, Tuple<Proposition, Proposition>> entry : table.entrySet()) {
			System.out.println(entry.getKey().toString() + "\t" 
							 + (entry.getValue().first != null ? entry.getValue().first.toString() : "-0-") + "\t"
							 + (entry.getValue().second != null ? entry.getValue().second.toString() : "-0-"));
		}
		System.out.println();
	}
	
	public Proposition get(Proposition key, boolean positive) {
		return positive ? table.get(key).first : table.get(key).second;
	}
}
