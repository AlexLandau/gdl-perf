package net.alloyggp.perf.runner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Joiner;
import com.google.common.base.Throwables;

public class PrintStreamGameActionRecorder implements GameActionRecorder {
    private final PrintStream out;

    private PrintStreamGameActionRecorder(PrintStream out) {
        this.out = out;
    }

    public static GameActionRecorder createWritingToConsole() {
        return new PrintStreamGameActionRecorder(System.out);
    }

    public static GameActionRecorder createWritingToFile(File file) throws FileNotFoundException {
        return new PrintStreamGameActionRecorder(new PrintStream(file));
    }

    @Override
    public void writeRoles(List<String> roleNames) {
        out.println(GameActionFormat.ROLES_PREFIX + toList(roleNames));
    }

    @Override
    public void recordTerminality(boolean isTerminal) {
        out.println(GameActionFormat.TERMINAL_PREFIX + isTerminal);
    }

    @Override
    public void recordLegalMoves(List<String> legalMoveNames) {
        out.println(GameActionFormat.LEGAL_MOVES_PREFIX + toList(legalMoveNames));
    }

    @Override
    public void recordChosenJointMove(List<String> jointMoveNames) {
        out.println(GameActionFormat.CHOSEN_MOVES_PREFIX + toList(jointMoveNames));
    }

    @Override
    public void recordGoalValues(List<Integer> goals) {
        List<String> goalStrings = goals.stream()
                .map(i -> i.toString())
                .collect(Collectors.toList());
        out.println(GameActionFormat.GOALS_PREFIX + toList(goalStrings));
    }

    /*
     * See GameActionMessage#split
     */
    private String toList(List<String> strings) {
        return Joiner.on(",").join(strings);
    }

    @Override
    public void recordTestFinished() {
        out.println(GameActionFormat.TEST_FINISHED_PREFIX);
    }

    @Override
    public void recordError(Exception e) {
        out.println(GameActionFormat.ERROR_PREFIX + escapeStacktrace(e));
    }

    private String escapeStacktrace(Exception e) {
        return Throwables.getStackTraceAsString(e)
                .replace("\n", "<br/>")
                .replace("\r", "")
                .replace(";", ":");
    }

}
