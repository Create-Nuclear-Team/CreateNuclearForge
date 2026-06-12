package net.nuclearteam.createnuclear.content.multiblock.alarm;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.nuclearteam.createnuclear.CNSoundEvents;
import net.nuclearteam.createnuclear.CreateNuclear;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.nuclearteam.createnuclear.foundation.advancement.CNAdvancement;
import net.nuclearteam.createnuclear.foundation.advancement.CNAdvancementBehaviour;

import java.util.List;

public class ReactorAlarmEntity extends SmartBlockEntity {

    public ReactorControllerBlockEntity controller = null;
    private CNAdvancementBehaviour advancement;
    private boolean advancementAwarded = false; // Anti-spam réseau

    @OnlyIn(Dist.CLIENT)
    protected ReactorAlarmSoundInstance soundInstance;

    public ReactorAlarmEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || !(getBlockState().getBlock() instanceof ReactorAlarm)) return;

        if (level.isClientSide) {
            tickAudio(); // Plus besoin de DistExecutor ici, le check de level suffit et évite le lag de thread
        } else {
            tickServer();
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected void tickAudio() {
        BlockState state = getBlockState();
        boolean powered = state.hasProperty(ReactorAlarm.POWERED) && state.getValue(ReactorAlarm.POWERED);

        if (!powered) {
            stopSound();
            return;
        }

        if (soundInstance == null || soundInstance.isStopped()) {
            Minecraft minecraft = Minecraft.getInstance();
            try {
                soundInstance = new ReactorAlarmSoundInstance(level, worldPosition, CNSoundEvents.REACTOR_ALARM_2.getMainEvent());
                minecraft.getSoundManager().play(soundInstance);
            } catch (Exception e) {
                CreateNuclear.LOGGER.warn("Échec du lancement du son d'alarme: " + e.getMessage());
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void stopSound() {
        if (soundInstance != null) {
            try {
                soundInstance.fadeOut();
            } catch (Exception e) {
                CreateNuclear.LOGGER.warn("Erreur lors de l'arrêt du son : " + e.getMessage());
            }
            soundInstance = null;
        }
    }

    private void tickServer() {
        BlockState state = getBlockState();
        if (!(state.getBlock() instanceof ReactorAlarm)) return;

        boolean powered = state.getValue(ReactorAlarm.POWERED);

        if (powered) {
            if (!advancementAwarded) { // On l'envoie UNE SEULE FOIS au front montant
                this.advancement.awardPlayer(CNAdvancement.SILENCE_THE_CORE);
                advancementAwarded = true;
            }
        } else {
            advancementAwarded = false; // Reset quand l'alarme s'éteint
        }
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(advancement = new CNAdvancementBehaviour(this, CNAdvancement.SILENCE_THE_CORE));
    }

    public void setController(ReactorControllerBlockEntity controller) {
        this.controller = controller;
    }
}