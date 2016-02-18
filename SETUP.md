The README.md file should contain instructions for using the framework in general. This file contains instructions
for any necessary setup for individual engines, e.g. any extra software that needs to be installed.

Note that it is __not__ necessary to have all engines set up before using the framework; if an engine
cannot play a simple game, it is assumed to not be set up correctly and will be ignored.

This currently only contains instructions for setup on Ubuntu.

Players not listed do not need any additional setup.

PALAMEDES_JAVA_ECLIPSE
======================

This engine uses the Eclipse Prolog system, which must be installed on your machine. The version of "eclipse" in
your PATH must point to that. (I neglected to record step-by-step instructions for this when first setting it up.)

FLUXPLAYER_PROLOG
=================

This engine uses the Eclipse Prolog system, which must be installed on your machine. 

This engine lives in a separate repository. This framework expects the following repository to be cloned in a directory adjacent to the gdl-perf directory on your machine: https://github.com/AlexLandau/fluxplayer-prolog-engine

Then you need to run "make" in that repository before running the tests.

This engine does not support the experimental correctness testing feature.

TODO: Make this and JavaEclipse work correctly without having to futz with your PATH. Also possibly have the user just provide a directory where fluxplayer-prolog-engine lives.