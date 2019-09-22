package course.project.condition;

import java.util.LinkedList;

import com.google.common.collect.Lists;

/**
 * Conjunction of Propositions.
 */
public class Conjunction {
    LinkedList<Proposition> conjunction = new LinkedList<>();

    public Conjunction() {
    }

    public Conjunction(Proposition proposition) {
        conjunction.add(proposition);
    }

    /**
     * Copy Constructor.
     */
    public Conjunction(Conjunction old) {
        for (Proposition old_proposition : old.conjunction) {
            conjunction.add(new Proposition(old_proposition));
        }
    }

    /**
     * Proposition which was added last.
     */
    public Proposition getKeyFactor() {
        return conjunction.getLast();
    }

    /**
     * Adds new Proposition to conjunctions.
     */
    public void updateKeyFactor(Proposition prop) {
        conjunction.add(prop);
    }

    /**
     * Removes keyFactor if it present.
     */
    public void removeKeyFactor() {
        if (!conjunction.isEmpty()) {
            conjunction.removeLast();
        }
    }

    /**
     * Return true if there's no Propositions in conjunctions.
     */
    public boolean isTrue() {
        return conjunction.isEmpty();
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