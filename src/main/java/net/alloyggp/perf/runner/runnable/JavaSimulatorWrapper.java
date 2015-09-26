package net.alloyggp.perf.runner.runnable;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.ggp.base.util.game.Game;

public interface JavaSimulatorWrapper<Simulator, State, Role, Move> {
    public static class RandomSource {
        public static final Random RANDOM = new Random();
    }

    Simulator createSimulator(String gameRules, Game game);

    State getInitialState(Simulator sm);

    boolean isTerminal(Simulator sm, State state);

    State getRandomNextState(Simulator sm, State state) throws Exception;

    List<Integer> getGoals(Simulator sm, State state) throws Exception;

    List<Role> getRoles(Simulator sm);

    List<Move> getLegalMoves(Simulator sm, State state, Role role) throws Exception;

    State getNextState(Simulator sm, State curState, List<Move> jointMove) throws Exception;

    default List<String> getMoveNames(Simulator sm, List<Move> legalMoves) {
        return legalMoves.stream()
                .map(move -> getMoveName(sm, move))
                .collect(Collectors.toList());
    }

    String getMoveName(Simulator sm, Move move);

    default List<String> getRoleNames(Simulator sm) {
        return getRoles(sm).stream()
                .map(role -> getRoleName(sm, role))
                .collect(Collectors.toList());
    }

    String getRoleName(Simulator sm, Role role);
}
