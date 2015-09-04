package net.alloyggp.perf.runner;

import java.util.ArrayList;
import java.util.List;

import stanfordlogic.knowledge.BasicKB;
import stanfordlogic.knowledge.KnowledgeBase;
import stanfordlogic.prover.GroundFact;
import stanfordlogic.prover.ProofContext;
import stanfordlogic.prover.TermObject;

public class JocularWrapper {

    public void getInitialState() {
     // Construct the root search state
        KnowledgeBase rootState = new BasicKB();
        for (GroundFact ground : currentState_.getIterable()) {
            rootState.setTrue(ground);
        }
        // ... and the proof contex to go with it
        ProofContext rootContext = new ProofContext(rootState, parser_);
    }

    public void isTerminal() {
        GroundFact isTerminal = reasoner_.getAnAnswer(QUERY_TERMINAL, context);
    }

    public void getLegalMoves() {
// Build a list of everybody's moves.

        List<List<GroundFact>> otherMoves = new ArrayList<List<GroundFact>>();
        List<GroundFact> myMoves = null;

        for (int i = 0; i < roles_.size(); i++)
        {
            TermObject role = roles_.get(i);
            List<GroundFact> roleMoves = getAllAnswers(context, "legal", role.toString(), "?x");

//            if (roleMoves.size() == 0) {
//                logger_.severe(gameId_ + ": role " + role.toString() + " had no legal moves!");
//            }

            if (i == myRoleIndex_) {
                myMoves = roleMoves;

//                if (currentDepth == 0 && bestMoveSoFar_ == null) {
//                    // pick a random first move if we don't have one yet
//                    bestMoveSoFar_ = myMoves.get(random_.nextInt(myMoves.size()))
//                                            .getTerm(1);
//                }
            }
            else {
                otherMoves.add(roleMoves);
            }
        }
    }

    public void getGoalValues() {
     // figure out my score in this outcome
        GroundFact myGoal = getAnAnswer(context, "goal", myRoleStr_, "?x");
        int myScore = Integer.parseInt(myGoal.getTerm(1).toString());
    }

    public void getNextState() {
     // Create a new state, based on state and context

        // First, add the moves
        for (GroundFact move: moves) {
            state.setTrue(move);
        }

        // Figure out what is true in the new state
        Iterable<GroundFact> nexts = reasoner_.getAllAnswersIterable(QUERY_NEXT, context);

        // FIXME: this should create a knowledge base of the same type as the current one
        KnowledgeBase newState = new BasicKB();

        for (GroundFact next: nexts) {
            newState.setTrue(trueProcessor_.processFact(next));
        }

        ProofContext newContext = new ProofContext(newState, parser_);

        // Run the recursive search
        Pair<Term, Integer> result = minimaxSearch(newState, newContext, currentDepth+1);

        // Remove the moves
        for (GroundFact move: moves) {
            state.setFalse(move);
        }
    }
}
