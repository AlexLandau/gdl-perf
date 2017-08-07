package net.alloyggp.perf.runner.runnable;

import java.util.concurrent.TimeUnit;

import org.ggp.base.util.game.Game;

import com.google.common.base.Stopwatch;

import net.alloyggp.perf.runner.PerfTestReport;

public class JavaPerfTestRunnable<Simulator, State, Role, Move> implements PerfTestRunnable {
    private final JavaSimulatorWrapper<Simulator, State, Role, Move> wrapper;

    private JavaPerfTestRunnable(JavaSimulatorWrapper<Simulator, State, Role, Move> wrapper) {
        this.wrapper = wrapper;
    }

    public static <S, St, R, M> PerfTestRunnable create(JavaSimulatorWrapper<S, St, R, M> wrapper) {
        return new JavaPerfTestRunnable<S, St, R, M>(wrapper);
    }

    @Override
    public PerfTestReport runPerfTest(String gameRules, int secondsToRun, String version) throws Exception {
        Game game = Game.createEphemeralGame(Game.preprocessRulesheet(gameRules));
        Simulator sm = wrapper.createSimulator(gameRules, game);

        long numStateChanges = 0;
        long numRollouts = 0;
        Stopwatch timer = Stopwatch.createStarted();
        outer : while (true) {
            if (timer.elapsed(TimeUnit.SECONDS) >= secondsToRun) {
                break outer;
            }
            State state = wrapper.getInitialState(sm);
            while (!wrapper.isTerminal(sm, state)) {
                if (timer.elapsed(TimeUnit.SECONDS) >= secondsToRun) {
                    break outer;
                }
                state = wrapper.getRandomNextState(sm, state);
                numStateChanges++;
            }
            wrapper.getGoals(sm, state);
            numRollouts++;
        }
        long millisecondsTaken = timer.stop().elapsed(TimeUnit.MILLISECONDS);

        return new PerfTestReport(version, millisecondsTaken, numStateChanges, numRollouts);
    }

}
