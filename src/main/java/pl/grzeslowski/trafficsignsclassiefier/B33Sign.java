package pl.grzeslowski.trafficsignsclassiefier;

import static com.google.common.base.Preconditions.checkArgument;

public class B33Sign extends Sign {
    private final int speed;

    public B33Sign(String type, String subType, int speed) {
        super(type, subType);
        checkArgument(speed > 0, "Speed = " + speed);
        this.speed = speed;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        final boolean equals = super.equals(o);
        if(equals) {
            return (o instanceof B33Sign) && speed == ((B33Sign) o).speed;
        } else {
            return false;
        }
    }

    @Override
    public String signLabel() {
        return super.signLabel() + ";" + speed + "km/h";
    }
}
