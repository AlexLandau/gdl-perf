# gdl-perf

This is a framework for testing the performance of Game Description Language (GDL) interpreters and reasoners used in General Game Playing. It allows for automatically running tests on a wide variety of reasoners across a wide variety of games, with minimal human intervention. It also supplies tools for analyzing the outputs of these tests.

This was inspired by an article by Yngvi Björnsson and Stephan Schiffel in the [GIGA '13](http://giga13.ru.is/) proceedings that tested the performance of various such interpreters. It is designed so that tools from different programming languages can be tested. As a benefit of being open-source, programmers can add hooks for their own creations, or at least check that their code is being tested correctly. Having the results in a standardized format also means that analyses and visualizations can be created once, committed to the code base, and regenerated for future sets of results.

All games available on [ggp.org](http://games.ggp.org/) are used in testing. Game IDs include the originating repository, the key within the repository, and a hash of the game contents, so no two versions of a game will be conflated.

There is also some preliminary work on a tool for testing the correctness of reasoners, which the performance-testing framework does not check.

How to use
==========

Currently, before using the framework, you must provide a ggp-base.jar file. Check out a recent version of the ggp-base repository, run "./gradlew assemble" in the ggp-base folder, and copy ggp-base/build/libs/ggp-base.jar into gdl-perf/lib. (In the future this can be converted into a different kind of dependency.)

Before running tests, you must create a file named "computerName.txt" in the root of the repository. This should contain a name to indicate your computer; a recommended format is your GitHub username followed by a numeral identifying the specific computer. This makes it possible to commit results from your hardware without conflicting with other users' results. (This in turn makes it possible to commit raw results of testing without making the engine public, which some GGP competitors would like to avoid.)

Some engines require additional setup or configuration that is specific to your system (e.g. installing libraries or specifying program locations). These are explained in the SETUP.md file.

## Gradle commands

The following commands can be run via the Gradle wrapper script (replace 'gradlew' with 'gradlew.bat' if running on Windows):

```
./gradlew eclipse          | Generate files needed to import this project in the Eclipse IDE.
./gradlew idea             | Generate files needed to import this project in the IntelliJ IDE.
./gradlew runPerfSample    | Run a sample selection of perf tests. (Takes under an hour.)
./gradlew runPerfAll       | Run perf tests for all untested game/engine pairs. (Takes multiple days, but progress is recorded as it goes.)
./gradlew perfAnalysis     | Generate a set of web pages containing an analysis of all perf tests that have been run. This is placed in the "analyses" folder.
```

## Adding an engine

Users are encouraged to add their own engines. There are two approaches to adding an engine, one specific for Java-based engines and one that will work with any language.

### Java engines

Java engines can be added as an entry in the JavaEngineType class, which can then be used to construct the corresponding EngineType entry. Once an EngineType entry is available, the engine will be automatically tested by the MissingEntriesPerfTestRunner (a.k.a. runPerfAll).

The first step to accomplishing this is to add your code as a dependency of the gdl-perf project, so you can write . This can be done in a few ways. From easiest-to-understand to most complete:

* You can copy your code into the project directly. This is not suitable for submitting to the repository, and it will require you to re-copy your code whenever you change it, but this requires relatively little understanding of the build system. (In some cases, however, you may need to add dependencies that the original project used.)
* You can tell your IDE to have the gdl-perf project depend on your engine's project. If you do this, you'll have to run the performance testing from your IDE instead of from the command line.
  * In Eclipse, this involves right-clicking on the gdl-perf project -> Build Path -> Configure Build Path... -> Projects tab and clicking "Add..." to add the dependency on the other project.
* You can add a jar created from the engine's codebase to a subdirectory of lib/ and add an entry to the build.gradle's dependencies section to include the library, next to the other jars in there. This will allow you to use Gradle to run the code, and it's the bare minimum for contributing back to the repository. Note, however, that you may need to add additional dependencies to support the code.
  * If you are developing an engine within GGP-Base, you can just use that version of GGP-Base when creating the ggp-base.jar to use locally. Otherwise, you may have to rename the packages in the project (there are tools like jarjar that can do this automatically) to avoid conflicts with the existing GGP-Base project.
* You can publish the jar with your code on Maven Central and add it as a non-file-based Gradle dependency. (Please use a fixed version, not a '+' version.) This is the best option for adding an engine to the repository, if possible.

Now that you can reference your engine in the gdl-perf code, you'll need to create three things: a JavaSimulatorWrapper implementation for your engine, a JavaEngineType entry, and an EngineType entry.

Make a Runnables class that contains a method returning a JavaSimulatorWrapper backed by your engine. Select types for the Engine, State, Role, and Move generics that suit your engine. This should expose all the functionality of your engine in a uniform way. See StateMachineRunnables, GameSimulatorRunnables, JavaSimulatorRunnables, and similarly-named classes for examples.

Then, add an entry to the JavaEngineType enum that has as its arguments a version string to identify the version of the enum and an instance of the wrapper.

Finally, add an EngineType entry with the JavaEngineType as an argument.

At this point, you can start testing your engine. Try adding it to the list of engine types in the SamplePerfTestRunner as a starting point.

### Adding an engine in a language-agnostic way

To test an engine in a non-Java language, you'll need to write an executable or script that adheres to the rules described in the "Performance testing" section below. Generally, it's best to put that script in the code repository for the engine being tested, not gdl-perf itself.

Within the repository, you'll need to create an EngineEnvironment and an EngineType entry.

- The EngineEnvironment specifies the working directory that the test should be run from (if relevant) and what values should be picked up from the localConfig.prefs files and set as environment variables. For example, the FLUXPLAYER_PROLOG engine uses one configuration setting to set the working directory (the location of the Fluxplayer codebase), and another to set an environment variable used by the perf-test script (the location of the ECLiPSe executable on the user's file system).
- The EngineType specifies what EngineEnvironment to use, what command to run to start a performance test process for the engine, and what to use as the working directory for that process.

Once the EngineType entry has been added, you can start testing your engine. Try adding it to the list of engine types in the SamplePerfTestRunner as a starting point.

Performance testing
===================

Performance tests are run in their own process, one per game. Interactions with the framework use the command line and files written in a standard format. This has two advantages:

1. The test framework can be used to run and test engines in different languages, including those which are not JVM-based.
2. Instability in the engine, whether in the form of crashes, infinite loops, or otherwise, will affect neither the test framework nor subsequent tests. The test process will be killed if it takes an excessive amount of time, and then the framework can move on.

## Performance test process specification

The following command line arguments are given to the test process:

1. The location of the GDL file containing that game to be played, in the form of an absolute local-system file path.
2. The location of the file to which results are to be recorded, in the form of an absolute local-system file path.
3. The target number of seconds to run the test.

The command line to run is specified in the EngineType enum entry for the engine. Note that additional arguments may be used in the command line; these will _precede_ the other arguments. For example, the implementation for Java-based engines (PerfTestProcess) takes an initial argument specifying the ID of the engine to be tested.

The test process should run the performance test by repeatedly getting the initial state; checking if the state is terminal; if not, getting the available legal moves, picking random moves for each player, and using them to get the next state; repeating this process until a terminal state is reached; and computing the goal values for each player in this state. (The initial state may optionally be generated only once.) The Java implementation of this is in JavaPerfTestRunnable.

Results are expected to be written by the test process in the following format:

1. Each line consists of a name specifying the variable type, followed by an equals sign, followed by the value of the variable. (Whitespace at the beginning or end of the variable name or value is optional and ignored.)
2. Newlines may be either \n or \r\n; either can be parsed correctly.

The variable types used in the results for a successful performance test are as follows:

* version: The version of the engine being tested. This may be an arbitrary string.
* millisecondsTaken: The actual length, in milliseconds, of the testing that was performed.
* numStateChanges: The total number of transitions from one state to a following state that were computed.
* numRollouts: The total number of times that the test process reached a terminal state.

An unsuccessful performance test may leave these variables to indicate the nature of the error:

* version: The version of the engine being tested. This may be an arbitrary string.
* errorMessage: A message indicating the nature of the test error.

## Results format

Each completed perf test appends a line to a file within the results directory. Each line is a series of entries delimited by semicolons.

The format of results is not yet documented.

Correctness testing
===================

This is not yet documented.
