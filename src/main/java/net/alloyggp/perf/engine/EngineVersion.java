package net.alloyggp.perf.engine;

import javax.annotation.concurrent.Immutable;

@Immutable
public class EngineVersion implements Comparable<EngineVersion> {
    private final EngineType type;
    private final String version;

    private EngineVersion(EngineType type, String version) {
        this.type = type;
        this.version = version;
    }

    public static EngineVersion create(EngineType engineType, String version) {
        return new EngineVersion(engineType, version);
    }

    public static EngineVersion parse(String type, String version) {
        return new EngineVersion(EngineType.valueOf(type), version);
    }

    public EngineType getType() {
        return type;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EngineVersion other = (EngineVersion) obj;
        if (type != other.type)
            return false;
        if (version == null) {
            if (other.version != null)
                return false;
        } else if (!version.equals(other.version))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return type + ":" + version;
    }

    @Override
    public int compareTo(EngineVersion o) {
        int comparison = this.type.toString().compareTo(o.type.toString());
        if (comparison != 0) {
            return comparison;
        }
        return this.version.compareTo(o.version);
    }

}
