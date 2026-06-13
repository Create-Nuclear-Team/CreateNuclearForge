package net.nuclearteam.createnuclear.content.multiblock.controller;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.foundation.utility.IInteractionChecker;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

import net.nuclearteam.createnuclear.CNBlocks;
import net.nuclearteam.createnuclear.CNEntityType;
import net.nuclearteam.createnuclear.CreateNuclear;
import net.nuclearteam.createnuclear.api.multiblock.IMultiblockController;
import net.nuclearteam.createnuclear.content.logistics.BigFluidStack;
import net.nuclearteam.createnuclear.content.multiblock.CNMultiblock;
import net.nuclearteam.createnuclear.content.multiblock.alarm.ReactorAlarm;
import net.nuclearteam.createnuclear.content.multiblock.input.fluid.FluidLockManager;
import net.nuclearteam.createnuclear.content.multiblock.IHeat;
import net.nuclearteam.createnuclear.content.explosion.NuclearExplosionEntity;
import net.nuclearteam.createnuclear.content.multiblock.output.ReactorOutput;
import net.nuclearteam.createnuclear.content.multiblock.output.ReactorOutputEntity;
import net.nuclearteam.createnuclear.foundation.advancement.CNAdvancement;
import net.nuclearteam.createnuclear.foundation.advancement.CNAdvancementBehaviour;
import net.nuclearteam.createnuclear.content.multiblock.controller.consumable.ConsumptionCycleManager;
import net.nuclearteam.createnuclear.foundation.utility.NotifyUtil;
import net.nuclearteam.createnuclear.infrastructure.config.CNConfigs;
import net.nuclearteam.createnuclear.infrastructure.worldgen.biome.CNBiomes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.nuclearteam.createnuclear.content.multiblock.input.fluid.PersistentFluidLocks;
import net.nuclearteam.createnuclear.content.multiblock.input.fluid.ReactorFluidInputEntity;
import net.nuclearteam.createnuclear.content.multiblock.input.fluid.VirtualReactorInputFluid;
import net.nuclearteam.createnuclear.content.multiblock.input.item.VirtualReactorInputsItem;
import net.nuclearteam.createnuclear.content.multiblock.controller.manager.*;
import net.nuclearteam.createnuclear.foundation.utility.CreateNuclearLang;
import net.nuclearteam.createnuclear.content.multiblock.pattern.ReactorPattern;
import net.nuclearteam.createnuclear.content.multiblock.reactorLogic.HeatManager;
import net.nuclearteam.createnuclear.content.multiblock.controller.service.IHeatService;
import net.nuclearteam.createnuclear.content.multiblock.controller.service.DefaultHeatService;
import net.nuclearteam.createnuclear.content.multiblock.controller.service.IPersistenceService;
import net.nuclearteam.createnuclear.content.multiblock.controller.service.DefaultPersistenceService;

