package net.alloyggp.perf;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.alloyggp.perf.io.LocalConfiguration.ConfigurationKey;
import net.alloyggp.perf.runner.CorrectnessTestProcess;
import net.alloyggp.perf.runner.JavaEngineType;
import net.alloyggp.perf.runner.PerfTestProcess;

public enum EngineType {
    GGP_BASE_PROVER(JavaEngineType.GGP_BASE_PROVER),
    //Palamedes BasicPlayer Java prover (linked from Dresden GGP page)
    PALAMEDES_JAVA_PROVER_USEOPT_FALSE(JavaEngineType.PALAMEDES_JAVA_PROVER_USEOPT_FALSE),
    PALAMEDES_JAVA_PROVER_USEOPT_TRUE(JavaEngineType.PALAMEDES_JAVA_PROVER_USEOPT_TRUE),
    //And others in Palamedes Core
    PALAMEDES_JOCULAR(JavaEngineType.PALAMEDES_JOCULAR),
    PALAMEDES_JAVA_ECLIPSE(JavaEngineType.PALAMEDES_JAVA_ECLIPSE),
    //Fluxplayer Prolog engine
    FLUXPLAYER_PROLOG(ConfiguredEngineEnvironment.createFluxplayer(),
            //new File("../fluxplayer-prolog-engine/"),
            //TODO: If we're changing the working directory, do we still need the directory change here? Check
            ImmutableList.of("../fluxplayer-prolog-engine/start_perf_test.sh"),
            ImmutableList.of()), // no support for correctness testing
    //From Peter Pham's Rekkura codebase
    REKKURA_GENERIC_FORWARD_PROVER_OSTD(JavaEngineType.REKKURA_GENERIC_FORWARD_PROVER_OSTD),
    REKKURA_GENERIC_FORWARD_PROVER(JavaEngineType.REKKURA_GENERIC_FORWARD_PROVER),
    //Note: These two are disabled because they appear to be strictly inferior to
    //their non-GENERIC versions.
//    REKKURA_GENERIC_BACKWARD_PROVER_OSTD(JavaEngineType.REKKURA_GENERIC_BACKWARD_PROVER_OSTD),
//    REKKURA_GENERIC_BACKWARD_PROVER(JavaEngineType.REKKURA_GENERIC_BACKWARD_PROVER),
    REKKURA_BACKWARD_PROVER_OSTD(JavaEngineType.REKKURA_BACKWARD_PROVER_OSTD),
    REKKURA_BACKWARD_PROVER(JavaEngineType.REKKURA_BACKWARD_PROVER),
    SANCHO_DEAD_RECKONING_PROPNET(JavaEngineType.SANCHO_DEAD_RECKONING_PROPNET),
    ;
    private final EngineEnvironment environment;
    private final ImmutableList<String> commandsForPerfTest;
    private final ImmutableList<String> commandsForCorrectnessTest;

    private EngineType(EngineEnvironment environment, List<String> commandsForPerfTest, List<String> commandsForCorrectnessTest) {
        this.environment = environment;
        this.commandsForPerfTest = ImmutableList.copyOf(commandsForPerfTest);
        this.commandsForCorrectnessTest = ImmutableList.copyOf(commandsForCorrectnessTest);
    }

    private EngineType(JavaEngineType engineType) {
        this.environment = ConfiguredEngineEnvironment.createEmpty();
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
        if (!environment.getUnconfiguredKeys().isEmpty()) {
            throw new RuntimeException(getUnconfiguredKeysExplanation());
        }

        List<String> commands = Lists.newArrayList();
        commands.addAll(getCommandsForPerfTest());
        commands.add(perfTestConfig.getGameFile().getAbsolutePath());
        commands.add(perfTestConfig.getOutputFile().getAbsolutePath());
        commands.add(Integer.toString(perfTestConfig.getNumSeconds()));
        ProcessBuilder pb = new ProcessBuilder(commands);
        pb.directory(environment.getWorkingDirectory());
        pb.environment().putAll(environment.getEnvironmentAdditions());

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
        return commandsForPerfTest;
    }

    public List<String> getCommandsForCorrectnessTest() {
        return commandsForCorrectnessTest;
    }

    /**
     * Runs a compatibility test that checks if the engine is configured
     * sufficiently correctly on this computer to give results for a
     * simple game.
     *
     * If this fails, it often means needed components or libraries need to
     * be installed on the computer in order for this engine to run (e.g.
     * a Prolog environment).
     */
    public CompatibilityResult runCompatibilityTest() throws IOException, InterruptedException {
        if (!environment.getUnconfiguredKeys().isEmpty()) {
            System.out.println(getUnconfiguredKeysExplanation());
            return CompatibilityResult.createFailure();
        }
        PerfTestResult result = PerfTest.runTest(
                GameKey.create(RepoId.BASE, "ticTacToe"),
                this,
                "unknownVersion",
                3, //test length in seconds
                60); //seconds before cancelling

        if (!result.wasSuccessful()) {
            System.out.println("Error from compatibility test: " + result.getErrorMessage());
            return CompatibilityResult.createFailure();
        }
        return CompatibilityResult.createSuccess(result.getEngineVersion().getVersion());
    }

    private String getUnconfiguredKeysExplanation() {
        StringBuilder message = new StringBuilder();
        message.append("Engine " + toString() + " requires the following values to be set in localConfig.prefs:\n");
        message.append("(Values are set in 'KEY = value' format, with one entry per line.)\n");
        for (ConfigurationKey key : environment.getUnconfiguredKeys()) {
            message.append(key.toString() + ": " + key.getDescription() + "\n");
        }
        return message.toString();
    }

    public EngineVersion getWithVersion(String version) {
        return EngineVersion.create(this, version);
    }
}
