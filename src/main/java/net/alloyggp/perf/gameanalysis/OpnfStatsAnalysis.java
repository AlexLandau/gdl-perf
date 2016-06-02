package net.alloyggp.perf.gameanalysis;

import java.util.Map;

import org.ggp.base.util.game.Game;
import org.ggp.base.util.propnet.architecture.PropNet;
import org.ggp.base.util.propnet.factory.OptimizingPropNetFactory;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

public class OpnfStatsAnalysis implements Function<Game, Map<String, String>> {
    public static final OpnfStatsAnalysis INSTANCE = new OpnfStatsAnalysis();
    private OpnfStatsAnalysis() {
        // Singleton
    }

    @Override
    public Map<String, String> apply(Game game) {
        Map<String, String> results = Maps.newHashMap();

        long startTime = System.currentTimeMillis();
        PropNet pn;
        try {
            pn = OptimizingPropNetFactory.create(game.getRules());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        long totalTime = System.currentTimeMillis() - startTime;

        results.put("opnfTimeToCreate", Long.toString(totalTime));
        results.put("opnfNumComponents", Integer.toString(pn.getSize()));
        results.put("opnfNumLinks", Integer.toString(pn.getNumLinks()));
        results.put("opnfNumAnds", Integer.toString(pn.getNumAnds()));
        results.put("opnfNumNots", Integer.toString(pn.getNumNots()));
        results.put("opnfNumOrs", Integer.toString(pn.getNumOrs()));

        return results;
    }
}
