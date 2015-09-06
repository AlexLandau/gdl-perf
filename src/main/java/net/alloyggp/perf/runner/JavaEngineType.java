package net.alloyggp.perf.runner;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import net.alloyggp.perf.EngineType;
import net.alloyggp.perf.ObservedError;
import net.alloyggp.perf.runner.ggpbase.ProverStateMachineFactory;
import net.alloyggp.perf.runner.ggpbase.StateMachineFactory;

import org.ggp.base.util.concurrency.ConcurrencyUtils;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public enum JavaEngineType {
    PROVER(getStateMachinePerfTestRunnable(ProverStateMachineFactory.create()),
            getStateMachineCorrectnessTestRunnable(ProverStateMachineFactory.create())),
            ;
    private final PerfTestRunnable perfRunnable;
    private final CorrectnessTestRunnable correctnessRunnable;

    private JavaEngineType(PerfTestRunnable perfRunnable,
            CorrectnessTestRunnable correctnessRunnable) {
        this.perfRunnable = perfRunnable;
        this.correctnessRunnable = correctnessRunnable;
    }

    public String getCurrentVersion() {
        //TODO: Come up with a non-hacky way to do this!
        return EngineType.valueOf(name()).getCurrentVersion();
    }

    public PerfTestReport runPerfTest(String gameRules, int secondsToRun) throws Exception {
        return perfRunnable.runPerfTest(gameRules, secondsToRun);
    }

    public void runCorrectnessTest(String gameRules, int stateChangesToRun, GameActionRecorder recorder) throws Exception {
        correctnessRunnable.runCorrectnessTest(gameRules, stateChangesToRun, recorder);
    }

    public static interface PerfTestRunnable {
        PerfTestReport runPerfTest(String gameRules, int secondsToRun) throws Exception;
    }

    public static interface CorrectnessTestRunnable {
        void runCorrectnessTest(String gameRules, int stateChangesToRun, GameActionRecorder recorder) throws Exception;
    }

    private static PerfTestRunnable getStateMachinePerfTestRunnable(
            StateMachineFactory smf) {
        return new PerfTestRunnable() {
            @Override
            public PerfTestReport runPerfTest(String gameRules, int secondsToRun) throws Exception {
                Game game = Game.createEphemeralGame(Game.preprocessRulesheet(gameRules));
                StateMachine sm = smf.buildInitializedForGame(game);

                long numStateChanges = 0;
                long numRollouts = 0;
                Stopwatch timer = new Stopwatch().start();
                outer : while (true) {
                    if (timer.elapsed(TimeUnit.SECONDS) >= secondsToRun) {
                        break outer;
                    }
                    MachineState state = sm.getInitialState();
                    while (!sm.isTerminal(state)) {
                        if (timer.elapsed(TimeUnit.SECONDS) >= secondsToRun) {
                            break outer;
                        }
                        state = sm.getRandomNextState(state);
                        numStateChanges++;
                    }
                    sm.getGoals(state);
                    numRollouts++;
                }
                long millisecondsTaken = timer.stop().elapsed(TimeUnit.MILLISECONDS);

                return new PerfTestReport(millisecondsTaken, numStateChanges, numRollouts);
            }
        };
    }

    private static CorrectnessTestRunnable getStateMachineCorrectnessTestRunnable(
            StateMachineFactory smf) {
        return (gameRules, stateChangesToRun, recorder) -> {
            Game game = Game.createEphemeralGame(Game.preprocessRulesheet(gameRules));
            StateMachine sm = smf.buildInitializedForGame(game);

            List<Role> roles = sm.getRoles();
            recorder.writeRoles(roles);
            int stateChangesSoFar = 0;
            MachineState initialState = sm.getInitialState();
            if (sm.isTerminal(initialState)) {
                recorder.recordTerminality(true);
                return; //otherwise stateChangesSoFar will never increase
            }
            while (true) {
                MachineState curState = initialState;
                while (!sm.isTerminal(curState)) {
                    recorder.recordTerminality(false);
                    List<Move> jointMove = Lists.newArrayList();
                    for (Role role : roles) {
                        List<Move> legalMoves = sm.getLegalMoves(curState, role);
                        recorder.recordLegalMoves(legalMoves);
                        jointMove.add(pickOneAtRandom(legalMoves));
                    }
                    recorder.recordChosenJointMove(jointMove);
                    curState = sm.getNextState(curState, jointMove);
                    stateChangesSoFar++;
                }
                recorder.recordTerminality(true);
                recorder.recordGoalValues(sm.getGoals(curState));
                //Do we end here?
                if (stateChangesSoFar > stateChangesToRun) {
                    return;
                }
            }
        };
    }

    private static final Random RANDOM = new Random();
    private static <T> T pickOneAtRandom(List<T> legalMoves) {
        int index = RANDOM.nextInt(legalMoves.size());
        return legalMoves.get(index);
    }

    //TODO: Limit number of errors we find?
    public Optional<ObservedError> validateCorrectnessTestOutput(
            Game game, BlockingQueue<GameActionMessage> messages) throws Exception {
        StateMachine sm = getStateMachine(this, game);

        int numStateChanges = 0;
        List<Role> ourRoles = sm.getRoles();
        List<Role> theirRoles = messages.take().expectRolesMessage().getRoles();
        if (!ourRoles.equals(theirRoles)) {
            return Optional.of(ObservedError.create("Role mismatch", ourRoles, theirRoles, numStateChanges));
        }
        //TODO: ...
        MachineState initialState = sm.getInitialState();
        while (true) {
            MachineState curState = initialState;
            List<List<Move>> moveHistory = Lists.newArrayList();
            //TODO: ...
            while (true) {
                ConcurrencyUtils.checkForInterruption();
                boolean ourTerminal = sm.isTerminal(curState);
                GameActionMessage message = messages.take();
                if (message.isEndOfMessages()) {
                    return Optional.empty();
                }
                boolean theirTerminal = message.expectTerminalityMessage().isTerminal();
                if (ourTerminal != theirTerminal) {
                    return Optional.of(ObservedError.create("Terminality mismatch", ourTerminal, theirTerminal, numStateChanges, moveHistory));
                }
                if (ourTerminal) {
                    break;
                }
                //TODO: Continue non-terminal case
                //Check legal moves
                for (Role role : theirRoles) {
                    Set<Move> ourMoves = Sets.newHashSet(sm.getLegalMoves(curState, role));
                    Set<Move> theirMoves = Sets.newHashSet(messages.take().expectLegalMovesMessage().getMoves());
                    if (!ourMoves.equals(theirMoves)) {
                        return Optional.of(ObservedError.create("Legal move mismatch for role " + role, ourMoves, theirMoves, numStateChanges, moveHistory));
                    }
                }
                List<Move> jointMove = messages.take().expectChosenMovesMessage().getJointMove();
                //Nothing to check
                moveHistory.add(jointMove);
                curState = sm.getNextState(curState, jointMove);
                numStateChanges++;
            }
            //TODO: Continue terminal case
            List<Integer> ourGoals = sm.getGoals(curState);
            List<Integer> theirGoals = messages.take().expectGoalsMessage().getGoals();
            if (!ourGoals.equals(theirGoals)) {
                return Optional.of(ObservedError.create("Goal values mismatch", ourGoals, theirGoals, numStateChanges, moveHistory));
            }
        }
    }

    private static StateMachine getStateMachine(JavaEngineType javaEngineType, Game game) {
        if (javaEngineType == PROVER) {
            return ProverStateMachineFactory.create().buildInitializedForGame(game);
        }
        throw new IllegalArgumentException(javaEngineType.toString());
    }


}
