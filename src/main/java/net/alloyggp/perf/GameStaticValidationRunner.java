package net.alloyggp.perf;

import java.io.IOException;

import org.ggp.base.util.game.Game;
import org.ggp.base.validator.StaticValidator;

import net.alloyggp.perf.game.GameKey;
import net.alloyggp.perf.game.InvalidGameResult;
import net.alloyggp.perf.game.InvalidGames;

public class GameStaticValidationRunner {

    public static void main(String[] args) throws Exception {
        for (GameKey gameKey : GameKey.loadAllValidGameKeys()) {
            invalidateGameIfFailsTest(gameKey);
        }
    }

    private static void invalidateGameIfFailsTest(GameKey gameKey) throws IOException {
        try {
            Game game = gameKey.loadGame();
            StaticValidator.validateDescription(game.getRules());
        } catch (Exception e) {
            InvalidGames.recordValidationFailure(
                    InvalidGameResult.create(gameKey, e.getMessage()));
        }
    }
}
