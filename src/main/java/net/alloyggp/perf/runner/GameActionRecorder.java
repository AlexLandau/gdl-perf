package net.alloyggp.perf.runner;

import java.util.List;

import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;

public interface GameActionRecorder {

    void writeRoles(List<Role> roles);

    void recordTerminality(boolean isTerminal);

    void recordLegalMoves(List<Move> legalMoves);

    void recordChosenJointMove(List<Move> jointMove);

    void recordGoalValues(List<Integer> goals);

}
