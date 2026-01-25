package net.nuclearteam.createnuclear.content.multiblock.controller;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.foundation.utility.IInteractionChecker;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.nuclearteam.createnuclear.*;
import net.nuclearteam.createnuclear.content.logistics.BigFluidStack;
import net.nuclearteam.createnuclear.content.multiblock.CNMultiblock;
import net.nuclearteam.createnuclear.content.multiblock.FluidLockManager;
import net.nuclearteam.createnuclear.content.multiblock.IHeat;
import net.nuclearteam.createnuclear.content.multiblock.PersistentFluidLocks;
import net.nuclearteam.createnuclear.content.multiblock.input.fluid.ReactorFluidInputEntity;
import net.nuclearteam.createnuclear.content.multiblock.input.fluid.VirtualReactorInputFluid;
import net.nuclearteam.createnuclear.content.multiblock.input.item.VirtualReactorInputsItem;
import net.nuclearteam.createnuclear.content.multiblock.controller.manager.*;
import net.nuclearteam.createnuclear.content.multiblock.output.ReactorOutput;
import net.nuclearteam.createnuclear.content.multiblock.output.ReactorOutputEntity;
import net.nuclearteam.createnuclear.foundation.utility.CreateNuclearLang;
import net.nuclearteam.createnuclear.content.multiblock.pattern.ReactorPattern;
import net.nuclearteam.createnuclear.content.multiblock.reactorLogic.HeatManager;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlock.ASSEMBLED;

@SuppressWarnings({"unused"})
public class ReactorControllerBlockEntity extends SmartBlockEntity implements IInteractionChecker, IHaveGoggleInformation {
    /** The assembled state is stored in the block state (`ReactorControllerBlock.ASSEMBLED`).
     *  Use the helper accessors below to query or toggle it to keep entity/blockstate consistent.
     */
    public int speed = 16; // This is the result speed of the reactor, change this to change the total capacity

    public ReactorControllerBlock controller;
    protected ReactorPattern pattern =  new ReactorPattern();
    public ReactorControllerInventory inventory;
    public int countUraniumRod;
    public int countGraphiteRod;
    public int heat;

    public double total;
    public CompoundTag screen_pattern = new CompoundTag();
    public ItemStack configuredPattern;

    private BigItemStack bigFuelItem;
    private BigItemStack bigCoolerItem;
    private List<BigFluidStack> bigFluidStack;

    public int reactorSize = 0;
    public String reactorFacing = "null";
    // les pos sont [xMin, xMax, yMin, yMax, zMin, zMax]
    public int[] reactorPos;
    private boolean needsToResolveEntities = false;

    private final ReactorInputManagerI inputManager;
    private final ReactorOutputManagerI outputManager;
    private final ReactorInputFluidManagerI inputFluidManager;

    HeatManager heatManager = new HeatManager();

    public ReactorControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        inventory = new ReactorControllerInventory(this);
        configuredPattern = ItemStack.EMPTY;

        inputManager = new ReactorInputManager();
        outputManager = new ReactorOutputManager();
        inputFluidManager = new ReactorInputFluidManager();

