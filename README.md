This is currently mostly usable but some more cleanup is in the works.

This is a framework for testing the performance of Game Description Language (GDL) interpreters and reasoners used in General Game Playing. It allows for automatically running tests on a wide variety of reasoners across a wide variety of games, with minimal human intervention. It interacts with reasoners at the process level, so it works across programming language. It also supplies tools for analyzing the outputs of these tests.

There is also some preliminary work on a tool for testing the correctness of reasoners.

How to use
==========

Currently, before using the framework, you must provide a ggp-base.jar file. Check out a recent version of the ggp-base repository, run "./gradlew assemble" in the ggp-base folder, and copy ggp-base/build/libs/ggp-base.jar into gdl-perf/lib. (In the future this can be converted into a different kind of dependency.)

Before running tests, you must create a file named "computerName.txt" in the root of the repository. This should contain a name to indicate your computer; a recommended format is your GitHub username followed by a numeral identifying the specific computer. This makes it possible to commit results from your hardware without conflicting with other users' results. (This in turn makes it possible to commit raw results of testing without making the engine public, which some GGP competitors would like to avoid.)

## Gradle commands

The following commands can be run via the Gradle wrapper script (replace 'gradlew' with 'gradlew.bat' if running on Windows):

```
./gradlew runPerfSample    | Run a sample selection of perf tests. (Takes under an hour.)
./gradlew runPerfAll       | Run perf tests for all untested game/engine pairs. (Takes multiple days, but progress is recorded as it goes.)
./gradlew perfAnalysis     | Generate a set of web pages containing analysis of all perf tests that have been run.
```

This is a tool for testing the performance and correctness of interpreters of GDL game rules. It was inspired by an article by Yngvi Bj�rnsson and Stephan Schiffel in the [GIGA '13](http://giga13.ru.is/) proceedings that tested the performance of various such interpreters. It is designed so that tools from different programming languages can be tested. As a benefit of being open-source, programmers can add hooks for their own creations, or at least check that their code is being tested correctly. Having the results in a standardized format also means that analyses and visualizations can be created once, committed to the code base, and used with future results.

All games available on [ggp.org](http://games.ggp.org/) are used in testing. Games IDs include the originating repository, the key within the repository, and a hash of the game contents.

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

TODO: Document exact format

Correctness test process specification
======================================

TODO: Add expected interface for correctness tests, which will work differently
