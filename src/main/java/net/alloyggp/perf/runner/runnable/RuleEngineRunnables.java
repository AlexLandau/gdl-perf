package net.alloyggp.perf.runner.runnable;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.ggp.base.util.game.Game;
import org.ggp.base.util.ruleengine.RuleEngine;
import org.ggp.base.util.ruleengine.RuleEngineFactory;
import org.ggp.base.util.ruleengine.RuleEngineState;

public class RuleEngineRunnables {
    public static PerfTestRunnable getPerfTestRunnable(
            RuleEngineFactory<? extends RuleEngine<?,?>> ref) {
        return JavaPerfTestRunnable.create(getWrapper(ref));
    }

    public static <M,S extends RuleEngineState<M,S>> JavaSimulatorWrapper<RuleEngine<M,S>, S, Integer, M>
            getWrapper(final RuleEngineFactory<? extends RuleEngine<M,S>> ref) {
        return new JavaSimulatorWrapper<RuleEngine<M,S>, S, Integer, M>() {
            @Override
            public boolean isTerminal(RuleEngine<M,S> sm, S state) {
                return sm.isTerminal(state);
            }

            @Override
            public List<Integer> getRoles(RuleEngine<M,S> sm) {
                return IntStream.range(0, sm.getNumRoles())
                                .boxed()
                                .collect(Collectors.toList());
            }

            @Override
            public S getRandomNextState(RuleEngine<M,S> sm, S state) throws Exception {
                return sm.getRandomNextState(state);
            }

            @Override
            public S getNextState(RuleEngine<M,S> sm, S curState, List<M> jointMove) throws Exception {
                return sm.getNextState(curState, jointMove);
            }

            @Override
            public List<M> getLegalMoves(RuleEngine<M,S> sm, S state, Integer roleIndex) throws Exception {
                return sm.getLegalMoves(state, roleIndex);
            }

            @Override
            public S getInitialState(RuleEngine<M,S> sm) {
                return sm.getInitialState();
            }

            @Override
            public List<Integer> getGoals(RuleEngine<M,S> sm, S state) throws Exception {
                return sm.getGoals(state).asList();
            }

            @Override
            public RuleEngine<M,S> createSimulator(String gameRules, Game game) {
                return ref.buildEngineForRules(game.getRules());
            }

            @Override
            public String getMoveName(RuleEngine<M,S> sm, M move) {
                return sm.getTranslator().getGdlMove(move).toString();
            }

            @Override
            public String getRoleName(RuleEngine<M,S> sm, Integer roleIndex) {
                return sm.getRoles().get(roleIndex).toString();
            }
        };
    }

    public static CorrectnessTestRunnable getCorrectnessTestRunnable(
            RuleEngineFactory<? extends RuleEngine<?,?>> ref) {
        return JavaCorrectnessTestRunnable.create(getWrapper(ref));
    }
}
