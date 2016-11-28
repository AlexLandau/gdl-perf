package net.alloyggp.perf.runner.runnable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.ggp.base.util.game.Game;
import org.ggp.base.util.gdl.grammar.Gdl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import rekkura.ggp.machina.GgpStateMachine;
import rekkura.ggp.machina.GgpStateMachine.Factory;
import rekkura.logic.format.KifFormat;
import rekkura.logic.model.Dob;
import rekkura.logic.model.Rule;

public class RekkuraRunnables {
    //GgpStateMachine doesn't use the concept of an ordered list of roles, but we
    //need that for our interface, so we add a layer that stores that here
    public static class GgpStateMachine2 implements GgpStateMachine {
        private final GgpStateMachine delegate;
        private final ImmutableList<Dob> roles;

        private GgpStateMachine2(GgpStateMachine delegate, ImmutableList<Dob> roles) {
            this.delegate = delegate;
            this.roles = roles;
        }

        public static GgpStateMachine2 create(GgpStateMachine delegate, List<Dob> roles) {
            return new GgpStateMachine2(delegate, ImmutableList.copyOf(roles));
        }

        public ImmutableList<Dob> getRoles() {
            return roles;
        }

        @Override
        public Set<Dob> getInitial() {
            return delegate.getInitial();
        }

        @Override
        public ListMultimap<Dob, Dob> getActions(Set<Dob> state) {
            return delegate.getActions(state);
        }

        @Override
        public Set<Dob> nextState(Set<Dob> state, Map<Dob, Dob> actions) {
            return delegate.nextState(state, actions);
        }

        @Override
        public boolean isTerminal(Set<Dob> state) {
            return delegate.isTerminal(state);
        }

        @Override
        public Map<Dob, Integer> getGoals(Set<Dob> state) {
            return delegate.getGoals(state);
        }
    }

    public static JavaSimulatorWrapper<GgpStateMachine2, Set<Dob>, Dob, Dob>
            getWrapper(final Factory<? extends GgpStateMachine> factory) {
        return new JavaSimulatorWrapper<GgpStateMachine2, Set<Dob>, Dob, Dob>() {
            @Override
            public GgpStateMachine2 createSimulator(String gameRules, Game game) {
                List<String> ruleStrings = game.getRules().stream()
                        .map(Gdl::toString)
                        .collect(Collectors.toList());
                List<Rule> rules = KifFormat.genericStringsToRules(ruleStrings.toArray(new String[0]));

                List<Dob> roles = rekkura.ggp.milleu.Game.getRoles(rules);
                return GgpStateMachine2.create(factory.create(rules), roles);
            }

            @Override
            public Set<Dob> getInitialState(GgpStateMachine2 sm) {
                return sm.getInitial();
            }

            @Override
            public boolean isTerminal(GgpStateMachine2 sm, Set<Dob> state) {
                return sm.isTerminal(state);
            }

            @Override
            public Set<Dob> getRandomNextState(GgpStateMachine2 sm, Set<Dob> state) throws Exception {
                ListMultimap<Dob,Dob> actions = sm.getActions(state);
                Map<Dob, Dob> chosenActions = Maps.newHashMap();
                for (Dob role : actions.keySet()) {
                    chosenActions.put(role, pickOneAtRandom(actions.get(role)));
                }
                return sm.nextState(state, chosenActions);
            }

            private <T> T pickOneAtRandom(List<T> legalMoves) {
                int index = ThreadLocalRandom.current().nextInt(legalMoves.size());
                return legalMoves.get(index);
            }

            @Override
            public List<Integer> getGoals(GgpStateMachine2 sm, Set<Dob> state) throws Exception {
                Map<Dob, Integer> goalsByRole = sm.getGoals(state);
                ImmutableList<Dob> roles = sm.getRoles();
                List<Integer> results = Lists.newArrayListWithCapacity(roles.size());
                for (Dob role : roles) {
                    results.add(goalsByRole.get(role));
                }
                return results;
            }

            @Override
            public List<Dob> getRoles(GgpStateMachine2 sm) {
                return sm.getRoles();
            }

            @Override
            public List<Dob> getLegalMoves(GgpStateMachine2 sm, Set<Dob> state, Dob role) throws Exception {
                return sm.getActions(state).get(role);
            }

            @Override
            public Set<Dob> getNextState(GgpStateMachine2 sm, Set<Dob> curState, List<Dob> jointMove) throws Exception {
                Map<Dob, Dob> moveMap = Maps.newHashMapWithExpectedSize(jointMove.size());
                ImmutableList<Dob> roles = sm.getRoles();
                for (int r = 0; r < roles.size(); r++) {
                    Dob role = sm.getRoles().get(r);
                    moveMap.put(role, jointMove.get(r));
                }
                return sm.nextState(curState, moveMap);
            }

            @Override
            public String getMoveName(GgpStateMachine2 sm, Dob move) {
                return KifFormat.inst.toString(move.at(2));
            }

            @Override
            public String getRoleName(GgpStateMachine2 sm, Dob role) {
                String inParens = role.toString();
                return inParens.substring(1, inParens.length() - 1);
            }

        };
    }
}
