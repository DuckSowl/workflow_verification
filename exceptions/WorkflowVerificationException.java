package course.project.exceptions;

public abstract class WorkflowVerificationException extends WorkflowException {
    protected String lable;

    /**
     * Returns label of a problem Node as an argument
     */
    public String getLable() {
        return lable;
    }

    protected WorkflowVerificationException(String lable) {
        this.lable = lable;
    }
}
