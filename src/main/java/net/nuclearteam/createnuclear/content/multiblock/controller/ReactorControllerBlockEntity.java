package net.nuclearteam.createnuclear.content.multiblock.controller;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.IInteractionChecker;
import net.minecraft.ChatFormatting;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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
import net.nuclearteam.createnuclear.api.multiblock.IMultiblockController;
import net.nuclearteam.createnuclear.content.logistics.BigFluidStack;
import net.nuclearteam.createnuclear.content.multiblock.CNMultiblock;
import net.nuclearteam.createnuclear.content.multiblock.alarm.ReactorAlarm;
import net.nuclearteam.createnuclear.content.multiblock.controller.display.ReactorDisplayState;
import net.nuclearteam.createnuclear.content.multiblock.controller.display.ReactorGoggleTooltipRenderer;
import net.nuclearteam.createnuclear.content.multiblock.controller.service.*;
import net.nuclearteam.createnuclear.content.multiblock.controller.snapshot.ReactorInputSnapshot;
import net.nuclearteam.createnuclear.content.multiblock.controller.snapshot.ReactorInputSnapshotBuilder;
import net.nuclearteam.createnuclear.content.multiblock.IHeat;
import net.nuclearteam.createnuclear.content.multiblock.output.ReactorOutput;
import net.nuclearteam.createnuclear.content.multiblock.output.ReactorOutputEntity;
import net.nuclearteam.createnuclear.foundation.advancement.CNAdvancement;
import net.nuclearteam.createnuclear.foundation.advancement.CNAdvancementBehaviour;
import net.nuclearteam.createnuclear.content.multiblock.controller.consumable.ConsumptionCycleManager;
import net.nuclearteam.createnuclear.foundation.utility.NotifyUtil;
import net.nuclearteam.createnuclear.infrastructure.config.CNConfigs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import net.minecraft.world.item.Item;

