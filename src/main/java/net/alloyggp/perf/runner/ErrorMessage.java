package net.alloyggp.perf.runner;

public class ErrorMessage implements GameActionMessage {
    private final Throwable error;

    private ErrorMessage(Throwable error) {
        this.error = error;
    }

    public static GameActionMessage create(Throwable t) {
        return new ErrorMessage(t);
    }

    @Override
    public String toString() {
        return error.toString();
    }
}
