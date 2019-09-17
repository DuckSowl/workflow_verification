package course.project;

import java.util.HashSet;
import java.util.LinkedList;


class Disjunction {
	HashSet<Conjunction> disjunction;
	
	Disjunction () {
		
	}
	
	Disjunction (Disjunction old) {
		for (Conjunction old_conjunction : old.disjunction) {
			disjunction.add(new Conjunction(old_conjunction));
		}
	}
	
	private Disjunction(Disjunction first, Disjunction second) {		
		disjunction = new Disjunction(first).disjunction;
		disjunction.addAll(new Disjunction(second).disjunction);
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
	
	public boolean isDeadlock(Disjunction other) {
		return getKeyFactors() != other.getKeyFactors();
	}
}

class Conjunction {
	LinkedList<Proposition> conjunction;
	
	public Conjunction() {
		
	}
	
	public Conjunction(Conjunction old) {
		for (Proposition old_proposition : conjunction) {
			conjunction.add(new Proposition(old_proposition));
		}
	}
	
	public Proposition getKeyFactor() {
		return conjunction.getLast();
	}
}

class Proposition implements Comparable<Proposition> {
	boolean isTrue;
	int index;
	
	public Proposition (int index, boolean isTrue) {
		this.index = Math.abs(index);
		this.isTrue = isTrue;
	}
	
	public Proposition (Proposition old) {
		this(old.index, old.isTrue);
	}

	public int compareTo(Proposition other) {
		return (index == other.index) && (isTrue == other.isTrue) ? 0 : -1;
	}
	
	public int hashCode() {
		return isTrue ? index : -index;
	}
}

