package net.alloyggp.perf.runner;

public class ErrorMessage implements GameActionMessage {
    private final String error;

    private ErrorMessage(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return error;
    }

    public String getError() {
        return error;
    }

    @Override
    public ChosenMovesMessage expectChosenMovesMessage() throws ErrorMessageException {
        throw new ErrorMessageException(this);
    }
    @Override
    public GoalsMessage expectGoalsMessage() throws ErrorMessageException {
        throw new ErrorMessageException(this);
    }
    @Override
    public LegalMovesMessage expectLegalMovesMessage() throws ErrorMessageException {
        throw new ErrorMessageException(this);
    }
    @Override
    public RolesMessage expectRolesMessage() throws ErrorMessageException {
        throw new ErrorMessageException(this);
    }
    @Override
    public TerminalityMessage expectTerminalityMessage() throws ErrorMessageException {
        throw new ErrorMessageException(this);
    }

    public static ErrorMessage parse(String line) {
        line = line.substring(GameActionFormat.ERROR_PREFIX.length());

        return new ErrorMessage(line);
    }
}
