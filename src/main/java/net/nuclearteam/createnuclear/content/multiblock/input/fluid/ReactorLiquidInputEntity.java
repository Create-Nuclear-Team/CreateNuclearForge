package net.nuclearteam.createnuclear.content.multiblock.input.fluid;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static java.lang.Math.abs;

public class ReactorLiquidInputEntity extends SmartBlockEntity implements IHaveGoggleInformation {

	private static final int CAPACITY = 16000;

	protected LazyOptional<IFluidHandler> fluidCapability;
	protected boolean forceFluidLevelUpdate;
	protected FluidTank tankInventory;

	private static final int SYNC_RATE = 8;
	protected int syncCooldown;
	protected boolean queuedSync;

	// For rendering purposes only
	private LerpedFloat fluidLevel;

	public ReactorLiquidInputEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		tankInventory = createInventory();
		fluidCapability = LazyOptional.of(() -> tankInventory);

		refreshCapability();
	}

	protected SmartFluidTank createInventory() {
		return new SmartFluidTank(CAPACITY, this::onFluidStackChanged);
	}

	 @Override
    public void tick() {
    }


	@Override
	public void initialize() {
		super.initialize();
		sendData();
		if (level.isClientSide)
			invalidateRenderBoundingBox();
	}

	protected void onFluidStackChanged(FluidStack newFluidStack) {
		if (!hasLevel())
			return;

		FluidType attributes = newFluidStack.getFluid()
			.getFluidType();
		int luminosity = (int) (attributes.getLightLevel(newFluidStack) / 1.2f);
		boolean reversed = attributes.isLighterThanAir();
//		int maxY = (int) ((getFillState() * height) + 1);
//
//		for (int yOffset = 0; yOffset < height; yOffset++) {
//			boolean isBright = reversed ? (height - yOffset <= maxY) : (yOffset < maxY);
//			int actualLuminosity = isBright ? luminosity : luminosity > 0 ? 1 : 0;
//
//			for (int xOffset = 0; xOffset < width; xOffset++) {
//				for (int zOffset = 0; zOffset < width; zOffset++) {
//					BlockPos pos = this.worldPosition.offset(xOffset, yOffset, zOffset);
//					ReactorLiquidInputEntity tankAt = ConnectivityHandler.partAt(getType(), level, pos);
//					if (tankAt == null)
//						continue;
//					level.updateNeighbourForOutputSignal(pos, tankAt.getBlockState()
//						.getBlock());
//					if (tankAt.luminosity == actualLuminosity)
//						continue;
//					tankAt.setLuminosity(actualLuminosity);
//				}
//			}
//		}

		if (!level.isClientSide) {
			setChanged();
			sendData();
		}

		if (isVirtual()) {
			if (fluidLevel == null)
				fluidLevel = LerpedFloat.linear()
					.startWithValue(getFillState());
			fluidLevel.chase(getFillState(), .5f, LerpedFloat.Chaser.EXP);
		}
	}

	public void sendDataImmediately() {
		syncCooldown = 0;
		queuedSync = false;
		sendData();
	}

	@Override
	public void sendData() {
		if (syncCooldown > 0) {
			queuedSync = true;
			return;
		}
		super.sendData();
		queuedSync = false;
		syncCooldown = SYNC_RATE;
	}

	private void refreshCapability() {
		LazyOptional<IFluidHandler> oldCap = fluidCapability;
		fluidCapability = LazyOptional.of(this::handlerForCapability);
		oldCap.invalidate();
	}

	private IFluidHandler handlerForCapability() {
		return isController()
				? tankInventory
				: this.handlerForCapability();
	}

	public boolean isController() {
		return true;
	}


	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		ReactorLiquidInputEntity controllerBE = this;

		return containedFluidTooltip(tooltip, isPlayerSneaking,
			controllerBE.getCapability(ForgeCapabilities.FLUID_HANDLER));
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		super.read(compound, clientPacket);

		if (isController()) {
			tankInventory.setCapacity(CAPACITY);
			tankInventory.readFromNBT(compound.getCompound("TankContent"));
			if (tankInventory.getSpace() < 0)
				tankInventory.drain(-tankInventory.getSpace(), IFluidHandler.FluidAction.EXECUTE);
		}


		if (compound.contains("ForceFluidLevel") || fluidLevel == null)
			fluidLevel = LerpedFloat.linear()
				.startWithValue(getFillState());


		if (!clientPacket)
			return;

		if (isController()) {
			float fillState = getFillState();
			if (compound.contains("ForceFluidLevel") || fluidLevel == null)
				fluidLevel = LerpedFloat.linear()
					.startWithValue(fillState);
			fluidLevel.chase(fillState, 0.5f, LerpedFloat.Chaser.EXP);
		}

		if (compound.contains("LazySync"))
			fluidLevel.chase(fluidLevel.getChaseTarget(), 0.125f, LerpedFloat.Chaser.EXP);
	}

	public float getFillState() {
		return (float) tankInventory.getFluidAmount() / tankInventory.getCapacity();
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {

		if (isController()) {
			compound.put("TankContent", tankInventory.writeToNBT(new CompoundTag()));

		}
		super.write(compound, clientPacket);

		if (!clientPacket)
			return;
		if (forceFluidLevelUpdate)
			compound.putBoolean("ForceFluidLevel", true);
		if (queuedSync)
			compound.putBoolean("LazySync", true);
		forceFluidLevelUpdate = false;
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		if (!fluidCapability.isPresent())
			refreshCapability();
		if (cap == ForgeCapabilities.FLUID_HANDLER)
			return fluidCapability.cast();
		return super.getCapability(cap, side);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
	}

	public FluidTank getTankInventory() {
		return tankInventory;
	}
}
