package net.alloyggp.perf;

import java.io.File;
import java.util.Map;
import java.util.Set;

public interface EngineEnvironment {
    File getWorkingDirectory();

    Set<String> getUnconfiguredKeys();

    Map<String, String> getEnvironmentAdditions();
}
