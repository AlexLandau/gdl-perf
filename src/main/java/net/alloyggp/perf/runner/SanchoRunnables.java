package net.alloyggp.perf.runner;

import java.util.Arrays;
import java.util.List;

import org.ggp.base.util.game.Game;
import org.repackage.sancho_v1_61c.org.ggp.base.util.gdl.grammar.Gdl;
import org.repackage.sancho_v1_61c.org.ggp.base.util.propnet.polymorphic.forwardDeadReckon.ForwardDeadReckonInternalMachineState;
import org.repackage.sancho_v1_61c.org.ggp.base.util.propnet.polymorphic.forwardDeadReckon.ForwardDeadReckonLegalMoveInfo;
import org.repackage.sancho_v1_61c.org.ggp.base.util.propnet.polymorphic.forwardDeadReckon.ForwardDeadReckonLegalMoveSet;
import org.repackage.sancho_v1_61c.org.ggp.base.util.statemachine.Role;
import org.repackage.sancho_v1_61c.org.ggp.base.util.statemachine.implementation.propnet.forwardDeadReckon.ForwardDeadReckonPropnetStateMachine;

import com.google.common.collect.Lists;

import net.alloyggp.perf.runner.runnable.JavaSimulatorWrapper;

public class SanchoRunnables {

    //TODO: This can probably be further optimized, but that probably needs the
    // help of Steve and Andrew
    //TODO: Add depth-charge-only performance testing
    public static JavaSimulatorWrapper<?,?,?,?> getWrapper() {
        return new JavaSimulatorWrapper<ForwardDeadReckonPropnetStateMachine,
                ForwardDeadReckonInternalMachineState, Role,
                ForwardDeadReckonLegalMoveInfo>() {

                    @Override
                    public ForwardDeadReckonPropnetStateMachine createSimulator(String gameRules, Game game) {
                        ForwardDeadReckonPropnetStateMachine machine =
                                new ForwardDeadReckonPropnetStateMachine();
                        //Convert into Sancho-shadowed Gdl objects
                        List<Gdl> rules = org.repackage.sancho_v1_61c.org.ggp.base.util.game.Game.createEphemeralGame(game.getRulesheet()).getRules();
                        machine.initialize(rules);
                        machine.enableGreedyRollouts(false, true);

                        machine.optimizeStateTransitionMechanism(System.currentTimeMillis()+5000);
                        return machine;
                    }

                    private ForwardDeadReckonInternalMachineState initialState = null;
                    @Override
                    public ForwardDeadReckonInternalMachineState getInitialState(ForwardDeadReckonPropnetStateMachine sm) {
                        if (initialState == null) {
                            this.initialState = sm.createInternalState(sm.getInitialState());
                        }
                        if (curState == null) {
                            this.curState = sm.createEmptyInternalState();
                        }

                        this.curState.copy(initialState);
                        //TODO: Maybe also copy it into nextState here?
                        return curState;
                    }

                    @Override
                    public boolean isTerminal(ForwardDeadReckonPropnetStateMachine sm, ForwardDeadReckonInternalMachineState state)
                            throws Exception {
                        return sm.isTerminal(state);
                    }

                    @Override
                    public ForwardDeadReckonInternalMachineState getRandomNextState(ForwardDeadReckonPropnetStateMachine sm, ForwardDeadReckonInternalMachineState state)
                            throws Exception {
                        ForwardDeadReckonLegalMoveSet moveSet = sm.getLegalMoveSet(state);
                        ForwardDeadReckonLegalMoveInfo[] chosenMoves = new ForwardDeadReckonLegalMoveInfo[sm.getNumRoles()];
                        for (int r = 0; r < chosenMoves.length; r++) {
                            chosenMoves[r] = moveSet.getRandomMove(r);
                        }

                        return getNextState(sm, state, chosenMoves);
                    }

                    @Override
                    public List<Integer> getGoals(ForwardDeadReckonPropnetStateMachine sm, ForwardDeadReckonInternalMachineState state)
                            throws Exception {
                        Role[] roles = sm.getRoles();
                        List<Integer> goals = Lists.newArrayListWithCapacity(roles.length);
                        for (Role role : roles) {
                            goals.add(sm.getGoal(state, role));
                        }
                        return goals;
                    }

                    @Override
                    public List<Role> getRoles(ForwardDeadReckonPropnetStateMachine sm) {
                        return Arrays.asList(sm.getRoles());
                    }

                    @Override
                    public List<ForwardDeadReckonLegalMoveInfo> getLegalMoves(ForwardDeadReckonPropnetStateMachine sm, ForwardDeadReckonInternalMachineState state,
                            Role role) throws Exception {
                        return Lists.newArrayList(sm.getLegalMoves(state, role));
                    }

                    private ForwardDeadReckonInternalMachineState curState;
                    private ForwardDeadReckonInternalMachineState nextState = null;

                    private ForwardDeadReckonInternalMachineState getNextState(ForwardDeadReckonPropnetStateMachine sm,
                            ForwardDeadReckonInternalMachineState curState, ForwardDeadReckonLegalMoveInfo[] chosenMoves) {
                        if (nextState == null) {
                            nextState = sm.createEmptyInternalState();
                        }
                        sm.getNextState(curState, null, chosenMoves, nextState);
                        curState.copy(nextState);
                        return curState;
                    }

                    @Override
                    public ForwardDeadReckonInternalMachineState getNextState(ForwardDeadReckonPropnetStateMachine sm, ForwardDeadReckonInternalMachineState curState,
                            List<ForwardDeadReckonLegalMoveInfo> jointMove) throws Exception {
                        ForwardDeadReckonLegalMoveInfo[] chosenMoves = jointMove.toArray(new ForwardDeadReckonLegalMoveInfo[jointMove.size()]);
                        return getNextState(sm, curState, chosenMoves);
                    }

                    @Override
                    public String getMoveName(ForwardDeadReckonPropnetStateMachine simulator, ForwardDeadReckonLegalMoveInfo move) {
                        return move.toString();
                    }

                    @Override
                    public String getRoleName(ForwardDeadReckonPropnetStateMachine simulator, Role role) {
                        return role.toString();
                    }
        };
    }

}
