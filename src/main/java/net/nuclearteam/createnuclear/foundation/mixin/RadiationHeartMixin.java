package net.nuclearteam.createnuclear.foundation.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.nuclearteam.createnuclear.CNEffects;
import net.nuclearteam.createnuclear.CreateNuclear;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

// Gui.class is the target because that's where renderHeart is defined
@Mixin(Gui.class)
public class RadiationHeartMixin {

    private static final ResourceLocation RADIATION_ICONS = CreateNuclear.asResource("textures/gui/icons.png");
    private static final ResourceLocation VANILLA_ICONS = new ResourceLocation("minecraft", "textures/gui/icons.png");

    @ModifyArg(
            method = "renderHeart", // The single-heart method that actually draws each heart icon
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"
            ),
            index = 0
    )
    private ResourceLocation createnuclear$changeHeartTexture(ResourceLocation originalTexture) {
        if (originalTexture.equals(VANILLA_ICONS)) {
            Player player = Minecraft.getInstance().player;

            // Swap to the radiation heart texture while the player has the radiation effect
            if (player != null && player.hasEffect(CNEffects.RADIATION.get())) {
                return RADIATION_ICONS;
            }
        }
        return originalTexture;
    }
}