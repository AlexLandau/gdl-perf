package net.alloyggp.perf.engine;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.alloyggp.perf.io.LocalConfiguration;
import net.alloyggp.perf.io.LocalConfiguration.ConfigurationKey;

public class EngineEnvironment {
    //If present, use the directory given by that localConfig key as the working directory
    //If absent, just use the current working directory
    private final Optional<ConfigurationKey> workingDirectoryKey;
    //Map from localConfig key to the environment variable to set
    private final ImmutableMap<ConfigurationKey, String> environmentVariablesToSet;

    private EngineEnvironment(Optional<ConfigurationKey> workingDirectoryKey,
            ImmutableMap<ConfigurationKey, String> environmentVariablesToSet) {
        this.workingDirectoryKey = workingDirectoryKey;
        this.environmentVariablesToSet = environmentVariablesToSet;
    }

    /**
     * Creates an engine environment that uses the current working directory and
     * does not set any environment variables.
     */
    public static EngineEnvironment createEmpty() {
        return new EngineEnvironment(Optional.empty(),
                ImmutableMap.of());
    }

    public static EngineEnvironment createFluxplayer() {
        return new EngineEnvironment(Optional.of(ConfigurationKey.FLUXPLAYER_PROLOG_ENGINE),
                ImmutableMap.of(ConfigurationKey.ECLIPSE_PROLOG, "ECLIPSE"));
    }

    public static EngineEnvironment createCadiaplayer() {
        return new EngineEnvironment(Optional.of(ConfigurationKey.CADIAPLAYER_PROLOG_ENGINE),
                ImmutableMap.of());
    }

    public File getWorkingDirectory() {
        if (workingDirectoryKey.isPresent()) {
            Optional<String> setWorkingDirectory = LocalConfiguration.loadValue(workingDirectoryKey.get());
            if (!setWorkingDirectory.isPresent()) {
                throw workingDirectoryKey.get().throwBecauseNotSet();
            }
            return new File(setWorkingDirectory.get());
        } else {
            return new File(".");
        }
    }

    public Set<ConfigurationKey> getUnconfiguredKeys() {
        Set<ConfigurationKey> unconfiguredKeys = Sets.newHashSet();
        workingDirectoryKey.ifPresent(it -> unconfiguredKeys.add(it));
        unconfiguredKeys.addAll(environmentVariablesToSet.keySet());
        unconfiguredKeys.removeAll(LocalConfiguration.loadConfiguration().keySet());
        return unconfiguredKeys;
    }

    public Map<String, String> getEnvironmentAdditions() {
        Map<String, String> environmentAdditions = Maps.newHashMap();

        for (Entry<ConfigurationKey, String> entry : environmentVariablesToSet.entrySet()) {
            ConfigurationKey configKey = entry.getKey();
            Optional<String> value = LocalConfiguration.loadValue(configKey);
            if (!value.isPresent()) {
                throw configKey.throwBecauseNotSet();
            }
            environmentAdditions.put(entry.getValue(), value.get());
        }

        return environmentAdditions;
    }

}
