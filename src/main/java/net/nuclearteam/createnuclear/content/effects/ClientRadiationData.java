package net.nuclearteam.createnuclear.content.effects;

public record ClientRadiationData(double radiation) {
    public ClientRadiationData() {
        this(0);
    }
}
