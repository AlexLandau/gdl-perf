package net.alloyggp.perf.engine;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.alloyggp.perf.CompatibilityResult;
import net.alloyggp.perf.PerfTest;
import net.alloyggp.perf.PerfTestConfig;
import net.alloyggp.perf.PerfTestResult;
import net.alloyggp.perf.game.GameKey;
import net.alloyggp.perf.game.RepoId;
import net.alloyggp.perf.io.LocalConfiguration.ConfigurationKey;
import net.alloyggp.perf.runner.CorrectnessTestProcess;
import net.alloyggp.perf.runner.JavaEngineType;
import net.alloyggp.perf.runner.PerfTestProcess;

public enum EngineType {
    GGP_BASE_PROVER(JavaEngineType.GGP_BASE_PROVER),
    PROVER_WITH_DISJUNCTIONS(JavaEngineType.PROVER_WITH_DISJUNCTIONS),
    GDL_CLEANER_TRANSFORMED_PROVER(JavaEngineType.GDL_CLEANER_TRANSFORMED_PROVER),
    DEORER_TRANSFORMED_PROVER(JavaEngineType.DEORER_TRANSFORMED_PROVER),
    CONDENSATION_ISOLATOR_TRANSFORMED_PROVER(JavaEngineType.CONDENSATION_ISOLATOR_TRANSFORMED_PROVER),
    VARIABLE_CONSTRAINER_TRANSFORMED_PROVER(JavaEngineType.VARIABLE_CONSTRAINER_TRANSFORMED_PROVER),
    CONJUNCT_DUALIZER_TRANSFORMED_PROVER(JavaEngineType.CONJUNCT_DUALIZER_TRANSFORMED_PROVER),
    RELATIONIZER_TRANSFORMED_PROVER(JavaEngineType.RELATIONIZER_TRANSFORMED_PROVER),
    DOMAIN_LIMITER_TRANSFORMED_PROVER(JavaEngineType.DOMAIN_LIMITER_TRANSFORMED_PROVER),
    REDUNDANCY_REMOVER_TRANSFORMED_PROVER(JavaEngineType.REDUNDANCY_REMOVER_TRANSFORMED_PROVER),
    ALLOY_TUPLE_PROVER(JavaEngineType.ALLOY_TUPLE_PROVER),
    ALLOY_COMPILED_PROVER_CACHING(JavaEngineType.ALLOY_COMPILED_PROVER_CACHING),
    ALLOY_COMPILED_PROVER_CACHING2(JavaEngineType.ALLOY_COMPILED_PROVER_CACHING2),
    ALLOY_DIFF_PROP_NET(JavaEngineType.ALLOY_DIFF_PROP_NET),
    ALLOY_PROPNET_64(JavaEngineType.ALLOY_PROPNET_64),
    ALLOY_FORWARD_PROP_NET(JavaEngineType.ALLOY_FORWARD_PROP_NET),
    ALLOY_FORWARD_PROP_NET2(JavaEngineType.ALLOY_FORWARD_PROP_NET2),
    ALLOY_FORWARD_PROP_NET3(JavaEngineType.ALLOY_FORWARD_PROP_NET3),
    ALLOY_FORWARD_PROP_NET4(JavaEngineType.ALLOY_FORWARD_PROP_NET4),
    //Jocular from Palamedes Core
    PALAMEDES_JOCULAR(JavaEngineType.PALAMEDES_JOCULAR),
    //Fluxplayer Prolog engine
    FLUXPLAYER_PROLOG(EngineEnvironment.createFluxplayer(),
            ExecutableType.RELATIVE_PATH,
            ImmutableList.of("start_perf_test.sh"),
            ImmutableList.of()), // no support for correctness testing
    //CadiaPlayer Prolog engine
    CADIAPLAYER_PROLOG(EngineEnvironment.createCadiaplayer(),
            ExecutableType.RELATIVE_PATH,
            ImmutableList.of("start_perf_test.sh"),
            ImmutableList.of()), // no support for correctness testing
    SANCHO_DEAD_RECKONING_PROPNET(JavaEngineType.SANCHO_DEAD_RECKONING_PROPNET),
    ;
    private final EngineEnvironment environment;
    private final ExecutableType executableType;
    private final ImmutableList<String> commandsForPerfTest;
    private final ImmutableList<String> commandsForCorrectnessTest;
    private final Optional<JavaEngineType> javaEngineType;

    private EngineType(EngineEnvironment environment, ExecutableType executableType,
            ImmutableList<String> commandsForPerfTest, ImmutableList<String> commandsForCorrectnessTest) {
        this.environment = environment;
        this.executableType = executableType;
        this.commandsForPerfTest = commandsForPerfTest;
        this.commandsForCorrectnessTest = commandsForCorrectnessTest;
        this.javaEngineType = Optional.empty();
    }

    private EngineType(JavaEngineType engineType) {
        this.environment = EngineEnvironment.createEmpty();
        this.executableType = ExecutableType.ABSOLUTE_PATH;
        this.commandsForPerfTest = getJavaPerfTestCommands(engineType);
        this.commandsForCorrectnessTest = getJavaCorrectnessTestCommands(engineType);
        this.javaEngineType = Optional.of(engineType);
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

        if (executableType == ExecutableType.RELATIVE_PATH) {
            //The first command in the list should be made relative to the working directory
            File workingDirectory = environment.getWorkingDirectory();
            File executable = new File(workingDirectory, commands.get(0));
            commands.set(0, executable.getAbsolutePath());
        }

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
        //TODO: When possible, switch to Java 9 and kill the process subtree.
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

    public Optional<JavaEngineType> getJavaEngineType() {
        return javaEngineType;
    }

    private static enum ExecutableType {
        /**
         * Specifies that the command should be treated as a relative path with
         * respect to the working directory that the EngineEnvironment specifies.
         */
        RELATIVE_PATH,
        /**
         * Specifies that the command should be treated as an absolute path.
         *
         * <p>This may be more convenient when working locally on a new engine type.
         */
        ABSOLUTE_PATH
    }
}
