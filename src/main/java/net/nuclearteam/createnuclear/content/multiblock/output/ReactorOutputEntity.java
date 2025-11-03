package net.nuclearteam.createnuclear.content.multiblock.output;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.motor.KineticScrollValueBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.math.AngleHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import net.nuclearteam.createnuclear.CNBlocks;

import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlock;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlockEntity;
import net.nuclearteam.createnuclear.foundation.utility.CreateNuclearLang;

import java.util.List;
import java.util.Objects;

import static net.nuclearteam.createnuclear.content.multiblock.output.ReactorOutput.DIR;


public class ReactorOutputEntity extends GeneratingKineticBlockEntity {
    public int speed = 1;
    public float heat = 0;

    ReactorControllerBlock controller = null;
    ReactorControllerBlockEntity controllerEntity = null;

    // protected ScrollValueBehaviour generatedSpeed;
    protected float generatedSpeed;

    public ReactorOutputEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        // generatedSpeed = new KineticScrollValueBehaviour(CreateNuclearLang.translateDirect("kinetics.reactor_output.rotation_speed"), this, new ReactorOutputValue());
        // generatedSpeed.between(-1500000, 1500000);
        // generatedSpeed.setValue(speed);
        // generatedSpeed.withCallback(i -> this.updateGeneratedRotation());
        // behaviours.add(generatedSpeed);

    }

    @Override
    public void lazyTick() {
        super.lazyTick();

        determineSpeed();
    }

    public void determineSpeed() {
        int deterSpeed = this.speed
        setSpeedAndUpdate(deterSpeed)
    }

    public void setSpeedAndUpdate(int speed) {
        if (generatedSpeed == speed) return;

        generatedSpeed = (float) speed;

        updateGeneratedRotation();
		setChanged();
    }

    @Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		super.read(compound, clientPacket);
		generatedSpeed = compound.getFloat("generatedSpeed");
	}

	
	
	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		super.write(compound, clientPacket);
		compound.putFloat("generatedSpeed", generatedSpeed);
	}

    @Override
    public void tick() {
        super.tick();

        BlockGetter level = getLevel();

        if (level.getBlockState(getBlockPos().above(3)).getBlock() == CNBlocks.REACTOR_CONTROLLER.get()) {
            controller = (ReactorControllerBlock) level.getBlockState(getBlockPos().above(3)).getBlock();
            controllerEntity = (ReactorControllerBlockEntity) level.getBlockEntity(getBlockPos().above(3));
            if (controllerEntity != null) {
                if (!controllerEntity.getAssembled() && getSpeed() != 0) {
                    setSpeed(0);
                }
            }
        } else setSpeed(0);
    }

    // @Override
    // public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {

    //     float stressBase = calculateAddedStressCapacity();

    //     CreateLang.translate("gui.goggles.generator_stats")
    //             .forGoggles(tooltip);
    //     CreateLang.translate("tooltip.capacityProvided")
    //             .style(ChatFormatting.GRAY)
    //             .forGoggles(tooltip);

    //     float speed = getTheoreticalSpeed();
    //     speed = Math.abs(speed);

    //     float stressTotal = stressBase * speed;

    //     CreateLang.number(stressTotal)
    //             .translate("generic.unit.stress")
    //             .style(ChatFormatting.AQUA)
    //             .space()
    //             .add(CreateLang.translate("gui.goggles.at_current_speed")
    //                     .style(ChatFormatting.DARK_GRAY))
    //             .forGoggles(tooltip, 1);
    //     return true;
    // }

    @Override
    public void initialize() {
        super.initialize();

        if (!hasSource() || getGeneratedSpeed() > getTheoreticalSpeed())
        {
            FindController(getBlockPos(), Objects.requireNonNull(getLevel()));
        }
    }

    public void FindController(BlockPos pos, Level level){
        if (level.getBlockState(pos.above(3)).getBlock() == CNBlocks.REACTOR_CONTROLLER.get()){
            ReactorControllerBlock controller = (ReactorControllerBlock)level.getBlockState(pos.above(3)).getBlock();
            controller.Verify(controller.defaultBlockState(), pos.above(3), level, level.players(), false);
        }
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getDir() {
        BlockState state = getBlockState();
        return state.getValue(DIR);
    }

    public void setDir(int dir, Level level, BlockPos pos) {
        BlockState state = getBlockState();
        level.setBlockAndUpdate(pos, state.setValue(DIR, dir));
    }

    @Override
    public float getGeneratedSpeed() {
        // if (!CNBlocks.REACTOR_OUTPUT.has(getBlockState()))
        //     return 0;
        // return speed; //convertToDirection(speed, getBlockState().getValue(ReactorOutput.FACING));
        return Mth.clamp(generatedSpeed, 0, 1500000)
    }

    @Override
    protected Block getStressConfigKey() {
        return super.getStressConfigKey();
    }

    class ReactorOutputValue extends ValueBoxTransform.Sided {

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