import java.util.Map;

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
    private int countUraniumRod;
    private int countGraphiteRod;
    private int heat;
    private int explosionCountdown = 0;
    private boolean isExploding = false;

    private final ConsumptionCycleManager cycleManager = new ConsumptionCycleManager();
    private double liquidLife;
    private ItemStack configuredPattern;

    private BigItemStack bigFuelItem;
    private BigItemStack bigCoolerItem;
    private List<BigFluidStack> bigFluidStack;

    private int reactorSize = 0;
    private String reactorFacing = "null";
    // les pos sont [xMin, xMax, yMin, yMax, zMin, zMax]
    private int[] reactorPos;


    private boolean needsToResolveEntities = false;
    private double fluidBuffer = 0.0;

    private CNAdvancementBehaviour advancement;

    private final ReactorInputManagerI inputManager;
    private final ReactorOutputManagerI outputManager;
    private final ReactorInputFluidManagerI inputFluidManager;
    private final ReactorAlarmManagerI alarmManager;
    private final ReactorFrameDisplayManagerI frameDisplayManager;

    // Client Display Data (Synced via NBT)
    private Map<Item, Integer> clientDisplayItems = new HashMap<>();
    private List<BigFluidStack> clientDisplayFluids = new ArrayList<>();
    private long clientMaxFluidCapacity = 0;

    // services (dependencies) - abstracted behind interfaces to follow DIP
    private final IHeatService heatService;
    private final IPersistenceService persistenceService;

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

    public BigItemStack getBigFuelItem() {
        return this.bigFuelItem;
    }

    public void setBigFuelItem(BigItemStack b) {
        this.bigFuelItem = b;
    }

    public BigItemStack getBigCoolerItem() {
        return this.bigCoolerItem;
    }

    public void setBigCoolerItem(BigItemStack b) {
        this.bigCoolerItem = b;
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
    public String getMultiblockFacing() {
        return this.reactorFacing;
    }

    @Override
    public void setMultiblockFacing(String f) {
        this.reactorFacing = f;
    }

    public CNAdvancementBehaviour getAdvancement() {
        return this.advancement;
    }

    /** Main constructor allowing dependency injection for testability and DIP compliance. */
    public int[] getMultiblockPos() {
        return this.reactorPos;
    }

    public void setMultiblockStructure(int[] p) {
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

        this.bigFuelItem = new BigItemStack(ItemStack.EMPTY);
        this.bigCoolerItem = new BigItemStack(ItemStack.EMPTY);
        this.bigFluidStack = new ArrayList<>();

        this.heatService = new DefaultHeatService(new HeatManager());
        this.persistenceService = new DefaultPersistenceService();
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

        CreateLang.translate("gui.gauge.info_header")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        IHeat.HeatLevel.getName("reactor_controller").style(ChatFormatting.GRAY).forGoggles(tooltip);

        IHeat.HeatLevel.getFormattedHeatText(patternTag.getInt("heat")).forGoggles(tooltip);

        if (clientDisplayItems.isEmpty()) {
            CreateNuclearLang.builder()
                    .add(Component.translatable("tooltip.item.empty.rod").withStyle(ChatFormatting.GRAY))
                    .add(CreateNuclearLang.number(0).style(ChatFormatting.GOLD))
                    .forGoggles(tooltip);
        } else {
            for (Map.Entry<Item, Integer> entry : clientDisplayItems.entrySet()) {
                ItemStack stack = new ItemStack(entry.getKey());
                int count = entry.getValue();
                CreateNuclearLang.itemName(stack)
                        .style(ChatFormatting.GRAY)
                        .text(": ")
                        .add(CreateNuclearLang.number(count).style(ChatFormatting.GOLD))
                        .forGoggles(tooltip);
            }
        }

        if (clientDisplayFluids.isEmpty()) {
            CreateNuclearLang
                    .translate("tooltip.fluid.none")
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip);

            CreateNuclearLang.builder()
                    .text(TooltipHelper.makeProgressBar(5, 0))
                    .style(ChatFormatting.BLUE)
                    .space()
                    .text("0%")
                    .forGoggles(tooltip);
        } else {
            for (BigFluidStack stack : clientDisplayFluids) {
                float fillRatio = clientMaxFluidCapacity > 0 ? (float) stack.amount / clientMaxFluidCapacity : 0;
                if (fillRatio >= 0.99f)
                    fillRatio = 1.0f;
                int filledBars = (int) Math.round(fillRatio * 5);

                CreateNuclearLang
                        .translate("tooltip.fluid", stack.stack.getDisplayName())
                        .style(ChatFormatting.GRAY)
                        .forGoggles(tooltip);

                CreateNuclearLang.builder()
                        .text(TooltipHelper.makeProgressBar(5, filledBars))
                        .style(ChatFormatting.BLUE)
                        .space()
                        .text(Math.round(fillRatio * 100) + "%")
                        .forGoggles(tooltip);
            }
        }

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

        if (clientPacket) {
            clientDisplayItems.clear();
            if (compound.contains("clientDisplayItems")) {
                ListTag list = compound.getList("clientDisplayItems",
                        Tag.TAG_COMPOUND);
                for (int i = 0; i < list.size(); i++) {
                    CompoundTag tag = list.getCompound(i);
                    Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(tag.getString("Item")));
                    if (item != null) {
                        clientDisplayItems.put(item, tag.getInt("Count"));
                    }
                }
            }

            clientDisplayFluids.clear();
            if (compound.contains("clientDisplayFluids")) {
                ListTag list = compound.getList("clientDisplayFluids",
                        Tag.TAG_COMPOUND);
                for (int i = 0; i < list.size(); i++) {
                    CompoundTag tag = list.getCompound(i);
                    FluidStack fstack = FluidStack
                            .loadFluidStackFromNBT(tag);
                    clientDisplayFluids.add(new BigFluidStack(fstack, tag.getInt("BigAmount")));
                }
            }
            clientMaxFluidCapacity = compound.getLong("clientMaxFluidCapacity");
        }

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

        if (clientPacket) {
            ListTag displayItemsTag = new ListTag();
            for (Map.Entry<Item, Integer> entry : clientDisplayItems.entrySet()) {
                CompoundTag tag = new CompoundTag();
                tag.putString("Item", ForgeRegistries.ITEMS.getKey(entry.getKey()).toString());
                tag.putInt("Count", entry.getValue());
                displayItemsTag.add(tag);
            }
            compound.put("clientDisplayItems", displayItemsTag);

            ListTag displayFluidsTag = new ListTag();
            for (BigFluidStack stack : clientDisplayFluids) {
                CompoundTag tag = new CompoundTag();
                stack.stack.writeToNBT(tag);
                tag.putInt("BigAmount", stack.amount);
                displayFluidsTag.add(tag);
            }
            compound.put("clientDisplayFluids", displayFluidsTag);
            compound.putLong("clientMaxFluidCapacity", clientMaxFluidCapacity);
        }

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

    public void logReactorConnections() {
        CreateNuclear.LOGGER.debug("Reactor input count: {}", this.inputManager.size());
        CreateNuclear.LOGGER.debug("Reactor output count: {}", this.outputManager.size());
        CreateNuclear.LOGGER.debug("Reactor fluid input count: {}", this.inputFluidManager.size());

        for (BlockPos pos : this.inputManager.getBlocksPosition()) {
            CreateNuclear.LOGGER.debug("Registered reactor input position: {}", pos);
        }

        for (BlockPos pos : this.inputManager.getBlocksPosition(level)) {
            CreateNuclear.LOGGER.debug("Valid reactor input position: {}", pos);
        }

        for (BlockPos pos : this.outputManager.getBlocksPosition()) {
            CreateNuclear.LOGGER.debug("Registered reactor output position: {}", pos);
        }

        for (BlockPos pos : this.outputManager.getBlocksPosition(level)) {
            CreateNuclear.LOGGER.debug("Valid reactor output position: {}", pos);
        }

        for (BlockPos pos : this.inputFluidManager.getBlocksPosition()) {
            CreateNuclear.LOGGER.debug("Registered reactor fluid input position: {}", pos);
        }

        for (BlockPos pos : this.inputFluidManager.getBlocksPosition(level)) {
            CreateNuclear.LOGGER.debug("Valid reactor fluid input position: {}", pos);
        }
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
        boolean isDanger = IHeat.HeatLevel.of(currentHeat) == IHeat.HeatLevel.DANGER;

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

                triggerNuclearExplosion();
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
            countGraphiteRod = this.getConfiguredPatternTag().getInt("countGraphiteRod");
            countUraniumRod = this.getConfiguredPatternTag().getInt("countUraniumRod");
        }
        resolveEntitiesIfNeeded();
        if (!isAssembled())
            return;

        // Populate display fields for client sync
        clientDisplayItems.clear();
        List<IItemHandler> itemHandlers = this.inputManager.getItemHandlers(level);
        for (IItemHandler h : itemHandlers) {
            for (int s = 0; s < h.getSlots(); s++) {
                ItemStack st = h.getStackInSlot(s);
                if (!st.isEmpty()) {
                    clientDisplayItems.put(st.getItem(),
                            clientDisplayItems.getOrDefault(st.getItem(), 0) + st.getCount());
                }
            }
        }

        clientMaxFluidCapacity = 0;
        List<IFluidHandler> fhandlers = this.inputFluidManager.getFuildHandlers(level);
        for (IFluidHandler h : fhandlers) {
            if (h.getTanks() > 0) {
                clientMaxFluidCapacity += h.getTankCapacity(0);
            }
        }

        VirtualReactorInputsItem virtualReactorInputsItem = inputManager.getInventory(level);
        VirtualReactorInputFluid virtualReactorInputFluid = inputFluidManager.getInventory(level);
        clientDisplayFluids = VirtualReactorInputFluid.toBigList(virtualReactorInputFluid.fluids());

        this.bigFuelItem = virtualReactorInputsItem.getBigFuelRod();
        this.bigCoolerItem = virtualReactorInputsItem.getBigCooledRod();
        this.bigFluidStack = VirtualReactorInputFluid.toBigList(virtualReactorInputFluid.fluids());

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
        heat = (int) heatService.calculateHeat(bigFuelItem, bigCoolerItem, fluidStack, countGraphiteRod,
                countUraniumRod, inventory, level);
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
        if (IHeat.HeatLevel.isNotDanger(heat)) {
            // normal
            if (!this.outputManager.getBlocksPosition().isEmpty()) {
                rotate(getBlockState(), getLevel(), heat);
            }
        }
    }

    private void triggerNuclearExplosion() {
        if (isExploding)
            return;
        isExploding = true;

        BlockPos explosionPos = getBlockPos().above(5);
        int configRadius = CNConfigs.server().notify.distanceOfWarning.get();
        boolean configWarnAll = CNConfigs.server().notify.warnAllPlayers.get();

        if (level instanceof ServerLevel serverLevel) {
            // --- MESSAGE D'EXPLOSION FINALE (TRADUIT) ---
            NotifyUtil.sendTitle(level, getBlockPos(),
                    CreateNuclearLang.translate("notification.reactor.destroyed"),
                    CreateNuclearLang.translate("notification.reactor.meltdown_finished"),
                    ChatFormatting.DARK_RED, configRadius, configWarnAll, 10, 60, 20);

            NuclearExplosionEntity explosion = new NuclearExplosionEntity(
                    CNEntityType.NUCLEAR_EXPLOSION.get(),
                    serverLevel);

            // --- CALCUL D'EXPLOSION ÉQUILIBRÉ ---

            // 1. On définit un modificateur de structure plus progressif
            // Au lieu de 1, 2, 3, on utilise un ratio basé sur la taille réelle
            float structureFactor = reactorSize / 5.0F; // 5x5 -> 1.0 | 7x7 -> 1.4 | 9x9 -> 1.8

            /*
             * 2. Utilisation de la racine carrée (Mth.sqrt)
             * L'uranium augmente la puissance, mais avec des rendements décroissants.
             * Plus on ajoute d'uranium, plus chaque barre supplémentaire ajoute "moins" au
             * rayon.
             */
            float fuelImpact = Mth.sqrt(countUraniumRod) * 0.3F;

            // 3. Taille finale : Base constante + (Impact Fuel * Facteur Structure)
            float size = 1.5F + (fuelImpact * structureFactor);

            size = Mth.clamp(size, 1.0F, 10.0F);

            explosion.setPos(explosionPos.getX() + 0.5D, explosionPos.getY() + 10.0D, explosionPos.getZ() - 2.0D);
            explosion.setSize(size);

            // Griefing selon les gamerules
            boolean griefing = serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
            explosion.setNoGriefing(!griefing);

            serverLevel.addFreshEntity(explosion);

            // On détruit le bloc après avoir envoyé les messages
            level.destroyBlock(getBlockPos(), false);

            // Changement de biome vers Irradiated Plain
            changeBiome(CNBiomes.Irradiated.PLAIN, (int) size * 30, explosionPos, serverLevel);
        }
    }

    private void changeBiome(ResourceKey<Biome> biomeResourceKey, int radius, BlockPos center, ServerLevel serverLevel) {
        Registry<Biome> biomeRegistry = serverLevel.registryAccess().registryOrThrow(Registries.BIOME);
        Holder<Biome> targetBiomeHolder = biomeRegistry.getHolderOrThrow(biomeResourceKey);

        // Vérification rapide du centre
        Holder<Biome> current = serverLevel.getBiome(center);
        if (current.is(biomeResourceKey)) {
            return;
        }

        // Définition de la zone de recherche (Bounding Box carrée qui contient le
        // cercle)
        int minX = center.getX() - radius;
        int maxX = center.getX() + radius;
        int minZ = center.getZ() - radius;
        int maxZ = center.getZ() + radius;

        double radiusSq = (double) radius * radius;
        ArrayList<ChunkAccess> chunks = new ArrayList<>();

        // On parcourt les chunks impactés
        for (int cz = SectionPos.blockToSectionCoord(minZ); cz <= SectionPos.blockToSectionCoord(maxZ); ++cz) {
            for (int cx = SectionPos.blockToSectionCoord(minX); cx <= SectionPos.blockToSectionCoord(maxX); ++cx) {
                ChunkAccess chunkAccess = serverLevel.getChunk(cx, cz, ChunkStatus.FULL, false);
                if (chunkAccess != null) {
                    // On utilise un resolver personnalisé qui vérifie la distance
                    chunkAccess.fillBiomesFromNoise(
                            createCircularResolver(targetBiomeHolder, center, radiusSq, serverLevel),
                            serverLevel.getChunkSource().randomState().sampler());
                    chunkAccess.setUnsaved(true);
                    chunks.add(chunkAccess);
                }
            }
        }

        // Notification aux clients
        serverLevel.getChunkSource().chunkMap.resendBiomesForChunks(chunks);
    }

    // Le resolver magique pour la forme circulaire
    private BiomeResolver createCircularResolver(Holder<Biome> targetBiome, BlockPos center, double radiusSq,
            ServerLevel level) {
        return (x, y, z, noise) -> {
            // x, y, z ici sont en "biome coordinates" (1 unité = 4 blocs)
            // On les multiplie par 4 pour revenir à une échelle de blocs
            int blockX = x << 2;
            int blockZ = z << 2;

            double distX = blockX - center.getX();
            double distZ = blockZ - center.getZ();

            // Equation du cercle : x² + z² <= r²
            if ((distX * distX) + (distZ * distZ) <= radiusSq) {
                return targetBiome;
            }

            // Si hors du cercle, on garde le biome d'origine (ou on laisse le bruit faire)
            // Note : Ici on demande au niveau le biome actuel à cette position
            return level.getBiome(new BlockPos(blockX, y << 2, blockZ));
        };
    }

    private boolean isReadyToRun() {
        return !isEmptyConfiguredPattern()
                && bigFuelItem.count > 0
                && bigCoolerItem.count > 0
                && this.inputManager.size() > 0
                && this.inputFluidManager.size() > 0;
    }

    private void updateHeatOnly() {
        // Guard against empty fluid list — HeatManager accepts null for empty/no-fluid
        // case
        BigItemStack fuel = bigFuelItem;
        BigItemStack cooler = bigCoolerItem;
        BigFluidStack fluid = bigFluidStack.isEmpty() ? null : bigFluidStack.get(0);
        double heat = 0;

        if (!isEmptyConfiguredPattern()) {
            heat = heatService.calculateHeat(fuel, cooler, fluid, countGraphiteRod, countUraniumRod, inventory, level);
            this.getConfiguredPatternTag().putDouble("heat", heat);
        }
    }

    private boolean isEmptyConfiguredPattern() {
        return configuredPattern.isEmpty() || this.getConfiguredPatternTag().isEmpty();
    }

    private boolean updateLiquidTimers() {
        liquidLife -= 1;
        return liquidLife <= 0;
    }

    @Deprecated
    private BlockPos getBlockPosForReactor(char character) {
        BlockPos pos = pattern.VerifyPattern5x5(character);
        BlockPos posController = getBlockPos();
        BlockPos posInput = new BlockPos(posController.getX(), posController.getY(), posController.getZ());

        int[][] directions = {
                { 0, 0, pos.getX() }, // NORTH
                { 0, 0, -pos.getX() }, // SOUTH
                { -pos.getX(), 0, 0 }, // EAST
                { pos.getX(), 0, 0 } // WEST
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
        if (this.outputManager.getBlocksPosition().isEmpty())
            return;
        int remainingRotation = rotation % this.outputManager.getBlocksPosition().size();
        for (int i = 0; i < this.outputManager.getBlocksPosition().size(); i++) {
            int dividedRotation = (rotation / this.outputManager.getBlocksPosition().size())
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

    @Deprecated
    public int[] getStructureBounds(BlockPos startPos, int structureSize, String facing) {
        int[] northOffsets5x5 = new int[] { -2, 2, -3, 3, 0, 4 };
        int[] northOffsets7x7 = new int[] { -3, 3, -4, 4, 0, 6 };
        int[] northOffsets9x9 = new int[] { -4, 4, -5, 5, 0, 8 };

        int[] eastOffsets5x5 = new int[] { -4, 0, -3, 3, -2, 2 };
        int[] eastOffsets7x7 = new int[] { -6, 0, -4, 4, -3, 3 };
        int[] eastOffsets9x9 = new int[] { -8, 0, -5, 5, -4, 4 };

        int[] southOffsets5x5 = new int[] { -2, 2, -3, 3, -4, 0 };
        int[] southOffsets7x7 = new int[] { -3, 3, -4, 4, -6, 0 };
        int[] southOffsets9x9 = new int[] { -4, 4, -5, 5, -8, 0 };

        int[] westOffsets5x5 = new int[] { 0, 4, -3, 3, -2, 2 };
        int[] westOffsets7x7 = new int[] { 0, 6, -4, 4, -3, 3 };
        int[] westOffsets9x9 = new int[] { 0, 8, -5, 5, -4, 4 };

        switch (facing) {
            case "north":
                switch (structureSize) {
                    case 5:
                        return applyOffset(startPos, northOffsets5x5);
                    case 7:
                        return applyOffset(startPos, northOffsets7x7);
                    case 9:
                        return applyOffset(startPos, northOffsets9x9);
                }
            case "east":
                switch (structureSize) {
                    case 5:
                        return applyOffset(startPos, eastOffsets5x5);
                    case 7:
                        return applyOffset(startPos, eastOffsets7x7);
                    case 9:
                        return applyOffset(startPos, eastOffsets9x9);
                }
            case "south":
                switch (structureSize) {
                    case 5:
                        return applyOffset(startPos, southOffsets5x5);
                    case 7:
                        return applyOffset(startPos, southOffsets7x7);
                    case 9:
                        return applyOffset(startPos, southOffsets9x9);
                }
            case "west":
                switch (structureSize) {
                    case 5:
                        return applyOffset(startPos, westOffsets5x5);
                    case 7:
                        return applyOffset(startPos, westOffsets7x7);
                    case 9:
                        return applyOffset(startPos, westOffsets9x9);
                }
            default:
                return new int[] { 0, 0, 0, 0, 0, 0 };
        }
    }

    @Deprecated
    private int[] applyOffset(BlockPos pos, int[] offset) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        return new int[] { x + offset[0], x + offset[1], y + offset[2], y + offset[3], z + offset[4], z + offset[5] };
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
        // server-persistent approach (preferred): use PersistentFluidLocks when on
        // server
        if (level != null && !level.isClientSide && level instanceof ServerLevel serverLevel) {
            return PersistentFluidLocks.get(serverLevel).tryLock(getBlockPos(), fluid);
        }
        // fallback to in-memory manager (single-server-run)
        return FluidLockManager.tryLock(getBlockPos(), fluid);
    }

    /** Returns whether the given FluidStack is acceptable for this controller. */
    public boolean canAcceptFluid(FluidStack stack) {
        if (stack == null || stack.isEmpty())
            return true;
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

    public void clearLockIfAllInputsEmpty() {
        if (level == null || level.isClientSide)
            return;

        final int SCAN_RADIUS = CNMultiblock.REGISTRATE_MULTIBLOCK.findStructure(level, getBlockPos(), this).data()
                .getSize(); // adapte selon la taille max du multiblock
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