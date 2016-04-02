package net.alloyggp.perf.runner.runnable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.palamedes.gdl.core.model.GameFactory;
import org.eclipse.palamedes.gdl.core.model.IGame;
import org.eclipse.palamedes.gdl.core.model.IGameNode;
import org.eclipse.palamedes.gdl.core.model.IMove;
import org.ggp.base.util.game.Game;

public class PalamedesCoreRunnables {
    public static JavaSimulatorWrapper<IGame, IGameNode, Integer, IMove> getWrapper(final String factoryName) {
        return new JavaSimulatorWrapper<IGame, IGameNode, Integer, IMove>() {

            @Override
            public IGame createSimulator(String gameRules, Game game) {
                return GameFactory.getInstance().createGame(factoryName, gameRules);
            }

            @Override
            public IGameNode getInitialState(IGame game) {
                return game.getTree().getRootNode();
            }

            @Override
            public boolean isTerminal(IGame game, IGameNode state) throws Exception {
                return game.isTerminal(state);
            }

            @Override
            public IGameNode getRandomNextState(IGame game, IGameNode state) throws Exception {
                IMove[] moves = game.getRandomMove(state);
                return game.getNextNode(state, moves);
            }

            @Override
            public List<Integer> getGoals(IGame game, IGameNode state) throws Exception {
                return Arrays.stream(game.getGoalValues(state))
                             .boxed()
                             .collect(Collectors.toList());
            }

            @Override
            public List<Integer> getRoles(IGame game) {
                return IntStream.range(0, game.getRoleCount())
                                .boxed()
                                .collect(Collectors.toList());
            }

            @Override
            public List<IMove> getLegalMoves(IGame game, IGameNode state, Integer roleIndex) throws Exception {
                //Somewhat inefficient, but this isn't used in perf testing
                IMove[] legalMoves = game.getLegalMoves(state)[roleIndex];
                return Arrays.asList(legalMoves);
            }

            //Minor esoteric perf optimization: use just one empty array for this next bit
            private final IMove[] EMPTY_IMOVE_ARRAY = new IMove[0];
            @Override
            public IGameNode getNextState(IGame game, IGameNode curState, List<IMove> jointMove) throws Exception {
                return game.getNextNode(curState, jointMove.toArray(EMPTY_IMOVE_ARRAY));
            }

            @Override
            public String getMoveName(IGame game, IMove move) {
                return move.getMoveTerm().toString();
            }

            @Override
            public String getRoleName(IGame game, Integer roleIndex) {
                return game.getRoleNames()[roleIndex];
            }

        };
    }
}
