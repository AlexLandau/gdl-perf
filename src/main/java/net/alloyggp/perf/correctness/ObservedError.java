package net.alloyggp.perf.correctness;

import java.util.List;

import org.ggp.base.util.statemachine.Move;

import com.google.common.collect.ImmutableList;

import net.alloyggp.perf.Immutables;

public class ObservedError {
    private final String errorString;
    private final ImmutableList<ImmutableList<Move>> moveHistory;
    private final int numStateChangesBeforeFinding;

    private ObservedError(String errorString,
            ImmutableList<ImmutableList<Move>> moveHistory,
            int numStateChangesBeforeFinding) {
        this.errorString = errorString;
        this.moveHistory = moveHistory;
        this.numStateChangesBeforeFinding = numStateChangesBeforeFinding;
    }

    public static ObservedError create(String message, int numStateChangesBeforeFinding) {
        return new ObservedError(message, ImmutableList.of(), numStateChangesBeforeFinding);
    }

    public static ObservedError create(String string,
            Object referenceResult,
            Object testResult,
            int numStateChangesBeforeFinding) {
        return create(string, referenceResult, testResult,
                numStateChangesBeforeFinding, ImmutableList.of());
    }

    public static ObservedError create(String string,
            Object referenceResult,
            Object testResult,
            int numStateChangesBeforeFinding,
            List<List<Move>> moveHistory) {
        String errorString = string
                + "\nReference engine value: " + referenceResult.toString()
                + "\nTested engine value:    " + testResult.toString();

        return new ObservedError(errorString,
                moveHistory.stream()
                .map(ImmutableList::copyOf)
                .collect(Immutables.collectList()),
                numStateChangesBeforeFinding);
    }

    public int getNumStateChangesBeforeFinding() {
        return numStateChangesBeforeFinding;
    }

    @Override
    public String toString() {
        if (moveHistory.isEmpty()) {
            return errorString;
        } else {
            return errorString + "\nMove history: " + moveHistory;
        }
    }

}
