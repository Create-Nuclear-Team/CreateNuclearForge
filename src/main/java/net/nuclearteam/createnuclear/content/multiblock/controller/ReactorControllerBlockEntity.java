package net.nuclearteam.createnuclear.content.multiblock.controller;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.IInteractionChecker;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import net.nuclearteam.createnuclear.CreateNuclear;
import net.nuclearteam.createnuclear.api.multiblock.BlockPattern;
import net.nuclearteam.createnuclear.api.multiblock.IMultiblockController;
import net.nuclearteam.createnuclear.api.multiblock.TypeMultiblock;
import net.nuclearteam.createnuclear.content.logistics.BigFluidStack;
import net.nuclearteam.createnuclear.content.multiblock.CNMultiblock;
import net.nuclearteam.createnuclear.content.multiblock.controller.display.ReactorDisplayState;
import net.nuclearteam.createnuclear.content.multiblock.controller.display.ReactorGoggleTooltipRenderer;
import net.nuclearteam.createnuclear.content.multiblock.controller.service.*;
import net.nuclearteam.createnuclear.content.multiblock.controller.snapshot.ReactorInputSnapshot;
import net.nuclearteam.createnuclear.content.multiblock.controller.snapshot.ReactorInputSnapshotBuilder;
import net.nuclearteam.createnuclear.content.multiblock.IHeat;
import net.nuclearteam.createnuclear.foundation.advancement.CNAdvancement;
import net.nuclearteam.createnuclear.foundation.advancement.CNAdvancementBehaviour;
import net.nuclearteam.createnuclear.content.multiblock.controller.consumable.ConsumptionCycleManager;

import java.util.ArrayList;
import java.util.List;

import net.nuclearteam.createnuclear.content.multiblock.input.fluid.PersistentFluidLocks;
import net.nuclearteam.createnuclear.content.multiblock.input.fluid.ReactorFluidInputEntity;
import net.nuclearteam.createnuclear.content.multiblock.controller.manager.*;
import net.nuclearteam.createnuclear.content.multiblock.pattern.ReactorPattern;
import net.nuclearteam.createnuclear.content.multiblock.reactorLogic.HeatManager;

import static net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlock.ASSEMBLED;

