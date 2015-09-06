package net.alloyggp.perf.analysis;

import java.io.IOException;
import java.util.List;

import net.alloyggp.perf.PerfTestResult;

import com.google.common.base.Function;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class MachineVsGameTableMaker {

    //Row, column, cell contents
    public static Table<String, String, String> createForStat(Function<PerfTestResult, String> stat) throws IOException {
        //Games going down, engine type going across
        List<PerfTestResult> allResults = PerfResultLoader.loadAllResults();

        Table<String, String, String> table = HashBasedTable.create();
        for (PerfTestResult result : allResults) {
            String gameKey = result.getGameKey().toString();
            String engineVersion = result.getEngineVersion().toString();
            String dataPoint = stat.apply(result);
            if (table.contains(gameKey, engineVersion)) {
                table.put(gameKey, engineVersion,
                        table.get(gameKey, engineVersion) + ", " + dataPoint);
            } else {
                table.put(gameKey, engineVersion, dataPoint);
            }
        }
        return table;
    }

}
