package net.alloyggp.perf.runner.ggpbase;

import org.ggp.base.util.game.Game;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;

public class ProverStateMachineFactory implements StateMachineFactory {
    private static final ProverStateMachineFactory INSTANCE = new ProverStateMachineFactory();
    private ProverStateMachineFactory() {
        //Use create() instead
    }

    public static ProverStateMachineFactory create() {
        return INSTANCE;
    }

    @Override
    public StateMachine buildInitializedForGame(Game game) {
        StateMachine sm = new ProverStateMachine();
        sm.initialize(game.getRules());
        return sm;
    }
}
