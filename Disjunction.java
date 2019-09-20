package course.project;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;

public class Disjunction {
	LinkedHashSet<Conjunction> disjunction = new LinkedHashSet<>();
	ConditionTable table;
	
	Disjunction () {
		
	}
	
	public Disjunction(Proposition proposition) {
		disjunction.add(new Conjunction(proposition));
	}
	
	Disjunction (Disjunction old) {
		for (Conjunction old_conjunction : old.disjunction) {
			disjunction.add(new Conjunction(old_conjunction));
		}
	}
	
	private Disjunction(Disjunction first, Disjunction second) {		
		disjunction = new Disjunction(first).disjunction;
		disjunction.addAll(new Disjunction(second).disjunction);
		simplify();
	}
	
	public HashSet<Proposition> getKeyFactors() {
		HashSet<Proposition> keyFactors = new HashSet<Proposition>();
		for (Conjunction conjunction : disjunction) {
			keyFactors.add(conjunction.getKeyFactor());
		}
		return keyFactors;
	}
	
	public Disjunction Union(Disjunction other) {
		return new Disjunction(this, other);
	}
	
	public Disjunction Union(Proposition prop) {
		if (disjunction.isEmpty()) {
			return new Disjunction(prop);
		}
		Disjunction new_disjunction = new Disjunction(this);
		for (Conjunction conjunction: new_disjunction.disjunction) {
			conjunction.updateKeyFactor(prop);
		}
		
		return new_disjunction;
	}
	
	public boolean isDeadlock(Disjunction other) {
		return getKeyFactors() != other.getKeyFactors();
	}
	
	public boolean isSynchronised(Disjunction other) {
		if (disjunction.isEmpty() || other.disjunction.isEmpty()) {
			return false;
		}
		
		if (table == null) {
			table = new ConditionTable(this);
		}
		
		if (other.table == null) {
			other.table = new ConditionTable(other);
		}
		
		Proposition minProposition = table.minProposition;
		if (!minProposition.equals(other.table.minProposition)) {
			return false;
		}
		
		PriorityQueue<Proposition> uncheckedChoiseNodes = new PriorityQueue<Proposition>();
		uncheckedChoiseNodes.add(minProposition);
		
		while (!uncheckedChoiseNodes.isEmpty()) {
			Proposition cMin = uncheckedChoiseNodes.poll();
			if(!checkForSynchronisation(table, other.table, cMin, true, uncheckedChoiseNodes)) {
				return false;
			}
		}

		return true;
	}
	
	public void printTable() {
		if (table == null) {
			table = new ConditionTable(this);
		} else {
			table.debugPrintTable();
		}
	}
	
	
	
	private boolean checkForSynchronisation(ConditionTable t1, ConditionTable t2,
			Proposition cMin, boolean cond, PriorityQueue<Proposition> uncheckedChoiceNodes) {
		Proposition c1, c2;
		if ((c1 = t1.get(cMin, cond)) == null) {
			return checkForSynchronisationNegative(t1, t2, cMin, cond, uncheckedChoiceNodes);
		} else if(c1.equals(cMin)) {
			if (t2.get(cMin, cond) != null) {
				return false;
			}
			return checkForSynchronisationNegative(t1, t2, cMin, cond, uncheckedChoiceNodes);
		} else if (!(c1).equals(cMin)) {
			if ((c2 = t2.get(cMin, cond)) == null) {
				return checkForSynchronisationNegative(t1, t2, cMin, cond, uncheckedChoiceNodes);
			} else if (!c2.equals(cMin)) {
				if (!c1.equals(c2)) {
					return false;
				} else {
					uncheckedChoiceNodes.add(c1);
					return checkForSynchronisationNegative(t1, t2, cMin, cond, uncheckedChoiceNodes);
				}
			}
		}
		
		return true;	
	}
	
	private boolean checkForSynchronisationNegative(ConditionTable t1, ConditionTable t2,
			Proposition cMin, boolean cond, PriorityQueue<Proposition> uncheckedChoiceNodes) {
		if (cond) {
			return checkForSynchronisation(t1, t2, cMin, false, uncheckedChoiceNodes);
		} else {
			return true;
		}
	}
		
	private Set<Proposition> getDuplicateKeyFactors() {
		HashMap<Proposition, Integer> keyFactorsOcurences = new HashMap<>();
		for (Conjunction conjunction : disjunction) {
			Proposition keyFactor = new Proposition(conjunction.getKeyFactor().index, true);
			boolean isDuplicateKeyFactor = (keyFactorsOcurences.putIfAbsent(keyFactor, 1) != null);
			if (isDuplicateKeyFactor) {
				keyFactorsOcurences.replace(keyFactor, keyFactorsOcurences.get(keyFactor) + 1);
			} 
		}
		
		HashSet<Proposition> duplicateKeyFactors = new HashSet<>();
		for (Entry<Proposition, Integer> entry : keyFactorsOcurences.entrySet()) {
			if (entry.getValue() > 1) {
				duplicateKeyFactors.add(entry.getKey());
			}
		}
		
		return duplicateKeyFactors;
	}
	
	private void simplify() {
		HashMap<Proposition, HashSet<Conjunction>> duplicateKeyFactors = new HashMap<>();
		for (Proposition duplicateKeyFactor : getDuplicateKeyFactors()) {
			duplicateKeyFactors.put(duplicateKeyFactor, new HashSet<>());
		}
		
		for (Conjunction conjunction : disjunction) {
			Proposition keyFactor = new Proposition(conjunction.getKeyFactor().index, true);
			
			HashSet<Conjunction> keyFactorSConjunctions = duplicateKeyFactors.getOrDefault(keyFactor, null);
			if (keyFactorSConjunctions != null) {
				keyFactorSConjunctions.add(conjunction);
			}
		}
		
		for (HashSet<Conjunction> conjunctionsToSimplification : duplicateKeyFactors.values()) {
			Iterator<Conjunction> it = conjunctionsToSimplification.iterator();
			Conjunction conjunction = it.next();
			
			conjunction.removeKeyFactor();
			
			if (conjunction.isEmpty()) {
				disjunction.remove(conjunction);
			}
			
			while (it.hasNext()) {
				disjunction.remove(it.next());
			}
		}
	}
	
	public String toString() {
		if (disjunction.isEmpty()) {
			return "true";
		}
		
		StringBuilder sb = new StringBuilder();
		for (Conjunction conjunction : disjunction) {
			sb.append(conjunction);
			sb.append("V");
		}
		
		sb.setLength(sb.length() - 1);
		return sb.toString();
	}
}
