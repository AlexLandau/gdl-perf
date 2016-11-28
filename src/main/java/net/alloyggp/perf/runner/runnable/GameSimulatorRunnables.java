package net.alloyggp.perf.runner.runnable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.ggp.base.util.game.Game;

import com.google.common.collect.Lists;

import cs227b.teamIago.gameProver.GameSimulator;
import cs227b.teamIago.resolver.ExpList;
import cs227b.teamIago.resolver.Expression;
import cs227b.teamIago.resolver.Predicate;

/**
 * Details with implementations of the Java "GameSimulator".
 *
 * This is used by the Palamedes BasicPlayer.
 */
public class GameSimulatorRunnables {
    public static JavaSimulatorWrapper<GameSimulator, Void, Expression, Expression> getWrapper(boolean useOpt) {
        return new JavaSimulatorWrapper<GameSimulator, Void, Expression, Expression>() {
            @Override
            public GameSimulator createSimulator(String gameRules, Game game) {
                GameSimulator simulator = new GameSimulator(false, useOpt);
                simulator.ParseDescIntoTheory(gameRules);
                return simulator;
            }

            @Override
            public Void getInitialState(GameSimulator simulator) {
                simulator.SimulateStart();
                return null;
            }

            @Override
            public boolean isTerminal(GameSimulator simulator, Void state) {
                return simulator.IsTerminal();
            }

            @Override
            public Void getRandomNextState(GameSimulator simulator, Void state) throws Exception {
                ExpList roles = simulator.GetRoles();
                ExpList moves = new ExpList();
                for (int r = 0; r < roles.size(); r++) {
                    Expression role = roles.get(r);
                    ExpList legalMoves = simulator.GetLegalMoves(role);
                    int chosenIndex = ThreadLocalRandom.current().nextInt(legalMoves.size());
                    Expression chosenMove = legalMoves.get(chosenIndex);
                    moves.add(chosenMove);
                }
                simulator.SimulateStep(moves);
                return null;
            }

            @Override
            public List<Integer> getGoals(GameSimulator simulator, Void state) throws Exception {
                ExpList roles = simulator.GetRoles();
                List<Integer> goals = Lists.newArrayListWithCapacity(roles.size());
                for (int r = 0; r < roles.size(); r++) {
                    Expression role = roles.get(r);
                    goals.add(simulator.GetGoalValue(role));
                }
                return goals;
            }

            @SuppressWarnings("unchecked")
            @Override
            public List<Expression> getRoles(GameSimulator simulator) {
                return simulator.GetRoles().toArrayList();
            }

            @SuppressWarnings("unchecked")
            @Override
            public List<Expression> getLegalMoves(GameSimulator simulator, Void curState, Expression role) throws Exception {
                return simulator.GetLegalMoves(role).toArrayList();
            }

            @Override
            public Void getNextState(GameSimulator simulator, Void curState, List<Expression> jointMove) throws Exception {
                simulator.SimulateStep(new ExpList(jointMove));
                return null;
            }

            @Override
            public String getMoveName(GameSimulator simulator, Expression move) {
                return ((Predicate) move).getOperands().get(1).toString();
            }

            @Override
            public String getRoleName(GameSimulator simulator, Expression role) {
                return role.toString();
            }

        };
    }
}
