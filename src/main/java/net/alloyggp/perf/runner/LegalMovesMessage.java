package net.alloyggp.perf.runner;

import java.util.List;

import org.ggp.base.util.statemachine.Move;

import com.google.common.collect.ImmutableList;

public class LegalMovesMessage implements GameActionMessage {
	private final ImmutableList<Move> moves;

	private LegalMovesMessage(ImmutableList<Move> moves) {
		this.moves = moves;
	}

	@Override
	public LegalMovesMessage expectLegalMovesMessage() {
		return this;
	}

	public List<Move> getMoves() {
		return moves;
	}

	public static GameActionMessage parse(String line) {
		line = line.substring(GameActionFormat.LEGAL_MOVES_PREFIX.length());

		ImmutableList<Move> moves = GameActionMessage.split(line, Move::create);
		return new LegalMovesMessage(moves);
	}
}
