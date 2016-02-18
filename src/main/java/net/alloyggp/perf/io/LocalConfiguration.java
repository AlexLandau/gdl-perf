package net.alloyggp.perf.io;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

public class LocalConfiguration {
    public static enum ConfigurationKey {
        ECLIPSE_PROLOG("The 'eclipse' executable for this platform from an installation "
                + "of the ECLiPSe Prolog environment. This is available at http://eclipseclp.org/. "
                + "(Note that this is unrelated to the commonly-used Eclipse IDE.)"),
        FLUXPLAYER_PROLOG_ENGINE("The base directory of the fluxplayer-prolog-engine repository, "
                + "available at https://github.com/AlexLandau/fluxplayer-prolog-engine"),
        ;
        private final String description;

        private ConfigurationKey(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public RuntimeException throwBecauseNotSet() {
            throw new RuntimeException("The expected environment variable "
                    + toString() + " was not set. This should contain: "
                    + description);
        }

        public static @Nullable ConfigurationKey parse(String keyString) {
            for (ConfigurationKey key : values()) {
                if (key.toString().equals(keyString)) {
                    return key;
                }
            }
            return null;
        }
    }

    public static Map<ConfigurationKey, String> loadConfiguration() {
        Map<String, String> stringMap = ResultFiles.read(getConfigurationFile());
        Map<ConfigurationKey, String> configMap = Maps.newHashMap();
        for (Entry<String, String> entry : stringMap.entrySet()) {
            @Nullable ConfigurationKey key = ConfigurationKey.parse(entry.getKey());
            if (key != null) {
                configMap.put(key, entry.getValue());
            } else {
                System.out.println("Warning: Unrecognized key " + entry.getKey() + " in localConfig.prefs.");
            }
        }
        return configMap;
    }

    private static File getConfigurationFile() {
        File file = new File("localConfig.prefs");
        if (!file.isFile()) {
            System.out.println("Creating localConfig.prefs file");
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("Tried to create a localConfig.prefs file, but could not.");
            }
        }
        return file;
    }

    public static Optional<String> loadValue(ConfigurationKey key) {
        Map<ConfigurationKey, String> config = loadConfiguration();
        if (config.containsKey(key)) {
            return Optional.of(config.get(key));
        } else {
            return Optional.empty();
        }
    }
}
