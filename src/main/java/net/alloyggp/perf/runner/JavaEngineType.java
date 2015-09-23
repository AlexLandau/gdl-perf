package net.alloyggp.perf.runner;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.ggp.base.util.concurrency.ConcurrencyUtils;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.alloyggp.perf.ObservedError;
import net.alloyggp.perf.runner.ggpbase.ProverStateMachineFactory;
import net.alloyggp.perf.runner.runnable.CorrectnessTestRunnable;
import net.alloyggp.perf.runner.runnable.GameSimulatorRunnables;
import net.alloyggp.perf.runner.runnable.JavaCorrectnessTestRunnable;
import net.alloyggp.perf.runner.runnable.JavaPerfTestRunnable;
import net.alloyggp.perf.runner.runnable.JavaSimulatorWrapper;
import net.alloyggp.perf.runner.runnable.PerfTestRunnable;
import net.alloyggp.perf.runner.runnable.RekkuraRunnables;
import net.alloyggp.perf.runner.runnable.StateMachineRunnables;
import rekkura.ggp.machina.GgpStateMachine;

public enum JavaEngineType {
    PROVER("2015-04-26",
            StateMachineRunnables.getWrapper(ProverStateMachineFactory.create())),
    //NOTE: The 0.6.1 version is taken from Palamedes, which includes this version.
    PALAMEDES_GAME_SIMULATOR_USEOPT_FALSE("0.6.1",
            GameSimulatorRunnables.getWrapper(false)),
    PALAMEDES_GAME_SIMULATOR_USEOPT_TRUE("0.6.1",
            GameSimulatorRunnables.getWrapper(true)),
    REKKURA_GENERIC_FORWARD_PROVER_OSTD("1.0.0",
            RekkuraRunnables.getWrapper(GgpStateMachine.GENERIC_FORWARD_PROVER_OSTD)),
    REKKURA_GENERIC_FORWARD_PROVER("1.0.0",
            RekkuraRunnables.getWrapper(GgpStateMachine.GENERIC_FORWARD_PROVER)),
    REKKURA_GENERIC_BACKWARD_PROVER_OSTD("1.0.0",
            RekkuraRunnables.getWrapper(GgpStateMachine.GENERIC_BACKWARD_PROVER_OSTD)),
    REKKURA_GENERIC_BACKWARD_PROVER("1.0.0",
            RekkuraRunnables.getWrapper(GgpStateMachine.GENERIC_BACKWARD_PROVER)),
    REKKURA_BACKWARD_PROVER_OSTD("1.0.0",
            RekkuraRunnables.getWrapper(GgpStateMachine.BACKWARD_PROVER_OSTD)),
    REKKURA_BACKWARD_PROVER("1.0.0",
            RekkuraRunnables.getWrapper(GgpStateMachine.BACKWARD_PROVER)),

    ;
    private final String version;
    private final PerfTestRunnable perfRunnable;
    private final CorrectnessTestRunnable correctnessRunnable;

    private JavaEngineType(String version,
            PerfTestRunnable perfRunnable,
            CorrectnessTestRunnable correctnessRunnable) {
        this.version = version;
        this.perfRunnable = perfRunnable;
        this.correctnessRunnable = correctnessRunnable;
    }

    private JavaEngineType(String version,
            JavaSimulatorWrapper<?,?,?,?> wrapper) {
        this.version = version;
        this.perfRunnable = JavaPerfTestRunnable.create(wrapper);
        this.correctnessRunnable = JavaCorrectnessTestRunnable.create(wrapper);
    }

    public String getVersion() {
        //TODO: Come up with a non-hacky way to do this! Obvious ways lead to circular dependencies.
        return version;
    }

    public PerfTestReport runPerfTest(String gameRules, int secondsToRun) throws Exception {
        return perfRunnable.runPerfTest(gameRules, secondsToRun);
    }

    public void runCorrectnessTest(String gameRules, int stateChangesToRun, GameActionRecorder recorder) throws Exception {
        correctnessRunnable.runCorrectnessTest(gameRules, stateChangesToRun, recorder);
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
                for (Role role : ourRoles) {
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

    private boolean equalsIgnoringCase(List<Role> ourRoles, List<Role> theirRoles) {
        if (ourRoles.size() != theirRoles.size()) {
            return false;
        }
        for (int i = 0; i < ourRoles.size(); i++) {
            if (!ourRoles.get(i).toString().equalsIgnoreCase(theirRoles.get(i).toString())) {
                return false;
            }
        }
        return true;
    }

    private static StateMachine getStateMachine(JavaEngineType javaEngineType, Game game) {
        if (javaEngineType == PROVER) {
            return ProverStateMachineFactory.create().buildInitializedForGame(game);
        }
        throw new IllegalArgumentException(javaEngineType.toString());
    }


//    private static PerfTestRunnable getRekkuraPerfTestRunnable(Factory<? extends GgpStateMachine> factory) {
//        return new PerfTestRunnable() {
//            @Override
//            public PerfTestReport runPerfTest(String gameRules, int secondsToRun) throws Exception {
//                Game game = Game.createEphemeralGame(Game.preprocessRulesheet(gameRules));
//                List<String> ruleStrings = game.getRules().stream()
//                        .map(Gdl::toString)
//                        .collect(Collectors.toList());
//                List<Rule> rules = KifFormat.genericStringsToRules(ruleStrings.toArray(new String[0]));
//                GgpStateMachine sm = factory.create(rules);
//
//                long numStateChanges = 0;
//                long numRollouts = 0;
//                Stopwatch timer = new Stopwatch().start();
//                outer : while (true) {
//                    if (timer.elapsed(TimeUnit.SECONDS) >= secondsToRun) {
//                        break outer;
//                    }
//                    Set<Dob> state = sm.getInitial();
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
//    }
//
//    private static CorrectnessTestRunnable getRekkuraCorrectnessTestRunnable(Factory<? extends GgpStateMachine> factory) {
//        return (gameRules, stateChangesToRun, recorder) -> {
//            Game game = Game.createEphemeralGame(Game.preprocessRulesheet(gameRules));
//            List<String> ruleStrings = game.getRules().stream()
//                    .map(Gdl::toString)
//                    .collect(Collectors.toList());
//            List<Rule> rules = KifFormat.genericStringsToRules(ruleStrings.toArray(new String[0]));
//            GgpStateMachine sm = factory.create(rules);
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
//    }

}
