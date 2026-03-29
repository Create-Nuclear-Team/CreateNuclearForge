package net.nuclearteam.createnuclear.foundation.data.radiation;

public record ClientRadiationData(double radiation) {
    public ClientRadiationData() {
        this(0);
    }
}
