package net.alloyggp.perf.runner;

public class TerminalityMessage implements GameActionMessage {
    private final boolean isTerminal;

    private TerminalityMessage(boolean isTerminal) {
        this.isTerminal = isTerminal;
    }

    @Override
    public TerminalityMessage expectTerminalityMessage() {
        return this;
    }

    public boolean isTerminal() {
        return isTerminal;
    }

    public static GameActionMessage parse(String line) {
        line = line.substring(GameActionFormat.TERMINAL_PREFIX.length());

        boolean isTerminal = Boolean.parseBoolean(line);
        return new TerminalityMessage(isTerminal);
    }
}
