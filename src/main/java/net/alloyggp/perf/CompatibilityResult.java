package net.alloyggp.perf;

import com.google.common.base.Preconditions;

public class CompatibilityResult {
    private final boolean compatible;
    private final String version;

    private CompatibilityResult(boolean compatible, String version) {
        if (compatible) {
            Preconditions.checkNotNull(version);
        }
        this.compatible = compatible;
        this.version = version;
    }

    public static CompatibilityResult createSuccess(String version) {
        return new CompatibilityResult(true, version);
    }

    public static CompatibilityResult createFailure() {
        return new CompatibilityResult(false, null);
    }

    public boolean isCompatible() {
        return compatible;
    }

    public String getVersion() {
        Preconditions.checkState(compatible);
        return version;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (compatible ? 1231 : 1237);
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CompatibilityResult other = (CompatibilityResult) obj;
        if (compatible != other.compatible) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CompatibilityResult [compatible=" + compatible + ", version=" + version + "]";
    }
}
