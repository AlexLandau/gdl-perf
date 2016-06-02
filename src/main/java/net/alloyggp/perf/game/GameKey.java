package net.alloyggp.perf.game;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Set;

import javax.annotation.concurrent.Immutable;

import org.ggp.base.util.game.Game;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;

/**
 * Note: If the contents of a game changes, its hash will change and
 * the corresponding GameKey will change to no longer equal its old
 * value.
 */
@Immutable
public class GameKey {
    private final RepoId repo;
    private final String gameKey;
    private final String hashString;

    private GameKey(RepoId repo, String gameKey, String gameHash) {
        Preconditions.checkArgument(!gameKey.contains("/"));
        this.repo = repo;
        this.gameKey = gameKey;
        this.hashString = gameHash;
    }

    public static GameKey create(RepoId repo, String gameKey) {
        //Get the current version
        String hashString = hash(repo.getRepo().getGame(gameKey));
        return new GameKey(repo, gameKey, hashString);
    }

    public Game loadGame() {
        return repo.getRepo().getGame(gameKey);
    }

    public static Set<GameKey> loadAllGameKeys() {
        Set<GameKey> keys = Sets.newHashSet();
        for (RepoId repo : RepoId.values()) {
            try {
                for (String gameKey : repo.getRepo().getGameKeys()) {
                    keys.add(create(repo, gameKey));
                }
            } catch (Exception e) {
                // continue if a repo fails
            }
        }
        return keys;
    }

    private static String hash(Game game) {
        int hashValue = game.getRulesheet().hashCode();
        byte[] hashBytes = Ints.toByteArray(hashValue);
        String rawHashString = Base64.getUrlEncoder().encodeToString(hashBytes);
        //This consistently ends in "==", so omit that for brevity
        return rawHashString.substring(0, rawHashString.length() - 2);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((hashString == null) ? 0 : hashString.hashCode());
        result = prime * result + ((gameKey == null) ? 0 : gameKey.hashCode());
        result = prime * result + ((repo == null) ? 0 : repo.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        GameKey other = (GameKey) obj;
        if (hashString == null) {
            if (other.hashString != null) {
                return false;
            }
        } else if (!hashString.equals(other.hashString)) {
            return false;
        }
        if (gameKey == null) {
            if (other.gameKey != null) {
                return false;
            }
        } else if (!gameKey.equals(other.gameKey)) {
            return false;
        }
        if (repo != other.repo) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return repo.toString() + "/" + gameKey + "/" + hashString;
    }

    //Reverse of toString()
    public static GameKey create(String gameKeyString) {
        String[] split = gameKeyString.split("/", 3);
        if (split.length != 3) {
            throw new IllegalArgumentException("Bad gameKeyString " + gameKeyString);
        }
        RepoId repo = RepoId.valueOf(split[0]);
        String game = split[1];
        String hashString = split[2];
        return new GameKey(repo, game, hashString);
    }

    /**
     * Loads all game keys that don't yet have entries in invalidGames.csv indicating that
     * they are invalid.
     */
    public static Set<GameKey> loadAllValidGameKeys() throws IOException {
        return Sets.difference(Sets.newHashSet(loadAllGameKeys()),
                InvalidGames.loadInvalidGames().keySet());
    }

    public static ImmutableSet<GameKey> createSet(RepoId repoId, String... gameNames) {
        return createSet(repoId, Arrays.asList(gameNames));
    }

    public static ImmutableSet<GameKey> createSet(RepoId repoId, Iterable<String> gameNames) {
        ImmutableSet.Builder<GameKey> result = ImmutableSet.builder();
        for (String gameName : gameNames) {
            result.add(create(repoId, gameName));
        }
        return result.build();
    }

    /**
     * TODO: This implementation is silly...
     */
    public boolean isValid() {
        try {
            loadGame();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
