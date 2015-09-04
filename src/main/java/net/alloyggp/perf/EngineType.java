package net.alloyggp.perf;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.ggp.base.util.statemachine.implementation.tupleprover.TupleProverStateMachine;
import org.ggp.base.util.statemachine.superprover2.CompiledProverRuleEngine;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.alloyggp.perf.runner.CorrectnessTestProcess;
import net.alloyggp.perf.runner.JavaEngineType;
import net.alloyggp.perf.runner.PerfTestProcess;

public enum EngineType {
    PROVER("2015-04-26", getJavaPerfTestCommands(JavaEngineType.PROVER),
            getJavaCorrectnessTestCommands(JavaEngineType.PROVER)),
    TUPLE_PROVER(TupleProverStateMachine.VERSION,
            getJavaPerfTestCommands(JavaEngineType.TUPLE_PROVER),
            getJavaCorrectnessTestCommands(JavaEngineType.TUPLE_PROVER)),
    COMPILED_PROVER(CompiledProverRuleEngine.VERSION,
            getJavaPerfTestCommands(JavaEngineType.COMPILED_PROVER),
            getJavaCorrectnessTestCommands(JavaEngineType.COMPILED_PROVER)),
    //Palamedes BasicPlayer Java prover (linked from Dresden GGP page)
    PALAMEDES_GAME_SIMULATOR_USEOPT_FALSE("0.6.1",
            getJavaPerfTestCommands(JavaEngineType.PALAMEDES_GAME_SIMULATOR_USEOPT_FALSE),
            getJavaCorrectnessTestCommands(JavaEngineType.PALAMEDES_GAME_SIMULATOR_USEOPT_FALSE)),
    PALAMEDES_GAME_SIMULATOR_USEOPT_TRUE("0.6.1",
            getJavaPerfTestCommands(JavaEngineType.PALAMEDES_GAME_SIMULATOR_USEOPT_TRUE),
            getJavaCorrectnessTestCommands(JavaEngineType.PALAMEDES_GAME_SIMULATOR_USEOPT_TRUE)),
    //
            ;
    private final String version;
    private final ImmutableList<String> commandsForPerfTest;
    private final ImmutableList<String> commandsForCorrectnessTest;

    private EngineType(String version, List<String> commandsForPerfTest, List<String> commandsForCorrectnessTest) {
        this.version = version;
        this.commandsForPerfTest = ImmutableList.copyOf(commandsForPerfTest);
        this.commandsForCorrectnessTest = ImmutableList.copyOf(commandsForCorrectnessTest);
    }

    private static List<String> getJavaPerfTestCommands(JavaEngineType engineType) {
        return ImmutableList.of(getJavaCommand(), "-cp", getClasspath(), PerfTestProcess.class.getName(), engineType.toString());
    }

    private static List<String> getJavaCorrectnessTestCommands(JavaEngineType engineType) {
        return ImmutableList.of(getJavaCommand(), "-cp", getClasspath(), CorrectnessTestProcess.class.getName(), engineType.toString());
    }

    private static String getClasspath() {
        return System.getProperty("java.class.path");
    }

    private static String getJavaCommand() {
        String command = System.getProperty("java.home") + "/bin/java";
        if (isWindows()) {
            return command + ".exe";
        }
        return command;
    }

    private static boolean isWindows() {
        //Apache commons uses this approach
        return System.getProperty("os.name").startsWith("Windows");
    }

    public static enum TestCompleted {
        YES,
        NO
    }

    public TestCompleted runPerfTest(PerfTestConfig perfTestConfig) throws IOException, InterruptedException {
        List<String> commands = Lists.newArrayList();
        commands.addAll(getCommandsForPerfTest());
        commands.add(perfTestConfig.getGameFile().getAbsolutePath());
        commands.add(perfTestConfig.getOutputFile().getAbsolutePath());
        commands.add(Integer.toString(perfTestConfig.getNumSeconds()));
        ProcessBuilder pb = new ProcessBuilder(commands);

        //These cause output from the test process to be displayed on the console of the
        //test runner process.
        pb.redirectOutput(Redirect.INHERIT);
        pb.redirectError(Redirect.INHERIT);

        Process process = pb.start();
        boolean exited = process.waitFor(perfTestConfig.getSecondsBeforeCancelling(), TimeUnit.SECONDS);
        if (exited) {
            return TestCompleted.YES;
        }
        //Kill the process, and wait until it dies before continuing
        process.destroyForcibly();
        process.waitFor();
        return TestCompleted.NO;
    }

    private List<String> getCommandsForPerfTest() {
        return this.commandsForPerfTest;
    }

    public List<String> getCommandsForCorrectnessTest() {
        return commandsForCorrectnessTest;
    }

    /**
     * Returns an EngineVersion with this engine's current version.
     */
    public EngineVersion getWithVersion() {
        return EngineVersion.create(this, version);
    }

    public String getCurrentVersion() {
        return version;
    }
}
