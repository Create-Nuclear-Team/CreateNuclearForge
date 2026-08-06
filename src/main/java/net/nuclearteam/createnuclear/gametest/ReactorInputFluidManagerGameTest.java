package net.nuclearteam.createnuclear.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;
import net.nuclearteam.createnuclear.CNBlocks;
import net.nuclearteam.createnuclear.CreateNuclear;
import net.nuclearteam.createnuclear.content.multiblock.controller.manager.ReactorInputFluidManager;
import net.nuclearteam.createnuclear.content.multiblock.input.fluid.ReactorFluidInputEntity;
import net.nuclearteam.createnuclear.content.multiblock.input.fluid.VirtualReactorInputFluid;
import net.nuclearteam.createnuclear.foundation.block.MultiDirectionalReactorBlock;

import java.util.List;

/**
 * GameTest coverage for {@link ReactorInputFluidManager}, run against a real
 * {@code ServerLevel} and real {@link ReactorFluidInputEntity} blocks (no mocks).
 * <p>
 * All tests share the "empty_platform" structure
 * (data/createnuclear/structures/empty_platform.nbt): a 5x3x5 area with a
 * stone floor at y=0, leaving y=1..2 free to place fluid input blocks. The structure
 * is a real compressed structure-block export (a hand-authored .snbt cannot be loaded
 * directly here: Forge only auto-discovers packaged ".nbt" structures under
 * data/&lt;namespace&gt;/structures/, not loose ".snbt" text unless it also sits on disk
 * relative to the run's working directory).
 * <p>
 * Tests named "*_expectedContract" cover the two extractFluids defects fixed on both branches:
 * fluidNeeded was never decremented between handlers (so a request of 10 units drained 10 from
 * EVERY input), and a request for exactly one unit was silently dropped by an
 * {@code if (toExtract > 1)} guard. The whole suite passes; the two characterization tests that
 * pinned the buggy behaviour were removed with the fix.
 */
@GameTestHolder(CreateNuclear.MOD_ID)
@PrefixGameTestTemplate(false)
public class ReactorInputFluidManagerGameTest {

    private static final String STRUCTURE = "empty_platform";

    // ---------- test fixtures ----------

    private static void place(GameTestHelper helper, BlockPos rel, Direction facing) {
        helper.setBlock(rel, CNBlocks.REACTOR_FLUID_INPUT.get().defaultBlockState()
                .setValue(MultiDirectionalReactorBlock.FACING, facing));
    }

    private static ReactorFluidInputEntity entity(GameTestHelper helper, BlockPos rel) {
        return (ReactorFluidInputEntity) helper.getBlockEntity(rel);
    }

    private static void fill(GameTestHelper helper, BlockPos rel, FluidStack stack) {
        entity(helper, rel).getCapability(ForgeCapabilities.FLUID_HANDLER)
                .ifPresent(h -> h.fill(stack, FluidAction.EXECUTE));
    }

    private static int tankAmount(GameTestHelper helper, BlockPos rel) {
        return entity(helper, rel).getCapability(ForgeCapabilities.FLUID_HANDLER)
                .map(h -> h.getFluidInTank(0).getAmount())
                .orElse(-1);
    }

    private static ReactorInputFluidManager manager(GameTestHelper helper, BlockPos... relPositions) {
        ReactorInputFluidManager manager = new ReactorInputFluidManager();
        for (BlockPos rel : relPositions) manager.addBlock(helper.absolutePos(rel));
        return manager;
    }

    // ================================================================
    // read / write (pure NBT, no world interaction required)
    // ================================================================

    @GameTest(template = STRUCTURE)
    public static void read_absentKey_leavesEmpty(GameTestHelper helper) {
        ReactorInputFluidManager manager = new ReactorInputFluidManager();
        manager.read(new CompoundTag());

        helper.assertTrue(manager.size() == 0, "expected no tracked positions when NBT key is absent");
        helper.succeed();
    }

