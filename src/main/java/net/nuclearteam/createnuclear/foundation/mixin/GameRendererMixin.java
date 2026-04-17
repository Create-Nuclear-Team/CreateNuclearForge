package net.nuclearteam.createnuclear.foundation.mixin;

import net.minecraft.client.renderer.GameRenderer;
import net.nuclearteam.createnuclear.CNClientProxy;
import net.nuclearteam.createnuclear.CreateNuclear;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Shadow
    private float darkenWorldAmount;

    // Cette partie gère l'assombrissement du ciel/monde lors de l'explosion
    @Inject(
            method = {"Lnet/minecraft/client/renderer/GameRenderer;tick()V"},
            remap = true,
            at = @At(value = "TAIL")
    )
    public void cn_tick(CallbackInfo ci) {
        // On utilise ton propre Proxy
        if (((CNClientProxy) CreateNuclear.PROXY).renderNukeSkyDarkFor > 0 && darkenWorldAmount < 1.0F) {
            darkenWorldAmount = Math.min(darkenWorldAmount + 0.3F, 1.0F);
        }
    }

    // Cette partie appelle le rendu du flash blanc (preScreenRender)
    @Inject(
            method = {"Lnet/minecraft/client/renderer/GameRenderer;render(FJZ)V"},
            remap = true,
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/platform/Lighting;setupFor3DItems()V",
                    shift = At.Shift.AFTER
            )
    )
    public void cn_render(float partialTick, long nanos, boolean idk, CallbackInfo ci) {
        ((CNClientProxy) CreateNuclear.PROXY).preScreenRender(partialTick);
    }
}