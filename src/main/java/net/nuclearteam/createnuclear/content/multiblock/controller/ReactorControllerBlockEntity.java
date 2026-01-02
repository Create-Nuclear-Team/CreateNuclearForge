package net.nuclearteam.createnuclear.content.multiblock.controller;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.foundation.utility.IInteractionChecker;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.nuclearteam.createnuclear.*;
import net.nuclearteam.createnuclear.content.multiblock.IHeat;
import net.nuclearteam.createnuclear.content.multiblock.controller.manager.ReactorInputManager;
import net.nuclearteam.createnuclear.content.multiblock.input.ReactorInputEntity;
import net.nuclearteam.createnuclear.content.multiblock.output.ReactorOutput;
import net.nuclearteam.createnuclear.content.multiblock.output.ReactorOutputEntity;
import net.nuclearteam.createnuclear.content.multiblock.pattern.ReactorPattern;

import java.util.ArrayList;
import java.util.List;

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

    int overFlowHeatTimer = 0;
    int overFlowLimiter = 30;
    double overHeat = 0;
    public int baseUraniumHeat = 25;
    public int baseGraphiteHeat = -10;
    public int proximityUraniumHeat = 5;
    public int proximityGraphiteHeat = -5;
    public int maxUraniumPerGraphite = 3;
    public int graphiteTimer = 3600;
    public int uraniumTimer = 3600;
    public int countUraniumRod;
    public int countGraphiteRod;
    public int heat;
    public double total;
    public CompoundTag screen_pattern = new CompoundTag();
    public ItemStack configuredPattern;

    private ItemStack fuelItem;
    private ItemStack coolerItem;

    public List<ReactorOutputEntity> reactorOutputEntityList = new ArrayList<>();
    public int reactorSize = 0;
    public String reactorFacing = "null";
    // les pos sont [xMin, xMax, yMin, yMax, zMin, zMax]
    public int[] reactorPos;
    private boolean needsToResolveEntities = false;

    private final int[][] formattedPattern = new int[][]{
            {99,99,99,0,1,2,99,99,99},
            {99,99,3,4,5,6,7,99,99},
            {99,8,9,10,11,12,13,14,99},
            {15,16,17,18,19,20,21,22,23},
            {24,25,26,27,28,29,30,31,32},
            {33,34,35,36,37,38,39,40,41},
            {99,42,43,44,45,46,47,48,99},
            {99,99,49,50,51,52,53,99,99},
            {99,99,99,54,55,56,99,99,99}
    };
    private final int[][] offsets = { {1, 0}, {-1, 0}, {0, 1}, {0, -1} };

    private final ReactorInputManager inputManager;

    public ReactorControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        inventory = new ReactorControllerInventory(this);
        configuredPattern = ItemStack.EMPTY;

        inputManager = new ReactorInputManager();
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

            if (fuelItem.isEmpty()) {
                // if rod empty we initialize it at 1 (and display it as 0) to avoid having air item displayed instead of the rod
                IHeat.HeatLevel.getFormattedItemText(new ItemStack(CNItems.URANIUM_ROD.asItem(), 1), true).forGoggles(tooltip);
            } else {
                IHeat.HeatLevel.getFormattedItemText(fuelItem, false).forGoggles(tooltip);
            }

            if (fuelItem.isEmpty()) {
                // if rod empty we initialize it at 1 (and display it as 0) to avoid having air item displayed instead of the rod
                IHeat.HeatLevel.getFormattedItemText(new ItemStack(CNItems.GRAPHITE_ROD.asItem(), 1), true).forGoggles(tooltip);
            } else {
                IHeat.HeatLevel.getFormattedItemText(coolerItem, false).forGoggles(tooltip);
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


        // 1. Tes nouvelles variables simples
        this.reactorSize = compound.getInt("reactorSize");
        this.reactorFacing = compound.getString("reactorFacing");
        this.reactorPos = compound.getIntArray("reactorPose");
        this.total = compound.getDouble("total");

        // 2. Gestion des items (Ton code existant)
        if (!clientPacket) {
            inventory.deserializeNBT(compound.getCompound("pattern"));
        }
        configuredPattern = ItemStack.of(compound.getCompound("items"));
        if (compound.contains("cooler") || compound.contains("fuel")) {
            coolerItem = ItemStack.of(compound.getCompound("cooler"));
            fuelItem = ItemStack.of(compound.getCompound("fuel"));
        }

        // 3. Reconstruction des listes d'entités (via les positions sauvegardées)
        this.reactorOutputEntityList.clear();

        this.needsToResolveEntities = true;
    }

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        this.inputManager.write(compound);


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
        if (coolerItem != null && !coolerItem.isEmpty()) {
            compound.put("cooler", coolerItem.serializeNBT());
        }
        if (fuelItem != null && !fuelItem.isEmpty()) {
            compound.put("fuel", fuelItem.serializeNBT());
        }
    }

    public void test() {
        CreateNuclear.LOGGER.warn("List d'input: {}", this.inputManager.size());
        for (BlockPos p : this.inputManager.getBlocksPosition()) {
                CreateNuclear.LOGGER.info("ReactorInputEntity BlockPos {}", p);
        }
        for (BlockPos p : this.inputManager.getBlocksPosition(level)) {
            CreateNuclear.LOGGER.warn("List vrais input: {}", p);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide)
            return;
        if (this.inputManager.size() > 0) {
//             for (BlockPos p : this.inputManager.getBlocksPosition()) {
//                 CreateNuclear.LOGGER.info("ReactorInputEntity BlockPos {}", p);
//             }
        }
        if (isEmptyConfiguredPattern()) {
            if (this.needsToResolveEntities) {
            }
            BlockEntity blockEntity = level.getBlockEntity(this.worldPosition);
            if (blockEntity instanceof ReactorInputEntity be) {
                CompoundTag tag = be.serializeNBT();
                ListTag inventoryTag = tag.getCompound("Inventory").getList("Items", Tag.TAG_COMPOUND);
                CreateNuclear.LOGGER.info("TAG: {}", inventoryTag);
                fuelItem = ItemStack.of(inventoryTag.getCompound(0));
                coolerItem = ItemStack.of(inventoryTag.getCompound(1));
                //BlockPos outputPos = pattern.FindOutputPos(getBlockPos(), getLevel(), getLevel().players(), true);
                if (fuelItem.getCount() > 0 && coolerItem.getCount() > 0) {
                    configuredPattern.getOrCreateTag().putDouble("heat", calculateHeat(tag));
                    if (updateTimers()) {
                        be.inventory.extractItem(0, 1, false);
                        be.inventory.extractItem(1, 1, false);
                        total = calculateProgress();
                        int heat = (int) configuredPattern.getOrCreateTag().getDouble("heat");
                        if (IHeat.HeatLevel.of(heat) == IHeat.HeatLevel.SAFETY || IHeat.HeatLevel.of(heat) == IHeat.HeatLevel.CAUTION || IHeat.HeatLevel.of(heat) == IHeat.HeatLevel.WARNING) {
                            //this.rotate(getBlockState(), new BlockPos(getBlockPos().getX() + outputPos.getX(), getBlockPos().getY() + outputPos.getY(), getBlockPos().getZ() + outputPos.getZ()), getLevel(), heat/4);
                        } else {
                            // Send a packet to all clients around this block within 16 blocks
                            EventTriggerPacket packet = new EventTriggerPacket(600); // display for 100 ticks
                            CreateNuclear.LOGGER.warn("hum EventTriggerBlock ? {}", packet);
                            CNPackets.sendToNear(level, getBlockPos(), 32, packet);
                            //this.rotate(getBlockState(), new BlockPos(getBlockPos().getX() + outputPos.getX(), getBlockPos().getY() + outputPos.getY(), getBlockPos().getZ() + outputPos.getZ()), getLevel(), 0);
                        }
                        return;
                    }
                } else {
                    //this.rotate(getBlockState(), new BlockPos(getBlockPos().getX() + outputPos.getX(), getBlockPos().getY() + outputPos.getY(), getBlockPos().getZ() + outputPos.getZ()), getLevel(), 0);
                }

                this.notifyUpdate();
            }
        }
    }

    private boolean isEmptyConfiguredPattern() {
        return !configuredPattern.isEmpty() || !configuredPattern.getOrCreateTag().isEmpty();
    }

    private boolean updateTimers() {

        total -= 1;
        return total <= 0;//(total/constTotal) <= 0;
    }

    private double calculateProgress() {
        countGraphiteRod = configuredPattern.getOrCreateTag().getInt("countGraphiteRod");
        countUraniumRod = configuredPattern.getOrCreateTag().getInt("countUraniumRod");
        // graphiteTimer = configuredPattern.getOrCreateTag().getInt("graphiteTime");
        // uraniumTimer = configuredPattern.getOrCreateTag().getInt("uraniumTime");

        double totalGraphiteRodLife = (double) graphiteTimer / countGraphiteRod;
        double totalUraniumRodLife = (double) uraniumTimer / countUraniumRod;

        return totalGraphiteRodLife + totalUraniumRodLife;
    }

    private double calculateHeat(CompoundTag tag) {
        countGraphiteRod = configuredPattern.getOrCreateTag().getInt("countGraphiteRod");
        countUraniumRod = configuredPattern.getOrCreateTag().getInt("countUraniumRod");
        heat = 0;

        // if more than maxUraniumPerGraphite of the rods are uranium, the reactor will overheat
        if (countUraniumRod > countGraphiteRod * maxUraniumPerGraphite) {
            overFlowHeatTimer++;
            if (overFlowHeatTimer >= overFlowLimiter) {
                overHeat+=1;
                overFlowHeatTimer= 0;
                if (overFlowLimiter > 2) {
                    overFlowLimiter -= 1;
                }
            }
        } else {
            overFlowHeatTimer = 0;
            overFlowLimiter = 30;
            if (overHeat > 0) {
                overHeat -= 2;
            } else {
                overHeat = 0;
            }
        }
        // the offsets for the four directions (down, up, right, left) is int[][] offsets = { {1, 0}, {-1, 0}, {0, 1}, {0, -1} }; (defined at the top of the class)
        String currentRod = "";
        ListTag list = inventory.getStackInSlot(0).getOrCreateTag().getCompound("pattern").getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            if (ItemStack.of(list.getCompound(i)).is(CNTags.CNItemTags.FUEL.tag)) {
                heat += baseUraniumHeat;
                currentRod = "u";
            } else if (ItemStack.of(list.getCompound(i)).is(CNTags.CNItemTags.COOLER.tag)) {
                heat += baseGraphiteHeat;
                currentRod = "g";
            }
            for (int j = 0; j < formattedPattern.length; j++) {
                for (int k = 0; k < formattedPattern[j].length; k++) {
                    // Skip if the current pattern value is 99
                    if (formattedPattern[j][k] == 99) continue;

                    // Check if the current slot matches the pattern
                    if (list.getCompound(i).getInt("Slot") != formattedPattern[j][k]) continue;

                    // For each neighbor (up, down, right, left)
                    for (int[] offset : offsets) {
                        int nj = j + offset[0];
                        int nk = k + offset[1];

                        // Check if the indices are within the array boundaries
                        if (nj < 0 || nj >= formattedPattern.length || nk < 0 || nk >= formattedPattern[j].length)
                            continue;

                        int neighborSlot = formattedPattern[nj][nk];

                        // Loop through the list to find the neighbor slot
                        for (int l = 0; l < list.size(); l++) {
                            if (list.getCompound(l).getInt("Slot") == neighborSlot) {
                                // If the currentRod equals "u", apply the corresponding heat
                                if (currentRod.equals("u")) {
                                    ItemStack stack = ItemStack.of(list.getCompound(i));
                                    if (stack.is(CNTags.CNItemTags.FUEL.tag)) {
                                        heat += proximityUraniumHeat;
                                    } else if (stack.is(CNTags.CNItemTags.COOLER.tag)) {
                                        heat += proximityGraphiteHeat;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return heat + overHeat;
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

    public void rotate(BlockState state, BlockPos pos, Level level, int rotation) {
        if (level.getBlockState(pos).is(CNBlocks.REACTOR_OUTPUT.get()) && rotation > 0) {
            if (level.getBlockState(pos).getBlock() instanceof ReactorOutput block) {
                ReactorOutputEntity entity = block.getBlockEntityType().getBlockEntity(level, pos);
                if (state.getValue(ASSEMBLED)) { // Starting the energy
                    entity.speed = rotation;
                    entity.heat = rotation;
                } else { // stopping the energy
                    entity.speed = 0;
                    entity.heat = 0;
                }
                entity.updateSpeed = true;
                entity.updateGeneratedRotation();
                entity.setSpeed(rotation);

            }
        }
        else {
            if (level.getBlockState(pos).getBlock() instanceof ReactorOutput block) {
                ReactorOutputEntity entity = block.getBlockEntityType().getBlockEntity(level, pos);
                entity.setSpeed(0);
                entity.heat = 0;
                entity.updateSpeed = true;
                entity.updateGeneratedRotation();
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

    public void addOutput(ReactorOutputEntity output, Level blockIn, BlockPos inputPos) {
        reactorOutputEntityList.add(output);
        this.setChanged();
    }

    public void removeOutput(ReactorOutputEntity output) {
        reactorOutputEntityList.remove(output);
        this.setChanged();
    }

    public void removeIOAll() {
        this.inputManager.clearInvalid(level);
        this.setChanged();
    }
}