import net.nuclearteam.createnuclear.content.multiblock.input.fluid.PersistentFluidLocks;
import net.nuclearteam.createnuclear.content.multiblock.input.fluid.ReactorFluidInputEntity;
import net.nuclearteam.createnuclear.content.multiblock.controller.manager.*;
import net.nuclearteam.createnuclear.foundation.utility.CreateNuclearLang;
import net.nuclearteam.createnuclear.content.multiblock.pattern.ReactorPattern;
import net.nuclearteam.createnuclear.content.multiblock.reactorLogic.HeatManager;
import net.nuclearteam.createnuclear.content.multiblock.controller.consumable.PatternReader;
import net.nuclearteam.createnuclear.api.multiblock.rods.RodType;

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
    private int explosionCountdown = 0;
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
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(advancement = new CNAdvancementBehaviour(this, CNAdvancement.T1_REACTOR, CNAdvancement.T2_REACTOR, CNAdvancement.T3_REACTOR, CNAdvancement.NO_TIME_TO_DIE, CNAdvancement.SILENCE_THE_CORE));
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
        boolean targetActive = isAssembled() && isReadyToRun();

        if (currentActive != targetActive) {
            level.setBlock(worldPosition, state.setValue(ReactorControllerBlock.ACTIVE, targetActive), 3);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide || isExploding)
            return;
        int currentHeat = (int) configuredPattern.getOrCreateTag().getDouble("heat");
        boolean isDanger = IHeat.HeatLevel.of(currentHeat, this.getMultiblockSize()) == IHeat.HeatLevel.DANGER;

        activateAlarms(isDanger);

        currentHeat = isEmptyConfiguredPattern() ? 0 : (int) this.getConfiguredPatternTag().getDouble("heat");

        // Récupération des configs pour l'utilitaire
        int configRadius = CNConfigs.server().notify.distanceOfWarning.get();
        boolean configWarnAll = CNConfigs.server().notify.warnAllPlayers.get();

        if (isDanger) {
            explosionCountdown++;
            int secondsLeft = (300 - explosionCountdown) / 20;

            // --- AFFICHAGE ALERTES ACTION BAR (TRADUITES) ---
            if (secondsLeft <= 10 && secondsLeft > 0) {
                boolean isWhite = (level.getGameTime() / 5) % 2 == 0;
                ChatFormatting flashColor = isWhite ? ChatFormatting.WHITE : ChatFormatting.RED;

                NotifyUtil.sendActionBar(level, getBlockPos(),
                        CreateNuclearLang.translate("notification.reactor.meltdown_in")
                                .add(CreateNuclearLang.number(secondsLeft))
                                .add(CreateNuclearLang.translate("generic.unit.seconds")),
                        flashColor, configRadius, configWarnAll);

            } else if (secondsLeft > 10 && explosionCountdown % 20 == 0) {
                NotifyUtil.sendActionBar(level, getBlockPos(),
                        CreateNuclearLang.translate("notification.reactor.overheating"),
                        ChatFormatting.DARK_RED, configRadius, configWarnAll);
            }

            // --- MOMENT DE L'EXPLOSION ---
            if (explosionCountdown >= 300) {
                NotifyUtil.sendTitle(level, getBlockPos(),
                        CreateNuclearLang.translate("notification.reactor.critical_failure"),
                        CreateNuclearLang.translate("notification.reactor.imminent_explosion"),
                        ChatFormatting.DARK_RED, configRadius, configWarnAll, 0, 40, 10);

                if (level instanceof ServerLevel serverLevel) {
                    this.meltdownExecutor.triggerExplosion(serverLevel, getBlockPos(), reactorSize, countFuelRod);
                }
                isExploding = true;
                return;
            }
        } else {
            // --- CŒUR STABILISÉ ---
            if (explosionCountdown > 0) {
                NotifyUtil.sendActionBar(level, getBlockPos(),
                        CreateNuclearLang.translate("notification.reactor.stabilized"),
                        ChatFormatting.GREEN, configRadius, configWarnAll);
            }
            explosionCountdown = 0;
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

    private void activateAlarms(boolean activate) {
        if (alarmManager == null)
            return;
        for (BlockPos pos : alarmManager.getBlocksPosition(level)) {
            if (level.isLoaded(pos)) {
                BlockState state = level.getBlockState(pos);
                if (state.getBlock() instanceof ReactorAlarm && state.getValue(ReactorAlarm.POWERED) != activate) {
                    level.setBlock(pos, state.setValue(ReactorAlarm.POWERED, activate), 3);
                    if (activate) this.advancement.awardPlayer(CNAdvancement.SILENCE_THE_CORE);
                }
            }
        }
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

    private void handleAssembledState() {
        if (!isReadyToRun()) {
            updateHeatOnly();
            if (!this.outputManager.getBlocksPosition().isEmpty())
                rotate(getBlockState(), getLevel(), 0);
            this.setChanged();
            this.notifyUpdate();
            return;
        }
        // ready to run
        this.setChanged();
        this.notifyUpdate();
        BigFluidStack fluidStack = bigFluidStack.isEmpty() ? null : bigFluidStack.get(0);
        heat = (int) heatService.calculateHeat(fluidStack, totalHeatRatio, inventory, level);
        this.getConfiguredPatternTag().putDouble("heat", heat);

        if (fluidStack != null) {
            if (fluidStack.amount > 1) {
                double amountPerCycle = (double) fluidStack.getFluidtype(level).efficiency();
                switch (reactorSize) {
                    case 5 -> amountPerCycle /= (double) heatService.getLiquidTimer() / 40;
                    case 7 -> amountPerCycle /= (double) heatService.getLiquidTimer() / 147;
                    case 9 -> amountPerCycle /= (double) heatService.getLiquidTimer() / 360;
                }

                fluidBuffer += amountPerCycle;

                if (fluidBuffer >= 1.0) {
                    int toExtract = (int) Math.floor(fluidBuffer);

                    boolean extracted = inputFluidManager.extractFluids(level, toExtract);

                    if (extracted) {
                        fluidBuffer -= toExtract;
                        // this.liquidLife = calculateLiquidProgress();
                    }
                }
            }
        }

        if (cycleManager.isEmpty() && !isEmptyConfiguredPattern()) {
            cycleManager.startCycle(configuredPattern, level);
        }

        if (!cycleManager.isEmpty()) {
            if (level.getGameTime() % 20 == 0
                    && cycleManager.hasPatternChanged(configuredPattern, level)) {
                cycleManager.resetCycle(configuredPattern, level, inputManager);
            }
            cycleManager.tick(inputManager, level);
        }
        if (IHeat.HeatLevel.isNotDanger(heat, this.getMultiblockSize())) {
            // normal
            if (!this.outputManager.getBlocksPosition().isEmpty()) {
                rotate(getBlockState(), getLevel(), heat);
            }
        }
    }

    private boolean isReadyToRun() {
        if (isEmptyConfiguredPattern() || this.inputFluidManager.size() == 0 || !isAssembled()) {
            return false;
        }

        Map<Item, Integer> patternCounts = PatternReader.readItemCounts(configuredPattern);
        Map<Item, Integer> currentItems = this.displayState != null && this.displayState.items() != null 
                ? this.displayState.items() : Collections.emptyMap();
        
        boolean hasAnyFuel = false;
        
        for (Map.Entry<Item, Integer> entry : patternCounts.entrySet()) {
            Item requiredItem = entry.getKey();
            int requiredCount = entry.getValue();
            
            RodType rodType = RodType.resolveRodType(requiredItem, level);
            if (rodType.isNotEmptyItem() && rodType.type() == RodType.TypeRod.FUEL) {
                hasAnyFuel = true;
                int availableCount = currentItems.getOrDefault(requiredItem, 0);
                if (availableCount <= 0) {
                    return false;
                }
            }
        }

        return hasAnyFuel;
    }

    private void updateHeatOnly() {
        // Guard against empty fluid list — HeatManager accepts null for empty/no-fluid
        // case
        BigFluidStack fluid = bigFluidStack.isEmpty() ? null : bigFluidStack.get(0);
        heat = 0;

        if (!isEmptyConfiguredPattern() && isAssembled()) {
            Map<Item, Integer> patternCounts = PatternReader.readItemCounts(configuredPattern);
            Map<Item, Integer> currentItems = this.displayState != null && this.displayState.items() != null 
                    ? this.displayState.items() : Collections.emptyMap();
            
            for (Map.Entry<Item, Integer> entry : patternCounts.entrySet()) {
                Item requiredItem = entry.getKey();
                int requiredCount = entry.getValue();
                
                int availableCount = currentItems.getOrDefault(requiredItem, 0);
                if (availableCount < requiredCount) {
                    this.getConfiguredPatternTag().putDouble("heat", heat);
                    return;
                }
            }

            heat = (int) heatService.calculateHeat(fluid, totalHeatRatio, inventory, level);
        }
        this.getConfiguredPatternTag().putDouble("heat", heat);
    }

    private boolean isEmptyConfiguredPattern() {
        return configuredPattern.isEmpty() || this.getConfiguredPatternTag().isEmpty();
    }

    private boolean updateLiquidTimers() {
        liquidLife -= 1;
        return liquidLife <= 0;
    }

    public void rotate(BlockState state, Level level, int rotation) {
        if (this.outputManager.getBlocksPosition().isEmpty())
            return;
        
        int RPMDivider = 32;
        int totalRpm = rotation / RPMDivider;

        int remainingRotation = totalRpm % this.outputManager.getBlocksPosition().size();
        for (int i = 0; i < this.outputManager.getBlocksPosition().size(); i++) {
            int dividedRotation = (totalRpm / this.outputManager.getBlocksPosition().size())
                    + (i < remainingRotation ? 1 : 0);
            BlockPos pos = this.outputManager.getBlocksPosition().get(i);

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
                    entity.setSpeedAndUpdate(dividedRotation);
                    entity.updateGeneratedRotation();

                }
            } else {
                if (level.getBlockState(pos).getBlock() instanceof ReactorOutput block) {
                    ReactorOutputEntity entity = block.getBlockEntityType().getBlockEntity(level, pos);
                    entity.setSpeedAndUpdate(0);
                    entity.heat = 0;
                    entity.updateSpeed = true;
                    entity.updateGeneratedRotation();
                }
            }
        }
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

        var structure = CNMultiblock.REGISTRATE_MULTIBLOCK.findStructure(level, getBlockPos(), this);
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