@SuppressWarnings({ "unused" })
public class ReactorControllerBlockEntity extends SmartBlockEntity
        implements IInteractionChecker, IHaveGoggleInformation, IMultiblockController {
    /**
     * The assembled state is stored in the block state
     * (`ReactorControllerBlock.ASSEMBLED`).
     * Use the helper accessors below to query or toggle it to keep
     * entity/blockstate consistent.
     */
    private final ReactorPattern pattern = new ReactorPattern();
    private final ReactorControllerInventory inventory;
    private int countFuelRod;
    private int countCoolerRod;
    private int totalHeatRatio;
    private int heat;
    private boolean isExploding = false;

    private final ConsumptionCycleManager cycleManager = new ConsumptionCycleManager();
    private double liquidLife;
    private ItemStack configuredPattern;

    private List<BigFluidStack> bigFluidStack;

    private int reactorSize = 0;
    private Direction reactorFacing = null;
    // les pos sont [xMin, xMax, yMin, yMax, zMin, zMax]
    private BoundingBox reactorPos;


    private boolean needsToResolveEntities = false;
    private double fluidBuffer = 0.0;

    private CNAdvancementBehaviour advancement;

    private final ReactorInputManagerI inputManager;
    private final ReactorOutputManagerI outputManager;
    private final ReactorInputFluidManagerI inputFluidManager;
    private final ReactorAlarmManagerI alarmManager;
    private final ReactorFrameDisplayManagerI frameDisplayManager;

   private ReactorDisplayState displayState = ReactorDisplayState.EMPTY;

    // services (dependencies) - abstracted behind interfaces to follow DIP
    private final IHeatService heatService;
    private final IPersistenceService persistenceService;
    private final IExplosionService meltdownExecutor;
    private final IReactorHeatUpdateCoordinator heatCoordinator;
    private final IFluidConsumptionRateCalculator fluidRateCalculator;
    /** Counts down to the next explosion once in danger; keeps the original 1-tick lag against the heat recalculated later this tick. */
    private final IReactorMeltdownMonitor meltdownMonitor;
    /** Drives the alarm blocks based on the danger flag; built in {@link #addBehaviours} since it depends on {@link #advancement}. */
    private IReactorAlarmCoordinator alarmCoordinator;

    // service fields are injected; implementations live in separate classes

    // --- Accessors used by external services (persistence) ---
    public ReactorControllerInventory getInventoryObject() {
        return this.inventory;
    }

    public void deserializeInventory(CompoundTag tag) {
        this.inventory.deserializeNBT(tag);
    }

    public CompoundTag serializeInventory() {
        return this.inventory.serializeNBT();
    }

    public ItemStack getConfiguredPattern() {
        return this.configuredPattern;
    }

    public void setConfiguredPattern(ItemStack stack) {
        this.configuredPattern = stack;
    }

    public CompoundTag getConfiguredPatternTag() {
        return this.configuredPattern.getTag();
    }

    /** @return the first loaded fluid, or {@code null} if no fluid is present. */
    private BigFluidStack currentFluidStack() {
        return bigFluidStack.isEmpty() ? null : bigFluidStack.get(0);
    }

    public List<BigFluidStack> getBigFluidStack() {
        return this.bigFluidStack;
    }

    public void setBigFluidStack(List<BigFluidStack> b) {
        this.bigFluidStack = b;
    }

    public int getMultiblockSize() {
        return this.reactorSize;
    }

    public void setMultiblockSize(int s) {
        this.reactorSize = s;
    }

    @Override
    public Direction getMultiblockFacing() {
        return this.reactorFacing;
    }

    @Override
    public void setMultiblockFacing(Direction f) {
        this.reactorFacing = f;
    }

    public CNAdvancementBehaviour getAdvancement() {
        return this.advancement;
    }

    /** Main constructor allowing dependency injection for testability and DIP compliance. */
    public BoundingBox getMultiblockPos() {
        return this.reactorPos;
    }

    public void setMultiblockStructure(BoundingBox p) {
        this.reactorPos = p;
    }

    public void clearTimers() {
        this.cycleManager.clear();
    }

    public double getLiquidLife() {
        return this.liquidLife;
    }

    public void setLiquidLife(double l) {
        this.liquidLife = l;
    }

    public ReactorFrameDisplayManagerI getFrameDisplayManager() {
        return this.frameDisplayManager;
    }

    public ReactorInputFluidManagerI getInputFluidManager() {
        return this.inputFluidManager;
    }

    public ReactorOutputManagerI getOutputManager() {
        return this.outputManager;
    }

    public void setDisplayState(ReactorDisplayState state) {
        this.displayState = state;
    }

    public ReactorDisplayState getDisplayState() {
        return this.displayState;
    }

    /**
     * Main constructor allowing dependency injection for testability and DIP
     * compliance.
     */
    public ReactorControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.inventory = new ReactorControllerInventory(this);
        this.configuredPattern = ItemStack.EMPTY;

        this.inputManager = new ReactorInputManager();
        this.outputManager = new ReactorOutputManager();
        this.inputFluidManager = new ReactorInputFluidManager();
        this.alarmManager = new ReactorAlarmManager();
        this.frameDisplayManager = new ReactorFrameDisplayManager();

        this.bigFluidStack = new ArrayList<>();

        this.heatService = new DefaultHeatService(new HeatManager());
        this.persistenceService = new DefaultPersistenceService();
        this.meltdownExecutor = new ReactorMeltdownExecutor();
        this.heatCoordinator = new ReactorHeatUpdateCoordinator(this.heatService);
        this.fluidRateCalculator = new FluidConsumptionRateCalculator(this.heatService);
        this.meltdownMonitor = new ReactorMeltdownMonitor();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(advancement = new CNAdvancementBehaviour(this, CNAdvancement.T1_REACTOR, CNAdvancement.T2_REACTOR, CNAdvancement.T3_REACTOR, CNAdvancement.NO_TIME_TO_DIE, CNAdvancement.SILENCE_THE_CORE));
        this.alarmCoordinator = new ReactorAlarmCoordinator(advancement);
    }

    public boolean getAssembled() { // permet de savoir si le réacteur est formé ou pas.
        BlockState state = getBlockState();
        return state.getValue(ASSEMBLED);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        CompoundTag patternTag = this.getConfiguredPatternTag();

        if (patternTag == null || patternTag.isEmpty()) {
            return false;
        }

        ReactorGoggleTooltipRenderer.render(tooltip, displayState, patternTag.getInt("heat"), isPlayerSneaking, reactorSize);

        return true;
    }

    // (Si les methode read et write ne sont pas implémenté alors lorsque l'on
    // relance le monde minecraft les items dans le composant auront disparu !)
    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket); // Toujours en premier pour les coordonnées de base
        // delegate managers and persistence
        this.inputManager.read(compound);
        this.outputManager.read(compound);
        this.inputFluidManager.read(compound);
        this.alarmManager.read(compound);
        this.frameDisplayManager.read(compound);

        this.persistenceService.readBasicState(this, compound, clientPacket);
        this.needsToResolveEntities = true;

        this.cycleManager.clear();
        if (compound.contains("cycleManager")) {
            this.cycleManager.deserializeNBT(compound.getCompound("cycleManager"));
        }
    }

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        this.inputManager.write(compound);
        this.outputManager.write(compound);
        this.inputFluidManager.write(compound);
        this.alarmManager.write(compound);
        this.frameDisplayManager.write(compound);

        this.persistenceService.writeBasicState(this, compound, clientPacket);

        compound.put("cycleManager", cycleManager.serializeNBT());
    }

    public boolean isAssembled() {
        if (level == null)
            return false;
        try {
            return level.getBlockState(worldPosition).getValue(ASSEMBLED);
        } catch (Exception e) {
            return false;
        }
    }

    public void setAssembled(boolean assembled) {
        if (level == null)
            return;
        level.setBlockAndUpdate(worldPosition, getBlockState().setValue(ASSEMBLED, assembled));
        this.setChanged();
    }

    public void logReactorConnections(Player player) {
        ReactorDebugDiagnostics.sendReactorConnectionsTo(player, level, inputManager, inputFluidManager, outputManager, alarmManager);
    }

    private void updateReactorStateVisibility() {
        if (level == null || level.isClientSide) return;

        BlockState state = getBlockState();
        if (!(state.getBlock() instanceof ReactorControllerBlock)) return;

        boolean currentActive = state.getValue(ReactorControllerBlock.ACTIVE);

        // Le réacteur est "ACTIVE" (ON) seulement s'il est assemblé ET qu'il a ses ressources
        boolean targetActive = isAssembled() && heatCoordinator.canRun(getConfiguredPattern(), getDisplayState(), getInputFluidManager(), level, getAssembled());

        if (currentActive != targetActive) {
            level.setBlock(worldPosition, state.setValue(ReactorControllerBlock.ACTIVE, targetActive), 3);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide || isExploding)
            return;
        // Heat value written by the previous tick's handleAssembledState(); this tick's recalculated
        // heat is not visible here yet, so the alarm/meltdown danger flag is intentionally 1 tick behind.
        int currentHeat = (int) configuredPattern.getOrCreateTag().getDouble("heat");
        boolean isDanger = alarmCoordinator.computeDanger(currentHeat, this.getMultiblockSize());

        alarmCoordinator.update(level, alarmManager, isDanger);

        IReactorMeltdownMonitor.MeltdownState meltdownState = meltdownMonitor.tick(level, getBlockPos(), isDanger);

        if (meltdownState == IReactorMeltdownMonitor.MeltdownState.EXPLODE) {
            if (level instanceof ServerLevel serverLevel) {
                this.meltdownExecutor.triggerExplosion(serverLevel, getBlockPos(), reactorSize, countFuelRod);
            }
            isExploding = true;
            return;
        }

        if (!isEmptyConfiguredPattern()) {
            int heat = (int) this.getConfiguredPatternTag().getDouble("heat");
            countCoolerRod = this.getConfiguredPatternTag().getInt("countCoolerRod");
            countFuelRod = this.getConfiguredPatternTag().getInt("countFuelRod");
            totalHeatRatio = this.getConfiguredPatternTag().getInt("totalHeatRatio");
        }
        resolveEntitiesIfNeeded();

        ReactorInputSnapshot snapshot = ReactorInputSnapshotBuilder.build(level, inputManager, inputFluidManager);
        this.displayState = new ReactorDisplayState(snapshot.items(), snapshot.fluids(), snapshot.maxFluidCapacity());
        this.bigFluidStack = snapshot.fluids();

        updateReactorStateVisibility();
        handleAssembledState();
    }

    // --- extracted sub-steps to keep single responsibility per method ---
    private void resolveEntitiesIfNeeded() {
        if (!needsToResolveEntities)
            return;
        List<IItemHandler> handlers = inputManager.getItemHandlers(level);
        CreateNuclear.LOGGER.warn("Resolving inputs after load, handlers found: {}", handlers.size());
        needsToResolveEntities = false;
        this.setChanged();
    }

    /**
     * Tick logic applied while the reactor is assembled: if it doesn't have
     * enough fuel ({@link IReactorHeatUpdateCoordinator#canRun}), only the
     * displayed heat is updated (and outputs stopped); otherwise the actual
     * heat is calculated, fluid consumed, the item consumption cycle advanced,
     * and outputs rotated according to the heat.
     */
    private void handleAssembledState() {
        if (!heatCoordinator.canRun(configuredPattern, displayState, inputFluidManager, level, isAssembled())) {
            heatCoordinator.updateHeatOnly(configuredPattern, displayState, currentFluidStack(), totalHeatRatio, inventory, level, isAssembled());
            if (!outputManager.getBlocksPosition(getLevel()).isEmpty()) {
                outputManager.rotateOutputs(getLevel(), getAssembled(), 0);
            }

            setChanged();
            notifyUpdate();
            return;
        }

        setChanged();
        notifyUpdate();

        BigFluidStack fluidStack = currentFluidStack();
        heat = heatCoordinator.calculateAndWriteHeat(configuredPattern, fluidStack, totalHeatRatio, inventory, level);
        fluidBuffer = fluidRateCalculator.tick(fluidStack, reactorSize, level, inputFluidManager, fluidBuffer);
        cycleManager.update(configuredPattern, level, inputManager, level.getGameTime() % 20 == 0);

        if (IHeat.HeatLevel.isNotDanger(heat, getMultiblockSize()) && !outputManager.getBlocksPosition(level).isEmpty()) {
            outputManager.rotateOutputs(getLevel(), getAssembled(), heat);
        }
    }

    private boolean isEmptyConfiguredPattern() {
        return configuredPattern.isEmpty() || this.getConfiguredPatternTag().isEmpty();
    }

    private boolean updateLiquidTimers() {
        liquidLife -= 1;
        return liquidLife <= 0;
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
        // Breaking a fluid input discards its tank contents along with the block entity.
        // Re-evaluate the fluid lock so a different fluid can be accepted once no remaining
        // input still holds liquid — otherwise the controller stays locked to the old fluid.
        clearLockIfAllInputsEmpty();
    }

    public void addAlarm(BlockPos alarmPos) {
        this.alarmManager.addBlock(alarmPos);
        this.setChanged();
    }

    public void removeAlarm(BlockPos alarmPos) {
        this.alarmManager.removeBlock(alarmPos);
        this.setChanged();
    }

    public void removeIOAll() {
        this.inputManager.clearInvalid(level);
        this.outputManager.clearInvalid(level);
        this.inputFluidManager.clearInvalid(level);
        this.alarmManager.clearInvalid(level);
        this.setChanged();
    }

    /** Try to lock this controller to the given Fluid. Returns true if allowed. */
    public boolean tryLockFluid(Fluid fluid) {
        // Fluid locks are server-authoritative and persisted per-level via PersistentFluidLocks.
        // On the client there is no lock to enforce, so stay permissive.
        if (level instanceof ServerLevel serverLevel) {
            return PersistentFluidLocks.get(serverLevel).tryLock(getBlockPos(), fluid);
        }
        return true;
    }

    /** Returns whether the given FluidStack is acceptable for this controller. */
    public boolean canAcceptFluid(FluidStack stack) {
        if (stack == null || stack.isEmpty())
            return true;
        if (level instanceof ServerLevel serverLevel) {
            return PersistentFluidLocks.get(serverLevel).canAccept(getBlockPos(), stack.getFluid());
        }
        return true;
    }

    /** Force-clear the lock on this controller. */
    public void clearLock() {
        if (level instanceof ServerLevel serverLevel) {
            PersistentFluidLocks.get(serverLevel).clearLock(getBlockPos());
            setChanged();
            sendData();
        }
    }

    public void clearLockIfAllInputsEmpty() {
        if (level == null || level.isClientSide)
            return;

        BlockPattern<TypeMultiblock> structure = CNMultiblock.REGISTRATE_MULTIBLOCK.findStructure(level, getBlockPos(), this);
        // findStructure renvoie null si la structure n'est plus formée : on retombe
        // alors sur la dernière taille assemblée (reactorSize) pour éviter un NPE serveur.
        final int SCAN_RADIUS = structure != null ? structure.data().getSize() : reactorSize;
        BlockPos center = getBlockPos();
        boolean anyNonEmpty = false;

        for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS && !anyNonEmpty; dx++) {
            for (int dy = -SCAN_RADIUS; dy <= SCAN_RADIUS && !anyNonEmpty; dy++) {
                for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS && !anyNonEmpty; dz++) {
                    BlockPos p = center.offset(dx, dy, dz);
                    BlockEntity be = level.getBlockEntity(p);
                    if (!(be instanceof ReactorFluidInputEntity))
                        continue;

                    IFluidHandler handler = be.getCapability(ForgeCapabilities.FLUID_HANDLER).orElse(null);
                    if (handler == null)
                        continue;

                    for (int t = 0; t < handler.getTanks(); t++) {
                        if (!handler.getFluidInTank(t).isEmpty()) {
                            anyNonEmpty = true;
                            break;
                        }
                    }
                }
            }
        }

        if (!anyNonEmpty)
            clearLock();
    }
}