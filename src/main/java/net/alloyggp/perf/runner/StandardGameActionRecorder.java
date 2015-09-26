package net.alloyggp.perf.runner;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Joiner;

public class StandardGameActionRecorder implements GameActionRecorder {

    @Override
    public void writeRoles(List<String> roleNames) {
        System.out.println(GameActionFormat.ROLES_PREFIX + toList(roleNames));
    }

    @Override
    public void recordTerminality(boolean isTerminal) {
        System.out.println(GameActionFormat.TERMINAL_PREFIX + isTerminal);
    }

    @Override
    public void recordLegalMoves(List<String> legalMoveNames) {
        System.out.println(GameActionFormat.LEGAL_MOVES_PREFIX + toList(legalMoveNames));
    }

    @Override
    public void recordChosenJointMove(List<String> jointMoveNames) {
        System.out.println(GameActionFormat.CHOSEN_MOVES_PREFIX + toList(jointMoveNames));
    }

    @Override
    public void recordGoalValues(List<Integer> goals) {
        List<String> goalStrings = goals.stream()
                .map(i -> i.toString())
                .collect(Collectors.toList());
        System.out.println(GameActionFormat.GOALS_PREFIX + toList(goalStrings));
    }

    /*
     * See GameActionMessage#split
     */
    private String toList(List<String> strings) {
        return Joiner.on(",").join(strings);
    }

}
