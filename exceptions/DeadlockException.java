package course.project.exceptions;

public class DeadlockException extends LackOfSynchronizationException {
	public DeadlockException(String message) {
		super(message);
	}
}