        bigFuelItem = new BigItemStack(ItemStack.EMPTY);
        bigCoolerItem = new BigItemStack(ItemStack.EMPTY);
        bigFluidStack = new ArrayList<>();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }

    public boolean getAssembled() { // permet de savoir si le réacteur est formé ou pas.
        BlockState state = getBlockState();
        return state.getValue(ASSEMBLED);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if(!configuredPattern.getOrCreateTag().isEmpty()) {
            CreateLang.translate("gui.gauge.info_header")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
            IHeat.HeatLevel.getName("reactor_controller").style(ChatFormatting.GRAY).forGoggles(tooltip);

            IHeat.HeatLevel.getFormattedHeatText(configuredPattern.getOrCreateTag().getInt("heat")).forGoggles(tooltip);

            if (bigFuelItem.stack.isEmpty()) {
                // if rod empty we initialize it at 1 (and display it as 0) to avoid having air item displayed instead of the rod
                IHeat.HeatLevel.getFormattedItemText(new ItemStack(CNItems.URANIUM_ROD.asItem(), 1), true).forGoggles(tooltip);
            } else {
                IHeat.HeatLevel.getFormattedItemText(bigFuelItem, false).forGoggles(tooltip);
            }

            if (bigCoolerItem.stack.isEmpty()) {
                // if rod empty we initialize it at 1 (and display it as 0) to avoid having air item displayed instead of the rod
                IHeat.HeatLevel.getFormattedItemText(new ItemStack(CNItems.GRAPHITE_ROD.asItem(), 1), true).forGoggles(tooltip);
            } else {
                IHeat.HeatLevel.getFormattedItemText(bigCoolerItem, false).forGoggles(tooltip);
            }

            Map<ResourceLocation, Long> m = this.inputFluidManager.getInventory(level).fluids();
            List<BigFluidStack> stacks = VirtualReactorInputFluid.toBigList(m);

            for (BigFluidStack stack : stacks) {
                CreateNuclearLang
                    .translate("tooltip.fluid", stack.stack.getDisplayName())
                    .translate("tooltip.fluid.amount", stack.amount)
                    .forGoggles(tooltip);
            }

        }

        return true;
    }


    //(Si les methode read et write ne sont pas implémenté alors lorsque l'on relance le monde minecraft les items dans le composant auront disparu !)
    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket); // Toujours en premier pour les coordonnées de base

        // Lecture des Inputs
        this.inputManager.read(compound);
        this.outputManager.read(compound);
        this.inputFluidManager.read(compound);

        // 1. Tes nouvelles variables simples
        this.reactorSize = compound.getInt("reactorSize");
        this.reactorFacing = compound.getString("reactorFacing");
        this.reactorPos = compound.getIntArray("reactorPose");
        this.total = compound.getDouble("total");

        // 2. Gestion des items
        if (!clientPacket) {
            inventory.deserializeNBT(compound.getCompound("pattern"));
        }
        configuredPattern = ItemStack.of(compound.getCompound("items"));

        bigFuelItem = BigItemStack.read(compound.getCompound("bigFuel"));
        bigCoolerItem = BigItemStack.read(compound.getCompound("bigCooler"));

        this.needsToResolveEntities = true;
    }

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        this.inputManager.write(compound);
        this.outputManager.write(compound);
        this.inputFluidManager.write(compound);

        // 1. Tes nouvelles variables simples
        compound.putInt("reactorSize", this.reactorSize);
        compound.putString("reactorFacing", this.reactorFacing);
        if (this.reactorPos != null) {
            compound.putIntArray("reactorPose", this.reactorPos);
        }

        compound.putDouble("total", calculateProgress());

        // 2. Gestion des items (Ton code existant)
        if (!clientPacket) {
            compound.put("pattern", inventory.serializeNBT());
        }
        compound.put("items", configuredPattern.serializeNBT());

        compound.put("bigFuel", bigFuelItem.write());
        compound.put("bigCooler", bigCoolerItem.write());

    }


    public boolean isAssembled() {
        if (level == null) return false;
        try {
            return level.getBlockState(worldPosition).getValue(ASSEMBLED);
        } catch (Exception e) {
            return false;
        }
    }

    public void setAssembled(boolean assembled) {
        if (level == null) return;
        level.setBlockAndUpdate(worldPosition, getBlockState().setValue(ASSEMBLED, assembled));
        this.setChanged();
    }

    public double calculateProgress() {
        countGraphiteRod = configuredPattern.getOrCreateTag().getInt("countGraphiteRod");
        countUraniumRod = configuredPattern.getOrCreateTag().getInt("countUraniumRod");

        double totalGraphiteRodLife = (double) heatManager.graphiteTimer / countGraphiteRod;
        double totalUraniumRodLife = (double) heatManager.uraniumTimer / countUraniumRod;

        return totalGraphiteRodLife + totalUraniumRodLife;
    }

    public void test() {
        CreateNuclear.LOGGER.warn("List d'input: {}", this.inputManager.size());
        CreateNuclear.LOGGER.warn("List d'output: {}", this.outputManager.size());
        CreateNuclear.LOGGER.warn("List d'input fluid: {}", this.inputFluidManager.size());

        for (BlockPos p : this.inputManager.getBlocksPosition()) {
                CreateNuclear.LOGGER.info("ReactorInputEntity BlockPos {}", p);
        }
        for (BlockPos p : this.inputManager.getBlocksPosition(level)) {
            CreateNuclear.LOGGER.warn("List vrais input: {}", p);
        }

        for (BlockPos p : this.outputManager.getBlocksPosition()) {
            CreateNuclear.LOGGER.info("ReactorOutputEntity BlockPos {}", p);
        }
        for (BlockPos p : this.outputManager.getBlocksPosition(level)) {
            CreateNuclear.LOGGER.warn("List vrais output: {}", p);
        }

        for (BlockPos p : this.inputFluidManager.getBlocksPosition()) {
            CreateNuclear.LOGGER.info("ReactorFluidInputEntity BlockPos {}", p);
        }
        for (BlockPos p : this.inputFluidManager.getBlocksPosition(level)) {
            CreateNuclear.LOGGER.warn("List vrais input fluid: {}", p);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide)
            return;

        int heat = (int) configuredPattern.getOrCreateTag().getDouble("heat");
        countGraphiteRod = configuredPattern.getOrCreateTag().getInt("countGraphiteRod");
        countUraniumRod = configuredPattern.getOrCreateTag().getInt("countUraniumRod");
        if (needsToResolveEntities) {
            List<IItemHandler> handlers = inputManager.getItemHandlers(level);
            CreateNuclear.LOGGER.warn("Resolving inputs after load, handlers found: {}", handlers.size());
            needsToResolveEntities = false;
            this.setChanged();
        }
        if (isAssembled()) {
            List<IItemHandler> handlers = inputManager.getItemHandlers(level);
            List<IFluidHandler> fluidHandlers = inputFluidManager.getFuildHandlers(level);
            VirtualReactorInputsItem virtualReactorInputsItem = inputManager.getInventory(level);
            VirtualReactorInputFluid virtualReactorInputFluid = inputFluidManager.getInventory(level);
            bigFuelItem = virtualReactorInputsItem.getBigFuelRod();
            bigCoolerItem = virtualReactorInputsItem.getBigCooledRod();
            bigFluidStack = VirtualReactorInputFluid.toBigList(virtualReactorInputFluid.fluids());

            if (!isEmptyConfiguredPattern() && bigFuelItem.count > 0 && bigCoolerItem.count > 0) {
                if (this.inputManager.size() > 0) {
                    this.setChanged();
                    this.notifyUpdate();
                        configuredPattern.getOrCreateTag().putDouble("heat", heatManager.calculateHeat(bigFuelItem, bigCoolerItem, countGraphiteRod, countUraniumRod, inventory));
                        if (!this.outputManager.getBlocksPosition().isEmpty()) {
                            rotate(getBlockState(), getLevel(), heat);
                        }

                        if (updateTimers()) {
                            boolean extracted = inputManager.extractItems(level, 1, 1);
                            if (extracted) {
                                this.setChanged();
                                this.notifyUpdate();
                                total = calculateProgress();

                                if (IHeat.HeatLevel.isNotDanger(heat)) {
                                    //...
                                } else {
                                    EventTriggerPacket packet = new EventTriggerPacket(600);
                                    CreateNuclear.LOGGER.warn("hum EventTriggerBlock ? {}", packet);
                                    CNPackets.sendToNear(level, getBlockPos(), 32, packet);
                                }
                                return;
                            }
                        }

                    this.setChanged();
                    this.notifyUpdate();
                }
            } else {
                configuredPattern.getOrCreateTag().putDouble("heat", heatManager.calculateHeat(bigFuelItem, bigCoolerItem, countGraphiteRod, countUraniumRod, inventory));
                if (!this.outputManager.getBlocksPosition().isEmpty()) {
                    rotate(getBlockState(), getLevel(), 0);
                }
                this.setChanged();
                this.notifyUpdate();
            }
        }
    }

    private boolean isEmptyConfiguredPattern() {
        return configuredPattern.isEmpty() || configuredPattern.getOrCreateTag().isEmpty();
    }

    private boolean updateTimers() {

        total -= 1;
        return total <= 0;//(total/constTotal) <= 0;
    }


    private BlockPos getBlockPosForReactor(char character) {
        BlockPos pos = pattern.VerifyPattern5x5(character);
        BlockPos posController = getBlockPos();
        BlockPos posInput = new BlockPos(posController.getX(), posController.getY(), posController.getZ());

        int[][] directions = {
                {0,0, pos.getX()}, // NORTH
                {0,0, -pos.getX()}, // SOUTH
                {-pos.getX(),0,0}, // EAST
                {pos.getX(),0,0} // WEST
        };


        for (int[] direction : directions) {
            BlockPos newPos = posController.offset(direction[0], direction[1], direction[2]);
            if (level.getBlockState(newPos).is(CNBlocks.REACTOR_INPUT.get())) {
                posInput = newPos;
                break;
            }
        }

        return posInput;
    }

    public void rotate(BlockState state, Level level, int rotation) {
        int remainingRotation = rotation % this.outputManager.getBlocksPosition().size();
        for (int i = 0; i < this.outputManager.getBlocksPosition().size(); i++) {
            int dividedRotation = rotation / this.outputManager.getBlocksPosition().size() + remainingRotation;
            remainingRotation = 0;
            BlockPos pos =  this.outputManager.getBlocksPosition().get(i);

            if (dividedRotation > 0) {
                if (level.getBlockState(pos).getBlock() instanceof ReactorOutput block) {
                    ReactorOutputEntity entity = block.getBlockEntityType().getBlockEntity(level, pos);
                    if (state.getValue(ASSEMBLED)) { // Starting the energy
                        entity.speed = dividedRotation;
                        entity.heat = dividedRotation;
                    } else { // stopping the energy
                        entity.speed = 0;
                        entity.heat = 0;
                    }
                    entity.updateSpeed = true;
                    entity.updateGeneratedRotation();
                    entity.setSpeed(dividedRotation);

                }
            } else {
                if (level.getBlockState(pos).getBlock() instanceof ReactorOutput block) {
                    ReactorOutputEntity entity = block.getBlockEntityType().getBlockEntity(level, pos);
                    entity.setSpeed(0);
                    entity.heat = 0;
                    entity.updateSpeed = true;
                    entity.updateGeneratedRotation();
                }
            }
        }
    }

    public int[] getStructureBounds(BlockPos startPos, int structureSize, String facing) {
        int[] northOffsets5x5 = new int[] {-2, 2, -3, 3, 0, 4};
        int[] northOffsets7x7 = new int[] {-3, 3, -4, 4, 0, 6};
        int[] northOffsets9x9 = new int[] {-4, 4, -5, 5, 0, 8};

        int[] eastOffsets5x5 = new int[] {-4, 0, -3, 3, -2, 2};
        int[] eastOffsets7x7 = new int[] {-6, 0, -4, 4, -3, 3};
        int[] eastOffsets9x9 = new int[] {-8, 0, -5, 5, -4, 4};

        int[] southOffsets5x5 = new int[] {-2, 2, -3, 3, -4, 0};
        int[] southOffsets7x7 = new int[] {-3, 3, -4, 4, -6, 0};
        int[] southOffsets9x9 = new int[] {-4, 4, -5, 5, -8, 0};

        int[] westOffsets5x5 = new int[] {0, 4, -3, 3, -2, 2};
        int[] westOffsets7x7 = new int[] {0, 6, -4, 4, -3, 3};
        int[] westOffsets9x9 = new int[] {0, 8, -5, 5, -4, 4};

        switch (facing) {
            case "north":
                switch (structureSize) {
                    case 5: return applyOffset(startPos, northOffsets5x5);
                    case 7: return applyOffset(startPos, northOffsets7x7);
                    case 9: return applyOffset(startPos, northOffsets9x9);
                }
            case "east":
                switch (structureSize) {
                    case 5: return applyOffset(startPos, eastOffsets5x5);
                    case 7: return applyOffset(startPos, eastOffsets7x7);
                    case 9: return applyOffset(startPos, eastOffsets9x9);
                }
            case "south":
                switch (structureSize) {
                    case 5: return applyOffset(startPos, southOffsets5x5);
                    case 7: return applyOffset(startPos, southOffsets7x7);
                    case 9: return applyOffset(startPos, southOffsets9x9);
                }
            case "west":
                switch (structureSize) {
                    case 5: return applyOffset(startPos, westOffsets5x5);
                    case 7: return applyOffset(startPos, westOffsets7x7);
                    case 9: return applyOffset(startPos, westOffsets9x9);
                }
            default: return new int[] {0, 0, 0, 0, 0, 0};
        }
    }

    private int[] applyOffset(BlockPos pos, int[] offset) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        return new int[] {x + offset[0], x + offset[1], y + offset[2], y + offset[3], z + offset[4], z + offset[5]};
    }

    public void addInput(BlockPos inputPos) {
        this.inputManager.addBlock(inputPos);
        this.setChanged();
    }

    public void removeInput(BlockPos inputPos) {
        this.inputManager.removeBlock(inputPos);
        this.setChanged();
    }

    public void addOutput(BlockPos outputPos) {
        this.outputManager.addBlock(outputPos);
        this.setChanged();
    }

    public void removeOutput(BlockPos outputPos) {
        this.outputManager.removeBlock(outputPos);
        this.setChanged();
    }

    public void addInputFluid(BlockPos outputPos) {
        this.inputFluidManager.addBlock(outputPos);
        this.setChanged();
    }

    public void removeInputFluid(BlockPos outputPos) {
        this.inputFluidManager.removeBlock(outputPos);
        this.setChanged();
    }

    public void removeIOAll() {
        this.inputManager.clearInvalid(level);
        this.outputManager.clearInvalid(level);
        this.inputFluidManager.clearInvalid(level);
        this.setChanged();
    }

    /** Try to lock this controller to the given Fluid. Returns true if allowed. */
    public boolean tryLockFluid(Fluid fluid) {
        // server-persistent approach (preferred): use PersistentFluidLocks when on server
        if (level != null && !level.isClientSide && level instanceof ServerLevel serverLevel) {
            return PersistentFluidLocks.get(serverLevel).tryLock(getBlockPos(), fluid);
        }
        // fallback to in-memory manager (single-server-run)
        return FluidLockManager.tryLock(getBlockPos(), fluid);
    }

    /** Returns whether the given FluidStack is acceptable for this controller. */
    public boolean canAcceptFluid(FluidStack stack) {
        if (stack == null || stack.isEmpty()) return true;
        if (level != null && !level.isClientSide && level instanceof ServerLevel serverLevel) {
            return PersistentFluidLocks.get(serverLevel).canAccept(getBlockPos(), stack.getFluid());
        }
        return FluidLockManager.canAccept(getBlockPos(), stack);
    }

    /** Force-clear the lock on this controller. */
    public void clearLock() {
        if (level != null && !level.isClientSide && level instanceof ServerLevel serverLevel) {
            PersistentFluidLocks.get(serverLevel).clearLock(getBlockPos());
        } else {
            FluidLockManager.clearLock(getBlockPos());
        }
        setChanged();
        sendData();
    }
//

    public void clearLockIfAllInputsEmpty() {
        if (level == null || level.isClientSide) return;

        final int SCAN_RADIUS = CNMultiblock.REGISTRATE_MULTIBLOCK.findStructure(level, getBlockPos(), this).data().getSize(); // adapte selon la taille max du multiblock
        BlockPos center = getBlockPos();
        boolean anyNonEmpty = false;

        for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS && !anyNonEmpty; dx++) {
            for (int dy = -SCAN_RADIUS; dy <= SCAN_RADIUS && !anyNonEmpty; dy++) {
                for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS && !anyNonEmpty; dz++) {
                    BlockPos p = center.offset(dx, dy, dz);
                    BlockEntity be = level.getBlockEntity(p);
                    if (!(be instanceof ReactorFluidInputEntity)) continue;

                    IFluidHandler handler = be.getCapability(ForgeCapabilities.FLUID_HANDLER).orElse(null);
                    if (handler == null) continue;

                    for (int t = 0; t < handler.getTanks(); t++) {
                        if (!handler.getFluidInTank(t).isEmpty()) {
                            anyNonEmpty = true;
                            break;
                        }
                    }
                }
            }
        }

        if (!anyNonEmpty) clearLock();
    }
}