    @GameTest(template = STRUCTURE)
    public static void read_clearsPreviousPositions(GameTestHelper helper) {
        ReactorInputFluidManager manager = new ReactorInputFluidManager();
        manager.addBlock(new BlockPos(1, 2, 3));

        manager.read(new CompoundTag());

        helper.assertTrue(manager.size() == 0, "read() must clear previously tracked positions before loading");
        helper.succeed();
    }

    @GameTest(template = STRUCTURE)
    public static void read_singleEntry_roundTrips(GameTestHelper helper) {
        CompoundTag entry = new CompoundTag();
        entry.putInt("x", 5);
        entry.putInt("y", 64);
        entry.putInt("z", -3);
        ListTag list = new ListTag();
        list.add(entry);
        CompoundTag root = new CompoundTag();
        root.put("ReactorInputFluid", list);

        ReactorInputFluidManager manager = new ReactorInputFluidManager();
        manager.read(root);

        helper.assertTrue(manager.size() == 1, "expected exactly one entry after read");
        helper.assertTrue(manager.contains(new BlockPos(5, 64, -3)), "expected BlockPos(5,64,-3) to be present");
        helper.succeed();
    }

    @GameTest(template = STRUCTURE)
    public static void read_multipleEntries_preservesOrder(GameTestHelper helper) {
        BlockPos a = new BlockPos(0, 0, 0);
        BlockPos b = new BlockPos(1, 1, 1);
        BlockPos c = new BlockPos(2, 2, 2);

        ListTag list = new ListTag();
        for (BlockPos p : List.of(a, b, c)) {
            CompoundTag entry = new CompoundTag();
            entry.putInt("x", p.getX());
            entry.putInt("y", p.getY());
            entry.putInt("z", p.getZ());
            list.add(entry);
        }
        CompoundTag root = new CompoundTag();
        root.put("ReactorInputFluid", list);

        ReactorInputFluidManager manager = new ReactorInputFluidManager();
        manager.read(root);

        helper.assertTrue(manager.getBlocksPosition().equals(List.of(a, b, c)),
                "expected insertion order to be preserved, found " + manager.getBlocksPosition());
        helper.succeed();
    }

    @GameTest(template = STRUCTURE)
    public static void write_emptyPositions_stillWritesKey(GameTestHelper helper) {
        CompoundTag tag = new CompoundTag();
        new ReactorInputFluidManager().write(tag);

        helper.assertTrue(tag.contains("ReactorInputFluid"), "write() should always write the NBT key, even when empty");
        helper.succeed();
    }

    @GameTest(template = STRUCTURE)
    public static void write_then_read_roundTrip(GameTestHelper helper) {
        BlockPos a = new BlockPos(1, 2, 3);
        BlockPos b = new BlockPos(-5, 64, 100);

        ReactorInputFluidManager original = new ReactorInputFluidManager();
        original.addBlock(a);
        original.addBlock(b);

        CompoundTag tag = new CompoundTag();
        original.write(tag);

        ReactorInputFluidManager restored = new ReactorInputFluidManager();
        restored.read(tag);

        helper.assertTrue(restored.getBlocksPosition().equals(List.of(a, b)),
                "round-trip through NBT should preserve exact positions and order");
        helper.succeed();
    }

    // ================================================================
    // clearInvalid
    // ================================================================

    @GameTest(template = STRUCTURE)
    public static void clearInvalid_missingBlockEntity_removesPosition(GameTestHelper helper) {
        BlockPos rel = new BlockPos(1, 1, 1); // never placed: air, no block entity
        ReactorInputFluidManager manager = manager(helper, rel);

        manager.clearInvalid(helper.getLevel());

        helper.assertTrue(manager.size() == 0, "a position without a block entity should be removed");
        helper.succeed();
    }

    @GameTest(template = STRUCTURE)
    public static void clearInvalid_noCapability_removesPosition(GameTestHelper helper) {
        BlockPos rel = new BlockPos(1, 1, 1);
        helper.setBlock(rel, Blocks.CHEST.defaultBlockState()); // real BE, no fluid capability
        ReactorInputFluidManager manager = manager(helper, rel);

        manager.clearInvalid(helper.getLevel());

        helper.assertTrue(manager.size() == 0, "a block entity without a fluid capability should be removed");
        helper.succeed();
    }

