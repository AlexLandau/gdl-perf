package net.alloyggp.perf.runner;

import java.util.List;

import com.google.common.collect.ImmutableList;

public class GoalsMessage implements GameActionMessage {
	private final ImmutableList<Integer> goals;

	private GoalsMessage(ImmutableList<Integer> goals) {
		this.goals = goals;
	}

	@Override
	public GoalsMessage expectGoalsMessage() {
		return this;
	}

	public List<Integer> getGoals() {
		return goals;
	}

	public static GameActionMessage parse(String line) {
		line = line.substring(GameActionFormat.GOALS_PREFIX.length());

		ImmutableList<Integer> goals = GameActionMessage.split(line, Integer::parseInt);
		return new GoalsMessage(goals);
	}
}
