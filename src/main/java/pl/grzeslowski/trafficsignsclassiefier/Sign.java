package pl.grzeslowski.trafficsignsclassiefier;

import org.jetbrains.annotations.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

public class Sign implements Comparable<Sign> {
    private final String type;
    private final String subType;

    public Sign(String type, String subType) {
        this.type = checkNotNull(type).toUpperCase();
        this.subType = checkNotNull(subType);
    }

    public String getType() {
        return type;
    }

    public String getSubType() {
        return subType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sign)) return false;

        Sign sign = (Sign) o;

        if (!type.equals(sign.type)) return false;
        return subType.equals(sign.subType);
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + subType.hashCode();
        return result;
    }

    public String signLabel() {
        return String.format("%s-%s", type, subType);
    }

    @Override
    public String toString() {
        return signLabel();
    }

    @Override
    public int compareTo(@NotNull Sign o) {
        return signLabel().compareTo(o.signLabel());
    }
}
