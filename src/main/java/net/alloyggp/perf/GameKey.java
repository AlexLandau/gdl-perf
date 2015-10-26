package net.alloyggp.perf;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.ggp.base.util.game.Game;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class GameKey {
    private final RepoId repo;
    private final String gameKey;

    private GameKey(RepoId repo, String gameKey) {
        this.repo = repo;
        this.gameKey = gameKey;
    }

    public Game loadGame() {
        return repo.getRepo().getGame(gameKey);
    }

    public static Collection<GameKey> loadAllGameKeys() {
        List<GameKey> keys = Lists.newArrayList();
        for (RepoId repo : RepoId.values()) {
            for (String gameKey : repo.getRepo().getGameKeys()) {
                keys.add(new GameKey(repo, gameKey));
            }
        }
        return keys;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((gameKey == null) ? 0 : gameKey.hashCode());
        result = prime * result + ((repo == null) ? 0 : repo.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GameKey other = (GameKey) obj;
        if (gameKey == null) {
            if (other.gameKey != null)
                return false;
        } else if (!gameKey.equals(other.gameKey))
            return false;
        if (repo != other.repo)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return repo.toString() + "/" + gameKey;
    }

    //Reverse of toString()
    public static GameKey create(String gameKeyString) {
        String[] split = gameKeyString.split("/", 2);
        if (split.length != 2) {
            throw new IllegalArgumentException("Bad gameKeyString " + gameKeyString);
        }
        RepoId repo = RepoId.valueOf(split[0]);
        String game = split[1];
        return new GameKey(repo, game);
    }

    /**
     * Loads all game keys that don't yet have entries in invalidGames.csv indicating that
     * they are invalid.
     */
    public static Set<GameKey> loadAllValidGameKeys() throws IOException {
        return Sets.difference(Sets.newHashSet(loadAllGameKeys()),
                InvalidGames.loadInvalidGames().keySet());
    }

    public static ImmutableSet<GameKey> createSet(String... gameKeyStrings) {
        ImmutableSet.Builder<GameKey> result = ImmutableSet.builder();
        for (String gameKeyString : gameKeyStrings) {
            result.add(create(gameKeyString));
        }
        return result.build();
    }
}
