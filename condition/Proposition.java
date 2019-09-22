package course.project.condition;

/**
 * Represent a proposition C_n with two main properties:
 * index = n,
 * and isTrue which represents negation of proposition.
 */
public class Proposition implements Comparable<Proposition> {
    /**
     * Is proposition not negated
     */
    boolean isTrue;

    /**
     * Index of the proposition
     */
    int index;

    public Proposition(int index, boolean isTrue) {
        this.index = Math.abs(index);
        this.isTrue = isTrue;
    }

    /**
     * Copy constructor
     */
    public Proposition(Proposition old) {
        this(old.index, old.isTrue);
    }

    /**
     * Comparing only by index, not using negation value.
     */
    public int compareTo(Proposition other) {
        return (index < other.index) ? -1 : (index == other.index ? 0 : 1);
    }

    public boolean equals(Object obj) {
        if (obj instanceof Proposition) {
            Proposition other = (Proposition) obj;
            return index == other.index && isTrue == other.isTrue;
        }
        return false;
    }

    /**
     * Returns not negated copy of this proposition.
     */
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