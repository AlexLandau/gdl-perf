package net.alloyggp.perf.runner;

import java.util.Arrays;
import java.util.List;

import org.ggp.base.util.game.Game;
import org.repackage.sancho_v1_61c.org.ggp.base.util.gdl.grammar.Gdl;
import org.repackage.sancho_v1_61c.org.ggp.base.util.statemachine.MachineState;
import org.repackage.sancho_v1_61c.org.ggp.base.util.statemachine.Move;
import org.repackage.sancho_v1_61c.org.ggp.base.util.statemachine.Role;
import org.repackage.sancho_v1_61c.org.ggp.base.util.statemachine.implementation.propnet.forwardDeadReckon.ForwardDeadReckonPropnetStateMachine;

import net.alloyggp.perf.runner.runnable.JavaSimulatorWrapper;

public class SanchoRunnables {

    //TODO: I'm pretty sure this can be further optimized
    public static JavaSimulatorWrapper<?,?,?,?> getWrapper() {
        return new JavaSimulatorWrapper<ForwardDeadReckonPropnetStateMachine,
                MachineState, Role, Move>() {

                    @Override
                    public ForwardDeadReckonPropnetStateMachine createSimulator(String gameRules, Game game) {
                        ForwardDeadReckonPropnetStateMachine machine =
                                new ForwardDeadReckonPropnetStateMachine();
                        //Convert into Sancho-shadowed Gdl objects
                        List<Gdl> rules = org.repackage.sancho_v1_61c.org.ggp.base.util.game.Game.createEphemeralGame(game.getRulesheet()).getRules();
                        machine.initialize(rules);
                        return machine;
                    }

                    @Override
                    public MachineState getInitialState(ForwardDeadReckonPropnetStateMachine sm) {
                        return sm.getInitialState();
                    }

                    @Override
                    public boolean isTerminal(ForwardDeadReckonPropnetStateMachine sm, MachineState state)
                            throws Exception {
                        return sm.isTerminal(state);
                    }

                    @Override
                    public MachineState getRandomNextState(ForwardDeadReckonPropnetStateMachine sm, MachineState state)
                            throws Exception {
                        return sm.getRandomNextState(state);
                    }

                    @Override
                    public List<Integer> getGoals(ForwardDeadReckonPropnetStateMachine sm, MachineState state)
                            throws Exception {
                        return sm.getGoals(state);
                    }

                    @Override
                    public List<Role> getRoles(ForwardDeadReckonPropnetStateMachine sm) {
                        return Arrays.asList(sm.getRoles());
                    }

                    @Override
                    public List<Move> getLegalMoves(ForwardDeadReckonPropnetStateMachine sm, MachineState state,
                            Role role) throws Exception {
                        return sm.getLegalMoves(state, role);
                    }

                    @Override
                    public MachineState getNextState(ForwardDeadReckonPropnetStateMachine sm, MachineState curState,
                            List<Move> jointMove) throws Exception {
                        return sm.getNextState(curState, jointMove);
                    }

                    @Override
                    public String getMoveName(ForwardDeadReckonPropnetStateMachine simulator, Move move) {
                        return move.toString();
                    }

                    @Override
                    public String getRoleName(ForwardDeadReckonPropnetStateMachine simulator, Role role) {
                        return role.toString();
                    }
        };
    }

}
