package net.alloyggp.perf.runner;

import java.io.File;

import com.google.common.base.Preconditions;

import net.alloyggp.perf.io.GameFiles;

public class CorrectnessTestProcess {

    public static void main(String[] args) throws Exception {
        Preconditions.checkArgument(args.length == 4);
        JavaEngineType engineType = JavaEngineType.valueOf(args[0]);
        File gameRulesFile = new File(args[1]);
        String gameRules = GameFiles.read(gameRulesFile);
        File outputFile = new File(args[2]);
        int stateChangesToRun = Integer.parseInt(args[3]);

        GameActionRecorder recorder = PrintStreamGameActionRecorder.createWritingToFile(outputFile);
        engineType.runCorrectnessTest(gameRules, stateChangesToRun, recorder);
    }

}
