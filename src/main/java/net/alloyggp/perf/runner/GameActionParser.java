package net.alloyggp.perf.runner;

import java.util.Optional;

public class GameActionParser {

    public static Optional<GameActionMessage> convertLine(String line) {
        if (line.startsWith(GameActionFormat.CHOSEN_MOVES_PREFIX)) {
            return Optional.of(ChosenMovesMessage.parse(line));
        } else if (line.startsWith(GameActionFormat.GOALS_PREFIX)) {
            return Optional.of(GoalsMessage.parse(line));
        } else if (line.startsWith(GameActionFormat.LEGAL_MOVES_PREFIX)) {
            return Optional.of(LegalMovesMessage.parse(line));
        } else if (line.startsWith(GameActionFormat.ROLES_PREFIX)) {
            return Optional.of(RolesMessage.parse(line));
        } else if (line.startsWith(GameActionFormat.TERMINAL_PREFIX)) {
            return Optional.of(TerminalityMessage.parse(line));
        } else if (line.startsWith(GameActionFormat.ERROR_PREFIX)) {
            return Optional.of(ErrorMessage.parse(line));
        } else if (line.startsWith(GameActionFormat.TEST_FINISHED_PREFIX)) {
            return Optional.of(GameActionMessage.endOfMessages());
        }
        return Optional.empty();
    }

}
