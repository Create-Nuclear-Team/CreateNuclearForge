package net.nuclearteam.createnuclear.content.multiblock.alarm;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlockEntity;
import java.util.List;

public class ReactorAlarmEntity extends SmartBlockEntity {

    public ReactorControllerBlockEntity controller = null;

    public ReactorAlarmEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tick() {
        super.tick();

        if (level.isClientSide) {
            BlockState state = getBlockState();
            if (state.hasProperty(ReactorAlarm.POWERED) && state.getValue(ReactorAlarm.POWERED)) {
                // Si c'est allumé et que le son ne joue pas encore
                if (!ReactorAlarm.ACTIVE_SOUNDS.containsKey(worldPosition) ||
                        ReactorAlarm.ACTIVE_SOUNDS.get(worldPosition).isStopped()) {
                    ReactorAlarm.startSound(level, worldPosition);
                }
            } else {
                // Si c'est éteint, on s'assure que le son s'arrête
                ReactorAlarm.stopSound(worldPosition);
            }
        }
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) { }

    public void setController(ReactorControllerBlockEntity controller) {
        this.controller = controller;
    }
}