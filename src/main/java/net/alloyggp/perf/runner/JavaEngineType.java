package net.alloyggp.perf.runner;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

import cs227b.teamIago.gameProver.GameSimulator;
import cs227b.teamIago.resolver.ExpList;
import cs227b.teamIago.resolver.Expression;
import cs227b.teamIago.resolver.Predicate;

public enum JavaEngineType {
    PROVER(getStateMachinePerfTestRunnable(ProverStateMachineFactory.create()),
            getStateMachineCorrectnessTestRunnable(ProverStateMachineFactory.create())),
    PALAMEDES_GAME_SIMULATOR_USEOPT_FALSE(getPalamedesPerfTestRunnable(false),
            getPalamedesCorrectnessTestRunnable(false)),
    PALAMEDES_GAME_SIMULATOR_USEOPT_TRUE(getPalamedesPerfTestRunnable(true),
            getPalamedesCorrectnessTestRunnable(true)),
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
    private static Expression pickOneAtRandom(ExpList legalMoves) {
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


    private static PerfTestRunnable getPalamedesPerfTestRunnable(boolean useOpt) {
        return new PerfTestRunnable() {
            @Override
            public PerfTestReport runPerfTest(String gameRules, int secondsToRun) throws Exception {
                GameSimulator simulator = new GameSimulator(false, useOpt);
                simulator.ParseDescIntoTheory(gameRules);

                ExpList roles = simulator.GetRoles();
                Random rand = new Random();

                long numStateChanges = 0;
                long numRollouts = 0;
                Stopwatch timer = new Stopwatch().start();
                outer : while (true) {
                    if (timer.elapsed(TimeUnit.SECONDS) >= secondsToRun) {
                        break outer;
                    }
                    simulator.SimulateStart();
                    while (!simulator.IsTerminal()) {
                        if (timer.elapsed(TimeUnit.SECONDS) >= secondsToRun) {
                            break outer;
                        }

                        ExpList moves = new ExpList();
                        for (int r = 0; r < roles.size(); r++) {
                            Expression role = roles.get(r);
                            ExpList legalMoves = simulator.GetLegalMoves(role);
                            int chosenIndex = rand.nextInt(legalMoves.size());
                            Expression chosenMove = legalMoves.get(chosenIndex);
                            moves.add(chosenMove);
                        }
                        simulator.SimulateStep(moves);

                        numStateChanges++;
                    }
                    for (int r = 0; r < roles.size(); r++) {
                        Expression role = roles.get(r);
                        simulator.GetGoalValue(role);
                    }

                    numRollouts++;
                }
                long millisecondsTaken = timer.stop().elapsed(TimeUnit.MILLISECONDS);

                return new PerfTestReport(millisecondsTaken, numStateChanges, numRollouts);
            }
        };
    }

    private static CorrectnessTestRunnable getPalamedesCorrectnessTestRunnable(boolean useOpt) {
        return new CorrectnessTestRunnable() {
            @Override
            public void runCorrectnessTest(String gameRules,
                    int stateChangesToRun, GameActionRecorder recorder) throws Exception {
                GameSimulator simulator = new GameSimulator(false, useOpt);
                simulator.ParseDescIntoTheory(gameRules);

                runTest(simulator, stateChangesToRun, recorder);
            }

            private void runTest(GameSimulator simulator, int stateChangesToRun,
                    GameActionRecorder recorder) throws Exception {
                ExpList roles = simulator.GetRoles();
                recorder.writeRoles(toRoles(roles));
                int stateChangesSoFar = 0;
                simulator.SimulateStart();
                if (simulator.IsTerminal()) {
                    recorder.recordTerminality(true);
                    return; //otherwise stateChangesSoFar will never increase
                }
                while (true) {
                    simulator.SimulateStart();
                    while (!simulator.IsTerminal()) {
                        recorder.recordTerminality(false);
                        ExpList jointMove = new ExpList();
                        for (int r = 0; r < roles.size(); r++) {
                            ExpList legalMoves = simulator.GetLegalMoves(roles.get(r));
                            List<Move> translatedMoves = translatePalamedesMoves(legalMoves);
                            recorder.recordLegalMoves(translatedMoves);
                            jointMove.add(pickOneAtRandom(legalMoves));
                        }
                        List<Move> translatedJointMove = translatePalamedesMoves(jointMove);
                        recorder.recordChosenJointMove(translatedJointMove);
                        simulator.SimulateStep(jointMove);
                        stateChangesSoFar++;
                    }
                    recorder.recordTerminality(true);
                    List<Integer> goalValues = Lists.newArrayList();
                    for (int r = 0; r < roles.size(); r++) {
                        Expression role = roles.get(r);
                        goalValues.add(simulator.GetGoalValue(role));
                    }
                    recorder.recordGoalValues(goalValues);
                    //Do we end here?
                    if (stateChangesSoFar > stateChangesToRun) {
                        return;
                    }
                }
            }

        };
    }

    public static List<Move> translatePalamedesMoves(ExpList legalMoves) {
        List<Move> translatedMoves = ((List<?>)legalMoves.toArrayList()).stream()
                .map(obj -> (Predicate) obj)
                .map(predicate -> predicate.getOperands().get(1))
                .map(Expression::toString)
                .map(Move::create)
                .collect(Collectors.toList());
        return translatedMoves;
    }

    public static List<Role> toRoles(ExpList expRoles) {
        List<Role> roles = Lists.newArrayList();
        for (int r = 0; r < expRoles.size(); r++) {
            Expression expRole = expRoles.get(r);
            roles.add(Role.create(expRole.toString()));
        }
        return roles;
    }
}
