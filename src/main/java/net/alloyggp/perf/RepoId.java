package net.alloyggp.perf;

import org.ggp.base.util.game.CloudGameRepository;
import org.ggp.base.util.game.GameRepository;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

public enum RepoId {
    BASE(() -> GameRepository.getDefaultRepository()),
    DRESDEN(() -> new CloudGameRepository("http://games.ggp.org/dresden/")),
    STANFORD(() -> new CloudGameRepository("http://games.ggp.org/stanford/")),
    ;
    private final Supplier<GameRepository> repoSupplier;

    private RepoId(Supplier<GameRepository> repoSupplier) {
        //Only load the repo once, and not until we need to
        this.repoSupplier = Suppliers.memoize(repoSupplier);
    }

    public GameRepository getRepo() {
        return repoSupplier.get();
    }
}
