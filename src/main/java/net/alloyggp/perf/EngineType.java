package net.alloyggp.perf;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.alloyggp.perf.runner.CorrectnessTestProcess;
import net.alloyggp.perf.runner.JavaEngineType;
import net.alloyggp.perf.runner.PerfTestProcess;

public enum EngineType {
    PROVER(JavaEngineType.PROVER),
    TUPLE_PROVER(JavaEngineType.TUPLE_PROVER),
    COMPILED_PROVER_CACHING(JavaEngineType.COMPILED_PROVER_CACHING),
    //Palamedes BasicPlayer Java prover (linked from Dresden GGP page)
    PALAMEDES_GAME_SIMULATOR_USEOPT_FALSE(JavaEngineType.PALAMEDES_GAME_SIMULATOR_USEOPT_FALSE),
    PALAMEDES_GAME_SIMULATOR_USEOPT_TRUE(JavaEngineType.PALAMEDES_GAME_SIMULATOR_USEOPT_TRUE),
    //And others in Palamedes Core
    PALAMEDES_JOCULAR(JavaEngineType.PALAMEDES_JOCULAR),
    PALAMEDES_JAVA_ECLIPSE(JavaEngineType.PALAMEDES_JAVA_ECLIPSE),
    //From Peter Pham's Rekkura codebase
    REKKURA_GENERIC_FORWARD_PROVER_OSTD(JavaEngineType.REKKURA_GENERIC_FORWARD_PROVER_OSTD),
    REKKURA_GENERIC_FORWARD_PROVER(JavaEngineType.REKKURA_GENERIC_FORWARD_PROVER),
    REKKURA_GENERIC_BACKWARD_PROVER_OSTD(JavaEngineType.REKKURA_GENERIC_BACKWARD_PROVER_OSTD),
    REKKURA_GENERIC_BACKWARD_PROVER(JavaEngineType.REKKURA_GENERIC_BACKWARD_PROVER),
    REKKURA_BACKWARD_PROVER_OSTD(JavaEngineType.REKKURA_BACKWARD_PROVER_OSTD),
    REKKURA_BACKWARD_PROVER(JavaEngineType.REKKURA_BACKWARD_PROVER),
    ;
    private final String version;
    private final ImmutableList<String> commandsForPerfTest;
    private final ImmutableList<String> commandsForCorrectnessTest;

    private EngineType(String version, List<String> commandsForPerfTest, List<String> commandsForCorrectnessTest) {
        this.version = version;
        this.commandsForPerfTest = ImmutableList.copyOf(commandsForPerfTest);
        this.commandsForCorrectnessTest = ImmutableList.copyOf(commandsForCorrectnessTest);
    }

    private EngineType(JavaEngineType engineType) {
        this.version = engineType.getVersion();
        this.commandsForPerfTest = getJavaPerfTestCommands(engineType);
        this.commandsForCorrectnessTest = getJavaCorrectnessTestCommands(engineType);
    }

    private static ImmutableList<String> getJavaPerfTestCommands(JavaEngineType engineType) {
        return ImmutableList.of(getJavaCommand(), "-cp", getClasspath(), PerfTestProcess.class.getName(), engineType.toString());
    }

    private static ImmutableList<String> getJavaCorrectnessTestCommands(JavaEngineType engineType) {
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
