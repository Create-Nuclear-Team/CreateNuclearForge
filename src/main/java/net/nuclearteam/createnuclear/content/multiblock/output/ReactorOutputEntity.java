package net.nuclearteam.createnuclear.content.multiblock.output;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.utility.CreateLang;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.math.AngleHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;

import net.nuclearteam.createnuclear.content.multiblock.pattern.ReactorPattern;

import java.util.List;

public class ReactorOutputEntity extends GeneratingKineticBlockEntity {
    public int speed = 0;
    public float heat = 0;

    protected ReactorPattern pattern =  new ReactorPattern();

    protected float generatedSpeed;

    public ReactorOutputEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);

    }

    @Override
    public void lazyTick() {
        super.lazyTick();

        determineSpeed();
    }

    public void determineSpeed() {
        int deterSpeed = this.speed;
        setSpeedAndUpdate(deterSpeed);
    }

    public void setSpeedAndUpdate(int speed) {
        if (generatedSpeed == speed) return;

        generatedSpeed = (float) speed;

        updateGeneratedRotation();
		setChanged();
    }

    // Tracks the output's linked block position for persistence across reloads.
    private BlockPos outputPos;

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);

        // Restore the generated rotation speed
        generatedSpeed = compound.getFloat("generatedSpeed");

        // Restore the output position, if present in the tag
        if (compound.contains("outputPos")) {
            this.outputPos = BlockPos.of(compound.getLong("outputPos"));
        }
    }

    @Override
    public void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);

        // Persist the generated rotation speed
        compound.putFloat("generatedSpeed", generatedSpeed);

        // Persist the output position, if set
        if (this.outputPos != null) {
            compound.putLong("outputPos", this.outputPos.asLong());
        }
    }

     @Override
     public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {

         float stressBase = calculateAddedStressCapacity();

         CreateLang.translate("gui.goggles.generator_stats")
                 .forGoggles(tooltip);
         CreateLang.translate("tooltip.capacityProvided")
                 .style(ChatFormatting.GRAY)
                 .forGoggles(tooltip);

         float speed = getTheoreticalSpeed();
         speed = Math.abs(speed);

         float stressTotal = stressBase * speed;

         CreateLang.number(stressTotal)
                 .translate("generic.unit.stress")
                 .style(ChatFormatting.AQUA)
                 .space()
                 .add(CreateLang.translate("gui.goggles.at_current_speed")
                         .style(ChatFormatting.DARK_GRAY))
                 .forGoggles(tooltip, 1);
         return true;
     }

    @Override
    public void initialize() {
        super.initialize();

        if (!hasSource() || getGeneratedSpeed() > getTheoreticalSpeed())
        {
            assert level != null;
            pattern.findController(getBlockPos(), level, true);
        }
    }

    @Override
    public float getGeneratedSpeed() {
        return Mth.clamp(generatedSpeed, 0, 1500000);
    }

    static class ReactorOutputValue extends ValueBoxTransform.Sided {

        @Override
        protected Vec3 getSouthLocation() {
            return net.createmod.catnip.math.VecHelper.voxelSpace(8, 8, 12.5);
        }

        @Override
        public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
            Direction facing = state.getValue(ReactorOutput.FACING);
            return super.getLocalOffset(level, pos, state).add(Vec3.atLowerCornerOf(facing.getNormal())
                    .scale(-1 / 16f));
        }

        @Override
        public void rotate(LevelAccessor level, BlockPos pos, BlockState state, PoseStack ms) {
            super.rotate(level, pos, state, ms);
            Direction facing = state.getValue(ReactorOutput.FACING);
            if (facing.getAxis() == Direction.Axis.Y)
                return;
            if (getSide() != Direction.UP)
                return;
            TransformStack.of(ms)
                    .rotateZ(-AngleHelper.horizontalAngle(facing) + 180);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            Direction facing = state.getValue(ReactorOutput.FACING);
            if (facing.getAxis() != Direction.Axis.Y && direction == Direction.DOWN)
                return false;
            return direction.getAxis() != facing.getAxis();
        }

    }
}