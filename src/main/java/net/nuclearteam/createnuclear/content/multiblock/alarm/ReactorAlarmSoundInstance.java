package net.nuclearteam.createnuclear.content.multiblock.alarm;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.nuclearteam.createnuclear.CreateNuclear;

public class ReactorAlarmSoundInstance extends AbstractTickableSoundInstance {
    private final BlockPos pos;
    private final Level level;

    public ReactorAlarmSoundInstance(Level level, BlockPos pos, SoundEvent sound) {
        super(sound, SoundSource.BLOCKS, level != null ? level.random : null);
        this.level = level;
        this.pos = pos;
        if (pos != null) {
            this.x = pos.getX();
            this.y = pos.getY();
            this.z = pos.getZ();
        }
        this.looping = true;
        this.delay = 0;
        this.volume = 10F;
    }

    @Override
    public void tick() {
        try {
            if (level == null || pos == null) {
                stop();
                return;
            }

            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof ReactorAlarm)) {
                stop();
            }
        } catch (Exception e) {
            stop();
            CreateNuclear.LOGGER.warn(e.getMessage());
        }
    }

    public void fadeOut() {
        stop();
    }
}