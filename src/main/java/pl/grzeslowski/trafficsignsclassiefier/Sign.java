package pl.grzeslowski.trafficsignsclassiefier;

import static com.google.common.base.Preconditions.checkNotNull;

public class Sign {
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

    @Override
    public String toString() {
        return String.format("%s-%s", type, subType);
    }
}
