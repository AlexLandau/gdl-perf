package net.alloyggp.perf.runner.ggpbase;

import org.ggp.base.util.game.Game;
import org.ggp.base.util.statemachine.StateMachine;

public interface StateMachineFactory {

    StateMachine buildInitializedForGame(Game game);

}