    @GameTest(template = STRUCTURE)
    public static void clearInvalid_validCapability_keepsPosition(GameTestHelper helper) {
        BlockPos rel = new BlockPos(1, 1, 1);
        place(helper, rel, Direction.NORTH);
        ReactorInputFluidManager manager = manager(helper, rel);

        manager.clearInvalid(helper.getLevel());

        helper.assertTrue(manager.size() == 1, "a valid ReactorFluidInputEntity should be kept");
        helper.succeed();
    }

    @GameTest(template = STRUCTURE)
    public static void clearInvalid_mixedSet_onlyInvalidRemoved(GameTestHelper helper) {
        BlockPos valid = new BlockPos(1, 1, 1);
        BlockPos chest = new BlockPos(3, 1, 1);
        BlockPos missing = new BlockPos(1, 1, 3);

        place(helper, valid, Direction.NORTH);
        helper.setBlock(chest, Blocks.CHEST.defaultBlockState());

        ReactorInputFluidManager manager = manager(helper, valid, chest, missing);
        manager.clearInvalid(helper.getLevel());

        helper.assertTrue(manager.getBlocksPosition().equals(List.of(helper.absolutePos(valid))),
                "only the valid fluid input position should remain, found " + manager.getBlocksPosition());
        helper.succeed();
    }

    // ================================================================
    // getBlocksPosition(Level)
    // ================================================================

    @GameTest(template = STRUCTURE)
    public static void getBlocksPosition_filtersNonMatchingEntityType(GameTestHelper helper) {
        BlockPos validRel = new BlockPos(1, 1, 1);
        BlockPos otherRel = new BlockPos(3, 1, 1);
        place(helper, validRel, Direction.NORTH);
        helper.setBlock(otherRel, Blocks.CHEST.defaultBlockState());

        ReactorInputFluidManager manager = manager(helper, validRel, otherRel);

        List<BlockPos> result = manager.getBlocksPosition(helper.getLevel());

        helper.assertTrue(result.equals(List.of(helper.absolutePos(validRel))),
                "only the ReactorFluidInputEntity position should be returned, found " + result);
        helper.succeed();
    }

    // ================================================================
    // getFuildHandlers
    // ================================================================

    @GameTest(template = STRUCTURE)
    public static void getFuildHandlers_capabilityPresent_included(GameTestHelper helper) {
        BlockPos rel = new BlockPos(1, 1, 1);
        place(helper, rel, Direction.NORTH);
        fill(helper, rel, new FluidStack(Fluids.WATER, 500));

        List<IFluidHandler> handlers = manager(helper, rel).getFuildHandlers(helper.getLevel());

        helper.assertTrue(handlers.size() == 1, "expected exactly one handler");
        helper.assertTrue(handlers.get(0).getFluidInTank(0).getAmount() == 500,
                "handler should expose the filled tank content");
        helper.succeed();
    }

    @GameTest(template = STRUCTURE)
    public static void getFuildHandlers_preservesTrackingOrder(GameTestHelper helper) {
        BlockPos rel1 = new BlockPos(1, 1, 1);
        BlockPos rel2 = new BlockPos(3, 1, 1);
        place(helper, rel1, Direction.NORTH);
        place(helper, rel2, Direction.NORTH);
        fill(helper, rel1, new FluidStack(Fluids.WATER, 100));
        fill(helper, rel2, new FluidStack(Fluids.LAVA, 200));

        List<IFluidHandler> handlers = manager(helper, rel1, rel2).getFuildHandlers(helper.getLevel());

        helper.assertTrue(handlers.size() == 2, "expected two handlers");
        helper.assertTrue(handlers.get(0).getFluidInTank(0).getFluid() == Fluids.WATER,
                "first handler should be the water tank (matches insertion order)");
        helper.assertTrue(handlers.get(1).getFluidInTank(0).getFluid() == Fluids.LAVA,
                "second handler should be the lava tank (matches insertion order)");
        helper.succeed();
    }

