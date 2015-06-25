package net.alloyggp.perf.runner;

import java.util.List;

import org.ggp.base.util.statemachine.Move;

import com.google.common.collect.ImmutableList;

public class ChosenMovesMessage implements GameActionMessage {
	private final ImmutableList<Move> jointMove;

	private ChosenMovesMessage(ImmutableList<Move> jointMove) {
		this.jointMove = jointMove;
	}

	@Override
	public ChosenMovesMessage expectChosenMovesMessage() {
		return this;
	}

	public List<Move> getJointMove() {
		return jointMove;
	}

	public static GameActionMessage parse(String line) {
		line = line.substring(GameActionFormat.CHOSEN_MOVES_PREFIX.length());

		ImmutableList<Move> jointMove = GameActionMessage.split(line, Move::create);
		return new ChosenMovesMessage(jointMove);
	}
}
