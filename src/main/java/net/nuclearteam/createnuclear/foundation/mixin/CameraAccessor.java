package net.nuclearteam.createnuclear.foundation.mixin; // Ton package mixin

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Camera.class)
public interface CameraAccessor {
    // L'invoker permet de "débloquer" l'accès à la méthode move
    @Invoker("move")
    void callMove(double distanceOffset, double verticalOffset, double horizontalOffset);
}