    // ================================================================
    // getInventory
    // ================================================================

    @GameTest(template = STRUCTURE)
    public static void getInventory_noHandlers_returnsEmpty(GameTestHelper helper) {
        VirtualReactorInputFluid inventory = new ReactorInputFluidManager().getInventory(helper.getLevel());

        helper.assertTrue(inventory.fluids().isEmpty(), "expected an empty inventory with no tracked handlers");
        helper.succeed();
    }

    @GameTest(template = STRUCTURE)
    public static void getInventory_aggregatesAcrossHandlers(GameTestHelper helper) {
        BlockPos rel1 = new BlockPos(1, 1, 1);
        BlockPos rel2 = new BlockPos(3, 1, 1);
        place(helper, rel1, Direction.NORTH);
        place(helper, rel2, Direction.NORTH);
        fill(helper, rel1, new FluidStack(Fluids.WATER, 300));
        fill(helper, rel2, new FluidStack(Fluids.WATER, 250));

        VirtualReactorInputFluid inventory = manager(helper, rel1, rel2).getInventory(helper.getLevel());

        ResourceLocation waterId = ForgeRegistries.FLUIDS.getKey(Fluids.WATER);
        long total = inventory.getAmount(waterId);
        helper.assertTrue(total == 550, "expected aggregated total of 550, found " + total);
        helper.succeed();
    }

    // ================================================================
    // extractFluids — core of the audited bug
    // ================================================================

    @GameTest(template = STRUCTURE)
    public static void extractFluids_nullLevel_returnsFalse(GameTestHelper helper) {
        boolean result = new ReactorInputFluidManager().extractFluids(null, 10);
        helper.assertTrue(!result, "extractFluids should return false for a null level");
        helper.succeed();
    }

    @GameTest(template = STRUCTURE)
    public static void extractFluids_noHandlers_returnsFalse(GameTestHelper helper) {
        boolean result = new ReactorInputFluidManager().extractFluids(helper.getLevel(), 10);
        helper.assertTrue(!result, "extractFluids should return false when there are no handlers");
        helper.succeed();
    }

    @GameTest(template = STRUCTURE)
    public static void extractFluids_singleHandlerEnoughStock_drainsExactAmount(GameTestHelper helper) {
        BlockPos rel = new BlockPos(1, 1, 1);
        place(helper, rel, Direction.NORTH);
        fill(helper, rel, new FluidStack(Fluids.WATER, 1000));

        boolean result = manager(helper, rel).extractFluids(helper.getLevel(), 400);

        helper.assertTrue(result, "extraction should succeed when enough fluid is available");
        helper.assertTrue(tankAmount(helper, rel) == 600, "expected 600 remaining, found " + tankAmount(helper, rel));
        helper.succeed();
    }

    @GameTest(template = STRUCTURE)
    public static void extractFluids_singleHandlerInsufficientStock_drainsAvailable(GameTestHelper helper) {
        BlockPos rel = new BlockPos(1, 1, 1);
        place(helper, rel, Direction.NORTH);
        fill(helper, rel, new FluidStack(Fluids.WATER, 30));

        boolean result = manager(helper, rel).extractFluids(helper.getLevel(), 100);

        helper.assertTrue(result, "extraction should be reported successful even for a partial drain");
        helper.assertTrue(tankAmount(helper, rel) == 0, "all 30 available units should have been drained");
        helper.succeed();
    }

    /**
     * A request for exactly 1 unit with stock available must succeed. This used to fail against
     * an {@code if (toExtract > 1)} guard that silently dropped single-unit requests.
     */
    @GameTest(template = STRUCTURE)
    public static void extractFluids_needOfOne_shouldSucceed_expectedContract(GameTestHelper helper) {
        BlockPos rel = new BlockPos(1, 1, 1);
        place(helper, rel, Direction.NORTH);
        fill(helper, rel, new FluidStack(Fluids.WATER, 5));

        boolean result = manager(helper, rel).extractFluids(helper.getLevel(), 1);

        helper.assertTrue(result, "a request for exactly 1 unit with stock available should succeed");
        helper.assertTrue(tankAmount(helper, rel) == 4, "expected exactly 1 unit to be drained");
        helper.succeed();
    }

