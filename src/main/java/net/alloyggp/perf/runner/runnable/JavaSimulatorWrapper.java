package net.alloyggp.perf.runner.runnable;

import java.util.List;
import java.util.stream.Collectors;

import org.ggp.base.util.game.Game;

public interface JavaSimulatorWrapper<Engine, State, Role, Move> {
    Engine createSimulator(String gameRules, Game game);

    State getInitialState(Engine sm);

    boolean isTerminal(Engine sm, State state) throws Exception;

    State getRandomNextState(Engine sm, State state) throws Exception;

    List<Integer> getGoals(Engine sm, State state) throws Exception;

    List<Role> getRoles(Engine sm);

    List<Move> getLegalMoves(Engine sm, State state, Role role) throws Exception;

    State getNextState(Engine sm, State curState, List<Move> jointMove) throws Exception;

    default List<String> getMoveNames(Engine simulator, List<Move> legalMoves) {
        return legalMoves.stream()
                .map(move -> getMoveName(simulator, move))
                .collect(Collectors.toList());
    }

    String getMoveName(Engine simulator, Move move);

    default List<String> getRoleNames(Engine sm) {
        return getRoles(sm).stream()
                .map(role -> getRoleName(sm, role))
                .collect(Collectors.toList());
    }

    String getRoleName(Engine simulator, Role role);
}
