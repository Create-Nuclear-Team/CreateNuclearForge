package net.nuclearteam.createnuclear;

import net.minecraft.client.Minecraft;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.nuclearteam.createnuclear.content.equipment.armor.AntiRadiationArmorItem;
import net.nuclearteam.createnuclear.foundation.mixin.CameraAccessor;
import net.nuclearteam.createnuclear.infrastructure.config.CNConfigs;

@Mod.EventBusSubscriber(modid = CreateNuclear.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {

    /**
     * GESTION DU TICK (LOGIQUE)
     * Ce code s'exécute 20 fois par seconde pour faire descendre les compteurs.
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {

            // On enregistre l'ancienne valeur pour l'interpolation fluide (Flash)
            CNClientProxy.prevNukeFlashAmount = CNClientProxy.nukeFlashAmount;

            // Gestion du décompte de l'obscurité du ciel
            if (CNClientProxy.renderNukeSkyDarkFor > 0) {
                CNClientProxy.renderNukeSkyDarkFor--;
            }

            // Gestion de la puissance du Flash blanc
            if (CNClientProxy.renderNukeFlashFor > 0) {
                if (CNClientProxy.nukeFlashAmount < 1F) {
                    CNClientProxy.nukeFlashAmount = Math.min(CNClientProxy.nukeFlashAmount + 0.4F, 1F);
                }
                CNClientProxy.renderNukeFlashFor--;
            } else if (CNClientProxy.nukeFlashAmount > 0F) {
                // Le flash s'estompe progressivement
                CNClientProxy.nukeFlashAmount = Math.max(CNClientProxy.nukeFlashAmount - 0.05F, 0F);
            }
        }
    }

    /**
     * GESTION DU SCREEN SHAKE (TREMBLEMENT DE CAMÉRA)
     * Ce code fait vibrer la vue du joueur pendant l'explosion.
     */
    @SubscribeEvent
    public static void computeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Entity player = Minecraft.getInstance().getCameraEntity();
        float partialTick = Minecraft.getInstance().getPartialTick();

        // On définit la puissance du tremblement.
        // Si le ciel est sombre (nuke active), on fait trembler à 1.5F.
        float tremorAmount = CNClientProxy.renderNukeSkyDarkFor > 0 ? 1.5F : 0F;

        if (player != null && CNConfigs.client().screenShaking.get()) {
            if (tremorAmount > 0) {
                // On génère des offsets aléatoires pour la vibration
                if (CNClientProxy.lastTremorTick != player.tickCount) {
                    RandomSource rng = player.level().random;
                    CNClientProxy.randomTremorOffsets[0] = rng.nextFloat();
                    CNClientProxy.randomTremorOffsets[1] = rng.nextFloat();
                    CNClientProxy.randomTremorOffsets[2] = rng.nextFloat();
                    CNClientProxy.lastTremorTick = player.tickCount;
                }

                // Intensité basée sur les réglages d'accessibilité de Minecraft
                double intensity = tremorAmount * Minecraft.getInstance().options.screenEffectScale().get();

                // On déplace physiquement la caméra
                ((CameraAccessor) event.getCamera()).callMove(
                        CNClientProxy.randomTremorOffsets[0] * 0.2F * intensity,
                        CNClientProxy.randomTremorOffsets[1] * 0.2F * intensity,
                        CNClientProxy.randomTremorOffsets[2] * 0.5F * intensity
                );
            }
        }
    }

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        PlayerModel<?> model = event.getRenderer().getModel();

        if (player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof AntiRadiationArmorItem) {
            model.hat.visible = false;
        }
        if (player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof AntiRadiationArmorItem) {
            model.jacket.visible = false;
            model.rightSleeve.visible = false;
            model.leftSleeve.visible = false;
        }
        if (player.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof AntiRadiationArmorItem) {
            model.rightPants.visible = false;
            model.leftPants.visible = false;
        }
    }
}