package net.nuclearteam.createnuclear.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.ItemStackHandler;
import net.nuclearteam.createnuclear.CNBlocks;
import net.nuclearteam.createnuclear.CNItems;
import net.nuclearteam.createnuclear.CreateNuclear;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlockEntity;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerInventory;
import net.nuclearteam.createnuclear.content.multiblock.controller.display.ReactorDisplayState;
import net.nuclearteam.createnuclear.content.multiblock.reactorLogic.DefaultHeatCalculator;
import net.nuclearteam.createnuclear.infrastructure.config.CNConfigs;

import java.util.List;
import java.util.Map;

/**
 * GameTest coverage for {@link DefaultHeatCalculator#computeHeat}, run against a real
 * {@code ServerLevel}, a real {@link ReactorControllerBlockEntity} and real registered
 * rod items (no mocks) — plain JUnit cannot exercise this method at all: {@code Item},
 * {@code ItemStack} and {@code RodType} resolution all depend on Minecraft/Forge registries
 * that only exist once the game has bootstrapped (see AUDIT_ACTUEL.md §3).
 * <p>
 * All tests share the "empty_platform" structure and place a single
 * {@code CNBlocks.REACTOR_CONTROLLER} to obtain a real {@link ReactorControllerInventory}.
 * The reactor "pattern" (57-slot {@link ItemStackHandler}, slots per
 * {@code DefaultHeatCalculator.formattedPattern}) is written directly into the blueprint
 * item's {@code pattern} NBT tag and set into inventory slot 0, exactly as
 * {@code ReactorBluePrintItem.getItemStorage} reads it back.
 * <p>
 * Expected heat values are computed from the live {@code CNConfigs.server().rods} values
 * rather than hardcoded, so these tests stay correct if the mod's default balance changes.
 */
@GameTestHolder(CreateNuclear.MOD_ID)
@PrefixGameTestTemplate(false)
public class DefaultHeatCalculatorGameTest {

    private static final String STRUCTURE = "empty_platform";
    private static final double DELTA = 1e-9;

    // ---------- test fixtures ----------

    private static ReactorControllerInventory placeController(GameTestHelper helper, BlockPos rel) {
        helper.setBlock(rel, CNBlocks.REACTOR_CONTROLLER.get().defaultBlockState());
        ReactorControllerBlockEntity be = (ReactorControllerBlockEntity) helper.getBlockEntity(rel);
        return be.getInventoryObject();
    }

    /** Writes {@code rodsBySlot} into the blueprint item's "pattern" NBT and loads it into slot 0. */
    private static void loadPattern(ReactorControllerInventory inventory, Map<Integer, Item> rodsBySlot) {
        ItemStackHandler patternHandler = new ItemStackHandler(57);
        rodsBySlot.forEach((slot, item) -> patternHandler.setStackInSlot(slot, new ItemStack(item)));

        ItemStack blueprint = new ItemStack(CNItems.REACTOR_BLUEPRINT.get());
        blueprint.getOrCreateTag().put("pattern", patternHandler.serializeNBT());
        inventory.setStackInSlot(0, blueprint);
    }

    private static final DefaultHeatCalculator CALCULATOR = new DefaultHeatCalculator();

    // ================================================================
    // 1. single fuel rod, no neighbors
    // ================================================================

    @GameTest(template = STRUCTURE)
    public static void singleFuelRod_noNeighbors_addsOnlyItsOwnBaseRodHeat(GameTestHelper helper) {
        Item thorium = CNItems.THORIUM_ROD.get();
        int baseRodHeat = CNConfigs.server().rods.baseValueThorium.get();

        ReactorControllerInventory inventory = placeController(helper, new BlockPos(1, 1, 1));
        // slot 18: middle row, no adjacent slot populated -> no neighbor bonus possible
        loadPattern(inventory, Map.of(18, thorium));

        ReactorDisplayState displayState = new ReactorDisplayState(Map.of(thorium, 1), List.of(), 0);
        double overHeat = 10.0;

        double heat = CALCULATOR.computeHeat(null, null, inventory, overHeat, displayState, helper.getLevel());

        helper.assertTrue(Math.abs(heat - (baseRodHeat + overHeat)) < DELTA,
                "an isolated fuel rod should only contribute its own baseRodHeat, plus overHeat: expected "
                        + (baseRodHeat + overHeat) + ", got " + heat);
        helper.succeed();
    }

