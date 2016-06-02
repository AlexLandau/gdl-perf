package net.alloyggp.perf.runner;

public class ErrorMessageException extends Exception {
    private final ErrorMessage errorMessage;

    public ErrorMessageException(ErrorMessage errorMessage) {
        this.errorMessage = errorMessage;
    }

    public ErrorMessage getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String getMessage() {
        return "ErrorMessageException: " + errorMessage.getError();
    }
}
