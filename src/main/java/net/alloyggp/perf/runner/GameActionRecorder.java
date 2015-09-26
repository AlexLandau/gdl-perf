package net.alloyggp.perf.runner;

import java.util.List;

public interface GameActionRecorder {

    void writeRoles(List<String> roleNames);

    void recordTerminality(boolean isTerminal);

    void recordLegalMoves(List<String> legalMoveNames);

    void recordChosenJointMove(List<String> jointMoveNames);

    void recordGoalValues(List<Integer> goals);

}
