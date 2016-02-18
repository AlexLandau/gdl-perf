package net.alloyggp.perf;

import java.io.File;
import java.util.Map;
import java.util.Set;

import net.alloyggp.perf.io.LocalConfiguration.ConfigurationKey;

public interface EngineEnvironment {
    File getWorkingDirectory();

    Set<ConfigurationKey> getUnconfiguredKeys();

    Map<String, String> getEnvironmentAdditions();
}
