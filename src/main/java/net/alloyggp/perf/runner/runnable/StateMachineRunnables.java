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
            public String getMoveName(StateMachine sm, Move move) {
                return move.toString();
            }

            @Override
            public String getRoleName(StateMachine sm, Role role) {
                return role.toString();
            }
        };
    }
}
