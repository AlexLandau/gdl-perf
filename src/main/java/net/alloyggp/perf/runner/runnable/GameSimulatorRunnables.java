package net.alloyggp.perf.runner.runnable;

import java.util.List;
import java.util.Random;

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

    public static PerfTestRunnable getPerfTestRunnable(boolean useOpt) {
        return JavaPerfTestRunnable.create(getWrapper(useOpt));

//        return new PerfTestRunnable() {
//            @Override
//            public PerfTestReport runPerfTest(String gameRules, int secondsToRun) throws Exception {
//                GameSimulator simulator = new GameSimulator(false, useOpt);
//                simulator.ParseDescIntoTheory(gameRules);
//
//                ExpList roles = simulator.GetRoles();
//                Random rand = new Random();
//
//                long numStateChanges = 0;
//                long numRollouts = 0;
//                Stopwatch timer = new Stopwatch().start();
//                outer : while (true) {
//                    if (timer.elapsed(TimeUnit.SECONDS) >= secondsToRun) {
//                        break outer;
//                    }
//                    simulator.SimulateStart();
//                    while (!simulator.IsTerminal()) {
//                        if (timer.elapsed(TimeUnit.SECONDS) >= secondsToRun) {
//                            break outer;
//                        }
//
//                        ExpList moves = new ExpList();
//                        for (int r = 0; r < roles.size(); r++) {
//                            Expression role = roles.get(r);
//                            ExpList legalMoves = simulator.GetLegalMoves(role);
//                            int chosenIndex = rand.nextInt(legalMoves.size());
//                            Expression chosenMove = legalMoves.get(chosenIndex);
//                            moves.add(chosenMove);
//                        }
//                        simulator.SimulateStep(moves);
//
//                        numStateChanges++;
//                    }
//                    for (int r = 0; r < roles.size(); r++) {
//                        Expression role = roles.get(r);
//                        simulator.GetGoalValue(role);
//                    }
//
//                    numRollouts++;
//                }
//                long millisecondsTaken = timer.stop().elapsed(TimeUnit.MILLISECONDS);
//
//                return new PerfTestReport(millisecondsTaken, numStateChanges, numRollouts);
//            }
//        };
    }

    public static JavaSimulatorWrapper<GameSimulator, Void, Expression, Expression> getWrapper(boolean useOpt) {
        return new JavaSimulatorWrapper<GameSimulator, Void, Expression, Expression>() {
            private final Random rand = new Random();

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
                    int chosenIndex = rand.nextInt(legalMoves.size());
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

    public static CorrectnessTestRunnable getCorrectnessTestRunnable(boolean useOpt) {
        return JavaCorrectnessTestRunnable.create(getWrapper(useOpt));
//        return new CorrectnessTestRunnable() {
//            @Override
//            public void runCorrectnessTest(String gameRules,
//                    int stateChangesToRun, GameActionRecorder recorder) throws Exception {
//                GameSimulator simulator = new GameSimulator(false, useOpt);
//                simulator.ParseDescIntoTheory(gameRules);
//
//                runTest(simulator, stateChangesToRun, recorder);
//            }
//
//            private void runTest(GameSimulator simulator, int stateChangesToRun,
//                    GameActionRecorder recorder) throws Exception {
//                ExpList roles = simulator.GetRoles();
//                recorder.writeRoles(toRoles(roles));
//                int stateChangesSoFar = 0;
//                simulator.SimulateStart();
//                if (simulator.IsTerminal()) {
//                    recorder.recordTerminality(true);
//                    return; //otherwise stateChangesSoFar will never increase
//                }
//                while (true) {
//                    simulator.SimulateStart();
//                    while (!simulator.IsTerminal()) {
//                        recorder.recordTerminality(false);
//                        ExpList jointMove = new ExpList();
//                        for (int r = 0; r < roles.size(); r++) {
//                            ExpList legalMoves = simulator.GetLegalMoves(roles.get(r));
//                            List<Move> translatedMoves = translatePalamedesMoves(legalMoves);
//                            recorder.recordLegalMoves(translatedMoves);
//                            jointMove.add(pickOneAtRandom(legalMoves));
//                        }
//                        List<Move> translatedJointMove = translatePalamedesMoves(jointMove);
//                        recorder.recordChosenJointMove(translatedJointMove);
//                        simulator.SimulateStep(jointMove);
//                        stateChangesSoFar++;
//                    }
//                    recorder.recordTerminality(true);
//                    List<Integer> goalValues = Lists.newArrayList();
//                    for (int r = 0; r < roles.size(); r++) {
//                        Expression role = roles.get(r);
//                        goalValues.add(simulator.GetGoalValue(role));
//                    }
//                    recorder.recordGoalValues(goalValues);
//                    //Do we end here?
//                    if (stateChangesSoFar > stateChangesToRun) {
//                        return;
//                    }
//                }
//            }
//
//        };
    }

}
