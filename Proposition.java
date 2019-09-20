package course.project;

public class Proposition implements Comparable<Proposition> {
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
		return (index < other.index) ? -1 : (index == other.index ? 0 : 1);
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof Proposition) {
			Proposition other = (Proposition)obj;
			return index == other.index && isTrue == other.isTrue;
		}
		return false;
	}
	
	public Proposition trueCopy() {
		return new Proposition(index, true);
	}
	
	@Override
	public int hashCode() {
		return isTrue ? index : -index;
	}
	
	@Override
	public String toString() {
		return (isTrue ? "" : "-") + "C" + index;
	}
}