    /**
     * Extracting 10 units from a 20-unit pool spread across two handlers must leave 10 total
     * remaining. The requested amount is a total across every input, not a per-input quota;
     * this used to drain both handlers in full because {@code fluidNeeded} was never
     * decremented between them.
     */
    @GameTest(template = STRUCTURE)
    public static void extractFluids_twoHandlers_shouldNotOverExtract_expectedContract(GameTestHelper helper) {
        BlockPos rel1 = new BlockPos(1, 1, 1);
        BlockPos rel2 = new BlockPos(3, 1, 1);
        place(helper, rel1, Direction.NORTH);
        place(helper, rel2, Direction.NORTH);
        fill(helper, rel1, new FluidStack(Fluids.WATER, 10));
        fill(helper, rel2, new FluidStack(Fluids.WATER, 10));

        manager(helper, rel1, rel2).extractFluids(helper.getLevel(), 10);

        int totalRemaining = tankAmount(helper, rel1) + tankAmount(helper, rel2);
        helper.assertTrue(totalRemaining == 10,
                "extracting 10 out of a 20-unit pool should leave 10 total remaining, found " + totalRemaining);
        helper.succeed();
    }

    @GameTest(template = STRUCTURE)
    public static void extractFluids_needZero_leavesTanksUntouched(GameTestHelper helper) {
        BlockPos rel1 = new BlockPos(1, 1, 1);
        BlockPos rel2 = new BlockPos(3, 1, 1);
        place(helper, rel1, Direction.NORTH);
        place(helper, rel2, Direction.NORTH);
        fill(helper, rel1, new FluidStack(Fluids.WATER, 10));
        fill(helper, rel2, new FluidStack(Fluids.WATER, 10));

        boolean result = manager(helper, rel1, rel2).extractFluids(helper.getLevel(), 0);

        helper.assertTrue(!result, "a non-positive fluidNeeded should never extract anything");
        helper.assertTrue(tankAmount(helper, rel1) == 10 && tankAmount(helper, rel2) == 10,
                "no tank should be touched when fluidNeeded <= 0");
        helper.succeed();
    }

    @GameTest(template = STRUCTURE)
    public static void extractFluids_emptyStackHandler_skippedGracefully(GameTestHelper helper) {
        BlockPos rel1 = new BlockPos(1, 1, 1);
        BlockPos rel2 = new BlockPos(3, 1, 1);
        place(helper, rel1, Direction.NORTH); // left empty
        place(helper, rel2, Direction.NORTH);
        fill(helper, rel2, new FluidStack(Fluids.WATER, 20));

        boolean result = manager(helper, rel1, rel2).extractFluids(helper.getLevel(), 15);

        helper.assertTrue(result, "the second, non-empty handler should still be usable");
        helper.assertTrue(tankAmount(helper, rel1) == 0, "empty handler should stay untouched");
        helper.assertTrue(tankAmount(helper, rel2) == 5, "expected 15 drained out of 20, 5 remaining");
        helper.succeed();
    }

    /**
     * A partial extraction still reports success: the reactor consumes whatever coolant it can
     * reach rather than refusing to run. The Javadoc used to promise "true if the full amount
     * was extracted", which never matched the implementation; it now documents this contract.
     */
    @GameTest(template = STRUCTURE)
    public static void extractFluids_partialExtraction_stillReportsSuccess(GameTestHelper helper) {
        BlockPos rel = new BlockPos(1, 1, 1);
        place(helper, rel, Direction.NORTH);
        fill(helper, rel, new FluidStack(Fluids.WATER, 30));

        boolean result = manager(helper, rel).extractFluids(helper.getLevel(), 100);

        helper.assertTrue(result, "current implementation returns true even for a partial extraction (Javadoc mismatch)");
        helper.assertTrue(tankAmount(helper, rel) == 0, "all available fluid should have been drained");
        helper.succeed();
    }
}
