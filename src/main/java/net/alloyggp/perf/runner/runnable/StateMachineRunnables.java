package net.alloyggp.perf.runner.runnable;

import java.util.List;

import org.ggp.base.util.game.Game;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;

import net.alloyggp.perf.runner.ggpbase.StateMachineFactory;

/**
 * Deals with implementations of the GGP-Base StateMachine.
 */
public class StateMachineRunnables {
    public static PerfTestRunnable getPerfTestRunnable(
            StateMachineFactory smf) {
//        return new PerfTestRunnable() {
//            @Override
//            public PerfTestReport runPerfTest(String gameRules, int secondsToRun) throws Exception {
//                Game game = Game.createEphemeralGame(Game.preprocessRulesheet(gameRules));
//                StateMachine sm = smf.buildInitializedForGame(game);
//
//                long numStateChanges = 0;
//                long numRollouts = 0;
//                Stopwatch timer = new Stopwatch().start();
//                outer : while (true) {
//                    if (timer.elapsed(TimeUnit.SECONDS) >= secondsToRun) {
//                        break outer;
//                    }
//                    MachineState state = sm.getInitialState();
//                    while (!sm.isTerminal(state)) {
//                        if (timer.elapsed(TimeUnit.SECONDS) >= secondsToRun) {
//                            break outer;
//                        }
//                        state = sm.getRandomNextState(state);
//                        numStateChanges++;
//                    }
//                    sm.getGoals(state);
//                    numRollouts++;
//                }
//                long millisecondsTaken = timer.stop().elapsed(TimeUnit.MILLISECONDS);
//
//                return new PerfTestReport(millisecondsTaken, numStateChanges, numRollouts);
//            }
//        };
        return JavaPerfTestRunnable.create(getWrapper(smf));
    }

    public static JavaSimulatorWrapper<StateMachine, MachineState, Role, Move>
            getWrapper(final StateMachineFactory smf) {
        return new JavaSimulatorWrapper<StateMachine, MachineState, Role, Move>() {
            @Override
            public boolean isTerminal(StateMachine sm, MachineState state) {
                return sm.isTerminal(state);
            }

            @Override
            public List<Role> getRoles(StateMachine sm) {
                return sm.getRoles();
            }

            @Override
            public MachineState getRandomNextState(StateMachine sm, MachineState state) throws Exception {
                return sm.getRandomNextState(state);
            }

            @Override
            public MachineState getNextState(StateMachine sm, MachineState curState, List<Move> jointMove) throws Exception {
                return sm.getNextState(curState, jointMove);
            }

            @Override
            public List<Move> getLegalMoves(StateMachine sm, MachineState state, Role role) throws Exception {
                return sm.getLegalMoves(state, role);
            }

            @Override
            public MachineState getInitialState(StateMachine sm) {
                return sm.getInitialState();
            }

            @Override
            public List<Integer> getGoals(StateMachine sm, MachineState state) throws Exception {
                return sm.getGoals(state);
            }

            @Override
            public StateMachine createSimulator(String gameRules, Game game) {
                return smf.buildInitializedForGame(game);
            }

            @Override
            public String getMoveName(Move move) {
                return move.toString();
            }

            @Override
            public String getRoleName(Role role) {
                return role.toString();
            }
        };
    }

    public static CorrectnessTestRunnable getCorrectnessTestRunnable(
            StateMachineFactory smf) {
        return JavaCorrectnessTestRunnable.create(getWrapper(smf));
//        return (gameRules, stateChangesToRun, recorder) -> {
//            Game game = Game.createEphemeralGame(Game.preprocessRulesheet(gameRules));
//            StateMachine sm = smf.buildInitializedForGame(game);
//
//            List<Role> roles = sm.getRoles();
//            recorder.writeRoles(roles);
//            int stateChangesSoFar = 0;
//            MachineState initialState = sm.getInitialState();
//            if (sm.isTerminal(initialState)) {
//                recorder.recordTerminality(true);
//                return; //otherwise stateChangesSoFar will never increase
//            }
//            while (true) {
//                MachineState curState = initialState;
//                while (!sm.isTerminal(curState)) {
//                    recorder.recordTerminality(false);
//                    List<Move> jointMove = Lists.newArrayList();
//                    for (Role role : roles) {
//                        List<Move> legalMoves = sm.getLegalMoves(curState, role);
//                        recorder.recordLegalMoves(legalMoves);
//                        jointMove.add(pickOneAtRandom(legalMoves));
//                    }
//                    recorder.recordChosenJointMove(jointMove);
//                    curState = sm.getNextState(curState, jointMove);
//                    stateChangesSoFar++;
//                }
//                recorder.recordTerminality(true);
//                recorder.recordGoalValues(sm.getGoals(curState));
//                //Do we end here?
//                if (stateChangesSoFar > stateChangesToRun) {
//                    return;
//                }
//            }
//        };
    }

}
