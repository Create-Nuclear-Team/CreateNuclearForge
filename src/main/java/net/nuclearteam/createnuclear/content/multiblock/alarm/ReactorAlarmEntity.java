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
import net.minecraftforge.fml.DistExecutor;

import java.util.List;

public class ReactorAlarmEntity extends SmartBlockEntity {

    public ReactorControllerBlockEntity controller = null;

    @OnlyIn(Dist.CLIENT)
    protected ReactorAlarmSoundInstance soundInstance;

    public ReactorAlarmEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || !(getBlockState().getBlock() instanceof ReactorAlarm)) return;

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            try {
                tickAudio();
            } catch (Exception e) {
                // Log l'erreur si nécessaire, mais évite les crashes
                CreateNuclear.LOGGER.warn(e.getMessage());
            }
        });
    }

    @OnlyIn(Dist.CLIENT)
    protected void tickAudio() {
        if (level == null) return;

        BlockState state = getBlockState();

        boolean powered = state.hasProperty(ReactorAlarm.POWERED) && state.getValue(ReactorAlarm.POWERED);

        if (!powered) {
            stopSound();
            return;
        }

        if (soundInstance == null || soundInstance.isStopped()) {
            Minecraft minecraft = Minecraft.getInstance();
            try {
                minecraft.getSoundManager().play(soundInstance = new ReactorAlarmSoundInstance(level, worldPosition, CNSoundEvents.REACTOR_ALARM.getMainEvent()));
            } catch (Exception e) {
                CreateNuclear.LOGGER.warn(e.getMessage());
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

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

    public void setController(ReactorControllerBlockEntity controller) {
        this.controller = controller;
    }
}