package net.alloyggp.perf.runner;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.eclipse.palamedes.gdl.core.model.GameFactory;
import org.ggp.base.util.concurrency.ConcurrencyUtils;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachineFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import formerlybase.util.ruleengine.diffpropnet.DiffPropNetRuleEngineFactory;
import formerlybase.util.ruleengine.fwdpropnet.ForwardPropNetRuleEngine;
import formerlybase.util.ruleengine.fwdpropnet.ForwardPropNetRuleEngineFactory;
import formerlybase.util.ruleengine.fwdpropnet.v2.ForwardPropNetRuleEngineFactory2;
import formerlybase.util.ruleengine.fwdpropnet.v3.ForwardPropNetRuleEngineFactory3;
import formerlybase.util.ruleengine.fwdpropnet.v4.ForwardPropNetRuleEngineFactory4;
import formerlybase.util.ruleengine.tupleprover.TupleProverRuleEngine;
import formerlybase.util.ruleengine.tupleprover.TupleProverRuleEngineFactory;
import formerlybase.util.statemachine.superprover2.CompiledProverRuleEngine;
import formerlybase.util.statemachine.superprover2.CompiledProverRuleEngineFactory;
import net.alloyggp.perf.correctness.ObservedError;
import net.alloyggp.perf.runner.runnable.CorrectnessTestRunnable;
import net.alloyggp.perf.runner.runnable.GameSimulatorRunnables;
import net.alloyggp.perf.runner.runnable.JavaCorrectnessTestRunnable;
import net.alloyggp.perf.runner.runnable.JavaPerfTestRunnable;
import net.alloyggp.perf.runner.runnable.JavaSimulatorWrapper;
import net.alloyggp.perf.runner.runnable.PalamedesCoreRunnables;
import net.alloyggp.perf.runner.runnable.PerfTestRunnable;
import net.alloyggp.perf.runner.runnable.RekkuraRunnables;
import net.alloyggp.perf.runner.runnable.RuleEngineRunnables;
import net.alloyggp.perf.runner.runnable.SanchoRunnables;
import net.alloyggp.perf.runner.runnable.StateMachineRunnables;
import rekkura.ggp.machina.GgpStateMachine;

public enum JavaEngineType {
    GGP_BASE_PROVER("2015-04-26",
            StateMachineRunnables.getWrapper(ProverStateMachineFactory.createNormal())),
    ALLOY_TUPLE_PROVER(TupleProverRuleEngine.VERSION,
            RuleEngineRunnables.getWrapper(TupleProverRuleEngineFactory.create())),
    ALLOY_COMPILED_PROVER_CACHING(CompiledProverRuleEngine.VERSION,
            RuleEngineRunnables.getWrapper(CompiledProverRuleEngineFactory.createCachingEverything())),
    ALLOY_DIFF_PROP_NET("1",
            RuleEngineRunnables.getWrapper(DiffPropNetRuleEngineFactory.createStandardOpnf())),
    ALLOY_FORWARD_PROP_NET(ForwardPropNetRuleEngine.VERSION,
            RuleEngineRunnables.getWrapper(ForwardPropNetRuleEngineFactory.create())),
    ALLOY_FORWARD_PROP_NET2(ForwardPropNetRuleEngine.VERSION,
            RuleEngineRunnables.getWrapper(ForwardPropNetRuleEngineFactory2.create())),
    ALLOY_FORWARD_PROP_NET3(ForwardPropNetRuleEngine.VERSION,
            RuleEngineRunnables.getWrapper(ForwardPropNetRuleEngineFactory3.create())),
    ALLOY_FORWARD_PROP_NET4(ForwardPropNetRuleEngine.VERSION,
            RuleEngineRunnables.getWrapper(ForwardPropNetRuleEngineFactory4.create())),
    ALLOY_DUALIZED_PROVER("2016-01-14",
            StateMachineRunnables.getWrapper(ProverStateMachineFactory.createDualized())),
    ALLOY_DUALIZED_COMPILED_PROVER("2016-01-14",
            RuleEngineRunnables.getWrapper(CompiledProverRuleEngineFactory.createDualized())),
    ALLOY_DUALIZED_DIFF_PROP_NET("2016-01-14",
            RuleEngineRunnables.getWrapper(DiffPropNetRuleEngineFactory.createDualizedOpnf())),
    //NOTE: The 0.6.1 version is taken from Palamedes, which includes this version.
    PALAMEDES_JAVA_PROVER_USEOPT_FALSE("0.6.1",
            GameSimulatorRunnables.getWrapper(false)),
    PALAMEDES_JAVA_PROVER_USEOPT_TRUE("0.6.1",
            GameSimulatorRunnables.getWrapper(true)),
    PALAMEDES_JOCULAR("0.6.1",
            PalamedesCoreRunnables.getWrapper(GameFactory.JOCULAR)),
    PALAMEDES_JAVA_ECLIPSE("0.6.1",
            PalamedesCoreRunnables.getWrapper(GameFactory.PROLOGPROVER)),
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

    SANCHO_DEAD_RECKONING_PROPNET("1.61c",
            SanchoRunnables.getWrapper()),
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
        return version;
    }

    public PerfTestReport runPerfTest(String gameRules, int secondsToRun) throws Exception {
        return perfRunnable.runPerfTest(gameRules, secondsToRun, version);
    }

    public void runCorrectnessTest(String gameRules, int stateChangesToRun, GameActionRecorder recorder) throws Exception {
        correctnessRunnable.runCorrectnessTest(gameRules, stateChangesToRun, recorder);
    }

    public Optional<ObservedError> validateCorrectnessTestOutput(
            Game game, BlockingQueue<GameActionMessage> messages) throws Exception {
        StateMachine sm = getStateMachine(this, game);

        int numStateChanges = 0;
        try {
            List<Role> ourRoles = sm.getRoles();
            List<Role> theirRoles = messages.take().expectRolesMessage().getRoles();
            if (!ourRoles.equals(theirRoles)) {
                return Optional.of(ObservedError.create("Role mismatch", ourRoles, theirRoles, numStateChanges));
            }

            MachineState initialState = sm.getInitialState();
            while (true) {
                MachineState curState = initialState;
                List<List<Move>> moveHistory = Lists.newArrayList();

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

                List<Integer> ourGoals = sm.getGoals(curState);
                List<Integer> theirGoals = messages.take().expectGoalsMessage().getGoals();
                if (!ourGoals.equals(theirGoals)) {
                    return Optional.of(ObservedError.create("Goal values mismatch", ourGoals, theirGoals, numStateChanges, moveHistory));
                }
            }
        } catch (ErrorMessageException e) {
            return Optional.of(ObservedError.create(e.getErrorMessage().getError(), numStateChanges));
        }
    }

    private static StateMachine getStateMachine(JavaEngineType javaEngineType, Game game) {
        if (javaEngineType == GGP_BASE_PROVER) {
            return ProverStateMachineFactory.createNormal().buildInitializedForGame(game);
        }
        throw new IllegalArgumentException(javaEngineType.toString());
    }
}
