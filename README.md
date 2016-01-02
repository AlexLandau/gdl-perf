This isn't ready for primetime yet! Check back later.

Currently, before using the framework, you must provide a ggp-base.jar file. Check out a recent version of the
ggp-base repository, run "./gradlew assemble" in the ggp-base folder, and copy ggp-base/build/libs/ggp-base.jar
into gdl-perf/lib. (In the future this can be converted into a different kind of dependency.)

TODO: Store game versions

TODO: Add Sancho engine

This is a tool for testing the performance and correctness of interpreters of GDL game rules. It was inspired by an article by Yngvi Björnsson and Stephan Schiffel in the [GIGA '13](http://giga13.ru.is/) proceedings that tested the performance of various such interpreters. It is designed so that tools from different programming languages can be tested. As a benefit of being open-source, programmers can add hooks for their own creations, or at least check that their code is being tested correctly. Having the results in a standardized format also means that analyses and visualizations can be created once, committed to the code base, and used with future results.

Performance tests are run in their own process, one per game. Interactions with the framework use the command line and files written in a standard format. This has two advantages:

1. The test framework can be used to run and test engines in different languages, including those which are not JVM-based.
2. Instability in the engine, whether in the form of crashes, infinite loops, or otherwise, will affect neither the test framework nor subsequent tests. The test process will be killed if it takes an excessive amount of time, and then the framework can move on.

The following command line arguments are given to the test process:

1. The location of the GDL file containing that game to be played, in the form of an absolute local-system file path.
2. The location of the file to which results are to be recorded, in the form of an absolute local-system file path.
3. The target number of seconds to run the test.

The test process should run the performance test by repeatedly getting the initial state; checking if the state is terminal; if not, getting the available legal moves, picking random moves for each player, and using them to get the next state; repeating this process until a terminal state is reached; and computing the goal values for each player in this state. (The initial state may optionally be generated only once.) A sample implementation based on the GGP-Base StateMachine abstraction is in JavaEngineType#getStateMachinePerfTestRunnable.

Results are expected to be written by the test process in the following format:

1. Each line consists of a name specifying the variable type, followed by an equals sign, followed by the value of the variable. (Whitespace at the beginning or end of the variable name or value is optional and ignored.)
2. Newlines may be either \n or \r\n; either can be parsed correctly.

The variable types used in the results for a successful performance test are as follows:

* millisecondsTaken: The actual length, in milliseconds, of the testing that was performed.
* numStateChanges: The total number of transitions from one state to a following state that were computed.
* numRollouts: The total number of times that the test process reached a terminal state.

An unsuccessful performance test may leave one variable to indicate the nature of the error:

* errorMessage: A message indicating the nature of the test error.

TODO: Add expected interface for correctness tests, which will work differently

Terminology notes

Result classes are used by the main coordinator process and include information on the engine type and the game key. Report classes look similar, but are used internally by the short-lived test processes and don't include the engine type or game key. 
