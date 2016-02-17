package net.alloyggp.perf;

import java.io.File;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * An engine environment that runs from the current working directory
 * and requires no environment variables.
 */
public class BasicEngineEnvironment implements EngineEnvironment {
    public static final BasicEngineEnvironment INSTANCE = new BasicEngineEnvironment();

    private BasicEngineEnvironment() {
        // Use INSTANCE
    }

    @Override
    public File getWorkingDirectory() {
        return new File(".");
    }

    @Override
    public Set<String> getUnconfiguredKeys() {
        return ImmutableSet.of();
    }

    @Override
    public Map<String, String> getEnvironmentAdditions() {
        return ImmutableMap.of();
    }
}
