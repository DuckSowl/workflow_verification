package course.project.condition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;

import com.google.common.collect.Lists;

import course.project.Tuple;

/**
 * Disjunction of Conjunctions.
 */
public class Condition {

    /**
     * Set of Conjunctions.
     */
    private LinkedHashSet<Conjunction> disjunction = new LinkedHashSet<>();
    private ConditionTable table;

    public Condition() {
    }

    public Condition(Proposition proposition) {
        disjunction.add(new Conjunction(proposition));
    }

    /**
     * Copy Constructor.
     */
    public Condition(Condition old) {
        for (Conjunction old_conjunction : old.disjunction) {
            disjunction.add(new Conjunction(old_conjunction));
        }
    }

    /**
     * Union Constructor.
     */
    private Condition(Condition first, Condition second) {
        disjunction = new Condition(first).disjunction;
        disjunction.addAll(new Condition(second).disjunction);
        simplify();
    }

    /**
     * Returns a set of key factors aka last added propositions.
     */
    public HashSet<Proposition> getKeyFactors() {
        HashSet<Proposition> keyFactors = new HashSet<Proposition>();
        for (Conjunction conjunction : disjunction) {
            keyFactors.add(conjunction.getKeyFactor());
        }
        return keyFactors;
    }

    /**
     * Returns new simplified Condition where 'this' united with 'other'.
     */
    public Condition Union(Condition other) {
        return new Condition(this, other);
    }

    /**
     * Returns new simplified Condition where every Conjunction intersected with 'prop'.
     */
    public Condition Union(Proposition prop) {
        if (disjunction.isEmpty()) {
            return new Condition(prop);
        }
        Condition new_disjunction = new Condition(this);
        for (Conjunction conjunction : new_disjunction.disjunction) {
            conjunction.updateKeyFactor(prop);
        }

        return new_disjunction;
    }

    /**
     * Returns true if union of two conditions is not true.
     */
    public boolean isDeadlock(Condition other) {
        return getKeyFactors() != other.getKeyFactors();
    }

    /**
     * Returns true if intersection of two conditions is not false.
     */
    public boolean isSynchronised(Condition other) {
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
            if (!checkForSynchronisation(table, other.table, cMin, true, uncheckedChoiseNodes)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns false if in current step of verification
     * (with current proposition 'cMin') has synchronization issue.
     */
    private boolean checkForSynchronisation(ConditionTable t1, ConditionTable t2,
                                            Proposition cMin, boolean cond, PriorityQueue<Proposition> uncheckedChoiceNodes) {
        Proposition c1, c2;
        if ((c1 = t1.get(cMin, cond)) == null) {
            return checkForSynchronisationNegative(t1, t2, cMin, cond, uncheckedChoiceNodes);
        } else if (c1.equals(cMin)) {
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

    /**
     * Calls checkForSynchronisation with false
     * condition or leaves true if it was already called with this cMin.
     */
    private boolean checkForSynchronisationNegative(ConditionTable t1, ConditionTable t2,
                                                    Proposition cMin, boolean cond, PriorityQueue<Proposition> uncheckedChoiceNodes) {
        if (cond) {
            return checkForSynchronisation(t1, t2, cMin, false, uncheckedChoiceNodes);
        } else {
            return true;
        }
    }

    /**
     * Gets a set of Propositions which has duplicate key factors ignoring negation.
     * In this set all factors are positive.
     */
    private Set<Proposition> getDuplicateKeyFactors() {
        HashMap<Proposition, Integer> keyFactorsOcurences = new HashMap<>();
        for (Conjunction conjunction : disjunction) {
            Proposition keyFactor = conjunction.getKeyFactor().trueCopy();
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

    /**
     * Simplifies condition while it is possible
     * by removing conjunctions with key factors with same index.
     */
    private void simplify() {
        HashMap<Proposition, HashSet<Conjunction>> duplicateKeyFactors = new HashMap<>();
        Set<Proposition> duplicateKeyFactorsSet = getDuplicateKeyFactors();

        while (!duplicateKeyFactorsSet.isEmpty()) {
            for (Proposition duplicateKeyFactor : duplicateKeyFactorsSet) {
                duplicateKeyFactors.put(duplicateKeyFactor, new HashSet<>());
            }

            // Connecting each factor with set of Conjunctions with this key factor.
            for (Conjunction conjunction : disjunction) {
                Proposition keyFactor = conjunction.getKeyFactor().trueCopy();

                // Checking for conjunctions with no key factors.
                HashSet<Conjunction> keyFactorsConjunctions = duplicateKeyFactors.getOrDefault(keyFactor, null);
                if (keyFactorsConjunctions != null) {
                    keyFactorsConjunctions.add(conjunction);
                }
            }

            // Removing conjunctions.
            for (HashSet<Conjunction> conjunctionsToSimplification : duplicateKeyFactors.values()) {
                Iterator<Conjunction> it = conjunctionsToSimplification.iterator();
                Conjunction conjunction = it.next();

                conjunction.removeKeyFactor();

                if (conjunction.isTrue()) {
                    disjunction.remove(conjunction);
                }

                while (it.hasNext()) {
                    disjunction.remove(it.next());
                }
            }

            duplicateKeyFactorsSet = getDuplicateKeyFactors();
        }
    }

    private class ConditionTable {

        /**
         * Table for algorithm with value Tuple<Positive Proposition, Negative Proposition>
         */
        private LinkedHashMap<Proposition, Tuple<Proposition, Proposition>> table = new LinkedHashMap<>();

        /**
         * Proposition in table with min index.
         */
        private Proposition minProposition;

        /**
         * Computes table representing 'condition'
         */
        private ConditionTable(Condition condition) {
            ArrayList<Conjunction> sortedConjunctions = new ArrayList<Conjunction>(condition.disjunction);
            Collections.sort(sortedConjunctions, (c1, c2) -> c1.getKeyFactor().compareTo(c2.getKeyFactor()));
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
            while (it.hasNext()) {
                minProposition = it.next();
            }
        }

        /**
         * Gets proposition from table:
         * from column with proposition with index=key and
         * column 'positive' or 'negative'
         */
        Proposition get(Proposition key, boolean positive) {
            return positive ? table.get(key).first : table.get(key).second;
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
