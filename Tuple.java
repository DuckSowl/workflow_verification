package course.project;

class Tuple<F, S> {
	public F first;
	public S second;
	
	public Tuple() { }
	
	public Tuple(F first, S second) {
		this.first = first;
		this.second = second;
	}
}