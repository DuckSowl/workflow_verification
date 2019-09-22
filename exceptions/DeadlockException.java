package course.project.exceptions;

public class DeadlockException extends WorkflowVerificationException {
	public DeadlockException(String lable) {
		super(lable);
	}
}
