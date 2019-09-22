package course.project;

/**
 * Mutable tuple realization
 */
public class Tuple<F, S> {
    public F first;
    public S second;

    public Tuple() {
    }

    public Tuple(F first, S second) {
        this.first = first;
        this.second = second;
    }
}