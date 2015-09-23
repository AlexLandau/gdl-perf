package net.alloyggp.perf.runner.runnable;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.ggp.base.util.game.Game;

import com.google.common.collect.Lists;

import net.alloyggp.perf.runner.GameActionRecorder;

public class JavaCorrectnessTestRunnable<Simulator, State, Role, Move> implements CorrectnessTestRunnable {
    private final JavaSimulatorWrapper<Simulator, State, Role, Move> wrapper;

    private JavaCorrectnessTestRunnable(JavaSimulatorWrapper<Simulator, State, Role, Move> wrapper) {
        this.wrapper = wrapper;
    }

    public static <S, St, R, M> CorrectnessTestRunnable create(
            JavaSimulatorWrapper<S, St, R, M> wrapper) {
        return new JavaCorrectnessTestRunnable<>(wrapper);
    }

    @Override
    public void runCorrectnessTest(String gameRules, int stateChangesToRun, GameActionRecorder recorder)
            throws Exception {
        Game game = Game.createEphemeralGame(Game.preprocessRulesheet(gameRules));
        Simulator sm = wrapper.createSimulator(gameRules, game);

        List<Role> roles = wrapper.getRoles(sm);
        List<String> roleNames = wrapper.getRoleNames(sm);
        recorder.writeRoles(roleNames);
        int stateChangesSoFar = 0;
        State initialState = wrapper.getInitialState(sm);
        if (wrapper.isTerminal(sm, initialState)) {
            recorder.recordTerminality(true);
            return; //otherwise stateChangesSoFar will never increase
        }
        while (true) {
            State curState = wrapper.getInitialState(sm);
            while (!wrapper.isTerminal(sm, curState)) {
                recorder.recordTerminality(false);
                List<Move> jointMove = Lists.newArrayList();
                for (Role role : roles) {
                    List<Move> legalMoves = wrapper.getLegalMoves(sm, curState, role);
                    recorder.recordLegalMoves(wrapper.getMoveNames(legalMoves));
                    jointMove.add(pickOneAtRandom(legalMoves));
                }
                recorder.recordChosenJointMove(wrapper.getMoveNames(jointMove));
                curState = wrapper.getNextState(sm, curState, jointMove);
                stateChangesSoFar++;
            }
            recorder.recordTerminality(true);
            recorder.recordGoalValues(wrapper.getGoals(sm, curState));
            //Do we end here?
            if (stateChangesSoFar > stateChangesToRun) {
                return;
            }
        }
    }



    public List<String> getMoveNames(List<Move> legalMoves) {
        List<String> moveNames = legalMoves.stream()
                .map(Object::toString)
                .collect(Collectors.toList());
        return moveNames;
    }

    private static final Random RANDOM = new Random();
    private static <T> T pickOneAtRandom(List<T> legalMoves) {
        int index = RANDOM.nextInt(legalMoves.size());
        return legalMoves.get(index);
    }




}
