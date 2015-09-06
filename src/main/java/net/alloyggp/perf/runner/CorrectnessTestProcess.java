package net.alloyggp.perf.runner;

import java.io.File;

import net.alloyggp.perf.io.GameFiles;

import com.google.common.base.Preconditions;

public class CorrectnessTestProcess {

    public static void main(String[] args) throws Exception {
        Preconditions.checkArgument(args.length == 3);
        JavaEngineType engineType = JavaEngineType.valueOf(args[0]);
        File gameRulesFile = new File(args[1]);
        String gameRules = GameFiles.read(gameRulesFile);
        int stateChangesToRun = Integer.parseInt(args[2]);

        GameActionRecorder recorder = new StandardGameActionRecorder();
        engineType.runCorrectnessTest(gameRules, stateChangesToRun, recorder);
    }

}
