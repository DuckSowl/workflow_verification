package course.project.exceptions;

public class DeadlockException extends WorkflowVerificationException {
	public DeadlockException(String message) {
		super(message);
	}
}