    // ================================================================
    // 2. single cooler rod, no neighbors
    // ================================================================

    @GameTest(template = STRUCTURE)
    public static void singleCoolerRod_noNeighbors_addsOnlyItsOwnBaseRodHeat(GameTestHelper helper) {
        Item graphite = CNItems.GRAPHITE_ROD.get();
        int baseRodHeat = CNConfigs.server().rods.baseValueGraphite.get();

        ReactorControllerInventory inventory = placeController(helper, new BlockPos(1, 1, 1));
        loadPattern(inventory, Map.of(18, graphite));

        ReactorDisplayState displayState = new ReactorDisplayState(Map.of(graphite, 1), List.of(), 0);

        double heat = CALCULATOR.computeHeat(null, null, inventory, /*overHeat*/ 0.0, displayState, helper.getLevel());

        helper.assertTrue(Math.abs(heat - baseRodHeat) < DELTA,
                "a cooler contributes its own baseRodHeat exactly like a fuel rod would, when isolated: expected "
                        + baseRodHeat + ", got " + heat);
        helper.succeed();
    }

    // ================================================================
    // 3. fuel/cooler mix adjacency: documents the fuel<->cooler asymmetry (AUDIT_ACTUEL.md §2.2)
    // ================================================================

    /**
     * Layout (row 3 of {@code formattedPattern}, three consecutive slots):
     * uranium A (18) - thorium B (19) - graphite C (20).
     * <p>
     * Expected total = sum of each rod's own baseRodHeat, PLUS proximity contributions that only
     * ever originate from a FUEL rod's own neighbor scan (a cooler's scan never contributes,
     * even though its neighbor is a fuel rod):
     * <ul>
     *   <li>A's scan (fuel, neighbor B is fuel) -&gt; + A.proximityRodHeat()</li>
     *   <li>B's scan (fuel, neighbor A is fuel) -&gt; + B.proximityRodHeat()</li>
     *   <li>B's scan (fuel, neighbor C is cooler) -&gt; + B.baseRodHeat() / C.proximityRodHeat()</li>
     *   <li>C's scan (cooler) -&gt; nothing at all, regardless of its fuel neighbor B</li>
     * </ul>
     */
    @GameTest(template = STRUCTURE)
    public static void fuelCoolerMix_onlyFuelScansContributeProximityHeat_coolerAdjacencyIsAsymmetric(GameTestHelper helper) {
        Item uranium = CNItems.URANIUM_ROD.get();
        Item thorium = CNItems.THORIUM_ROD.get();
        Item graphite = CNItems.GRAPHITE_ROD.get();

        int uraniumBase = CNConfigs.server().rods.baseValueUranium.get();
        float uraniumProxy = CNConfigs.server().rods.uraniumProxyBonus.get();
        int thoriumBase = CNConfigs.server().rods.baseValueThorium.get();
        float thoriumProxy = CNConfigs.server().rods.thoriumProxyBonus.get();
        int graphiteBase = CNConfigs.server().rods.baseValueGraphite.get();
        float graphiteProxy = CNConfigs.server().rods.graphiteProxyMalus.getF();

        ReactorControllerInventory inventory = placeController(helper, new BlockPos(1, 1, 1));
        loadPattern(inventory, Map.of(18, uranium, 19, thorium, 20, graphite));

        ReactorDisplayState displayState = new ReactorDisplayState(
                Map.of(uranium, 1, thorium, 1, graphite, 1), List.of(), 0);

        double heat = CALCULATOR.computeHeat(null, null, inventory, /*overHeat*/ 0.0, displayState, helper.getLevel());

        double expected = (uraniumBase + thoriumBase + graphiteBase)  // each rod's own baseRodHeat
                + uraniumProxy    // A's scan: fuel neighbor B (thorium) -> addition
                + thoriumProxy    // B's scan: fuel neighbor A (uranium) -> addition
                + (thoriumBase / graphiteProxy);
                // B's scan: cooler neighbor C -> division (fuel.base / cooler.proximity)
                // C's scan contributes 0: a cooler never examines its own neighbors

        helper.assertTrue(Math.abs(heat - expected) < DELTA,
                "cooler-side proximity must not contribute anything, even though C is adjacent to fuel rod B: expected "
                        + expected + ", got " + heat);
        helper.succeed();
    }
}
