package net.nuclearteam.createnuclear.content.multiblock.liquidInput;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.fluids.tank.FluidTankBlock;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;
import com.simibubi.create.content.fluids.transfer.GenericItemFilling;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.fluid.FluidHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.nuclearteam.createnuclear.CNBlockEntityTypes;
import net.nuclearteam.createnuclear.CNShapes;
import net.nuclearteam.createnuclear.content.multiblock.MultiblockHelpers;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlockEntity;
import net.nuclearteam.createnuclear.foundation.block.MultiDirectionalReactorBlock;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class ReactorLiquidInput extends MultiDirectionalReactorBlock implements IWrenchable, IBE<ReactorFluidInputEntity> {

	@Override
	public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
		super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
	}

	public ReactorLiquidInput(Properties properties) {
		super(properties);
	}


	@Override
	public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean moved) {
		super.onPlace(state, world, pos, oldState, moved);
		MultiblockHelpers.handleOnPlace(pos, world, ReactorControllerBlockEntity::addInputFluid);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
        super.createBlockStateDefinition(builder);
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		return InteractionResult.SUCCESS;
	}

	@Override
	public VoxelShape getBlockSupportShape(BlockState pState, BlockGetter pReader, BlockPos pPos) {
		return Shapes.block();
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState()
                .setValue(FACING, context.getNearestLookingDirection().getOpposite());
	}

	@Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return CNShapes.REACTOR_LIQUID_INPUT.get(state.getValue(FACING));
    }


	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
								 BlockHitResult ray) {
		ItemStack heldItem = player.getItemInHand(hand);
		boolean onClient = world.isClientSide;

		if (heldItem.isEmpty())
			return InteractionResult.PASS;
		if (!player.isCreative())
			return InteractionResult.PASS;

		FluidHelper.FluidExchange exchange = null;
		ReactorLiquidInputEntity be = ConnectivityHandler.partAt(getBlockEntityType(), world, pos);
		if (be == null)
			return InteractionResult.FAIL;

		LazyOptional<IFluidHandler> tankCapability = be.getCapability(ForgeCapabilities.FLUID_HANDLER);
		if (!tankCapability.isPresent())
			return InteractionResult.PASS;
		IFluidHandler fluidTank = tankCapability.orElse(null);
		FluidStack prevFluidInTank = fluidTank.getFluidInTank(0)
			.copy();

		if (FluidHelper.tryEmptyItemIntoBE(world, player, hand, heldItem, be))
			exchange = FluidHelper.FluidExchange.ITEM_TO_TANK;
		else if (FluidHelper.tryFillItemFromBE(world, player, hand, heldItem, be))
			exchange = FluidHelper.FluidExchange.TANK_TO_ITEM;

		if (exchange == null) {
			if (GenericItemEmptying.canItemBeEmptied(world, heldItem)
				|| GenericItemFilling.canItemBeFilled(world, heldItem))
				return InteractionResult.SUCCESS;
			return InteractionResult.PASS;
		}

		SoundEvent soundevent = null;
		BlockState fluidState = null;
		FluidStack fluidInTank = tankCapability.map(fh -> fh.getFluidInTank(0))
			.orElse(FluidStack.EMPTY);

		if (exchange == FluidHelper.FluidExchange.ITEM_TO_TANK) {
			if (!onClient) {
				FluidStack fluidInItem = GenericItemEmptying.emptyItem(world, heldItem, true)
					.getFirst();

			}

			Fluid fluid = fluidInTank.getFluid();
			fluidState = fluid.defaultFluidState()
				.createLegacyBlock();
			soundevent = FluidHelper.getEmptySound(fluidInTank);
		}

		if (exchange == FluidHelper.FluidExchange.TANK_TO_ITEM) {

			Fluid fluid = prevFluidInTank.getFluid();
			fluidState = fluid.defaultFluidState()
				.createLegacyBlock();
			soundevent = FluidHelper.getFillSound(prevFluidInTank);
		}

		if (soundevent != null && !onClient) {
			float pitch = Mth
				.clamp(1 - (1f * fluidInTank.getAmount() / (FluidTankBlockEntity.getCapacityMultiplier() * 16)), 0, 1);
			pitch /= 1.5f;
			pitch += .5f;
			pitch += (world.random.nextFloat() - .5f) / 4f;
			world.playSound(null, pos, soundevent, SoundSource.BLOCKS, .5f, pitch);
		}

//		if (!fluidInTank.isFluidStackIdentical(prevFluidInTank)) {
//			if (be instanceof ReactorLiquidInputEntity) {
//				ReactorLiquidInputEntity controllerBE = ((ReactorLiquidInputEntity) be).getControllerBE();
//				if (controllerBE != null) {
//					if (fluidState != null && onClient) {
//						BlockParticleOption blockParticleData =
//							new BlockParticleOption(ParticleTypes.BLOCK, fluidState);
//						float level = (float) fluidInTank.getAmount() / fluidTank.getTankCapacity(0);
//
//						boolean reversed = fluidInTank.getFluid()
//							.getFluidType()
//							.isLighterThanAir();
//						if (reversed)
//							level = 1 - level;
//
//						Vec3 vec = ray.getLocation();
//						vec = new Vec3(vec.x, controllerBE.getBlockPos()
//							.getY() + level * (controllerBE.getHeight() - .5f) + .25f, vec.z);
//						Vec3 motion = player.position()
//							.subtract(vec)
//							.scale(1 / 20f);
//						vec = vec.add(motion);
//						world.addParticle(blockParticleData, vec.x, vec.y, vec.z, motion.x, motion.y, motion.z);
//						return InteractionResult.SUCCESS;
//					}
//
//					controllerBE.sendDataImmediately();
//					controllerBE.setChanged();
//				}
//			}
//		}

		return InteractionResult.SUCCESS;
	}

	@Override
	public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
		super.playerDestroy(level, player, pos, state, blockEntity, tool);
		MultiblockHelpers.handleRemoval(pos, level, ReactorControllerBlockEntity::removeInputFluid);
	}

	@Override
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
		super.onRemove(state, world, pos, newState, isMoving);
		MultiblockHelpers.handleRemoval(pos, world, ReactorControllerBlockEntity::removeInputFluid);
	}

	@Override
	public Class<ReactorFluidInputEntity> getBlockEntityClass() {
		return ReactorFluidInputEntity.class;
	}

	@Override
	public BlockEntityType<? extends ReactorFluidInputEntity> getBlockEntityType() {
		return CNBlockEntityTypes.REACTOR_LIQUID_INPUT.get();
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}
}