package net.alloyggp.perf.runner;

import java.util.List;
import java.util.stream.Collectors;

import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;

import com.google.common.base.Joiner;

public class StandardGameActionRecorder implements GameActionRecorder {

    @Override
    public void writeRoles(List<Role> roles) {
        List<String> roleStrings = roles.stream()
                .map(Role::toString)
                .collect(Collectors.toList());
        System.out.println(GameActionFormat.ROLES_PREFIX + toList(roleStrings));
    }

    @Override
    public void recordTerminality(boolean isTerminal) {
        System.out.println(GameActionFormat.TERMINAL_PREFIX + isTerminal);
    }

    @Override
    public void recordLegalMoves(List<Move> legalMoves) {
        List<String> moveStrings = legalMoves.stream()
                .map(Move::toString)
                .collect(Collectors.toList());
        System.out.println(GameActionFormat.LEGAL_MOVES_PREFIX + toList(moveStrings));
    }

    @Override
    public void recordChosenJointMove(List<Move> jointMove) {
        List<String> moveStrings = jointMove.stream()
                .map(Move::toString)
                .collect(Collectors.toList());
        System.out.println(GameActionFormat.CHOSEN_MOVES_PREFIX + toList(moveStrings));
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
