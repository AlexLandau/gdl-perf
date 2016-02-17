package net.alloyggp.perf;

import java.io.File;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

//TODO: Unhack this class
public class FluxplayerEngineEnvironment implements EngineEnvironment {

    public static FluxplayerEngineEnvironment create() {
        return new FluxplayerEngineEnvironment();
    }

    @Override
    public File getWorkingDirectory() {
        return new File("../fluxplayer-prolog-engine/");
    }

    @Override
    public Set<String> getUnconfiguredKeys() {
        return ImmutableSet.of();
    }

    @Override
    public Map<String, String> getEnvironmentAdditions() {
        return ImmutableMap.of("ECLIPSE", "/home/alex/prologEclipse/bin/x86_64_linux/eclipse");
    }

}
