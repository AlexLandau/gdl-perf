The README.md file should contain instructions for using the framework in general. This file contains instructions
for any necessary setup for individual engines, e.g. any extra software that needs to be installed.

Note that it is __not__ necessary to have all engines set up before using the framework; if an engine
cannot play a simple game, it is assumed to not be set up correctly and will be ignored.

This currently only contains instructions for setup on Ubuntu.

Players not listed do not need any additional setup.

PALAMEDES_JAVA_ECLIPSE
======================

This engine uses the ECLiPSe Prolog system, which must be installed on your machine. See the instructions below.

Also, it expects ECLiPSe to be installed in the directory `/usr/local/eclipse/`. If it is not, you can try creating a symlink at /usr/local/eclipse to point to your installation location.

FLUXPLAYER_PROLOG
=================

This engine uses the Eclipse Prolog system, which must be installed on your machine. It requires version 5.10 or higher.

This engine lives in a separate repository. This framework expects the following repository to be cloned somewhere on your machine: https://github.com/AlexLandau/fluxplayer-prolog-engine

Then you need to run "make" in that repository before running the tests. Eclipse Prolog must already be installed for this step. If it fails, you may need to adjust the line "ECLIPSE=eclipse" in Makefile to point to the eclipse executable location (if it is not on your PATH).

Then set the following two values in localConfig.prefs:

ECLIPSE_PROLOG = <the location of the "eclipse" executable for your machine's installation of ECLiPSe Prolog>
FLUXPLAYER_PROLOG_ENGINE = <the location of the fluxplayer-prolog-engine repository on your machine>

This engine does not support the experimental correctness testing feature.

Installing Eclipse Prolog from the command line
===============================================

(Work in progress)

Note: This has no relation to the Eclipse IDE commonly used for Java.

You can download and install ECLiPSe Prolog from http://eclipseclp.org/. Versions 5.10 and later should work with the engines supported. Download the binaries that will work with your operating system. You will need the eclipse_basic and eclipse_misc packages; unzip them into the same directory and then run the RUNME executable to install ECLiPSe.

Command-line only installation instructions: (Note that the URLs needed may have changed since these instructions were written.) Create a directory where you'd like Eclipse Prolog to be installed (e.g. /usr/local/eclipse), then cd to that directory. Then run the following commands. These URLs are for installing the 64-bit Linux version of ECLiPSe 6.1.

```
wget https://sourceforge.net/projects/eclipse-clp/files/Binaries%20Linux%20x86_64/ECLiPSe%206.1%23194%20x86-64%20Linux%2064%20bit%20bin/eclipse_basic.tar/download
tar -xf download
rm download
wget https://sourceforge.net/projects/eclipse-clp/files/Binaries%20Linux%20x86_64/ECLiPSe%206.1%23194%20x86-64%20Linux%2064%20bit%20bin/eclipse_misc.tar/download
tar -xf download
rm download
./RUNME (following instructions; typically can just use defaults for everything)
```

Note: You do not need to specify the JVM location or add the eclipse executable to your PATH for any of the engines that require ECLiPSe. However, those engines do require some additional subsequent setup; see their entries above.
