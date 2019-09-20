package course.project;

import java.util.LinkedList;

import com.google.common.collect.Lists;

public class Conjunction {
	LinkedList<Proposition> conjunction = new LinkedList<>();
	
	public Conjunction() {
		
	}
	
	public Conjunction(Proposition proposition) {
		conjunction.add(proposition);
	}
	
	public Conjunction(Conjunction old) {
		for (Proposition old_proposition : old.conjunction) {
			conjunction.add(new Proposition(old_proposition));
		}
	}
	
	public Proposition getKeyFactor() {
		return conjunction.getLast();
	}
	
	public void removeKeyFactor() {
		if (!conjunction.isEmpty()) {
			conjunction.removeLast();
		} else {
			System.out.println("debug mistake removeKeyFactor");
		}
	}
	
	public boolean isEmpty() {
		return conjunction.isEmpty();
	}
	
	public void updateKeyFactor(Proposition prop) {
		conjunction.add(prop);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (conjunction.isEmpty()) {
			return "true";
		}
		
		for (Proposition proposition : Lists.reverse(conjunction)) {
			sb.append(proposition);
			sb.append("&");
		}
				
		sb.setLength(sb.length() - 1);
		return sb.toString();
	}
}