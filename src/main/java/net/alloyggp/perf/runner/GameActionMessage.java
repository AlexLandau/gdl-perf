package net.alloyggp.perf.runner;

import java.util.Arrays;
import java.util.function.Function;

import net.alloyggp.perf.Immutables;

import com.google.common.collect.ImmutableList;

public interface GameActionMessage {

    /*
     * See StandardGameActionRecorder#toList
     *
     * TODO: Can this be made more private? Maybe make this an abstract class instead?
     */
    public static <T> ImmutableList<T> split(String line, Function<String, T> fn) {
        return Arrays.stream(line.split(","))
                .map(String::trim)
                .map(fn)
                .collect(Immutables.collectList());
    }

    default RolesMessage expectRolesMessage() {
        throw new IllegalStateException("Expected a RolesMessage, but was a " + getClass().getSimpleName());
    }

    default TerminalityMessage expectTerminalityMessage() {
        throw new IllegalStateException("Expected a TerminalityMessage, but was a " + getClass().getSimpleName());
    }

    default LegalMovesMessage expectLegalMovesMessage() {
        throw new IllegalStateException("Expected a LegalMovesMessage, but was a " + getClass().getSimpleName());
    }

    default ChosenMovesMessage expectChosenMovesMessage() {
        throw new IllegalStateException("Expected a ChosenMovesMessage, but was a " + getClass().getSimpleName());
    }

    default GoalsMessage expectGoalsMessage() {
        throw new IllegalStateException("Expected a GoalsMessage, but was a " + getClass().getSimpleName());
    }

    default boolean isEndOfMessages() {
        return false;
    }

    public static GameActionMessage endOfMessages() {
        return new GameActionMessage() {
            @Override
            public boolean isEndOfMessages() {
                return true;
            }
        };
    }

}
