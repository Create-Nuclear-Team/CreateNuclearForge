package net.nuclearteam.createnuclear.content.effects.capability;

public interface IRadiationCapability {
    double getRadiation();
    void setRadiation(double value);

    long getInventoryHash();
    void setInventoryHash(long hash);
}
